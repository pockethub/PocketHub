package com.github.pockethub.api;

import com.alorma.github.sdk.bean.dto.response.User;
import com.alorma.github.sdk.bean.dto.response.events.payload.GithubEventPayload;

public class FollowEventPayload extends GithubEventPayload {

    public User target;
}
