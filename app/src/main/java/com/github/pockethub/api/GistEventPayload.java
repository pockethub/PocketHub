/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.pockethub.api;

import android.os.Parcel;

import com.alorma.github.sdk.bean.dto.response.Gist;
import com.alorma.github.sdk.bean.dto.response.events.payload.Payload;

public class GistEventPayload extends Payload {

    public static final Creator<GistEventPayload> CREATOR = new Creator<GistEventPayload>() {
        public GistEventPayload createFromParcel(Parcel source) {
            return new GistEventPayload(source);
        }

        public GistEventPayload[] newArray(int size) {
            return new GistEventPayload[size];
        }
    };

    public Gist gist;

    public GistEventPayload() {
        super();
    }

    protected GistEventPayload(Parcel in) {
        super(in);
        this.gist = in.readParcelable(Gist.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(this.gist, 0);
    }
}
