/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class AuthorizationResponse implements Serializable {

    public static String APP_KEY_URL = "url";
    public static String APP_KEY_NAME = "name";
    
    private String[] scopes;
    private Date createdAt;
    private String token;
    private Date updatedAt;
    private String note;
    private String noteUrl;
    private String url;
    private Map<String, String> app;
    private int id;

    public String[] getScopes() { return scopes; }
    public Date getCreatedAt() { return createdAt; }
    public String getToken() { return token; }
    public Date getUpdatedAt() { return updatedAt; }
    public String getNote() { return note; }
    public String getNoteUrl() { return noteUrl; }
    public String getUrl() { return url; }
    public int getId() { return id; }
   
    public String getAppData(String appKey) { 
        return app.containsKey(appKey) ? app.get(appKey) : new String(); 
    }
    
}
