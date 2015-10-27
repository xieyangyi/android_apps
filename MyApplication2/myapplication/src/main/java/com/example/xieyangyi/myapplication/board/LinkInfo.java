package com.example.xieyangyi.myapplication.board;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xieyangyi on 2015/9/20.
 */
public class LinkInfo {
    private List<Point> pointList = new ArrayList<>();

    public LinkInfo(Point point1, Point point2) {
        pointList.add(point1);
        pointList.add(point2);
    }
    public LinkInfo(Point point1, Point point2, Point point3) {
        pointList.add(point1);
        pointList.add(point2);
        pointList.add(point3);
    }
    public LinkInfo(Point point1, Point point2, Point point3, Point point4) {
        pointList.add(point1);
        pointList.add(point2);
        pointList.add(point3);
        pointList.add(point4);
    }
    public List<Point> getPoints() {
        return this.pointList;
    }
}
