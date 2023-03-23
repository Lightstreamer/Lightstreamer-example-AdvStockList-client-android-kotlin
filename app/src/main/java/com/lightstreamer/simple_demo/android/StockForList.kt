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

import android.widget.ListView
import com.lightstreamer.client.ItemUpdate
import com.lightstreamer.simple_demo.android.StocksAdapter.RowHolder
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class StockForList(private val pos: Int) {

    private var stockName = "N/A"
    private var lastPrice = "N/A"
    private var lastPriceNum = 0.0
    private var time = "N/A"

    private var stockNameColor = R.color.background
    private var lastPriceColor = R.color.background
    private var timeColor = R.color.background
    private var turningOff: TurnOffRunnable? = null

    private val format = DecimalFormat("#.00")
    private val dateFormat = SimpleDateFormat("HH:mm:ss")

    fun update(newData: ItemUpdate, context: Context) {
        val isSnapshot = newData.isSnapshot
        if (newData.isValueChanged("stock_name")) {
            stockName = newData.getValue("stock_name") ?: ""
            stockNameColor = if (isSnapshot) R.color.snapshot_highlight else R.color.higher_highlight
        }
        if (newData.isValueChanged("timestamp")) {
            time = dateFormat.format(Date(java.lang.Long.parseLong(newData.getValue("timestamp"))))
            timeColor = if (isSnapshot) R.color.snapshot_highlight else R.color.higher_highlight
        }
        if (newData.isValueChanged("last_price")) {
            val newPrice = java.lang.Double.parseDouble(newData.getValue("last_price"))
            lastPrice = format.format(newPrice)

            if (isSnapshot) {
                lastPriceColor = R.color.snapshot_highlight
            } else {
                lastPriceColor = if (newPrice < lastPriceNum) R.color.lower_highlight else R.color.higher_highlight
                lastPriceNum = newPrice
            }
        }

        this.turningOff?.disable()

        context.handler.post {
            val holder = extractHolder(context.listView)
            if (holder != null) {
                fill(holder)
            }
        }

        this.turningOff = TurnOffRunnable(context)
        context.handler.postDelayed(this.turningOff!!, 600)
    }

    fun clean() {
        this.turningOff?.disable()
        this.turningOff = null

        stockNameColor = R.color.background
        lastPriceColor = R.color.background
        timeColor = R.color.background
    }


    fun fill(holder: RowHolder) {
        holder.stock_name.text = stockName
        holder.last_price.text = lastPrice
        holder.time.text = time

        this.fillColor(holder)
    }

    fun fillColor(holder: RowHolder) {
        holder.stock_name.setBackgroundResource(stockNameColor)
        holder.last_price.setBackgroundResource(lastPriceColor)
        holder.time.setBackgroundResource(timeColor)
    }

    internal fun extractHolder(listView: ListView): RowHolder? {
        val row = listView.getChildAt(pos - listView.firstVisiblePosition) ?: return null
        return row.tag as RowHolder
    }


    private inner class TurnOffRunnable(private val context: Context) : Runnable {

        private var valid = true

        @Synchronized fun disable() {
            valid = false
        }

        @Synchronized override fun run() {
            if (!valid) {
                return
            }
            stockNameColor = R.color.transparent
            lastPriceColor = R.color.transparent
            timeColor = R.color.transparent


            extractHolder(context.listView)?.let {
                fillColor(it)
            }
        }
    }

}
