package com.example.xieyangyi.myapplication.board;

import android.content.Context;

/**
 * Created by xieyangyi on 2015/9/20.
 */
public class GameConf {
    public final static int PIECE_WIDTH = 60;
    public final static int PIECE_HEIGHT = 60;
    public static int DEFAULT_TIME = 100;
    private int xSize;
    private int ySize;
    private int xBeginPos;
    private int yBeginPos;
    private int pieceWidth;
    private int pieceHeight;
    private int initialLeftTime;
    private Context context;

    public GameConf(int xSize, int ySize, int xBeginPos, int yBeginPos,
                    int initialLeftTime, Context context) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.xBeginPos = xBeginPos;
        this.yBeginPos = yBeginPos;
        this.initialLeftTime = initialLeftTime;
        this.context = context;
    }

    public int getxSize() {
        return xSize;
    }

    public int getySize() {
        return ySize;
    }

    public int getxBeginPos() {
        return xBeginPos;
    }

    public int getyBeginPos() {
        return yBeginPos;
    }

    public int getPieceWidth() {
        return pieceWidth;
    }

    public int getPieceHeight() {
        return pieceHeight;
    }

    public int getInitialLeftTime() {
        return initialLeftTime;
    }

    public Context getContext() {
        return context;
    }

    public void setPieceWidth(int pieceWidth) {
        this.pieceWidth = pieceWidth;
    }

    public void setPieceHeight(int pieceHeight) {
        this.pieceHeight = pieceHeight;
    }
}
