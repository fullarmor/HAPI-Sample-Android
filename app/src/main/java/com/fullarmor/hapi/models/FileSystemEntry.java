package com.fullarmor.hapi.models;

import java.util.Date;

/*****************************************************************
 <copyright file="FileSystemEntry.java" company="FullArmor Corp.">
 Copyright (c) 2015 FullArmor Corporation. All rights reserved.
 </copyright>
 *****************************************************************/
// represents a File/Folder in HAPI
public class FileSystemEntry {
    public String Name;
    public String FileIdentifier;
    // ThumbnailImages
    public int Type;
    public String LastModifiedDate;
    public long Size;

    public static final int ENTRYTYPE_FILE = 1;
    public static final int ENTRYTYPE_FOLDER = 2;
    public static final int ENTRYTYPE_PRINTER = 3;
    public static final int ENTRYTYPE_SYSTEMOBJECT = 4;
    public static final int ENTRYTYPE_COMPUTER = 5;
    public static final int ENTRYTYPE_MAIL = 6;
}

