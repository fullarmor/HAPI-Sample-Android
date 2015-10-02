package com.fullarmor.hapi.models;

/*****************************************************************
 <copyright file="FolderEnumRequest.java" company="FullArmor Corp.">
 Copyright (c) 2015 FullArmor Corporation. All rights reserved.
 </copyright>
 *****************************************************************/
// Represents a request to GetFilesAndFolders
public class FolderEnumRequest {
    public String FileIdentifier;
    public String Name;
    public int MaximumFiles;
    public WireFilterCriteria[] Filters;
    public int MaxLevels;
    public Boolean FlattenResults;
    public int CachedDataOption;
    public int[] ThumbnailSizes;
    public Boolean ThrowUnauthorizedAccessException;
}
