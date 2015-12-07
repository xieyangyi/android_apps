package com.fsl.fslclubs.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.fsl.fslclubs.main.MainActivity;
import com.fsl.fslclubs.R;
import com.fsl.fslclubs.util.HttpUtil;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends Activity {
    private EditText edtPhoneNo;
    private EditText edtPassword;
    private Button btnSubmmit;
    private Button btnRegister;
    private Button btnStroll;
    private static User loggedInUser = null;          // to reserve the information of logged in user
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        edtPhoneNo = (EditText)findViewById(R.id.edt_phone);
        edtPassword = (EditText)findViewById(R.id.edt_password);
        btnSubmmit = (Button)findViewById(R.id.btn_submmit);
        btnRegister = (Button)findViewById(R.id.btn_register);
        btnStroll = (Button)findViewById(R.id.btn_stroll);
        preferences = getSharedPreferences("lastLoginUser", MODE_PRIVATE);
        if (preferences != null)
            editor = preferences.edit();

        // get last loggin phone from shared preference
        if (preferences != null) {
            String lastPhone = preferences.getString("phone", null);
            edtPhoneNo.setText(lastPhone);
        }

        // submmit button
        btnSubmmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    login();
                }
            }
        });

        // register button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // stroll button
        btnStroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }

    private void myShowDialog(String msg) {
        new AlertDialog.Builder(this)
                .setMessage(msg)
                .setPositiveButton(R.string.Yes, null)
                .show();
    }

    private boolean validate() {
        String phoneNo = edtPhoneNo.getText().toString();
        String password = edtPassword.getText().toString();

        if (phoneNo.equals("")) {
            myShowDialog(getString(R.string.phone_not_fill));
            return false;
        }

        if (password.equals("")) {
            myShowDialog(getString(R.string.password_not_fill));
            return false;
        }

        return true;
    }

    private void login() {
         /* query server for phoneNo and password */
        Map<String, String> params = new HashMap<>();
        params.put("requestCode", HttpUtil.REQUEST_LOGIN);
        params.put("phone", edtPhoneNo.getText().toString());
        params.put("password", edtPassword.getText().toString());

        final String url = HttpUtil.BASE_URL + "login";
        String queryResult = null;
        try {
            queryResult = HttpUtil.queryStringForPost(url, params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // check query result
        if(queryResult == null) {
            myShowDialog(getString(R.string.network_error));
        } else if(queryResult.equals(HttpUtil.LOGIN_PASSWORD_WRONG)) {
            myShowDialog(getString(R.string.password_wrong));
        } else if(queryResult.equals(HttpUtil.LOGIN_PHONE_NOT_EXIST)) {
            myShowDialog(getString(R.string.phone_not_exist));
        } else {
            // save user to local SQLite
            String results[] = queryResult.split(";");
            loggedInUser = new User(results[0], results[1], results[2], results[3] ,
                    results[4], results[5], results[6], results[7], results[8], results[9], results[10]);
            loggedInUser.saveUsertoLocal(this);

            // save phone to shared perference for next log in
            editor.putString("phone", results[1]);
            editor.commit();

            // turn to main activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    public static User getLoggedInUser() {
        return loggedInUser;
    }

    public static void setLoggedInUser(User user) {
        loggedInUser = user;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
