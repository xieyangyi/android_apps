package com.example.b47714.mymemo;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by B47714 on 10/1/2015.
 */
public class AlarmActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm);

        TextView txtMsg = (TextView)findViewById(R.id.msg_text);
        Button btnCancel = (Button)findViewById(R.id.btn_cancel);
        final Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.sleep_away);;

        // sound or vibrate
        Bundle bundle = getIntent().getExtras();
        final Boolean isSoundOn = bundle.getBoolean("isSoundOn");
        if(isSoundOn) {
            mediaPlayer.start();
        } else {
            vibrator.vibrate(new long[]{400, 800, }, 0);    // repating vibrate, one cycle: wait 400ms, continue 800ms
        }

        // notification
        String msg = getIntent().getStringExtra("content");
        final NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new Notification();
        notification.tickerText = msg;
        nm.notify(1, notification);

        // textview and cancel button
        txtMsg.setText(msg);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nm.cancel(1);
                if(isSoundOn) {
                    mediaPlayer.stop();
                } else {
                    vibrator.cancel();
                }
                finish();
            }
        });
    }
}
