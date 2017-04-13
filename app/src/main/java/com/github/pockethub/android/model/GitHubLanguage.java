package com.github.pockethub.android.model;

/**
 * This Class represents the POJO  of the language in the color.json.
 */

public class GitHubLanguage {

    private String color;

    private String url;

    public GitHubLanguage(String color, String url) {
        this.color = color;
        this.url = url;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
