/*
 * Copyright (c) Lightstreamer Srl
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
package com.lightstreamer.simple_demo.android

import com.lightstreamer.client.ItemUpdate
import com.lightstreamer.client.SubscriptionListener
import kotlin.properties.Delegates

class MainSubscription(private val list: List<StockForList>) :
        SubscriptionListener by EmptySubscriptionListener {

    var context: Context by Delegates.notNull()

    override fun onItemUpdate(update: ItemUpdate) {
        val toUpdate = list[update.itemPos - 1]
        toUpdate.update(update, this.context)
    }
}