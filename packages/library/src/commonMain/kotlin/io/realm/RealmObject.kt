/*
 * Copyright 2020 Realm Inc.
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

package io.realm

import io.realm.internal.Mediator
import io.realm.internal.RealmObjectInternal
import io.realm.internal.RealmReference
import io.realm.internal.link
import io.realm.interop.Link
import io.realm.interop.RealmInterop
import io.realm.notifications.Callback
import io.realm.notifications.Cancellable
import io.realm.notifications.ObjectChange
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

/**
 * Marker interface to define a model (managed by Realm).
 */
interface RealmObject

/**
 * Returns whether the object is frozen or not.
 *
 * A frozen object is tied to a specific version of the data in the realm and fields retrieved
 * from this object instance will not update even if the object is updated in the Realm.
 *
 * @return true if the object is frozen, false otherwise.
 */
public fun RealmObject.isFrozen(): Boolean {
    val internalObject = this as RealmObjectInternal
    internalObject.`$realm$ObjectPointer`?.let {
        return RealmInterop.realm_is_frozen(it)
    } ?: throw IllegalArgumentException("Cannot get version from an unmanaged object.")
}

/**
 * Returns the Realm version of this object. This version number is tied to the transaction the object was read from.
 */
// TODO Should probably be a function as it can potentially change over time and can throw?
public var RealmObject.version: VersionId
    get() {
        val internalObject = this as RealmObjectInternal
        internalObject.`$realm$Owner`?.let {
            // FIXME This check is required as realm_get_version_id doesn't throw if closed!? Core bug?
            val dbPointer = it.dbPointer
            if (RealmInterop.realm_is_closed(dbPointer)) {
                throw IllegalStateException("Cannot access properties on closed realm")
            }
            return VersionId(RealmInterop.realm_get_version_id(dbPointer))
        } ?: throw IllegalArgumentException("Cannot get version from an unmanaged object.")
    }
    private set(_) {
        throw UnsupportedOperationException("Setter is required by the Kotlin Compiler, but should not be called directly")
    }

/**
 * Deletes the RealmObject.
 *
 * @throws InvalidArgumentException if invoked on an invalid object
 * @throws RuntimeException if invoked outside of a [Realm.write] or [Realm.writeBlocking] block.
 */
// FIXME API Currently just adding these as extension methods as putting them directly into
//  RealmModel would break compiler plugin. Reiterate along with
//  https://github.com/realm/realm-kotlin/issues/83
fun RealmObject.delete() {
    MutableRealm.delete(this)
}

/**
 * Returns whether or not this object is managed by Realm.
 *
 * Managed objects are only valid to use while the Realm is open, but also have access to all Realm API's like
 * queries or change listeners. Unmanaged objects behave like normal Kotlin objects and are completely seperate from
 * Realm.
 */
fun RealmObject.isManaged(): Boolean {
    val internalObject = this as RealmObjectInternal
    return internalObject.`$realm$IsManaged`
}

/**
 * Returns true if this object is still valid to use, i.e. the Realm is open and the underlying object has
 * not been deleted. Unmanaged objects are always valid.
 */
public fun RealmObject.isValid(): Boolean {
    return if (isManaged()) {
        val internalObject = this as RealmObjectInternal
        val ptr = internalObject.`$realm$ObjectPointer`
        return if (ptr != null) {
            RealmInterop.realm_object_is_valid(ptr)
        } else {
            false
        }
    } else {
        // Unmanaged objects are always valid
        true
    }
}

/**
 * FIXME Hidden until we can add proper support
 */
internal fun <T : RealmObject> RealmObject.addChangeListener(callback: Callback<T?>): Cancellable {
    checkNotificationsAvailable()
    val realm = ((this as RealmObjectInternal).`$realm$Owner`!!).owner
    @Suppress("UNCHECKED_CAST")
    return realm.registerObjectChangeListener(this as T, callback)
}

/**
 * Observe changes to a Realm object. Any change to the object, will cause the flow to emit the updated
 * object. If the observed object is deleted from the Realm, the flow will complete, otherwise it will
 * continue running until canceled.
 *
 * The change calculations will on on the thread represented by [RealmConfiguration.notificationDispatcher].
 *
 * @return a flow representing changes to the object.
 */
public fun <T : RealmObject> T.observe(): Flow<ObjectChange<T>> {
    checkNotificationsAvailable()
    val internalObject = this as RealmObjectInternal
    @Suppress("UNCHECKED_CAST")
    TODO()
//    return (internalObject.`$realm$Owner`!!).owner.registerObjectObserver(this as T)
}

public fun <T : RealmObject> T.addChangeListener(callback: Callback<ObjectChange<T>>): Cancellable {
    TODO()
}

private fun RealmObject.checkNotificationsAvailable() {
    val internalObject = this as RealmObjectInternal
    val realm = internalObject.`$realm$Owner`
    if (!isManaged()) {
        throw IllegalStateException("Changes cannot be observed on unmanaged objects.")
    }
    if (realm != null && RealmInterop.realm_is_closed(realm.dbPointer)) {
        throw IllegalStateException("Changes cannot be observed when the Realm has been closed.")
    }
    if (!isValid()) {
        throw IllegalStateException("Changes cannot be observed on objects that have been deleted from the Realm.")
    }
}

/**
 * Instantiates a [RealmObject] from its Core [Link] representation. For internal use only.
 */
internal fun <T : RealmObject> Link.toRealmObject(
    clazz: KClass<T>,
    mediator: Mediator,
    realm: RealmReference
): T {
    return mediator.createInstanceOf(clazz)
        .link(realm, mediator, clazz, this)
}
