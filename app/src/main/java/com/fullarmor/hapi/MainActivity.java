package com.fullarmor.hapi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.support.v7.app.ActionBarActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.fullarmor.hapi.R;

/*****************************************************************
 <copyright file="MainActivity.java" company="FullArmor Corp.">
 Copyright (c) 2015 FullArmor Corporation. All rights reserved.
 </copyright>
 *****************************************************************/
public class MainActivity extends Activity {

    public final static String USERNAME = "com.fullarmor.hapi.USERNAME";
    public final static String PASSWORD = "com.fullarmor.hapi.PASSWORD";
    public final static String DISPLAYNAME = "com.fullarmor.hapi.DISPLAYNAME";
    public final static String FILEIDENTIFIER = "com.fullarmor.hapi.FILEIDENTIFIER";
    public String mToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preferences_file), Context.MODE_PRIVATE);
        this.mToken = sharedPref.getString(getString(R.string.hapiToken), "");
        if (!mToken.isEmpty()) {
            CheckTokenTask t = new CheckTokenTask(this,mToken);
            t.execute();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        switch (id) {
            case R.id.action_login:
                openLogin();
                return true;
            case R.id.action_userinfo:
                openUserInfo();
                return true;
            case R.id.action_folderenum:
                openFolderEnum();
                return true;
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onLogin(View view) {
        openLogin();
    }

    public void openLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void openUserInfo() {
        Intent intent = new Intent(this, UserInfoActivity.class);
        // temp debug code
        startActivity(intent);
    }

    public void openFolderEnum() {
        Intent intent = new Intent(this, FolderEnumActivity.class);
        startActivity(intent);
    }

    public class CheckTokenTask extends AsyncTask<Void, Void, Boolean> {

        private Activity mActivity;
        private String mHapiToken;

        CheckTokenTask(Activity activity, String token) {
            this.mActivity = activity;
            this.mHapiToken = token;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Boolean success = false;
            // call the HAPI gatekeeper to check whether the token is valid
            HAPIHelper helper = new HAPIHelper(getApplicationContext(),mHapiToken);
            SharedPreferences settingsPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            helper.gatekeeperBaseUrl = settingsPref.getString(SettingsActivity.KEY_PREF_GATEKEEPER_URL, "");
            success = helper.CheckToken(mHapiToken);
            return success;

        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (success) {
                finish();
                // open up a new FolderEnumActivity
                Intent myIntent;
                myIntent = new Intent(this.mActivity, FolderEnumActivity.class);
                startActivity(myIntent);
            }
        }
 }
}
