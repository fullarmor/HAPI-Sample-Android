package com.fullarmor.hapi.models;

/*****************************************************************
 <copyright file="FolderEnumResponse.java" company="FullArmor Corp.">
 Copyright (c) 2015 FullArmor Corporation. All rights reserved.
 </copyright>
 *****************************************************************/
// represents HAPI response to GetFilesAndFolders call
public class FolderEnumResponse {
    public SupportedFilterInfo[] SupportedFilters;
    public FileSystemEntry[] Items;
}
