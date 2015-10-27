package com.fsl.fslclubs.me;

import android.app.Fragment;
import android.content.Intent;
import android.media.audiofx.BassBoost;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fsl.fslclubs.R;
import com.fsl.fslclubs.login.LoginActivity;
import com.fsl.fslclubs.login.User;

import org.w3c.dom.Text;

/**
 * Created by B47714 on 10/14/2015.
 */
public class MeFragment extends Fragment {
    private TextView txtMe;
    private TextView txtSettings;
    private TextView txtCheckVersion;
    private TextView txtAbout;
    private TextView txtExit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, null);
        txtMe = (TextView)view.findViewById(R.id.txt_fragment_me_me);
        txtSettings = (TextView)view.findViewById(R.id.txt_fragment_me_settings);
        txtCheckVersion = (TextView)view.findViewById(R.id.txt_fragment_me_version);
        txtAbout = (TextView)view.findViewById(R.id.txt_fragment_me_about);
        txtExit = (TextView)view.findViewById(R.id.txt_fragment_me_exit);

        // text me
        setTextMe();

        // text settings
        setTextSettings();

        // text check version
        setTextCheckVersion();

        // text about
        setTextAbout();

        // text exit
        setTextExit();

        return view;
    }

    private void setTextMe() {
        // get logged in user information
        final User loggedInUser = LoginActivity.getLoggedInUser();
        if (loggedInUser == null) {
            txtMe.setText(getString(R.string.please_login));
        } else {
            String name = loggedInUser.getName();
            txtMe.setText(name);
        }

        txtMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(loggedInUser == null) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getActivity(), PersonInfoActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void setTextSettings() {
        txtSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setTextCheckVersion() {
        txtCheckVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // todo
            }
        });
    }

    private void setTextAbout() {
        txtAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AboutActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setTextExit() {
        txtExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
