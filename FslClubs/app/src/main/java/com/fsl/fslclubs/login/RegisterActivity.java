package com.fsl.fslclubs.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fsl.fslclubs.R;
import com.fsl.fslclubs.util.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by B47714 on 10/12/2015.
 */
public class RegisterActivity extends Activity {
    private Button btnRegister;
    private EditText edtPhoneNo;
    private EditText edtPassword;
    private EditText edtQueryPassword;
    private EditText edtName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getActionBar().setDisplayHomeAsUpEnabled(true);     // set action bar navigation
        btnRegister = (Button)findViewById(R.id.btn_reg_submmit);
        edtPhoneNo = (EditText)findViewById(R.id.edt_reg_phone);
        edtPassword = (EditText)findViewById(R.id.edt_reg_password);
        edtQueryPassword = (EditText)findViewById(R.id.edt_reg_query_password);
        edtName = (EditText)findViewById(R.id.edt_reg_name);

        // register button click
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check whether the required edittext been filled
                if(TextUtils.isEmpty(edtPhoneNo.getText())) {
                    myShowDialog(getString(R.string.phone_not_fill));
                    return;
                }
                if(TextUtils.isEmpty(edtPassword.getText())) {
                    myShowDialog(getString(R.string.password_not_fill));
                    return;
                }
                if(TextUtils.isEmpty(edtQueryPassword.getText())) {
                    myShowDialog(getString(R.string.password_again_not_fill));
                    return;
                }
                if(TextUtils.isEmpty(edtName.getText())) {
                    myShowDialog(getString(R.string.name_not_fill));
                    return;
                }

                // check whether 2 times password aligned
                String password = edtPassword.getText().toString();
                String queryPassword = edtQueryPassword.getText().toString();
                if(!password.equals(queryPassword)) {
                    myShowDialog(getString(R.string.password_not_match));
                    return;
                }

                // register
                register();
            }
        });

        // check whether phoneNo exist, query server database
        edtPhoneNo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    Map<String, String> params = new HashMap<>();
                    params.put("requestCode", HttpUtil.REQUEST_IS_PHONE_EXSIT);
                    params.put("phone", edtPhoneNo.getText().toString());

                    final String url = HttpUtil.BASE_URL + "login";
                    String queryResult = null;
                    try {
                        queryResult = HttpUtil.queryStringForPost(url, params);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // check query result
                    if (queryResult == null) {
                        myShowDialog(getString(R.string.network_error));
                    } else if (queryResult.equals(HttpUtil.CHECK_PHONE_EXIST_TRUE)) {
                        myShowDialog(getString(R.string.phone_already_exist));
                    }
                }
            }
        });
    }

    private void myShowDialog(String msg) {
        new AlertDialog.Builder(this)
                .setMessage(msg)
                .setPositiveButton(R.string.Yes, null)
                .show();
    }

    private void register() {
        Map<String, String> params = new HashMap<>();
        params.put("requestCode", HttpUtil.REQUEST_REGISTER);
        params.put("phone", edtPhoneNo.getText().toString());
        params.put("password", edtPassword.getText().toString());
        params.put("name", edtName.getText().toString());

        final String url = HttpUtil.BASE_URL + "login";
        String queryResult = null;
        try {
            queryResult = HttpUtil.queryStringForPost(url, params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // check query result
        if(queryResult == null || queryResult.equals(HttpUtil.REGISTER_FAILED) ) {
            Log.v("register", "network error");
            Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        } else if(queryResult.equals(HttpUtil.REGISTER_SUCEESS)) {
            Log.v("register", "register success");
            Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_SHORT).show();
            // turn to login activity
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        }

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
