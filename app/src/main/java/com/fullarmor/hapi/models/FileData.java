package com.fullarmor.hapi.models;

/*****************************************************************
 <copyright file="FileData.java" company="FullArmor Corp.">
 Copyright (c) 2015 FullArmor Corporation. All rights reserved.
 </copyright>
 *****************************************************************/

// FileData is used to represent a local file on the android device.

public class FileData implements Comparable<FileData> {

    /** Constant that specifies the object is a reference to the parent */
    public static final int PARENT = 0;

    /** Name of the file */
    final private String FileName;

    /** Type of file. Can be one of PARENT, DIRECTORY or FILE */
    final private int FileType;


    public FileData(final String fileName, final int fileType) {

        if (fileType != PARENT && fileType != FileSystemEntry.ENTRYTYPE_FOLDER && fileType != FileSystemEntry.ENTRYTYPE_FILE) {
            throw new IllegalArgumentException("Unrecognized file type.");
        }
        this.FileName = fileName;
        this.FileType = fileType;
    }

    @Override
    public int compareTo(final FileData another) {
        if (FileType != another.FileType) {
            return FileType - another.FileType;
        }
        return FileName.compareTo(another.FileName);
    }

    public String getFileName() {
        return FileName;
    }

    public int getFileType() {
        return FileType;
    }
}

