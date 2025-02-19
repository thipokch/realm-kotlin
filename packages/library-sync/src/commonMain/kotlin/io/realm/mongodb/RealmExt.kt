/*
 * Copyright 2022 Realm Inc.
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
package io.realm.mongodb

import io.realm.Realm
import io.realm.mongodb.internal.SyncedRealmContext
import io.realm.mongodb.internal.executeInSyncContext
import io.realm.mongodb.sync.SyncSession

/**
 * This class contains extension methods that are available when using synced realms.
 *
 * Calling these methods on a local realms created using a [io.realm.RealmConfiguration] will
 * throw an [IllegalStateException].
 */

/**
 * Returns the [SyncSession] associated with this Realm.
 */
public val Realm.syncSession: SyncSession
    get() {
        return executeInSyncContext(this) { context: SyncedRealmContext ->
            context.session
        }
    }
