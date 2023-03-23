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

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.util.Log
import com.androidplot.util.PixelUtils
import com.androidplot.xy.*
import com.lightstreamer.client.ItemUpdate
import com.lightstreamer.client.Subscription
import com.lightstreamer.client.SubscriptionListener
import java.text.FieldPosition
import java.text.Format
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*

class Chart(private val dynamicPlot: XYPlot, private val handler: Handler) : SubscriptionListener by EmptySubscriptionListener {

    //to avoid synchronizations and concurrency issues max, min and series must be only modified by the handler thread
    internal var maxY = 0.0
    internal var minY = 0.0
    private val series: Series

    init {
        this.series = Series()

        dynamicPlot.setDomainStep(StepMode.SUBDIVIDE, 4.0)
        dynamicPlot.setRangeStep(StepMode.SUBDIVIDE, 5.0)
        dynamicPlot.legend.isVisible = false

        dynamicPlot.backgroundPaint.color = Color.BLACK
        dynamicPlot.graph.backgroundPaint.color = Color.BLACK
        dynamicPlot.graph.gridBackgroundPaint.color = Color.BLACK

        dynamicPlot.graph.getLineLabelStyle(XYGraphWidget.Edge.LEFT).paint.color = Color.WHITE
        dynamicPlot.graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).paint.color = Color.WHITE

        dynamicPlot.setRangeBoundaries(minY, maxY, BoundaryMode.FIXED)

        dynamicPlot.graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).format = FormatDateLabel()
    }

    fun onResume(context: Context) {
        PixelUtils.init(context)

        val line = context.resources.getColor(R.color.chart_line)
        val formatter = LineAndPointFormatter(line, line, null, null)
        this.dynamicPlot!!.addSeries(series, formatter)

        this.clean()
    }

    fun onPause() {
        this.dynamicPlot.removeSeries(series)
    }

    override fun onListenStart(subscription: Subscription) {
        this.clean()
    }

    override fun onItemUpdate(update: ItemUpdate) {
        val lastPrice = update.getValue("last_price")
        val time = update.getValue("timestamp")
        if (lastPrice != null && time != null)
            this.addPoint(time, lastPrice)
    }

    private fun addPoint(time: String, lastPrice: String) {
        handler.post {
            Log.v(TAG, "New point")
            series.add(time, lastPrice)
        }
        this.redraw()
    }

    private fun clean() {
        handler.post {
            Log.i(TAG, "Reset chart")
            series.reset()
            maxY = 0.0
            minY = 0.0
        }
        this.redraw()
    }

    private fun redraw() {
        handler.post {
            dynamicPlot.setRangeBoundaries(minY, maxY, BoundaryMode.FIXED)
            Log.v(TAG, "Redraw chart")
            dynamicPlot.redraw()
        }
    }

    private fun onYOverflow(last: Double) {
        Log.d(TAG, "Y overflow detected")
        //XXX currently never shrinks
        val shift = 1
        if (last > maxY) {
            var newMax = maxY + shift
            if (last > newMax) {
                newMax = last
            }

            this.maxY = newMax

        } else if (last < minY) {
            var newMin = minY - shift
            if (last < newMin) {
                newMin = last
            }

            this.minY = newMin
        }
        Log.i(TAG, "New Y boundaries: " + this.minY + " -> " + this.maxY)
    }

    private fun onFirstPoint(newPrice: Double) {
        Log.d(TAG, "First point on chart")
        minY = (newPrice - 1).coerceAtLeast(0.0)
        maxY = newPrice + 1
        Log.i(TAG, "New Y boundaries: " + this.minY + " -> " + this.maxY)
    }


    private inner class Series : XYSeries {
        internal var prices = ArrayList<Number>()
        internal var times = ArrayList<Number>()

        override fun getTitle(): String = ""

        fun add(time: String, lastPrice: String) {
            if (prices.size >= MAX_SERIES_SIZE) {
                prices.removeAt(0)
                times.removeAt(0)
            }

            val longTime = java.lang.Long.parseLong(time)
            val newPrice = java.lang.Double.parseDouble(lastPrice)

            if (prices.isEmpty()) {
                onFirstPoint(newPrice)
            }

            if (newPrice < minY || newPrice > maxY) {
                onYOverflow(newPrice)
            }

            prices.add(newPrice)
            times.add(longTime)
        }

        fun reset() {
            prices.clear()
            times.clear()
        }

        override fun getX(index: Int): Number {
            Log.v(TAG, "Extract X")
            return times[index]
        }

        override fun getY(index: Int): Number {
            Log.v(TAG, "Extract Y")
            return prices[index]
        }

        override fun size(): Int {
            Log.v(TAG, "Extract size")
            return prices.size
        }
    }

    private inner class FormatDateLabel : Format() {
        private val dateFormat = SimpleDateFormat("HH:mm:ss")

        override fun format(any: Any, buffer: StringBuffer,
                            field: FieldPosition): StringBuffer {
            val num = any as Number
            return buffer.append(dateFormat.format(Date(num.toLong())))
        }

        override fun parseObject(string: String, position: ParsePosition): Any? = null
    }

    companion object {
        private const val MAX_SERIES_SIZE = 40
        private const val TAG = "Chart"
    }
}
