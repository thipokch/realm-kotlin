/*
 * Copyright 2021 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.mongodb.internal

import io.realm.internal.interop.RealmAppPointer
import io.realm.internal.interop.RealmInterop
import io.realm.internal.interop.RealmUserPointer
import io.realm.internal.platform.appFilesDirectory
import io.realm.internal.platform.freeze
import io.realm.internal.util.Validation
import io.realm.internal.util.use
import io.realm.mongodb.App
import io.realm.mongodb.Credentials
import io.realm.mongodb.User
import io.realm.mongodb.auth.EmailPasswordAuth
import kotlinx.coroutines.channels.Channel

// TODO Public due to being a transitive dependency to UserImpl
public class AppImpl(
    override val configuration: AppConfigurationImpl,
) : App {

    internal val nativePointer: RealmAppPointer = RealmInterop.realm_app_get(
        configuration.nativePointer,
        configuration.synClientConfig,
        appFilesDirectory()
    )

    override val emailPasswordAuth: EmailPasswordAuth by lazy { EmailPasswordAuthImpl(nativePointer) }

    override val currentUser: User?
        get() = RealmInterop.realm_app_get_current_user(nativePointer)
            ?.let { UserImpl(it, this) }

    override fun allUsers(): Map<String, User> {
        val nativeUsers: List<RealmUserPointer> = RealmInterop.realm_app_get_all_users(nativePointer)
        val map = mutableMapOf<String, User>()
        nativeUsers.map { ptr: RealmUserPointer ->
            val user = UserImpl(ptr, this)
            map[user.identity] = user
        }
        return map
    }

    override suspend fun login(credentials: Credentials): User {
        // suspendCoroutine doesn't allow freezing callback capturing continuation
        // ... and cannot be resumed on another thread (we probably also want to guarantee that we
        // are resuming on the same dispatcher), so run our own implementation using a channel
        Channel<Result<User>>(1).use { channel ->
            RealmInterop.realm_app_log_in_with_credentials(
                nativePointer,
                Validation.checkType<CredentialsImpl>(credentials, "credentials").nativePointer,
                channelResultCallback<RealmUserPointer, User>(channel) { userPointer ->
                    UserImpl(userPointer, this)
                }.freeze()
            )
            return channel.receive()
                .getOrThrow()
        }
    }
}
