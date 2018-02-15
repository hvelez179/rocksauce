package com.example.android.hectorvelez_androidcodechallenge.activities;


import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.example.android.hectorvelez_androidcodechallenge.R;
import com.example.android.hectorvelez_androidcodechallenge.models.Post;

import java.io.Serializable;

public class ShareActivity extends AppCompatActivity implements View.OnClickListener{

    public final static String ARG_POST = "arg_post";
    public final static String ARG_SHARE_METHOD = "arg_share_method";
    public final static int REQUEST_CODE_SHARE_METHOD = 1001;
    public final static String SHARE_METHOD_EMAIL = "text/html";
    public final static String SHARE_METHOD_SMS = "text/plain";

    private Post mPost;
    private String mShareMethod = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) { //old devices

            getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else { // Jellybean and up

            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);

            ActionBar actionBar = getActionBar();

            if(actionBar != null) {

                actionBar.hide();
            }
        }

        if(getIntent() != null) {

            if(getIntent().getExtras() != null) {

                Serializable postObj = getIntent().getExtras().getSerializable(ARG_POST);

                if(postObj == null) {

                    finish();
                }

                mPost = (Post) postObj;
            }
        }

        View button = findViewById( R.id.btnEmail);
        button.setOnClickListener(this);

        button = findViewById(R.id.btnSms);
        button.setOnClickListener(this);

        button = findViewById(R.id.btnCancel);
        button.setOnClickListener(this);
    }

    @Override
    public void finish() {

        if(mPost != null && !"".equals(mShareMethod)) {

            Intent intent = new Intent();
            intent.putExtra(ARG_POST, mPost);
            intent.putExtra(ARG_SHARE_METHOD, mShareMethod);
            setResult(RESULT_OK, intent);
        } else {

            setResult(RESULT_CANCELED);
        }

        super.finish();
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.slide_out_to_bottom);
    }

    @Override
    public void onBackPressed() {

        finish();
    }

    @Override
    public void onClick(View view) {

        switch(view.getId()) {

            case R.id.btnEmail:
                mShareMethod = SHARE_METHOD_EMAIL;
                break;

            case R.id.btnSms:
                mShareMethod = SHARE_METHOD_SMS;
                break;
            default:
                //in case "Cancel" button is clicked, make mShareMethod == "" to fire RESULT_CANCELED result code on finish()
                mShareMethod = "";
        }

        finish();

    }
}
