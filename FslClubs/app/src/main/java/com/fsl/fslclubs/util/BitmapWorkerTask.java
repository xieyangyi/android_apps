package com.fsl.fslclubs.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by B47714 on 11/27/2015.
 */
public class BitmapWorkerTask extends AsyncTask<String, Void, BitmapDrawable> {
    private OnFinishListener finishListener;
    private Context mContext;

    public BitmapWorkerTask(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected BitmapDrawable doInBackground(String... params) {
        String bitmapUrl = params[0];
        HttpURLConnection conn = null;

        try {
            URL url = new URL(bitmapUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5 * 1000);
            conn.connect();
            BitmapCacheUtil bitmapCache = BitmapCacheUtil.getInstance(mContext);
            bitmapCache.addBitmapToCache(bitmapUrl, conn.getInputStream());
            BitmapDrawable drawable = bitmapCache.getBitmapFromCache(bitmapUrl);
            return drawable;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(BitmapDrawable drawable) {
        super.onPostExecute(drawable);
        this.finishListener.onFinish(drawable);
    }

    public void setOnFinishListener(OnFinishListener listener) {
        this.finishListener = listener;
    }

    public interface OnFinishListener {
        void onFinish(BitmapDrawable drawable);
    }
}
