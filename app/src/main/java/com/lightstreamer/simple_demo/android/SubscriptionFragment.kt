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


import android.util.Log
import com.lightstreamer.client.Subscription

/**
 * We may subscribe/unsubscribe during onAttach/onDetach events (to keep the subscription alive as much as possible even
 * if the fragment is not visible) or during onResume/onPause so that the subscription is only alive if the fragment is visible.
 * This implementation follows the latter approach while the StocksFragment uses the former.

 */
class SubscriptionFragment {

    private var lsClient: StockListDemoApplication.ClientProxy? = null
    private var subscription: Subscription? = null
    private var subscribed = false
    private var running = false

    @Synchronized fun setSubscription(subscription: Subscription) {
        if (this.subscription != null && subscribed) {
            Log.d(TAG, "Replacing subscription")
            this.lsClient!!.removeSubscription(this.subscription!!)
        }
        Log.d(TAG, "New subscription " + subscription)
        this.subscription = subscription

        if (running) {
            this.lsClient!!.addSubscription(this.subscription!!)
        }
    }

    @Synchronized fun onResume() {
        //subscribe
        if (this.lsClient != null && this.subscription != null) {
            this.lsClient!!.addSubscription(this.subscription!!)
            subscribed = true
        }
        running = true
    }


    @Synchronized fun onPause() {
        //unsubscribe
        if (this.lsClient != null && this.subscription != null) {
            this.lsClient!!.removeSubscription(this.subscription!!)
            subscribed = false
        }
        running = false
    }

    @Synchronized fun onAttach() {
        lsClient = StockListDemoApplication.client
    }

    companion object {
        private const val TAG = "SubscriptionFragment"
    }
}
