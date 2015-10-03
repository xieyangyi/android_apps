package com.example.b47714.mymemo;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.b47714.mymemo.MemoConstants.*;

/**
 * Created by B47714 on 10/3/2015.
 */
public class AlarmActivity extends Activity {
    private Button btnCancel;
    private TextView txtMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        btnCancel = (Button)findViewById(R.id.btn_cancel);
        txtMsg = (TextView)findViewById(R.id.txt_msg);
        final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.sleep_away);
        final Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);

        // get intent and data
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String msg = bundle.getString(TableColumnNames.CONTENT);
        final boolean isSoundOn = bundle.getBoolean(TableColumnNames.IS_SOUND_ON);

        // play music or vibrate
        if(isSoundOn) {
            mediaPlayer.start();
        } else {
            vibrator.vibrate(new long[]{400, 800}, 0);
        }

        // set textview and button
        txtMsg.setText(msg);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSoundOn) {
                    mediaPlayer.stop();
                } else {
                    vibrator.cancel();
                }
                finish();
            }
        });
    }
}
