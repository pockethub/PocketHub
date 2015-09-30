package com.github.pockethub.ui;

import com.alorma.github.sdk.bean.dto.response.Organization;

public class NavigationDrawerObject {
    public static final int TYPE_SEPERATOR = -1;
    public static final int TYPE_SUBHEADER = 0;
    public static final int TYPE_ITEM_MENU = 1;
    public static final int TYPE_ITEM_ORG = 2;
    public static final int TYPE_LOG_OUT = 3;

    private String title;
    private String iconString;
    private Organization user;
    private int type;

    public NavigationDrawerObject(String title, String icon, int type) {
        this.title = title;
        this.iconString = icon;
        this.type = type;
    }

    public NavigationDrawerObject(String title, int type, Organization user) {
        this.title = title;
        this.type = type;
        this.user = user;

    }

    public NavigationDrawerObject(String title, int type) {
        this.title = title;
        this.type = type;
    }

    public NavigationDrawerObject(int type) {
        this.type = type;
    }

    public Organization getUser() {
        return user;
    }


    public int getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getIconString() {
        return iconString;
    }
}