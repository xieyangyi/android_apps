package com.fsl.fslclubs.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.util.LruCache;

import com.fsl.fslclubs.events.ClubEvent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

import libcore.io.DiskLruCache;

/**
 * Created by B47714 on 11/27/2015.
 */
public class BitmapCacheUtil {
    public static final int BITMAP_DISK_CACHE_SIZE = 200 * 1024 * 1024;       // bitmap disk cache 40M
    private static BitmapCacheUtil sBitmapCache;
    private LruCache<String, BitmapDrawable> mMemoryCache;
    private DiskLruCache mDiskLurCache;
    private Context mContext;

    private BitmapCacheUtil(Context context) {
        mContext = context;
        // init memoryCache
        int maxMemory = (int)Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, BitmapDrawable>(cacheSize) {
            @Override
            protected int sizeOf(String key, BitmapDrawable value) {
                return value.getBitmap().getByteCount();
            }
        };

        // init diskLruCache
        try {
            // create directory if not exist
            File cacheDir = getDiskCacheDir(mContext, "photoes");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            // create diskLruCache object
            mDiskLurCache = DiskLruCache.open(cacheDir, getAppVersion(mContext), 1, BITMAP_DISK_CACHE_SIZE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BitmapCacheUtil getInstance(Context context) {
        if (sBitmapCache == null) {
            sBitmapCache = new BitmapCacheUtil(context);
        }
        return sBitmapCache;
    }

    public void addBitmapToCache(final String key, final BitmapDrawable drawable) {
//        new Thread(new Runnable() {
//            @Override
//            public synchronized void run() {
                DiskLruCache.Snapshot snapshot = null;
                OutputStream os = null;
                ByteArrayOutputStream baos = null;
                InputStream is = null;

                // add to memory cache
                if (mMemoryCache.get(key) == null) {
                    mMemoryCache.put(key, drawable);
                }

                // add to disk cache
                try {
                    String hashKey = hashKeyForDisk(key);
                    snapshot = mDiskLurCache.get(hashKey);
                    if (snapshot == null) {
                        DiskLruCache.Editor editor = mDiskLurCache.edit(hashKey);
                        if (editor != null) {
                            os = editor.newOutputStream(0);
                            baos = new ByteArrayOutputStream();
                            drawable.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            is = new ByteArrayInputStream(baos.toByteArray());
                            BufferedOutputStream bos = new BufferedOutputStream(os, 8 * 1024);
                            BufferedInputStream bis = new BufferedInputStream(is, 8 * 1024);

                            int readByte;
                            while ((readByte = bis.read()) != -1) {
                                bos.write(readByte);
                            }
                            editor.commit();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (os != null) {
                            os.close();
                        }
                        if (is != null) {
                            is.close();
                        }
                        if (baos != null) {
                            baos.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
//            }
//        }).start();
    }

    public void addBitmapToCache(final String key, final InputStream inputStream) {
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        DiskLruCache.Editor editor;

        // add to disk cache
        try {
            String hashKey = hashKeyForDisk(key);
            DiskLruCache.Snapshot snapshot = mDiskLurCache.get(hashKey);
            if (snapshot == null) {
                editor = mDiskLurCache.edit(hashKey);
                if (editor != null) {
                    bos = new BufferedOutputStream(editor.newOutputStream(0), 8 * 1024);
                    bis = new BufferedInputStream(inputStream, 8 * 1024);
                    int readBytes;
                    while ((readBytes = bis.read()) != -1) {
                        bos.write(readBytes);
                    }
                    editor.commit();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (bis != null) {
                    bis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // add to memory cache
        if (mMemoryCache.get(key) == null) {
            BitmapDrawable drawable = getBitmapFromCache(key);
            mMemoryCache.put(key, drawable);
        }
    }

    public BitmapDrawable getBitmapFromCache(String key) {
        BitmapDrawable drawable = null;
        DiskLruCache.Snapshot snapshot = null;
        FileInputStream fileInputStream = null;
        FileDescriptor fileDescriptor = null;

        // get from memory cache first
        if ((drawable = mMemoryCache.get(key)) != null) {
            return drawable;
        }

        // if not exsit in memory cache, get from disk cache
        try {
            key = hashKeyForDisk(key);
            snapshot = mDiskLurCache.get(key);
            if (snapshot != null) {
                fileInputStream = (FileInputStream) snapshot.getInputStream(0);
                fileDescriptor = fileInputStream.getFD();
                if (fileDescriptor != null) {
                    Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                    if (bitmap != null) {
                        drawable = new BitmapDrawable(mContext.getResources(), bitmap);
                        return drawable;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileDescriptor == null && fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    /*
        flush DiskLruCache datas
     */
    public void flushCache() {
        if (mDiskLurCache != null) {
            try {
                mDiskLurCache.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
        use external cache dir if SD exist, otherwise use cache dir
     */
    private File getDiskCacheDir(Context context, String name) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }

        return new File(cachePath + File.separator + name);
    }

    /*
        get app version, used in DiskLruCache
     */
    private int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0
            );
            return info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 1;
    }

    /*
        Hash key for disk cache
     */
    private String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(key.getBytes());
            cacheKey = bytesToHexString(digest.digest());
        } catch (Exception e) {
            cacheKey = String.valueOf(key.hashCode());
        }

        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

}
