package com.example.xieyangyi.myapplication.board;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.xieyangyi.myapplication.R;
import com.example.xieyangyi.myapplication.view.PieceImage;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by xieyangyi on 2015/9/20.
 */
public class ImageUtil {
    private static List<Integer> imageValues = getImageValues();

    public static List<Integer> getImageValues() {
        List<Integer> list = new ArrayList<>();
        try {
            Field[] drawableFields = R.drawable.class.getFields();
            for (Field field :drawableFields) {
                if(field.getName().startsWith("pi_")) {
                    list.add(field.getInt(R.drawable.class));
                }
            }
        } catch (Exception e) {
            return null;
        }
        return list;
    }
    public static List<Integer> getRandomImageValues(List<Integer> imageValues, int size) {
        List<Integer> list = new ArrayList<>();
        Random random = new Random();
        for(int i = 0; i < size; i++) {
            try {
                int index = random.nextInt(imageValues.size());
                list.add(imageValues.get(index));
            } catch (IndexOutOfBoundsException e) {
                return list;
            }
        }
        return list;
    }
    public static List<Integer> getPlayValue(int size) {
        if (size % 2 != 0)
            size += 1;
        List<Integer> list = getRandomImageValues(imageValues, size/2);
        list.addAll(list);
        Collections.shuffle(list);
        return list;
    }
    public static List<PieceImage> getPlayImages(Context context, int size) {
        List<Integer> playImageIds = getPlayValue(size);
        List<PieceImage> playImages = new ArrayList<>();

        for(int i = 0; i < playImageIds.size(); i++) {
            int imageId = playImageIds.get(i);
            Bitmap image = BitmapFactory.decodeResource(context.getResources(), imageId);
            PieceImage pieceImage = new PieceImage(imageId, image);
            playImages.add(pieceImage);
        }

        return playImages;
    }
    public static Bitmap getSelectImage(Context context) {
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.selected);
    }
}
