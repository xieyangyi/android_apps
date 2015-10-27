package com.example.xieyangyi.myapplication.view;

import android.graphics.Point;

/**
 * Created by xieyangyi on 2015/9/19.
 */
public class Piece {
    private PieceImage image;
    private int xIndex;         // the index in 2D array
    private int yIndex;
    private int xPosition;      // piece left up side (x,y)
    private int yPosition;

    public Piece(int xIndex, int yIndex) {
        this.xIndex = xIndex;
        this.yIndex = yIndex;
    }

    public PieceImage getImage() {
        return image;
    }

    public void setImage(PieceImage image) {
        this.image = image;
    }

    public int getxIndex() {
        return xIndex;
    }

    public void setxIndex(int xIndex) {
        this.xIndex = xIndex;
    }

    public int getyIndex() {
        return yIndex;
    }

    public void setyIndex(int yIndex) {
        this.yIndex = yIndex;
    }

    public int getxPosition() {
        return xPosition;
    }

    public void setxPosition(int xPosition) {
        this.xPosition = xPosition;
    }

    public int getyPosition() {
        return yPosition;
    }

    public void setyPosition(int yPosition) {
        this.yPosition = yPosition;
    }

    public boolean isSameImage(Piece otherPiece) {
        if (this.image == null) {
            if (otherPiece.getImage() != null)
                return false;
            else
                return true;
        } else {
            return (this.image.getImageId() == otherPiece.getImage().getImageId());
        }
    }

    public Point getCenter() {
        int x = xPosition + image.getImage().getWidth() / 2;
        int y = yPosition + image.getImage().getHeight() / 2;
        return new Point(x, y);
    }
}
