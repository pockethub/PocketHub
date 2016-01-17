/*
 * Copyright (c) 2016 PocketHub
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

import com.alorma.github.sdk.bean.dto.response.User;
import com.alorma.github.sdk.bean.dto.response.events.payload.Payload;

public final class MemberEventPayload extends Payload {

    public static final Creator<MemberEventPayload> CREATOR = new Creator<MemberEventPayload>() {
        public MemberEventPayload createFromParcel(Parcel source) {
            return new MemberEventPayload(source);
        }

        public MemberEventPayload[] newArray(int size) {
            return new MemberEventPayload[size];
        }
    };
    public User member;

    public MemberEventPayload() {
    }

    protected MemberEventPayload(Parcel in) {
        super(in);
        this.member = in.readParcelable(User.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(this.member, 0);
    }

}
