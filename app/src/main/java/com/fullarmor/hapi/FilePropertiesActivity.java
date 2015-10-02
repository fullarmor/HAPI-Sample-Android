package com.fullarmor.hapi;
/*****************************************************************
 <copyright file="FilePropertiesActivity.java" company="FullArmor Corp.">
 Copyright (c) 2015 FullArmor Corporation. All rights reserved.
 </copyright>
 *****************************************************************/
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.fullarmor.hapi.R;
import com.fullarmor.hapi.models.FileSystemEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
/**
 *  Activity used to display properties for a selected HAPI file.
 *  This activity uses the activity_file_properties layout.
 */
public class FilePropertiesActivity extends Activity {

    // extra info passed to intent
    public final static String PARENTFILEIDENTIFIER = "com.fullarmor.hapi.PARENTFILEIDENTIFIER";
    // HAPI fileIdentifier for the parent folder
    private String mParentFileIdentifier;

    /**
     * Creates the activity.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_properties);
        TextView vPath = (TextView)findViewById(R.id.fileinfo_FileIdentifier);
        TextView vSize = (TextView) findViewById(R.id.fileinfo_size);
        TextView vWhenModified = (TextView) findViewById(R.id.fileinfo_whenmodified);
        Intent myIntent = getIntent();
        mParentFileIdentifier = myIntent.getStringExtra(PARENTFILEIDENTIFIER);
        String fileId = myIntent.getStringExtra(getString(R.string.extra_fileIdentifier));
        int itemType = myIntent.getIntExtra(getString(R.string.extra_fileType), FileSystemEntry.ENTRYTYPE_FILE);
        long size = myIntent.getLongExtra(getString(R.string.extra_size), 0);
        String whenModified = myIntent.getStringExtra(getString(R.string.extra_whenModified));

        vPath.setText(fileId);
        vSize.setText(Long.toString(size)); // TODO: format the size with units
        if ( ! whenModified.isEmpty()) {
            try {
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'");
                f.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date modifiedDate = f.parse(whenModified);
                java.text.DateFormat dateFormat =
                        android.text.format.DateFormat.getDateFormat(getApplicationContext());
                java.text.DateFormat timeFormat =
                        android.text.format.DateFormat.getTimeFormat(getApplicationContext());

                StringBuilder formatted = new StringBuilder();
                formatted.append(dateFormat.format(modifiedDate));
                formatted.append(" ");
                formatted.append(timeFormat.format(modifiedDate));
                vWhenModified.setText(formatted);

            } catch (Exception ex){
                // do nothing - if an exception happens while trying to format the modified date property,
                // the property will be left blank
                String msg = ex.getMessage();
            }
        }
        if ( getActionBar() != null )
            getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_properties, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Intent getParentActivityIntent() {
        if (mParentFileIdentifier != null && !mParentFileIdentifier.isEmpty()) {
            Intent intent = new Intent(this, FolderEnumActivity.class);
            intent.putExtra(FolderEnumActivity.FILEIDENTIFIER, mParentFileIdentifier);
            return intent;
        }

        return super.getParentActivityIntent();
    }
}
