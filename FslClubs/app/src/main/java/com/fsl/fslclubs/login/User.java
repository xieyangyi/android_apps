package com.fsl.fslclubs.login;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.fsl.fslclubs.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by B47714 on 10/15/2015.
 */
public class User {
    private String id;
    private String phone;
    private String password;
    private String name;
    private String club;
    private String sex;
    private String email;
    private String coreid;
    private String signature;
    private String legalid;
    private String activity;

    public User(String id, String phone, String password, String name,
                String club, String sex, String email, String coreid,
                String signature, String legalid, String activity) {
        this.id = id;
        this.phone = phone;
        this.password = password;
        this.name = name;
        this.club = club;
        this.sex = sex;
        this.email = email;
        this.coreid = coreid;
        this.signature = signature;
        this.legalid = legalid;
        this.activity = activity;
    }

    public static User createUserFromLocal(Context context, String id) {
        int userId = Integer.valueOf(id);
        Uri uri = ContentUris.withAppendedId(UserInfoProvider.USER_TBL_URI, userId);
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        return new User(
                id,
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getString(7),
                cursor.getString(8),
                cursor.getString(9),
                null                // todo, add activity item to local SQLite
        );
    }

    public boolean saveUsertoLocal(Context context) {
        ContentValues values = new ContentValues();
        values.clear();
        values.put(UserInfoProvider.TBL_PHONE, getPhone());
        values.put(UserInfoProvider.TBL_PASSWORD, getPassword());
        values.put(UserInfoProvider.TBL_NAME, getName());
        values.put(UserInfoProvider.TBL_CLUB, getClub());
        values.put(UserInfoProvider.TBL_SEX, getSex());
        values.put(UserInfoProvider.TBL_EMAIL, getEmail());
        values.put(UserInfoProvider.TBL_COREID, getCoreid());
        values.put(UserInfoProvider.TBL_SIGNATURE, getSignature());
        values.put(UserInfoProvider.TBL_LEGALID, getLegalid());

        int userId = Integer.valueOf(getId());
        Uri uri = ContentUris.withAppendedId(UserInfoProvider.USER_TBL_URI, userId);
        context.getContentResolver().update(uri, values, null, null);

        return true;
    }

    public static User createUserFromServer() {
        return null;
    }

    public boolean saveUsertoServer(Context context) {
        Map<String, String> params = new HashMap<>();
        params.put("requestCode", HttpUtil.REQUEST_SAVE_USER);
        params.put("id", getId());
        params.put("phone", getPhone());
        params.put("password", getPassword());
        params.put("name", getName());
        params.put("club", getClub());
        params.put("sex", getSex());
        params.put("email", getEmail());
        params.put("coreid", getCoreid());
        params.put("signature", getSignature());
        params.put("legal_id", getLegalid());

        final String url = HttpUtil.BASE_URL + "login";
        String queryResult = null;
        try {
            queryResult = HttpUtil.queryStringForPost(url, params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // check query result
        if(queryResult == null || queryResult.equals(HttpUtil.SAVE_USER_FAILED) ) {
            Log.v("fslclubs", "save user network error");
            Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            return false;
        } else if(queryResult.equals(HttpUtil.SAVE_USER_SUCEESS)) {
            Log.v("fslclubs", "save user success");
            Toast.makeText(context, context.getString(R.string.save_user_success), Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    public String getId() {
        return id;
    }

    public String getPhone() {
        return phone;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getClub() {
        return club;
    }

    public String getSex() {
        return sex;
    }

    public String getEmail() {
        return email;
    }

    public String getCoreid() {
        return coreid;
    }

    public String getSignature() {
        return signature;
    }

    public String getLegalid() {
        return legalid;
    }

    public String getActivity() {
        return activity;
    }
}
