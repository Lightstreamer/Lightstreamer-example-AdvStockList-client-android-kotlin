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
import android.support.v4.app.ListFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.lightstreamer.client.Subscription

class StocksFragment : ListFragment() {

    internal lateinit var listener: onStockSelectedListener

    interface onStockSelectedListener {
        /** Called by HeadlinesFragment when a list item is selected  */
        fun onStockSelected(item: Int)
    }

    private val handler: Handler = Handler()
    internal lateinit var lsClient: StockListDemoApplication.ClientProxy

    private var list = items.indices.map { StockForList(it) }
    private val mainSubscriptionListener = MainSubscription(list)

    private val mainSubscription = Subscription("MERGE", items, subscriptionFields).apply {
        dataAdapter = "QUOTE_ADAPTER"
        requestedMaxFrequency = "1"
        requestedSnapshot = "yes"
        addListener(LogSubscriptionListener)
        addListener(mainSubscriptionListener)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.list_view, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listAdapter = StocksAdapter(activity, R.layout.row_layout, list)
    }

    override fun onStart() {
        super.onStart()

        //there's always only one StocksFragment at a time
        mainSubscriptionListener.context = Context(handler, listView)

        if (fragmentManager.findFragmentById(R.id.details_fragment) != null) {
            listView.choiceMode = ListView.CHOICE_MODE_SINGLE
        }
    }

    override fun onResume() {
        super.onResume()
        list.forEach { it.clean() }
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)

        lsClient = StockListDemoApplication.client
        lsClient.addSubscription(mainSubscription)

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        listener = activity as? onStockSelectedListener
                ?: throw ClassCastException("$activity must implement OnHeadlineSelectedListener")
    }

    override fun onDetach() {
        super.onDetach()
        lsClient.removeSubscription(mainSubscription)
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        // Notify the parent activity of selected item
        listener.onStockSelected(position + 1)

        // Set the item as checked to be highlighted when in two-pane layout
        listView.setItemChecked(position, true)
    }

    private companion object {
        val items = (1..20).map { "item$it" }.toTypedArray()
        val subscriptionFields = arrayOf("stock_name", "last_price", "timestamp")
    }
}
