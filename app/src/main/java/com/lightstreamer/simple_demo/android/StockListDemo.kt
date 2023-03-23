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


import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.DialogFragment
import android.support.v4.view.GestureDetectorCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.lightstreamer.client.ClientListener


class StockListDemo : AppCompatActivity(), StocksFragment.onStockSelectedListener {

    private val currentListener = LSClientListener()
    private val pnEnabled = false
    private var isConnectionExpected = false

    private lateinit var mDetector: GestureDetectorCompat

    internal lateinit var client: StockListDemoApplication.ClientProxy

    private lateinit var handler: Handler

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        client = StockListDemoApplication.client

        val gs = GestureControls()
        mDetector = GestureDetectorCompat(this, gs).apply {
            setOnDoubleTapListener(gs)
        }

        this.handler = Handler()
        supportActionBar?.setTitle(R.string.lightstreamer)
        setContentView(R.layout.stocks)

        if (findViewById(R.id.fragment_container) != null) {
            //single fragment view (phone)
            if (savedInstanceState != null) {
                return
            }

            val firstFragment = StocksFragment()
            firstFragment.arguments = intent.extras
            supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, firstFragment).commit()
        }
    }

    private val intentItem: Int
        get() {
            var openItem = 0
            val launchIntent = intent
            if (launchIntent != null) {
                val extras = launchIntent.extras
                if (extras != null) {
                    openItem = extras.getInt("itemNum")
                }
            }
            return openItem
        }

    public override fun onNewIntent(intent: Intent?) {
        Log.d(TAG, "New intent received")
        setIntent(intent)
    }

    public override fun onPause() {
        super.onPause()
        client.removeListener(currentListener)
        client.stop(false)
    }

    public override fun onResume() {
        super.onResume()

        client.addListener(this.currentListener)
        isConnectionExpected = client.start(false)

        var openItem = intentItem
        if (openItem == 0 && findViewById(R.id.fragment_container) == null) {
            //tablet, always start with an open stock
            val df = detailsFragment
            if (df != null) {
                openItem = df.currentStock
            }

            if (openItem == 0) {
                openItem = 2
            }
        }

        if (openItem != 0) {
            onStockSelected(openItem)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        Log.v(TAG, "Switch button: " + isConnectionExpected)
        menu.findItem(R.id.start).isVisible = !isConnectionExpected
        menu.findItem(R.id.stop).isVisible = isConnectionExpected
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        return when (itemId) {
            R.id.stop -> {
                Log.i(TAG, "Stop")
                supportInvalidateOptionsMenu()
                client.stop(true)
                isConnectionExpected = false
                true
            }
            R.id.start -> {
                Log.i(TAG, "Start")
                supportInvalidateOptionsMenu()
                client.start(true)
                isConnectionExpected = true
                true
            }
            R.id.about -> {
                AboutDialog().show(supportFragmentManager, null)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val detailsFragment: DetailsFragment?
        get() = supportFragmentManager.findFragmentById(R.id.details_fragment) as DetailsFragment?
                ?: supportFragmentManager.findFragmentByTag("DETAILS_FRAGMENT") as DetailsFragment?

    override fun onStockSelected(item: Int) {
        Log.v(TAG, "Stock detail selected")

        val detailsFrag = detailsFragment

        if (detailsFrag != null) {
            //tablets
            detailsFrag.updateStocksView(item)
        } else {
            val newFragment = DetailsFragment()
            newFragment.arguments = Bundle().apply {
                putInt(DetailsFragment.ARG_ITEM, item)
                putBoolean(DetailsFragment.ARG_PN_CONTROLS, pnEnabled)
            }

            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)

            transaction.replace(R.id.fragment_container, newFragment, "DETAILS_FRAGMENT")
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    inner class LSClientListener : ClientListener {

        override fun onListenEnd(arg0: com.lightstreamer.client.LightstreamerClient) {}

        override fun onListenStart(client: com.lightstreamer.client.LightstreamerClient) {
            this.onStatusChange(client.status)
        }

        override fun onPropertyChange(arg0: String) {}

        override fun onServerError(code: Int, message: String) {
            Log.e(TAG, "Error $code: $message")
        }

        override fun onStatusChange(status: String) {
            handler.post(StatusChange(status))
        }
    }


    private inner class StatusChange(private val status: String) : Runnable {

        private fun applyStatus(statusId: Int, textId: Int) {
            val statusIcon = findViewById(R.id.status_image) as ImageView
            val textStatus = findViewById(R.id.text_status) as TextView

            statusIcon.contentDescription = resources.getString(textId)
            statusIcon.setImageResource(statusId)
            textStatus.text = resources.getString(textId)
        }

        override fun run() {
            when (status) {
                "CONNECTING" -> applyStatus(R.drawable.status_disconnected, R.string.status_connecting)
                "CONNECTED:STREAM-SENSING" -> applyStatus(R.drawable.status_connected_polling, R.string.status_connecting)
                "CONNECTED:HTTP-STREAMING" -> applyStatus(R.drawable.status_connected_streaming, R.string.status_streaming)
                "CONNECTED:WS-STREAMING" -> applyStatus(R.drawable.status_connected_streaming, R.string.status_ws_streaming)
                "CONNECTED:HTTP-POLLING" -> applyStatus(R.drawable.status_connected_polling, R.string.status_polling)
                "CONNECTED:WS-POLLING" -> applyStatus(R.drawable.status_connected_polling, R.string.status_ws_polling)

                "DISCONNECTED" -> applyStatus(R.drawable.status_disconnected, R.string.status_disconnected)
                "DISCONNECTED:WILL-RETRY" -> applyStatus(R.drawable.status_disconnected, R.string.status_waiting)

                "STALLED" -> applyStatus(R.drawable.status_stalled, R.string.status_stalled)

                else -> Log.wtf(TAG, "Recevied unexpected connection status: " + status)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        this.mDetector.onTouchEvent(event)
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event)
    }

    class AboutDialog : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(activity)
            val inflater = activity.layoutInflater
            builder.setView(inflater.inflate(R.layout.dialog_about, null)).setPositiveButton("OK", null)
            return builder.create()
        }

    }

    //we simply use this class to listen for double taps in which case we reveal/hide
    //a textual version of the connection status
    private inner class GestureControls : GestureDetector.SimpleOnGestureListener(), GestureDetector.OnDoubleTapListener {

        override fun onDown(event: MotionEvent): Boolean {
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            //toggleContainer.setVisibility(show ? View.VISIBLE : View.GONE);
            val textStatus = findViewById(R.id.text_status) as TextView
            textStatus.visibility = if (textStatus.visibility == View.VISIBLE) View.GONE else View.VISIBLE

            return true
        }
    }

    companion object {
        private const val TAG = "StockListDemo"
    }
}
