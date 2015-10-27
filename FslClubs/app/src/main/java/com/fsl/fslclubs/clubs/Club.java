package com.fsl.fslclubs.clubs;

import android.content.Context;

import com.fsl.fslclubs.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by B47714 on 10/14/2015.
 */
public class Club {
    public static final int BASKETBALL_ID = 1;
    public static final int FOOTBALL_ID = 2;
    public static final int SNOOKER_ID = 3;
    public static final int YOGA_ID = 4;
    public static final int PINGPANG_ID = 5;
    public static final int TENNIS_ID = 6;
    public static final int RIDING_ID = 7;
    public static final int BADMINTON_ID = 8;
    private int id;
    private String name;
    private String desc;
    private int iconId;
    private String webViewAddr;


    public Club(int id, String name, String desc, int iconId, String webViewAddr) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.iconId = iconId;
        this.webViewAddr = webViewAddr;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public int getIconId() {
        return iconId;
    }

    public String getWebViewAddr() {
        return webViewAddr;
    }

    public static Map<Integer, String> getClubInfos(Context context) {
        HashMap<Integer, String> map = new HashMap<>();
        map.put(BASKETBALL_ID, context.getString(R.string.basketball));
        map.put(FOOTBALL_ID, context.getString(R.string.football));
        map.put(SNOOKER_ID, context.getString(R.string.snooker));
        map.put(YOGA_ID, context.getString(R.string.yoga));
        map.put(PINGPANG_ID, context.getString(R.string.pingpang));
        map.put(TENNIS_ID, context.getString(R.string.tennis));
        map.put(RIDING_ID, context.getString(R.string.riding));
        map.put(BADMINTON_ID, context.getString(R.string.badminton));

        return map;
    }
}
