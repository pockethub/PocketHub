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
package com.github.pockethub.android.ui.user

import android.os.Bundle
import com.github.pockethub.android.Intents.EXTRA_USER
import com.github.pockethub.android.R
import com.meisolsson.githubsdk.core.ServiceGenerator
import com.meisolsson.githubsdk.model.Page
import com.meisolsson.githubsdk.model.User
import com.meisolsson.githubsdk.service.organizations.OrganizationMemberService
import io.reactivex.Single
import retrofit2.Response

/**
 * Fragment to display the members of an org.
 */
class MembersFragment : PagedUserFragment() {

    internal var service = ServiceGenerator.createService(context, OrganizationMemberService::class.java)

    private var org: User? = null

    override val emptyText: Int = R.string.no_members

    override val errorMessage: Int = R.string.error_members_load

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (org != null) {
            outState.putParcelable(EXTRA_USER, org)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        org = arguments!!.getParcelable("org")
        if (org == null && savedInstanceState != null) {
            org = savedInstanceState.getParcelable(EXTRA_USER)
        }

        super.onActivityCreated(savedInstanceState)
    }

    override fun loadData(page: Int): Single<Response<Page<User>>> {
        return service.getMembers(org!!.login(), page.toLong())
    }
}
