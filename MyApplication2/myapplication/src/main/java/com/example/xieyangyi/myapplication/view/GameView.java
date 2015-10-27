package com.example.xieyangyi.myapplication.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import com.example.xieyangyi.myapplication.board.ImageUtil;
import com.example.xieyangyi.myapplication.board.LinkInfo;
import com.example.xieyangyi.myapplication.service.GameService;

import java.util.List;

/**
 * Created by xieyangyi on 2015/9/19.
 */
public class GameView extends View {
    private GameService gameService = null;
    private Piece selectedPiece;
    private LinkInfo linkInfo;
    private Paint paint;
    private Bitmap selectImage;

    public GameView(Context context, AttributeSet set) {
        super(context, set);
        // set paint
        this.paint = new Paint();
        this.paint.setStrokeWidth(7);
        this.selectImage = ImageUtil.getSelectImage(context);
    }

    public void setGameService(GameService gameService) {
        this.gameService = gameService;
    }
    public void setLinkInfo(LinkInfo linkInfo) {
        this.linkInfo = linkInfo;
    }
    public void setSelectedPiece(Piece selectedPiece) {
        this.selectedPiece = selectedPiece;
    }
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.gameService == null)
            return;

        // draw all the pieces
        Piece[][] pieces = gameService.getPieces();
        if(pieces != null) {
            for (int i = 0; i < pieces.length; i++) {
                for (int j = 0; j < pieces[i].length; j++) {
                    Piece piece = pieces[i][j];
                    if (piece != null)
                        canvas.drawBitmap(piece.getImage().getImage(), piece.getxPosition(), piece.getyPosition(), null);
                }
            }
        }
        // draw the link line
        if(this.linkInfo != null) {
            List<Point> points = this.linkInfo.getPoints();
            for(int i = 0; i < points.size() - 1; i++) {
                Point currentPoint = points.get(i);
                Point nextPoint = points.get(i+1);
                canvas.drawLine(currentPoint.x, currentPoint.y, nextPoint.x, nextPoint.y, paint);
            }
            this.linkInfo = null;
        }
        // draw select image
        if(this.selectedPiece != null) {
            canvas.drawBitmap(this.selectImage, selectedPiece.getxPosition(), selectedPiece.getyPosition(), null);
        }
    }

    public void startGame() {
        this.gameService.start();
        this.postInvalidate();
    }
}
