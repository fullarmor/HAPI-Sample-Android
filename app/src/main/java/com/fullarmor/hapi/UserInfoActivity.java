package com.fullarmor.hapi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.fullarmor.hapi.R;
import com.fullarmor.hapi.models.DomainLoginResponse;

/*****************************************************************
 <copyright file="UserInfoActivity.java" company="FullArmor Corp.">
 Copyright (c) 2015 FullArmor Corporation. All rights reserved.
 </copyright>
 *****************************************************************/
public class UserInfoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ( getActionBar() != null )
            getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_user_info);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preferences_file), Context.MODE_PRIVATE);
        String display = sharedPref.getString(getString(R.string.preferences_displayName), "");
        int role = sharedPref.getInt(getString(R.string.preferences_role), DomainLoginResponse.ROLE_UNKNOWN);
        TextView vDisplayName = (TextView)findViewById(R.id.userinfo_displayname);
        TextView vRole = (TextView)findViewById(R.id.userinfo_role);
        // Set the text view as the activity layout
        if ( display == null || display.isEmpty()) {
            LaunchLoginActivity();
        }
        else {
            vDisplayName.setText(display);
            vRole.setText(getRoleString(role));
        }
    }

    private void LaunchLoginActivity()
    {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(LoginActivity.CALLINGACTIVITY, "@string/calling_activity_userinfo");
        startActivity(intent);
        finish();
    }

    private String getRoleString(int role) {
        switch (role) {
            case DomainLoginResponse.ROLE_ADMIN:
                return getString(R.string.role_admin);
            case DomainLoginResponse.ROLE_NONE:
                return getString(R.string.role_none);
            case DomainLoginResponse.ROLE_READONLYADMIN:
                return getString(R.string.role_readonly_admin);
            case DomainLoginResponse.ROLE_READONLYUSER:
                return getString(R.string.role_readonly_user);
            case DomainLoginResponse.ROLE_USER:
                return getString(R.string.role_user);
            case DomainLoginResponse.ROLE_SYSTEM:
                return getString(R.string.role_system);
            case DomainLoginResponse.ROLE_UNKNOWN:
                return getString(R.string.role_unknown);
        }
        // TODO: log an error here
        return getString(R.string.role_unknown);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            openSettings();
            return true;
        }
        else if (id == R.id.action_logout) {
            logoutFromHAPI();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void logoutFromHAPI() {
        HAPIHelper helper = new HAPIHelper(getApplicationContext(),null);
        SharedPreferences settingsPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        helper.gatekeeperBaseUrl = settingsPref.getString(SettingsActivity.KEY_PREF_GATEKEEPER_URL, "");
        helper.Logout();
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preferences_file),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.preferences_displayName), "");
        editor.putString(getString(R.string.preferences_hapiToken), "");
        editor.putInt(getString(R.string.preferences_role), DomainLoginResponse.ROLE_UNKNOWN);
        editor.commit();
        finish();
        // open up a new FolderEnumActivity
        Intent myIntent;
        myIntent = new Intent(this, FolderEnumActivity.class);
        startActivity(myIntent);
    }
}
