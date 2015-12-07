package com.fsl.fslclubs.me;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fsl.fslclubs.R;
import com.fsl.fslclubs.clubs.Club;
import com.fsl.fslclubs.login.LoginActivity;
import com.fsl.fslclubs.login.User;
import com.fsl.fslclubs.login.UserInfoProvider;
import com.fsl.fslclubs.main.MainActivity;

import org.w3c.dom.Text;

import java.util.Map;
import java.util.zip.Inflater;

/**
 * Created by B47714 on 10/15/2015.
 */
public class PersonInfoActivity extends Activity {
    private TextView txtInfoName;
    private TextView txtInfoCoreid;
    private TextView txtInfoEmail;
    private TextView txtInfoSignature;
    private TextView txtInfoClubs;
    private RadioButton rbtnSexMale;
    private RadioButton rbtnSexFemale;
    private Button btnInfoSave;
    private RelativeLayout layoutInfoName;
    private RelativeLayout layoutInfoCoreid;
    private RelativeLayout layoutInfoEmail;
    private RelativeLayout layoutInfoSignature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_info);
        getActionBar().setDisplayHomeAsUpEnabled(true);     // set action bar navigation

        txtInfoName = (TextView)findViewById(R.id.txt_activity_info_name);
        txtInfoCoreid = (TextView)findViewById(R.id.txt_activity_info_coreid);
        txtInfoEmail = (TextView)findViewById(R.id.txt_activity_info_email);
        txtInfoSignature = (TextView)findViewById(R.id.txt_activity_info_signature);
        txtInfoClubs = (TextView)findViewById(R.id.txt_activity_info_clubs);
        rbtnSexMale = (RadioButton)findViewById(R.id.rbtn_activity_info_male);
        rbtnSexFemale = (RadioButton)findViewById(R.id.rbtn_activity_info_female);
        btnInfoSave = (Button)findViewById(R.id.btn_activity_info_save);
        layoutInfoName = (RelativeLayout)findViewById(R.id.layout_activity_info_name);
        layoutInfoCoreid = (RelativeLayout)findViewById(R.id.layout_activity_info_coreid);
        layoutInfoEmail = (RelativeLayout)findViewById(R.id.layout_activity_info_email);
        layoutInfoSignature = (RelativeLayout)findViewById(R.id.layout_activity_info_signature);

        // display, using local SQLite
        displayText();

        // modification listener
        mofifyText();

        // save and upload to server
        btnInfoSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (saveText()) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("radioButtonCheckId", R.id.main_rbtn_me);
                    Intent intent = new Intent(PersonInfoActivity.this, MainActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
    }

    private void myEditDialog(final TextView txtContent, String title) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_text, null);
        final EditText editText = (EditText)view.findViewById(R.id.edt_dialog);

        new AlertDialog.Builder(PersonInfoActivity.this)
                .setTitle(title)
                .setView(view)
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String editedText = editText.getText().toString();
                        txtContent.setText(editedText);
                    }
                })
                .show();
    }

    private void displayText() {
        User user = LoginActivity.getLoggedInUser();

        if(!user.getName().equals("null"))
            txtInfoName.setText(user.getName());
        if(!user.getCoreid().equals("null"))
            txtInfoCoreid.setText(user.getCoreid());
        if(!user.getEmail().equals("null"))
            txtInfoEmail.setText(user.getEmail());
        if(!user.getSignature().equals("null"))
            txtInfoSignature.setText(user.getSignature());
        if(!user.getSex().equals("null")) {
            rbtnSexMale.setChecked(user.getSex().equals(getString(R.string.male)));
            rbtnSexFemale.setChecked(user.getSex().equals(getString(R.string.female)));
        } else {
            rbtnSexMale.setChecked(true);
            rbtnSexFemale.setChecked(false);
        }
        if(!user.getClub().equals("null")) {
            String parsedClub = parseClub(user.getClub());
            txtInfoClubs.setText(parsedClub);
        }
    }

    private void mofifyText() {
        layoutInfoName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myEditDialog(txtInfoName, getString(R.string.info_title_name));
            }
        });
        layoutInfoCoreid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myEditDialog(txtInfoCoreid, null);
            }
        });
        layoutInfoEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myEditDialog(txtInfoEmail, null);
            }
        });
        layoutInfoSignature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myEditDialog(txtInfoSignature, null);
            }
        });
    }

    private boolean saveText() {
        User user = LoginActivity.getLoggedInUser();
        User modifiedUser = new User(
                user.getId(),
                user.getPhone(),
                user.getPassword(),
                txtInfoName.getText().toString(),
                user.getClub(),
                rbtnSexMale.isChecked() ? getString(R.string.male) : getString(R.string.female),
                txtInfoEmail.getText().toString(),
                txtInfoCoreid.getText().toString(),
                txtInfoSignature.getText().toString(),
                user.getLegalid(),
                user.getActivity()
        );
        LoginActivity.setLoggedInUser(modifiedUser);

        if (!modifiedUser.saveUsertoLocal(this))
            return false;
        if (!modifiedUser.saveUsertoServer(this))
            return false;

        return true;
    }

    private String parseClub(String infoClub) {
        String[] clubIds = infoClub.split(",");
        Map<Integer, String> clubInfos = Club.getClubInfos(this);
        String res = null;

        for (int i = 0; i < clubIds.length; i++) {
            if (res == null) {
                res = clubInfos.get(Integer.valueOf(clubIds[i]));
            } else {
                res += ", " + clubInfos.get(Integer.valueOf(clubIds[i]));
            }
        }

        return res;
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
