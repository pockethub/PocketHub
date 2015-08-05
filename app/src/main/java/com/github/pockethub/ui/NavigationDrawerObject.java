package com.github.pockethub.ui;

import org.eclipse.egit.github.core.User;

public class NavigationDrawerObject {
    public static final int TYPE_SEPERATOR = -1;
    public static final int TYPE_SUBHEADER = 0;
    public static final int TYPE_ITEM_MENU = 1;
    public static final int TYPE_ITEM_ORG = 2;

    private String title;
    private String iconString;
    private User user;
    private int type;

    public NavigationDrawerObject(String title, String icon, int type) {
        this.title = title;
        this.iconString = icon;
        this.type = type;
    }

    public NavigationDrawerObject(String title, int type, User user) {
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

    public User getUser() {
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