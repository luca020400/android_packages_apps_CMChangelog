/*
 * Copyright (c) 2016 The CyanogenMod Project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cyanogenmod.changelog

import java.net.MalformedURLException
import java.net.URL

internal class RestfulUrl(val baseUrl: String, val endpoint: String) {
    /**
     * Query string
     */
    private var query = ""

    /**
     * The n parameter can be used to limit the returned results.
     */
    private var n = 0

    /**
     * The start query parameter can be supplied to skip a number of changes from the list.
     */
    private var start = 0

    fun setN(n: Int) {
        this.n = n
    }

    fun setStart(start: Int) {
        this.start = start
    }

    fun appendQuery(query: String) {
        if (!query.isEmpty()) {
            if (this.query.isEmpty())
                this.query = query
            else
                this.query = this.query + "+" + query
        }
    }

    override fun toString(): String {
        val string = StringBuilder(128)
        string.append(baseUrl).append(endpoint).append("?pp=0")
        if (!query.isEmpty()) {
            string.append("&q=").append(query)
            if (n > 0) string.append("&n=").append(n)
            if (start > 0) string.append("&start=").append(start)
        }
        return string.toString()
    }

    @Throws(MalformedURLException::class)
    fun createUrl(): URL {
        return URL(toString())
    }
}
