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

package org.cyanogenmod.changelog;

import java.net.MalformedURLException;
import java.net.URL;

public class RestfulUrl {

    /**
     * Base url
     */
    private String baseUrl;

    /**
     * Rest endpoint
     */
    private String endpoint;

    /**
     * Query string
     */
    private String query;

    /**
     * The n parameter can be used to limit the returned results.
     */
    private int n;

    /**
     * The start query parameter can be supplied to skip a number of changes from the list.
     */
    private int start;

    /**
     * Set to true to request a compact JSON with no extra whitespaces.
     */
    private boolean requestCompactJSON;

    public RestfulUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        this.query = "";
        this.n = 0;
        this.start = 0;
        this.requestCompactJSON = false;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public boolean isRequestCompactJSON() {
        return requestCompactJSON;
    }

    public void setRequestCompactJSON(boolean requestCompactJSON) {
        this.requestCompactJSON = requestCompactJSON;
    }

    public void appendQuery(String query) {
        if (!query.isEmpty()) {
            if (this.query.isEmpty())
                this.query = this.query + query;
            else
                this.query = this.query + "+" + query;
        }
    }

    public void clearQuery() {
        this.query = "";
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder(128);
        string.append(baseUrl).append(endpoint);
        if (query != null && !query.isEmpty()) {
            string.append("?q=").append(query);
            if (n > 0) string.append("&n=").append(n);
            if (start > 0) string.append("&start=").append(start);
            if (requestCompactJSON) string.append("&pp=0");
        }
        return string.toString();
    }

    public URL createUrl() throws MalformedURLException {
        return new URL(toString());
    }
}
