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

import android.os.Handler
import android.widget.TextView
import com.lightstreamer.client.ItemUpdate
import com.lightstreamer.client.Subscription
import com.lightstreamer.client.SubscriptionListener
import java.text.SimpleDateFormat
import java.util.*

class Stock(internal var numericField: Set<String>,
            private val fields: Array<String>,
            private val handler: Handler,
            private val holder: HashMap<String, TextView>)
    : SubscriptionListener by EmptySubscriptionListener {

    private val dateFormat = SimpleDateFormat("HH:mm:ss")

    private val turnOffRunnables = HashMap<String, UpdateRunnable>()
    @Volatile private var subscription: Subscription? = null

    fun setSubscription(sub: Subscription) {
        this.subscription = sub
    }

    override fun onListenStart() {
        handler.post(ResetRunnable())
    }

    override fun onListenEnd() {
        this.subscription = null
    }


    override fun onItemUpdate(update: ItemUpdate) {
        this.updateView(update)
    }

    private fun updateView(newData: ItemUpdate) {
        val snapshot = newData.isSnapshot
        val itemName = checkNotNull(newData.itemName)

        val changedFields = newData.changedFields.entries.iterator()
        while (changedFields.hasNext()) {

            val updatedField = changedFields.next()
            var value = updatedField.value
            val fieldName = updatedField.key
            val field = holder[fieldName]

            if (field != null) {
                if (fieldName == "timestamp") {
                    val then = Date(java.lang.Long.parseLong(value))
                    value = dateFormat.format(then)
                }

                val color: Int
                if (snapshot) {
                    color = R.color.snapshot_highlight
                } else {
                    // update cell color
                    val upDown =
                            if (numericField.contains(fieldName)) {
                                //get the current value so that we can compare it with the new ones.
                                value.toDouble() - (subscription?.getValue(itemName, fieldName)?.toDouble() ?: 0.0)
                            } else 0.0

                    if (upDown < 0) {
                        color = R.color.lower_highlight
                    } else {
                        color = R.color.higher_highlight
                    }

                }

                var turnOff: UpdateRunnable? = turnOffRunnables[fieldName]
                turnOff?.invalidate()
                turnOff = UpdateRunnable(field, null, R.color.transparent)
                this.turnOffRunnables.put(fieldName, turnOff)

                handler.post(UpdateRunnable(field, value, color))
                handler.postDelayed(turnOff, 600)
            }
        }
    }

    private inner class ResetRunnable : Runnable {

        @Synchronized override fun run() {
            resetHolder(holder, fields)
        }

        private fun resetHolder(holder: HashMap<String, TextView>, fields: Array<String>) {
            fields.indices
                    .mapNotNull { holder[fields[it]] }
                    .forEach { it.text = "N/A" }
        }

    }

    private inner class UpdateRunnable internal constructor(private val view: TextView, private val text: String?, private val background: Int) : Runnable {
        private var valid = true

        @Synchronized override fun run() {
            if (this.valid) {
                if (this.text != null) {
                    view.text = text
                }
                view.setBackgroundResource(background)
                view.invalidate()
            }
        }

        @Synchronized fun invalidate() {
            this.valid = false
        }
    }
}
