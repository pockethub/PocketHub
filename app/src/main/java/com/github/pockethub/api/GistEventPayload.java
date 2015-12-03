package com.github.pockethub.api;

import com.alorma.github.sdk.bean.dto.response.Gist;
import com.alorma.github.sdk.bean.dto.response.events.payload.ActionEventPayload;

public class GistEventPayload extends ActionEventPayload {

    public Gist gist;
}
