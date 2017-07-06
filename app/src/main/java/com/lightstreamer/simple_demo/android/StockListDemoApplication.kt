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


import android.app.Application

import com.lightstreamer.client.ClientListener
import com.lightstreamer.client.LightstreamerClient
import com.lightstreamer.client.Subscription
import kotlin.concurrent.thread
import kotlin.properties.Delegates

class StockListDemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        client = ClientProxy() //expose the instance
    }


    inner class ClientProxy {
        private var connectionWish = false
        private var userWantsConnection = true

        private val lsClient = LightstreamerClient(
                resources.getString(R.string.host),
                "DEMO")
                .apply {
                    connect()
                }

        fun start(userCall: Boolean): Boolean {
            synchronized(lsClient) {
                if (!userCall) {
                    if (!userWantsConnection) {
                        return false
                    }
                } else {
                    userWantsConnection = true
                }

                connectionWish = true
                lsClient.connect()
                return true
            }
        }

        fun stop(userCall: Boolean) {
            synchronized(lsClient) {
                connectionWish = false

                if (userCall) {
                    userWantsConnection = false
                    lsClient.disconnect()
                } else {
                    thread {
                        Thread.sleep(5000)
                        synchronized(lsClient) {
                            if (!connectionWish) {
                                lsClient.disconnect()
                            }
                        }
                    }
                }
            }
        }

        fun addSubscription(sub: Subscription) {
            lsClient.subscribe(sub)
        }

        fun removeSubscription(sub: Subscription) {
            lsClient.unsubscribe(sub)
        }


        fun addListener(listener: ClientListener) {
            lsClient.addListener(listener)
        }

        fun removeListener(listener: ClientListener) {
            lsClient.removeListener(listener)
        }
    }

    companion object {
        var client by Delegates.notNull<ClientProxy>()
            private set
    }
}
