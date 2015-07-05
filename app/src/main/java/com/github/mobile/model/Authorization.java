package com.github.mobile.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Authorization {

    private int id;
    private String url;
    private App app;
    private String token;
    private String note;
    private Object noteUrl;
    private String createdAt;
    private String updatedAt;
    private List<String> scopes = new ArrayList<>();
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     *
     * @return
     * The id
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The url
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     * @param url
     * The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     *
     * @return
     * The app
     */
    public App getApp() {
        return app;
    }

    /**
     *
     * @param app
     * The app
     */
    public void setApp(App app) {
        this.app = app;
    }

    /**
     *
     * @return
     * The token
     */
    public String getToken() {
        return token;
    }

    /**
     *
     * @param token
     * The token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     *
     * @return
     * The note
     */
    public String getNote() {
        return note;
    }

    /**
     *
     * @param note
     * The note
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     *
     * @return
     * The noteUrl
     */
    public Object getNoteUrl() {
        return noteUrl;
    }

    /**
     *
     * @param noteUrl
     * The note_url
     */
    public void setNoteUrl(Object noteUrl) {
        this.noteUrl = noteUrl;
    }

    /**
     *
     * @return
     * The createdAt
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     *
     * @param createdAt
     * The created_at
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     *
     * @return
     * The updatedAt
     */
    public String getUpdatedAt() {
        return updatedAt;
    }

    /**
     *
     * @param updatedAt
     * The updated_at
     */
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     *
     * @return
     * The scopes
     */
    public List<String> getScopes() {
        return scopes;
    }

    /**
     *
     * @param scopes
     * The scopes
     */
    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}