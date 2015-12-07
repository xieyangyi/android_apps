package com.fsl.fslclubs.main;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.fsl.fslclubs.R;
import com.fsl.fslclubs.login.LoginActivity;

import java.io.InputStream;

/**
 * Created by B47714 on 12/5/2015.
 */
public class UserNotLoggedInFragment extends Fragment {
    private Button btnPleaseLogin;
    private Context context;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        View view = inflater.inflate(R.layout.fragment_user_not_logged_in, null);
        btnPleaseLogin = (Button) view.findViewById(R.id.btn_login);

        btnPleaseLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}
