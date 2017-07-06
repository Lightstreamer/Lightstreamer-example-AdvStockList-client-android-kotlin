package com.lightstreamer.simple_demo.android

import android.util.Log

import com.lightstreamer.client.ItemUpdate
import com.lightstreamer.client.Subscription
import com.lightstreamer.client.SubscriptionListener

object LogSubscriptionListener : SubscriptionListener {

    private val tag = "SubscriptionListener"

    override fun onClearSnapshot(arg0: String?, arg1: Int) {
        Log.i(tag, "clear snapshot call") //the default stocklist demo adapter does not send this event
    }

    override fun onCommandSecondLevelItemLostUpdates(arg0: Int, arg1: String) {
        Log.wtf(tag, "Not expecting 2nd level events")
    }

    override fun onCommandSecondLevelSubscriptionError(arg0: Int, arg1: String?,
                                                       arg2: String) {
        Log.wtf(tag, "Not expecting 2nd level events")
    }

    override fun onEndOfSnapshot(itemName: String?, arg1: Int) {
        Log.v(tag, "Snapshot end for $itemName")
    }

    override fun onItemLostUpdates(arg0: String?, arg1: Int, arg2: Int) {
        Log.wtf(tag, "Not expecting lost updates")
    }

    override fun onItemUpdate(update: ItemUpdate) {
        Log.v(tag, "Update for ${update.itemName}")
    }

    override fun onListenEnd(subscription: Subscription) {
        Log.d(tag, "Start listening")
    }

    override fun onListenStart(subscription: Subscription) {
        Log.d(tag, "Stop listening")
    }

    override fun onSubscription() {
        Log.v(tag, "Subscribed")
    }

    override fun onSubscriptionError(code: Int, message: String?) {
        Log.e(tag, "Subscription error $code: $message")
    }

    override fun onUnsubscription() {
        Log.v(tag, "Unsubscribed")
    }

    override fun onRealMaxFrequency(frequency: String?) {
        Log.d(tag, "Frequency is $frequency")
    }
}
