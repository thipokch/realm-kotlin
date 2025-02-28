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

import realm_wrapper.realm_app_error_category

actual enum class AppErrorCategory(actual val description: String, val nativeValue: realm_app_error_category) {
    RLM_APP_ERROR_CATEGORY_HTTP("Http", realm_app_error_category.RLM_APP_ERROR_CATEGORY_HTTP),
    RLM_APP_ERROR_CATEGORY_JSON("Json", realm_app_error_category.RLM_APP_ERROR_CATEGORY_JSON),
    RLM_APP_ERROR_CATEGORY_CLIENT("Client", realm_app_error_category.RLM_APP_ERROR_CATEGORY_CLIENT),
    RLM_APP_ERROR_CATEGORY_SERVICE("Service", realm_app_error_category.RLM_APP_ERROR_CATEGORY_SERVICE),
    RLM_APP_ERROR_CATEGORY_CUSTOM("Custom", realm_app_error_category.RLM_APP_ERROR_CATEGORY_CUSTOM);

    actual companion object {

        actual fun fromInt(nativeValue: Int): AppErrorCategory {
            for (value in values()) {
                if (value.nativeValue.value.toInt() == nativeValue) {
                    return value
                }
            }
            error("Unknown app error category: $nativeValue")
        }

        internal fun of(nativeValue: realm_app_error_category): AppErrorCategory {
            return fromInt(nativeValue.value.toInt())
        }
    }
}
