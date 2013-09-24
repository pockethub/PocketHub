/*
 * Copyright 2013 GitHub Inc.
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
package com.github.mobile.core.search;

import java.io.Serializable;

/**
 * GitHub v2 user model class.
 */
public class SearchUser implements Serializable {

    /** serialVersionUID */
    private static final long serialVersionUID = 159979362732689788L;

    private String id;

    private final String gravatarId = null;

    private String login;

    private String name;

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
}
