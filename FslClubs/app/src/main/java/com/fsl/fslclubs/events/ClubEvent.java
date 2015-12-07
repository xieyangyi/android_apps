package com.fsl.fslclubs.events;

import android.graphics.drawable.BitmapDrawable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by B47714 on 11/20/2015.
 */
public class ClubEvent implements Parcelable {
    private String id;
    private String name;
    private String icon;
    private String website;
    private String address;
    private String time;
    private String expireTime;
    private BitmapDrawable drawable;

    public ClubEvent(String id, String name, String icon, String website,
                     String address, String time, String expireTime, BitmapDrawable drawable) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.website = website;
        this.address = address;
        this.time = time;
        this.expireTime = expireTime;
        this.drawable = drawable;
    }

    public ClubEvent(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.icon = in.readString();
        this.website = in.readString();
        this.address = in.readString();
        this.time = in.readString();
        this.expireTime = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(icon);
        dest.writeString(website);
        dest.writeString(address);
        dest.writeString(time);
        dest.writeString(expireTime);
    }

    public static final Parcelable.Creator<ClubEvent> CREATOR = new Parcelable.Creator<ClubEvent>() {
        public ClubEvent createFromParcel(Parcel in) {
            return new ClubEvent(in);
        }

        public ClubEvent[] newArray(int size) {
            return new ClubEvent[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public String getWebsite() {
        return website;
    }

    public String getAddress() {
        return address;
    }

    public String getTime() {
        return time;
    }

    public String getExpireTime() {
        return expireTime;
    }

    public BitmapDrawable getDrawable() {
        return drawable;
    }

    public void setDrawable(BitmapDrawable drawable) {
        this.drawable = drawable;
    }
}
