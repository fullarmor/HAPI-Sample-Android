package com.fullarmor.hapi;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.fullarmor.hapi.R;
import com.fullarmor.hapi.models.FileSystemEntry;
import com.fullarmor.hapi.models.FolderEnumRequest;
import com.fullarmor.hapi.models.FolderEnumResponse;
import com.fullarmor.hapi.models.DomainLoginResponse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
/*****************************************************************
 <copyright file="FolderEnumActivity" company="FullArmor Corp.">
 Copyright (c) 2015 FullArmor Corporation. All rights reserved.
 </copyright>
 *****************************************************************/

public class FolderEnumActivity extends ListActivity {

    public final static String FILEIDENTIFIER = "com.fullarmor.hapi.FILEIDENTIFIER";
    public final static String DISPLAYNAME = "com.fullarmor.hapi.DISPLAYNAME";

    public ArrayList<HashMap<String,String>> mFolderNames;
    public ArrayList<FileSystemEntry> mFolderData;
    public static Stack<Intent> parents = new Stack<Intent>();
    private View mProgressView;
    private View mListView;
    private FolderEnumTask mFolderEnumTask;
    private String mFileIdentifier;
    private ActionMode mActionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_enum);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        mFolderNames = null;
        mFolderData = null;
        mFileIdentifier = null;
        mListView = findViewById(android.R.id.list);
        setupActionBar();
        registerForContextMenu(mListView);
        mProgressView = findViewById(R.id.enum_progress);
        // get fileIdentifier and other search info from the Intent
        Intent intent = getIntent();
        parents.push(intent);
        if (intent != null) {
            mFileIdentifier = intent.getStringExtra(FILEIDENTIFIER);
        }
        if ( getActionBar() != null ) {
            if (mFileIdentifier == null || mFileIdentifier.isEmpty())
                getActionBar().setDisplayHomeAsUpEnabled(false);
            else
                getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        attemptEnum(mFileIdentifier);

        mListView.setOnLongClickListener(new View.OnLongClickListener() {
            // Called when the user long-clicks on someView
            public boolean onLongClick(View view) {
                if (mActionMode != null) {
                    return false;
                }

                // Start the CAB using the ActionMode.Callback defined above
                view.setSelected(true);
                return true;
            }
        });
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Hide the Up button in the action bar.
            if ( getActionBar() != null ) {
                if (mFileIdentifier == null || mFileIdentifier.isEmpty())
                    getActionBar().setDisplayHomeAsUpEnabled(false);
                else
                    getActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_folder_enum, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        int pos = ((ListView)v).getSelectedItemPosition();
        inflater.inflate(R.menu.menu_filecontext, menu);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        FileSystemEntry fse = mFolderData.get(info.position);
        switch (item.getItemId()) {
            case R.id.action_download:
                Toast.makeText(getApplicationContext(),"Context-Download - " + fse.FileIdentifier ,Toast.LENGTH_SHORT).show();
                FileDownloadTask task = new FileDownloadTask(this,fse.FileIdentifier);
                task.execute();
                return true;
            case R.id.action_fileinfo:
                //show the file properties
                Intent newIntent = new Intent(this,FilePropertiesActivity.class);
                newIntent.putExtra(getString(R.string.extra_fileIdentifier), fse.FileIdentifier);
                newIntent.putExtra(getString(R.string.extra_fileType), fse.Type);
                newIntent.putExtra(getString(R.string.extra_size), fse.Size);
                newIntent.putExtra(getString(R.string.extra_whenModified), fse.LastModifiedDate);
                newIntent.putExtra(FilePropertiesActivity.PARENTFILEIDENTIFIER, mFileIdentifier);
                startActivity(newIntent);

                return true;
            default:
                return super.onContextItemSelected(item);
        }
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

        if (id == R.id.action_logout) {
            logoutFromHAPI();
            return true;
        }
        if (id == R.id.action_userinfo) {
            openUserInfo();
            return true;
        }
        if (id == android.R.id.home){
            parents.pop();
            Intent parentActivityIntent = parents.pop();
            parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(parentActivityIntent);
            finish();
        }
        if ( id == R.id.action_uploadfile ) {
            Toast.makeText(this, "Upload file ", Toast.LENGTH_SHORT).show();
            Intent selectFileIntent = new Intent(this, SelectFileActivity.class);
            selectFileIntent.putExtra(FILEIDENTIFIER,mFileIdentifier);
            startActivity(selectFileIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openUserInfo() {
        Intent intent = new Intent(this, UserInfoActivity.class);
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
        // open up a new FolderEnumActivity
        finish();
        Intent myIntent;
        myIntent = new Intent(this, FolderEnumActivity.class);
        startActivity(myIntent);
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void attemptEnum(String fileId) {
        // if enum already in progress, return
        if ( mFolderEnumTask != null )
            return;
        // check if we are connected to a network
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        // TODO: error checking
        Boolean cancel = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data

        } else {
            cancel = true;
        }
        if (!cancel) {
            // Show a progress spinner, and kick off a background task to
            // enumerate the files
            showProgress(true);
            mFolderEnumTask = new FolderEnumTask(this, fileId);
            mFolderEnumTask.execute((Void) null);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mListView.setVisibility(show ? View.GONE : View.VISIBLE);
            mListView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mListView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mListView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous task used to enumerate files and folders
     */
    public class FolderEnumTask extends AsyncTask<Void, Void, FolderEnumResponse> {

        private final String mFileIdentifier;
        private String mToken;
        private String mError;
        private FolderEnumActivity mActivity;
        FolderEnumTask(FolderEnumActivity activity,String fileIdentifier) {
            this.mActivity = activity;
            this.mFileIdentifier = fileIdentifier;
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preferences_file), Context.MODE_PRIVATE);
            this.mToken = sharedPref.getString(getString(R.string.hapiToken),"");

        }

        @Override
        protected FolderEnumResponse doInBackground(Void... params) {
            if (this.mToken == null || this.mToken.isEmpty()) {
                finish();
                LaunchLoginActivity();
            }

            FolderEnumResponse response = null;
            while (response == null) {
                try {
                    HAPIHelper helper = new HAPIHelper(getApplicationContext(),this.mToken);
                    SharedPreferences settingsPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    helper.gatekeeperBaseUrl = settingsPref.getString(SettingsActivity.KEY_PREF_GATEKEEPER_URL, "");
                    helper.mToken = this.mToken;
                    FolderEnumRequest request = new FolderEnumRequest();
                    request.FileIdentifier = this.mFileIdentifier;
                    response = helper.GetFilesAndFolders(request);
                    if (response == null) {
                        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preferences_file),Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        this.mToken = null;
                        editor.putString(getString(R.string.preferences_hapiToken), this.mToken);
                        finish();
                        LaunchLoginActivity();
                        break;

                    }
                } catch (IOException e) {
                    mError = e.getMessage();
                    FolderEnumResponse defaultResponse = new FolderEnumResponse();
                    defaultResponse.Items = new FileSystemEntry[] { };
                    return defaultResponse;
                }
            }

            return response;
        }

        @Override
        protected void onPostExecute(final FolderEnumResponse response) {

            showProgress(false);

            if (response != null && response.Items != null && response.Items.length > 0) {
                // put the results into a hashmap
                mActivity.mFolderNames = new ArrayList<HashMap<String, String>>();
                mActivity.mFolderData = new ArrayList<FileSystemEntry>();
                mActivity.mFileIdentifier = this.mFileIdentifier;
                for (int i = 0 ; i < response.Items.length ; i++ ) {
                    HashMap<String,String> map = new HashMap<String,String>();
                    map.put("name",response.Items[i].Name);
                    map.put("fileIdentifier",response.Items[i].FileIdentifier);
                    mActivity.mFolderNames.add(map);
                    mActivity.mFolderData.add(response.Items[i]);
                }
                ListView view = (ListView) findViewById(android.R.id.list);
                ListAdapter adapter = new FolderEnumAdapter(mActivity,mActivity.mFolderData);

                view.setAdapter(adapter);
                view.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                       // Toast.makeText(mActivity, "You Clicked " + mActivity.mFolderNames.get(position).get("name"), Toast.LENGTH_SHORT).show();

                        if ( mActivity.mFolderData.get(position).Type == FileSystemEntry.ENTRYTYPE_FOLDER ) {
                            // open the child folder
                            Intent intent = new Intent(mActivity, FolderEnumActivity.class);
                            String fileId = mActivity.mFolderNames.get(position).get("fileIdentifier");
                            intent.putExtra(FILEIDENTIFIER, fileId);
                            startActivity(intent);
                            finish();
                        } else if ( mActivity.mFolderData.get(position).Type == FileSystemEntry.ENTRYTYPE_FILE ) {
                            // Download and launch the file?
                            // for now, just do nothing - the user can pick download from the context menu
                        }
                    }

                });


            } else {
                // TODO: show an error message
            }
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
        }

        protected void LaunchLoginActivity()
        {
            Intent intent = new Intent(mActivity, LoginActivity.class);
            intent.putExtra(LoginActivity.CALLINGACTIVITY, "@string/calling_activity_folderenum");
            startActivity(intent);
        }

    }
    public class FileDownloadTask extends AsyncTask<Void, Void, Uri> {

        private final String mFileIdentifier;
        private final String mToken;
        private String mError;
        private Activity mActivity;
        FileDownloadTask(Activity activity,String fileIdentifier) {
            this.mActivity = activity;
            this.mFileIdentifier = fileIdentifier;
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preferences_file), Context.MODE_PRIVATE);
            this.mToken = sharedPref.getString(getString(R.string.hapiToken),"");

        }

        @Override
        protected Uri doInBackground(Void... params) {

            try {
                // Simulate network access.
                HAPIHelper helper = new HAPIHelper(getApplicationContext(),this.mToken);
                SharedPreferences settingsPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                helper.gatekeeperBaseUrl = settingsPref.getString(SettingsActivity.KEY_PREF_GATEKEEPER_URL, "");
                helper.mToken = this.mToken;
                return helper.DownloadFile(this.mFileIdentifier,getApplicationContext());
            } catch ( IOException e) {
                mError = e.getMessage();
            }
            catch ( Exception ex) {
                mError = ex.getMessage() + ex.getStackTrace();
                Log.e("HAPI", "FolderEnum - " + mError, ex);
            }


            return null;
        }

        @Override
        protected void onPostExecute(final Uri targetUri) {

            showProgress(false);

            if (targetUri != null  ) {
                // open the file for viewing
                String targetPath = targetUri.getPath();
                int pos = targetPath.lastIndexOf('.');
                String ext = null;
                if ( pos > 0 ) {
                    ext = targetPath.substring(pos+1);
                }
                if ( ext != null ) {
                    String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(targetUri, mime);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(getApplicationContext(), "File " + targetPath + " does not have a valid extension", Toast.LENGTH_LONG);
                    Log.e("HAPI","Downloaded file " + targetPath + " does not have a valid extension.");

                }
            } else {
                Toast.makeText(getApplicationContext(),mError, Toast.LENGTH_LONG);
                Log.e("HAPI","Error downloading file" + mError);
            }
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
        }



    }
}
