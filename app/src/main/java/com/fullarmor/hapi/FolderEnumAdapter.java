package com.fullarmor.hapi;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.fullarmor.hapi.models.FileSystemEntry;

import java.util.ArrayList;
import java.util.HashMap;

/*****************************************************************
 <copyright file="FolderEnumAdapter.java" company="FullArmor Corp.">
 Copyright (c) 2015 FullArmor Corporation. All rights reserved.
 </copyright>
 *****************************************************************/
public class FolderEnumAdapter extends ArrayAdapter<FileSystemEntry> {

    private final Activity context;
    private final ArrayList<FileSystemEntry> entries;
    public FolderEnumAdapter(Activity context,ArrayList<FileSystemEntry> data){
        super(context,R.layout.file_entry_row,data);
        this.context = context;
        this.entries = data;
    }

    private int getImageResourceForFileType(String filename) {
        // start with the default file icon
        int resourceId = R.drawable.iconfile_168p;
        // get the file extension
        int dot = filename.lastIndexOf('.');
        if ( dot > 0 ) {
            String extension = filename.substring(dot+1);
           // Set the icon based on the file extension
            if ( 0 == extension.compareToIgnoreCase("AVI"))
                resourceId = R.drawable.iconavi_48;
            if ( 0 == extension.compareToIgnoreCase("CSS"))
                resourceId = R.drawable.iconcss_48;
            if ( 0 == extension.compareToIgnoreCase("DLL"))
                resourceId = R.drawable.icondll_48;
            if ( 0 == extension.compareToIgnoreCase("DOC"))
                resourceId = R.drawable.icondoc_48;
            if ( 0 == extension.compareToIgnoreCase("DOCX"))
                resourceId = R.drawable.icondocx_48;
            if ( 0 == extension.compareToIgnoreCase("EPS"))
                resourceId = R.drawable.iconeps_48;
            if ( 0 == extension.compareToIgnoreCase("HTM"))
                resourceId = R.drawable.iconhtm_48;
            if ( 0 == extension.compareToIgnoreCase("HTML"))
                resourceId = R.drawable.iconhtml_48;
            if ( 0 == extension.compareToIgnoreCase("JPG"))
                resourceId = R.drawable.iconjpg_48;
            if ( 0 == extension.compareToIgnoreCase("JPEG"))
                resourceId = R.drawable.iconjpg_48;
            if ( 0 == extension.compareToIgnoreCase("MP3"))
                resourceId = R.drawable.iconmp3_48;
            if ( 0 == extension.compareToIgnoreCase("PDF"))
                resourceId = R.drawable.iconpdf_48;
            if ( 0 == extension.compareToIgnoreCase("PNG"))
                resourceId = R.drawable.iconpng_120;
            if ( 0 == extension.compareToIgnoreCase("PPT"))
                resourceId = R.drawable.iconppt_120;
            if ( 0 == extension.compareToIgnoreCase("PPTX"))
                resourceId = R.drawable.iconpptx_48;
            if ( 0 == extension.compareToIgnoreCase("PSD"))
                resourceId = R.drawable.iconpsd_48;
            if ( 0 == extension.compareToIgnoreCase("TXT"))
                resourceId = R.drawable.icontxt_48;
            if ( 0 == extension.compareToIgnoreCase("WAV"))
                resourceId = R.drawable.iconwav_48;
            if ( 0 == extension.compareToIgnoreCase("XLS"))
                resourceId = R.drawable.iconxls_48;
            if ( 0 == extension.compareToIgnoreCase("XLSX"))
                resourceId = R.drawable.iconxlsx_48;
            if ( 0 == extension.compareToIgnoreCase("ZIP"))
                resourceId = R.drawable.iconzip_48;
        }
        return resourceId;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.file_entry_row, null, true);

        TextView txtDisplay = (TextView) rowView.findViewById(R.id.name);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);

        FileSystemEntry entry = this.entries.get(position);
        txtDisplay.setText(entry.Name);
        if ( entry.Type == FileSystemEntry.ENTRYTYPE_FOLDER ) {

            imageView.setImageResource(R.drawable.iconfolder_96p);
        }
        else {
            imageView.setImageResource(getImageResourceForFileType(entry.Name));
        }
        return rowView;
    }
}