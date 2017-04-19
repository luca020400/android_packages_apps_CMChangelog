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

import android.util.Log;

import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

class ChangelogParser {

    /**
     * Logcat tag.
     */
    private static final String TAG = "ChangelogParser";

    public List<Change> readJsonStream(InputStream in) throws IOException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"))) {
            reader.setLenient(true); // strip XSSI protection
            return parseChangeInfoList(reader);
        }
    }

    /**
     * Read a Collection of ChangeInfo JSON entities
     * See https://review.cyanogenmod.org/Documentation/rest-api.html
     *
     * @param reader the JsonReader to use
     * @return a List of Changes
     * @throws IOException
     */
    private List<Change> parseChangeInfoList(JsonReader reader) throws IOException {
        List<Change> changes = new LinkedList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            Change newChange = parseChangeInfo(reader);
            // check if its a legit change
            if (newChange.getSubmitted() != null && newChange.isDeviceSpecific()) {
                changes.add(newChange);
            }
        }
        reader.endArray();
        Collections.sort(changes, (c1, c2) -> {
            if (c1.getSubmitted() == null || c2.getSubmitted() == null)
                return 0;
            return c2.getSubmitted().compareTo(c1.getSubmitted());
        });
        return changes;
    }

    /**
     * Read ChangeInfo JSON entity
     * See https://review.cyanogenmod.org/Documentation/rest-api.html
     *
     * @param reader the JsonReader to use
     * @return the parsed Change.
     * @throws IOException
     */
    private Change parseChangeInfo(JsonReader reader) throws IOException {
        Change change = new Change();
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "_number":
                    change.setChangeId(reader.nextString());
                    break;
                case "project":
                    change.setProject(reader.nextString());
                    break;
                case "subject":
                    change.setSubject(reader.nextString());
                    break;
                case "submitted":
                    change.setSubmitted(parseTimestamp(reader.nextString()));
                    break;
                case "insertions":
                    change.setInsertions(reader.nextInt());
                    break;
                case "deletions":
                    change.setDeletions(reader.nextInt());
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();
        return change;
    }

    /**
     * Parse ChangeInfo timestamp values.
     * Timestamps are given in UTC and have the format "'yyyy-mm-dd hh:mm:ss.fffffffff'"
     * where "'ffffffffff'" represents nanoseconds.
     *
     * @param timestamp timestamp String from a ChangeInfo entity
     * @return the parsed Date
     */
    private Date parseTimestamp(String timestamp) {
        Date date = new Date(0);
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            /* Parse UTC date */
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            date = formatter.parse(timestamp);
        } catch (ParseException e) {
            Log.e(TAG, "Couldn't parse timestamp.");
        }
        return date;
    }
}
