package com.fsl.fslclubs.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fsl.fslclubs.R;
import com.fsl.fslclubs.clubs.Club;
import com.fsl.fslclubs.login.LoginActivity;
import com.fsl.fslclubs.login.User;
import com.fsl.fslclubs.util.SlidingLayout;

import java.util.Map;

/**
 * Created by B47714 on 12/5/2015.
 */
public class UserLoggedInFragment extends Fragment {
    private TextView txtInfoName;
    private TextView txtInfoCoreid;
    private TextView txtInfoEmail;
    private TextView txtInfoSignature;
    private TextView txtInfoClubs;
    private RadioButton rbtnSexMale;
    private RadioButton rbtnSexFemale;
    private Button btnInfoSave;
    private Button btnExit;
    private RelativeLayout layoutInfoName;
    private RelativeLayout layoutInfoCoreid;
    private RelativeLayout layoutInfoEmail;
    private RelativeLayout layoutInfoSignature;
    private Context context;
    private SlidingLayout slidingLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        slidingLayout = ((MainActivity) context).getSlidingLayout();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_logged_in, null);
        txtInfoName = (TextView) view.findViewById(R.id.txt_activity_info_name);
        txtInfoCoreid = (TextView) view.findViewById(R.id.txt_activity_info_coreid);
        txtInfoEmail = (TextView) view.findViewById(R.id.txt_activity_info_email);
        txtInfoSignature = (TextView) view.findViewById(R.id.txt_activity_info_signature);
        txtInfoClubs = (TextView) view.findViewById(R.id.txt_activity_info_clubs);
        rbtnSexMale = (RadioButton) view.findViewById(R.id.rbtn_activity_info_male);
        rbtnSexFemale = (RadioButton) view.findViewById(R.id.rbtn_activity_info_female);
        btnInfoSave = (Button) view.findViewById(R.id.btn_activity_info_save);
        btnExit = (Button) view.findViewById(R.id.btn_activity_info_exit);
        layoutInfoName = (RelativeLayout) view.findViewById(R.id.layout_activity_info_name);
        layoutInfoCoreid = (RelativeLayout) view.findViewById(R.id.layout_activity_info_coreid);
        layoutInfoEmail = (RelativeLayout) view.findViewById(R.id.layout_activity_info_email);
        layoutInfoSignature = (RelativeLayout) view.findViewById(R.id.layout_activity_info_signature);

        // display, using local SQLite
        displayText();

        // modification listener
        mofifyText();

        // save and upload to server
        btnInfoSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (saveText()) {
                    slidingLayout.scrollToRightlayout();
                }
            }
        });

        // exit
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.setLoggedInUser(null);
                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
            }
        });

        return view;
    }

    private void myEditDialog(final TextView txtContent, String title) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_edit_text, null);
        final EditText editText = (EditText)view.findViewById(R.id.edt_dialog);

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(view)
                .setNegativeButton(context.getString(R.string.cancel), null)
                .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
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
            rbtnSexMale.setChecked(user.getSex().equals(context.getString(R.string.male)));
            rbtnSexFemale.setChecked(user.getSex().equals(context.getString(R.string.female)));
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
                myEditDialog(txtInfoName, context.getString(R.string.info_title_name));
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
                rbtnSexMale.isChecked() ? context.getString(R.string.male) : context.getString(R.string.female),
                txtInfoEmail.getText().toString(),
                txtInfoCoreid.getText().toString(),
                txtInfoSignature.getText().toString(),
                user.getLegalid(),
                user.getActivity()
        );
        LoginActivity.setLoggedInUser(modifiedUser);

        if (!modifiedUser.saveUsertoLocal(context))
            return false;
        if (!modifiedUser.saveUsertoServer(context))
            return false;

        return true;
    }

    private String parseClub(String infoClub) {
        String[] clubIds = infoClub.split(",");
        Map<Integer, String> clubInfos = Club.getClubInfos(context);
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
}
