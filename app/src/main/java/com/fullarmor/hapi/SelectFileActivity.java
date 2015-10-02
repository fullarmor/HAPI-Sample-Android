package com.fullarmor.hapi;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.View;
import android.content.Context;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
/*****************************************************************
 <copyright file="SelectFileActivity.java" company="FullArmor Corp.">
 Copyright (c) 2015 FullArmor Corporation. All rights reserved.
 </copyright>
 *****************************************************************/
public class SelectFileActivity extends Activity {


    String mFolderIdentifier;
    private View mProgressView;
    public final static String FILEIDENTIFIER = "com.fullarmor.hapi.FILEIDENTIFIER";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_file);
        mProgressView = findViewById(R.id.upload_progress);
        Intent intent = getIntent();
        if ( intent != null ) {
            mFolderIdentifier = intent.getStringExtra(FILEIDENTIFIER);
        }

        new FileSelector(SelectFileActivity.this, mUploadFileListener).show();
    }
    String getFileNameFromPath(String targetPath) {
        int pos = targetPath.lastIndexOf(File.separatorChar);
        String fileName = null;
        if (pos > 0) {
            fileName = targetPath.substring(pos + 1);
        }
        return fileName;
    }
    OnHandleFileListener mUploadFileListener = new OnHandleFileListener() {
        @Override
        public void handleFile(final String filePath) {
            Toast.makeText(SelectFileActivity.this, "Upload: " + filePath, Toast.LENGTH_SHORT).show();
            String fileName = getFileNameFromPath(filePath);
            Uri fileUri = Uri.fromFile(new File(filePath));
            FileUploadTask uploadTask = new FileUploadTask(SelectFileActivity.this,mFolderIdentifier,fileName,fileUri);
            uploadTask.execute();
            finish();

        }
    };

    public class FileUploadTask extends AsyncTask<Void, Void, Uri> {

        private final String mFolderIdentifier;
        private final String mTargetFileName;
        private final Uri mSourceFilePath;
        private final String mToken;
        private String mError;
        private Activity mActivity;
        FileUploadTask(Activity activity,String targetFolderIdentifier,String targetFileName, Uri fileUrl) {
            this.mActivity = activity;
            this.mFolderIdentifier = targetFolderIdentifier;
            this.mTargetFileName = targetFileName;
            this.mSourceFilePath = fileUrl;
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preferences_file), Context.MODE_PRIVATE);
            this.mToken = sharedPref.getString(getString(R.string.hapiToken),"");
        }

        @Override
        protected Uri doInBackground(Void... params) {

            try {
                // perform the upload
                HAPIHelper helper = new HAPIHelper(getApplicationContext(),this.mToken);
                SharedPreferences settingsPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                helper.gatekeeperBaseUrl = settingsPref.getString(SettingsActivity.KEY_PREF_GATEKEEPER_URL, "");
                helper.mToken = this.mToken;

                Boolean result  = helper.UploadFile(this.mToken,mSourceFilePath,mFolderIdentifier,mTargetFileName);

                if ( result )
                {
                    finish();
                }

            } catch ( IOException e) {
                mError = e.getMessage();
            }
            catch ( ExecutionException ex) {
                mError = ex.getMessage() + ex.getStackTrace();
            }


            return null;
        }
        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
        public void showProgress(final boolean show) {
            // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
            // for very easy animations. If available, use these APIs to fade-in
            // the progress spinner.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                int shortTime = getResources().getInteger(android.R.integer.config_shortAnimTime);


                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                mProgressView.animate().setDuration(shortTime).alpha(
                        show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });
            } else {
                // The ViewPropertyAnimator APIs are not available, so simply show
                // and hide the relevant UI components.
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
               // //mListView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        }
        @Override
        protected void onPostExecute(final Uri targetUri) {

            showProgress(false);

            if (mError != null ) {
                Toast.makeText(getApplicationContext(),mError, Toast.LENGTH_LONG);
            }
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
        }



    }
}
