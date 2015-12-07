package com.fsl.fslclubs.me;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.fsl.fslclubs.R;
import com.fsl.fslclubs.util.HttpUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by B47714 on 12/4/2015.
 */
public class UpdateManager {
    private static final int DOWNLOADING = 1;
    private static final int DOWNLOAD_FINISH = 2;
    private static final int MSG_NEW_VERSION_EXSIT = 10;
    private static final int MSG_NEW_VERSION_NOT_EXSIT = 11;
    HashMap<String, String> mHashMap;
    private String mSavePath;
    private int progress;
    private boolean cancelUpdate = false;
    private Context mContext;
    private ProgressBar mProgressBar;
    private Dialog mDownloadDialog;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOADING:
                    mProgressBar.setProgress(progress);
                    break;
                case DOWNLOAD_FINISH:
                    mDownloadDialog.dismiss();
                    installAPK();
                    break;
                case MSG_NEW_VERSION_EXSIT:
                    showNoticeDialog();
                    break;
                case MSG_NEW_VERSION_NOT_EXSIT:
                    Toast.makeText(mContext, mContext.getString(R.string.softwear_update_no_need),
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;

            }
        }
    };

    public UpdateManager(Context context) {
        this.mContext = context;
    }

    public void checkUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int versionCode = getVersionCode(mContext);
                ParseXmlService service = new ParseXmlService();
                try {
                    String path = HttpUtil.BASE_URL + "myRes/version.xml";
                    URL url = new URL(path);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5 * 1000);
                    conn.connect();
                    InputStream inputStream = conn.getInputStream();
                    mHashMap = service.parseXml(inputStream);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (mHashMap != null) {
                    int serviceCode = Integer.valueOf(mHashMap.get("version"));
                    if (serviceCode > versionCode) {
                        mHandler.sendEmptyMessage(MSG_NEW_VERSION_EXSIT);
                    }
                } else {
                    mHandler.sendEmptyMessage(MSG_NEW_VERSION_NOT_EXSIT);
                }
            }
        }).start();

    }

    private int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            versionCode = context.getPackageManager().getPackageInfo("com.fsl.fslclubs", 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return versionCode;
    }

    private void showNoticeDialog() {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.softwear_update)
                .setMessage(R.string.softwear_update_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        showDownloadDialog();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void showDownloadDialog() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_download_apk, null);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar_download_apk);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setTitle(R.string.downloading)
                .setView(view)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        cancelUpdate = true;
                    }
                });

        mDownloadDialog = builder.create();
        mDownloadDialog.show();
        downloadApk();
    }

    private void downloadApk() {
        new downloadApkThread().start();
    }

    private class downloadApkThread extends Thread {
        @Override
        public void run() {
            try {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    String sdPath = Environment.getExternalStorageDirectory() + "/";
                    mSavePath = sdPath + "download";
                    URL url = new URL(mHashMap.get("url"));
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    int length = conn.getContentLength();
                    InputStream is = conn.getInputStream();

                    File file = new File(mSavePath);
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    File apkFile = new File(mSavePath, mHashMap.get("name"));
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int count = 0;
                    byte[] buf = new byte[1024];
                    do {
                        int numRead = is.read(buf);
                        count += numRead;
                        progress = (int) (((float) count / length) * 100);
                        mHandler.sendEmptyMessage(DOWNLOADING);
                        if (numRead <= 0) {
                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                            break;
                        }

                        fos.write(buf, 0, numRead);

                    } while (!cancelUpdate);
                    fos.close();
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void installAPK() {
        File apkFile = new File(mSavePath, mHashMap.get("name"));
        if (!apkFile.exists())
            return;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + apkFile.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }
}
