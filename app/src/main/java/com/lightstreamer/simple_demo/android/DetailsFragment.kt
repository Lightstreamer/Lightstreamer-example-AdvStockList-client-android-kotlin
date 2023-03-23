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

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.androidplot.xy.XYPlot
import com.lightstreamer.client.Subscription
import java.util.*

class DetailsFragment : Fragment() {

    private val subscriptionHandling = SubscriptionFragment()
    private lateinit var handler: Handler
    private val holder = HashMap<String, TextView>()
    private lateinit var chart: Chart

    var currentStock = 0
        private set

    private var currentSubscription: Subscription? = null

    private lateinit var stockListener: Stock

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // If activity recreated (such as from screen rotate), restore
        // the previous article selection set by onSaveInstanceState().
        // This is primarily necessary when in the two-pane layout.
        if (savedInstanceState != null) {
            currentStock = savedInstanceState.getInt(ARG_ITEM)
        }


        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.details_view, container, false)

        with(holder) {
            put("stock_name", view.findViewById<TextView>(R.id.d_stock_name) as TextView)
            put("last_price", view.findViewById<TextView>(R.id.d_last_price) as TextView)
            put("timestamp", view.findViewById<TextView>(R.id.d_time) as TextView)
            put("pct_change", view.findViewById<TextView>(R.id.d_pct_change) as TextView)
            put("bid_quantity", view.findViewById<TextView>(R.id.d_bid_quantity) as TextView)
            put("bid", view.findViewById<TextView>(R.id.d_bid) as TextView)
            put("ask", view.findViewById<TextView>(R.id.d_ask) as TextView)
            put("ask_quantity", view.findViewById<TextView>(R.id.d_ask_quantity) as TextView)
            put("min", view.findViewById<TextView>(R.id.d_min) as TextView)
            put("max", view.findViewById<TextView>(R.id.d_max) as TextView)
            put("open_price", view.findViewById<TextView>(R.id.d_open_price) as TextView)
        }

        val plot = view.findViewById<TextView>(R.id.mySimpleXYPlot) as XYPlot
        chart = Chart(plot, handler)

        stockListener = Stock(numericFields, subscriptionFields, handler, holder)

        return view
    }

    override fun onStart() {
        super.onStart()

        val args = arguments
        if (args != null) {
            updateStocksView(args.getInt(ARG_ITEM))
        } else if (currentStock != 0) {
            updateStocksView(currentStock)
        }
    }

    override fun onPause() {
        super.onPause()
        chart.onPause()
        this.subscriptionHandling.onPause()
    }

    override fun onResume() {
        super.onResume()
        chart.onResume(this.activity)
        this.subscriptionHandling.onResume()
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        this.subscriptionHandling.onAttach()
    }


    fun updateStocksView(item: Int) {
        if (item != currentStock || this.currentSubscription == null) {
            val itemName = "item" + item

            this.currentSubscription = Subscription("MERGE", itemName, subscriptionFields).apply {
                dataAdapter = "QUOTE_ADAPTER"
                requestedSnapshot = "yes"

                addListener(LogSubscriptionListener)
                stockListener.setSubscription(this)
                addListener(stockListener)
                addListener(chart)
                subscriptionHandling.setSubscription(this)
            }

            currentStock = item
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ARG_ITEM, currentStock)
    }

    companion object {

        val numericFields: Set<String> = setOf("last_price", "pct_change", "bid_quantity", "bid", "ask", "ask_quantity", "min", "max", "open_price")
        val subscriptionFields = arrayOf("stock_name", "last_price", "timestamp", "pct_change", "bid_quantity", "bid", "ask", "ask_quantity", "min", "max", "open_price")

        const val ARG_ITEM = "item"
        const val ARG_PN_CONTROLS = "pn_controls"
    }
}
