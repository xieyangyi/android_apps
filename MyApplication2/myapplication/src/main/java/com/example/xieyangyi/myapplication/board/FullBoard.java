package com.example.xieyangyi.myapplication.board;

import com.example.xieyangyi.myapplication.view.Piece;
import com.example.xieyangyi.myapplication.view.PieceImage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xieyangyi on 2015/9/20.
 */
public class FullBoard {
    public List<Piece> createPieces(GameConf gameConf, Piece[][] pieces) {
        List<Piece> notNullPieces = new ArrayList<>();
        for(int i = 1; i < pieces.length - 1; i++) {            // left one null piece around, for link line
            for(int j = 1; j < pieces[i].length - 1; j++) {
                Piece piece = new Piece(i, j);
                notNullPieces.add(piece);
            }
        }
        return notNullPieces;
    }

    public Piece[][] create(GameConf gameConf) {
        Piece[][] pieces = new Piece[gameConf.getxSize()][gameConf.getySize()];
        List<Piece> notNullPieces = createPieces(gameConf, pieces);
        List<PieceImage> pieceImages = ImageUtil.getPlayImages(gameConf.getContext(), notNullPieces.size());

        for(int i = 0; i < notNullPieces.size(); i++) {
            Piece piece = notNullPieces.get(i);
            // set image, x position, y position
            piece.setImage(pieceImages.get(i));
            piece.setxPosition(gameConf.getxBeginPos() + gameConf.getPieceWidth() * piece.getxIndex());
            piece.setyPosition(gameConf.getyBeginPos() + gameConf.getPieceHeight() * piece.getyIndex());

            pieces[piece.getxIndex()][piece.getyIndex()] = piece;
        }
        return pieces;
    }
}
