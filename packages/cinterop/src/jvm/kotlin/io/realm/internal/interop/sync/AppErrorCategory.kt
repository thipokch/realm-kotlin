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

package io.realm.internal.interop.sync

import io.realm.internal.interop.NativeEnumerated
import io.realm.internal.interop.realm_app_error_category_e

actual enum class AppErrorCategory(override val nativeValue: Int) : NativeEnumerated {
    RLM_APP_ERROR_CATEGORY_HTTP(realm_app_error_category_e.RLM_APP_ERROR_CATEGORY_HTTP),
    RLM_APP_ERROR_CATEGORY_JSON(realm_app_error_category_e.RLM_APP_ERROR_CATEGORY_JSON),
    RLM_APP_ERROR_CATEGORY_CLIENT(realm_app_error_category_e.RLM_APP_ERROR_CATEGORY_CLIENT),
    RLM_APP_ERROR_CATEGORY_SERVICE(realm_app_error_category_e.RLM_APP_ERROR_CATEGORY_SERVICE),
    RLM_APP_ERROR_CATEGORY_CUSTOM(realm_app_error_category_e.RLM_APP_ERROR_CATEGORY_CUSTOM);

    actual companion object {

        actual fun fromInt(nativeValue: Int): AppErrorCategory {
            for (value in values()) {
                if (value.nativeValue == nativeValue) {
                    return value
                }
            }
            error("Unknown app error category value: $nativeValue")
        }

        @JvmStatic
        fun of(nativeValue: Int): AppErrorCategory {
            return fromInt(nativeValue)
        }
    }
}
