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
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class StocksAdapter(private val activity: Activity, layout: Int, list: List<StockForList>) : ArrayAdapter<StockForList>(activity, layout, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val holder: RowHolder

        if (row == null) {
            val inflater = this.activity.layoutInflater
            row = inflater.inflate(R.layout.row_layout, parent, false)!!

            holder = RowHolder(
                    row.findViewById(R.id.stock_name) as TextView,
                    row.findViewById(R.id.last_price) as TextView,
                    row.findViewById(R.id.time) as TextView
            )

            row.tag = holder

        } else {
            holder = row.tag as RowHolder
        }

        val stock = getItem(position)
        stock.fill(holder)

        return row
    }

    class RowHolder(val stock_name: TextView,
                          val last_price: TextView,
                          val time: TextView)

}
