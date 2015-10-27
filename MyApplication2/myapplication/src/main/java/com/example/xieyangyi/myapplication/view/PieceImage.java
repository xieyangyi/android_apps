package com.example.xieyangyi.myapplication.view;

import android.graphics.Bitmap;

/**
 * Created by xieyangyi on 2015/9/19.
 */
public class PieceImage {
    private int imageId;
    private Bitmap image;

    public PieceImage(int imageId, Bitmap image) {
        this.imageId = imageId;
        this.image = image;
    }

    public int getImageId() {
        return imageId;
    }

    public Bitmap getImage() {
        return image;
    }

}
