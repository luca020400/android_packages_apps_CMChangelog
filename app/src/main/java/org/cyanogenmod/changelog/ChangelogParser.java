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

import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class ChangelogParser {
    private static final String TAG = "ChangelogParser";

    public List<Change> readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            reader.setLenient(true); // strip XSSI protection
            return parseChangeInfoList(reader);
        } finally {
            reader.close();
        }
    }

    /**
     * Parse a list ChangeInfo JSON entities
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
            if (newChange.isDeviceSpecific()) {
                changes.add(newChange);
            }
        }
        reader.endArray();
        return changes;
    }

    /**
     * Parse a single ChangeInfo JSON entity
     * @param reader the JsonReader to use
     * @return the parsed Change
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
                case "updated":
                    change.setLastUpdate(reader.nextString());
                    break;
                case "insertions":
                    change.setInsertions(reader.nextInt());
                    break;
                case "deletions":
                    change.setDeletions(reader.nextInt());
                    break;
                case "messages":
                    change.setMergeDate(parseChangeMessageInfoDate(reader));
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();
        return change;
    }

    /**
     * Parse ChangeMessageInfo entity, try to pull out the merge date and return it.
     * @param reader the JsonReader to use
     * @return the timestamp of when the Change has been merged
     * @throws IOException
     */
    private String parseChangeMessageInfoDate(JsonReader reader) throws IOException {
        boolean merged = false;
        String mergeDate = "";
        reader.beginArray();
        while (reader.hasNext()) {
            reader.beginObject();
            while (reader.hasNext()) {
                switch (reader.nextName()) {
                    case "date":
                        mergeDate = reader.nextString();
                        break;
                    case "message":
                        merged = reader.nextString().contains("successfully merged");
                        break;
                    default:
                        reader.skipValue();
                }
            }
            reader.endObject();
        }
        reader.endArray();
        if (merged)
            return mergeDate;
        else
            return "";
    }
}
