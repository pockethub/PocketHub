package com.github.mobile.core.search;

import java.io.Serializable;
import java.util.Date;

import org.eclipse.egit.github.core.util.DateUtils;

/**
 * GitHub v2 user model class.
 */
public class SearchUser implements Serializable {

    /** serialVersionUID */
    private static final long serialVersionUID = 159979362732689788L;

    private Date createdAt;

    private int followers;

    private String id;

    private String gravatarId;

    private String location;

    private String login;

    private String name;

    private String language;

    /**
     * @return createdAt
     */
    public Date getCreatedAt() {
        return DateUtils.clone(createdAt);
    }

    /**
     * @param createdAt
     * @return this user
     */
    public SearchUser setCreatedAt(Date createdAt) {
        this.createdAt = DateUtils.clone(createdAt);
        return this;
    }

    /**
     * @return followers
     */
    public int getFollowers() {
        return followers;
    }

    /**
     * @param followers
     * @return this user
     */
    public SearchUser setFollowers(int followers) {
        this.followers = followers;
        return this;
    }

    /**
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     * @return this user
     */
    public SearchUser setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return gravatarId
     */
    public String getGravatarId() {
        return gravatarId;
    }

    /**
     * @param gravatarId
     * @return this user
     */
    public SearchUser setGravatarId(String gravatarId) {
        this.gravatarId = gravatarId;
        return this;
    }

    /**
     * @return location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location
     * @return this user
     */
    public SearchUser setLocation(String location) {
        this.location = location;
        return this;
    }

    /**
     * @return login
     */
    public String getLogin() {
        return login;
    }

    /**
     * @param login
     * @return this user
     */
    public SearchUser setLogin(String login) {
        this.login = login;
        return this;
    }

    /**
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     * @return this user
     */
    public SearchUser setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @return language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @param language
     * @return this user
     */
    public SearchUser setLanguage(String language) {
        this.language = language;
        return this;
    }
}
