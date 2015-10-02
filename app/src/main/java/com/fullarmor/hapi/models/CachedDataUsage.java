package com.fullarmor.hapi.models;

/*****************************************************************
 <copyright file="CachedDataUsage.java" company="FullArmor Corp.">
 Copyright (c) 2015 FullArmor Corporation. All rights reserved.
 </copyright>
 *****************************************************************/
// flags used for FolderEnumRequest, to determine when cached data will be used.
public enum CachedDataUsage {
    DontUseCachedData(0),
    UseCachedDataIfAvailable(1),
    OnlyUseCachedData(2);
    private int value;
    private CachedDataUsage(int v) {
        this.value = v;}
    public int getValue() {
        return this.value;
    }

};