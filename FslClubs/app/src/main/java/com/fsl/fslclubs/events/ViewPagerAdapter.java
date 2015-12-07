package com.fsl.fslclubs.events;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fsl.fslclubs.R;
import com.fsl.fslclubs.util.BitmapCacheUtil;
import com.fsl.fslclubs.util.BitmapWorkerTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by B47714 on 11/25/2015.
 */
public class ViewPagerAdapter extends PagerAdapter {
    private List<String> listUrl = null;
    private List<View> mlist = new ArrayList<>();
    private Context mContext;

    public ViewPagerAdapter(List<String> list, Context context) {
        this.listUrl = list;
        this.mContext = context;

        LayoutInflater inflater = LayoutInflater.from(mContext);
        for (int i = 0; i < list.size(); i++) {
            View view = inflater.inflate(R.layout.viewpager_imageview, null);
            mlist.add(view);
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mlist.get(position));
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        // get from cache first
        final String url = listUrl.get(position);
        final BitmapCacheUtil bitmapCache = BitmapCacheUtil.getInstance(mContext);
        BitmapDrawable drawable = bitmapCache.getBitmapFromCache(url);

        // if not exist in cache, download it
        if (drawable == null) {
            BitmapWorkerTask task = new BitmapWorkerTask(mContext);
            task.setOnFinishListener(new BitmapWorkerTask.OnFinishListener() {
                @Override
                public void onFinish(BitmapDrawable bitmapDrawable) {
                    if (bitmapDrawable != null) {
                        ImageView imageView = (ImageView) mlist.get(position).findViewById(R.id.viewpager_imageview);
                        imageView.setImageDrawable(bitmapDrawable);
                        container.removeView(mlist.get(position));
                        container.addView(mlist.get(position));
                    }
                }
            });
            task.execute(url);
        }

        // set imageview
        ImageView imageView = (ImageView) mlist.get(position).findViewById(R.id.viewpager_imageview);
        imageView.setImageDrawable(drawable);
        container.removeView(mlist.get(position));
        container.addView(mlist.get(position));

        return mlist.get(position);
    }

    @Override
    public int getCount() {
        return listUrl.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return (view == o);
    }

}
