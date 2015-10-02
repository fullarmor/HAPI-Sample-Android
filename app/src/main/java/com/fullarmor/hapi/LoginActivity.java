package com.fullarmor.hapi;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fullarmor.hapi.R;
import com.fullarmor.hapi.models.DomainLoginResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/*****************************************************************
 <copyright file="LoginActivity.java" company="FullArmor Corp.">
 Copyright (c) 2015 FullArmor Corporation. All rights reserved.
 </copyright>
 *****************************************************************/

/**
 * A login screen that offers login via AD credentials.
 */
public class LoginActivity extends Activity {

    public final static String CALLINGACTIVITY = "com.fullarmor.hapi.CALLINGACTIVITY";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private String caller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupActionBar();

        caller = null;
        Intent intent = getIntent();
        if (intent != null) {
            caller = intent.getStringExtra(CALLINGACTIVITY);
        }

        // Set up the login form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if ((id == mPasswordView.getImeActionId()) || (id == EditorInfo.IME_NULL)) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preferences_file),Context.MODE_PRIVATE);
        String user = sharedPref.getString(getString(R.string.preferences_username),"");
        if ( !user.isEmpty() )
            mUsernameView.setText(user);
        String password = sharedPref.getString(getString(R.string.preferences_password),"");
        if ( !password.isEmpty() )
            mPasswordView.setText(password);
        Button mEmailSignInButton = (Button) findViewById(R.id.sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        Button mSettingsButton = (Button)findViewById(R.id.settings_button);
        mSettingsButton.setOnClickListener(new OnClickListener()  {
            @Override
            public void onClick(View view) {
            openSettings();
            }
        });
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            if ( getActionBar() != null )
                getActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        // check if we are connected to a network
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data

        } else {
            // display error
            cancel = true;
            focusView = mUsernameView;
            mUsernameView.setError("Not connected to the network.");
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(this, username, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isUsernameValid(String username) {
        // username should be specified in DOMAIN\\USERNAME format
        return username.contains("\\");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return true;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;
        private String mError;
        private Activity mActivity;
        UserLoginTask(Activity activity, String username, String password) {
            this.mActivity = activity;
            this.mUsername = username;
            this.mPassword = password;
            this.mError = getString(R.string.error_incorrect_password);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Boolean success = true;
            try {
                // call HAPI gatekeeper to perform login
                HAPIHelper helper = new HAPIHelper(getApplicationContext(),null);
                SharedPreferences settingsPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                helper.gatekeeperBaseUrl = settingsPref.getString(SettingsActivity.KEY_PREF_GATEKEEPER_URL, "");
                DomainLoginResponse response = helper.Login(mUsername,mPassword);
                success = response.Success;
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preferences_file),Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                if ( success ) {
                    editor.putString(getString(R.string.preferences_displayName), response.UserDisplayName);
                    editor.putString(getString(R.string.preferences_hapiToken), response.Token);
                    editor.putString(getString(R.string.preferences_username), mUsername);
                    editor.putInt(getString(R.string.preferences_role), response.Role);
                }
                else
                {

                    editor.putString(getString(R.string.preferences_displayName), "");
                    editor.putString(getString(R.string.preferences_hapiToken), "");
                    editor.putInt(getString(R.string.preferences_role), DomainLoginResponse.ROLE_UNKNOWN);
                    mError = response.Error;
                }
                editor.commit();
            } catch ( IOException e) {
                mError = e.getMessage();
                success = false;
            }


            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
                // open up a new FolderEnumActivity
                Intent myIntent;
                if (caller == "@string/calling_activity_userinfo") {
                    myIntent = new Intent(this.mActivity, UserInfoActivity.class);
                }
                else
                    myIntent = new Intent(this.mActivity, FolderEnumActivity.class);
                startActivity(myIntent);
            } else {
                mPasswordView.setError(mError);
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }



    }
}

