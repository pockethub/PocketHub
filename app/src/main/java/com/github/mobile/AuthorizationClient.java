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

import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.github.mobile.AuthorizationResponse;
import static com.github.mobile.AuthorizationResponse.APP_KEY_URL;
import static com.github.mobile.AuthorizationResponse.APP_KEY_NAME;

// import static org.eclipse.egit.github.core.client.IGitHubConstants.HOST_API;

/**
 * Default client used to communicate with GitHub API
 */
public class AuthorizationClient extends DefaultClient {

    /**
     * Create client
     */
    public AuthorizationClient(String username, String password) {
        super();
        setCredentials(username, password);
    }

    public AuthorizationResponse[] getAuthorizations() throws IOException {
        HttpURLConnection request = createGet("/authorizations");
        return parseJson(request.getInputStream(), AuthorizationResponse[].class);
    }

    public static boolean isAuthorizedForGitHubAndroid(AuthorizationResponse auth) {
      if(auth.getAppData(APP_KEY_URL).equals("https://github.com/github/android"))
        return true;
      return false;
    }
    
}
