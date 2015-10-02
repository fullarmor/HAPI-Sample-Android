package com.fullarmor.hapi;

/*****************************************************************
 <copyright file="FileSelector.java" company="FullArmor Corp.">
 Copyright (c) 2015 FullArmor Corporation. All rights reserved.
 </copyright>
 *****************************************************************/
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Dialog;
import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.fullarmor.hapi.models.FileData;
import com.fullarmor.hapi.models.FileSystemEntry;

/**
 This class is a file selection dialog that allows the user to select a local file/folder on the
 android device to upload.
  */
public class FileSelector {

    // list of files to display
    private ListView mFileListView;

    // upload button
    private Button mUploadButton;
    // cancel button
    private Button mCancelButton;

    // current folder location
    private File mCurrentFolder;

    // file selector dialog
    private final Dialog mDialog;

    // application context
    private Context mContext;

    // file listener
    final OnHandleFileListener mOnHandleFileListener;


    public FileSelector(final Context context,
                        final OnHandleFileListener onHandleFileListener) {
        mContext = context;
        mOnHandleFileListener = onHandleFileListener;

        final File sdCard = Environment.getExternalStorageDirectory();
        if (sdCard.canRead()) {
            mCurrentFolder = sdCard;
        } else {
            mCurrentFolder = Environment.getRootDirectory();
        }

        mDialog = new Dialog(context);
        mDialog.setContentView(R.layout.file_select_dialog);
        mDialog.setTitle(mCurrentFolder.getAbsolutePath());

        initFilesList();


        mUploadButton = (Button) mDialog.findViewById(R.id.fileUpload);
        mUploadButton.setOnClickListener(new UploadClickListener(this, mContext));

        setCancelButton();
    }



    // initializes the files list
    private void initFilesList() {
        mFileListView = (ListView) mDialog.findViewById(R.id.fileList);

        mFileListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                // Check if "../" item should be added.
                ((EditText) mDialog.findViewById(R.id.fileName)).setText("");
                if (id == 0) {
                    final String parentLocation = mCurrentFolder.getParent();
                    if (parentLocation != null) { // text == "../"
                        mCurrentFolder = new File(parentLocation);
                        loadFileList(mCurrentFolder);
                    } else {
                        onItemSelect(parent, position);
                    }
                } else {
                    onItemSelect(parent, position);
                }
            }
        });

        loadFileList(mCurrentFolder);
    }

    // loads the list of files for the specified directory into the file list for display
    private void loadFileList(final File location) {
        final ArrayList<FileData> fileList = new ArrayList<FileData>();
        final String parentLocation = location.getParent();
        if (parentLocation != null) {
            // insert .. for parent as first item in the list
            fileList.add(new FileData("../", FileData.PARENT));
        }
        File listFiles[] = location.listFiles();
        if (listFiles != null) {
            ArrayList<FileData> fileDataList = new ArrayList<FileData>();
            for (int index = 0; index < listFiles.length; index++) {
                File tempFile = listFiles[index];

                int type = tempFile.isDirectory() ? FileSystemEntry.ENTRYTYPE_FOLDER : FileSystemEntry.ENTRYTYPE_FILE;
                fileDataList.add(new FileData(listFiles[index].getName(), type));

            }
            fileList.addAll(fileDataList);
            Collections.sort(fileList);
        }
        // Fill the list with the contents of fileList.
        if (mFileListView != null) {
            FileListAdapter adapter = new FileListAdapter(mContext, fileList);
            mFileListView.setAdapter(adapter);
        }
    }

// handler when item is selected
    private void onItemSelect(final AdapterView<?> parent, final int position) {
        final String itemText = ((FileData) parent.getItemAtPosition(position)).getFileName();
        final String itemPath = mCurrentFolder.getAbsolutePath() + File.separator + itemText;
        final File itemLocation = new File(itemPath);

        if (!itemLocation.canRead()) {
            Toast.makeText(mContext, "Access is denied", Toast.LENGTH_SHORT).show();
        } else if (itemLocation.isDirectory()) {
            mCurrentFolder = itemLocation;
            loadFileList(mCurrentFolder);
        } else if (itemLocation.isFile()) {
            final EditText fileName = (EditText) mDialog.findViewById(R.id.fileName);
            fileName.setText(itemText);
        }
    }

    // set up handler for cancel button
    private void setCancelButton() {
        mCancelButton = (Button) mDialog.findViewById(R.id.fileCancel);
        mCancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                mDialog.cancel();
            }
        });
    }
    // gets the name of the currently selected file
    public String getSelectedFileName() {
        final EditText fileName = (EditText) mDialog.findViewById(R.id.fileName);
        return fileName.getText().toString();
    }

    // gets the currently selected folder
    public File getCurrentFolder() {
        return mCurrentFolder;
    }

    public void show() {
        mDialog.show();
    }

    public void dismiss() {
        mDialog.dismiss();
    }
}
