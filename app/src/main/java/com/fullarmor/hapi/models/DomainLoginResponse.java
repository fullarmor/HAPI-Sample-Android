package com.fullarmor.hapi.models;

/*****************************************************************
 <copyright file="DomainLoginResponse.java" company="FullArmor Corp.">
 Copyright (c) 2015 FullArmor Corporation. All rights reserved.
 </copyright>
 *****************************************************************/

public class DomainLoginResponse {
    public boolean Success;
    public String Error;
    public String Token;
    public String UserSID;
    public String UserDisplayName;
    public String EmailAddress;
    public int Role;

    public static final int ROLE_UNKNOWN = 0;
    public static final int ROLE_USER = 1;
    public static final int ROLE_READONLYUSER = 9;
    public static final int ROLE_ADMIN = 2;
    public static final int ROLE_READONLYADMIN = 10;
    public static final int ROLE_SYSTEM = 4;
    public static final int ROLE_NONE = 32;
}
