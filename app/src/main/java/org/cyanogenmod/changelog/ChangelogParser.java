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

    public List<Change> parseJSON(InputStream inputStream) throws IOException {
        List<Change> changes = new LinkedList<>();
        /* Parse JSON */
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
        reader.setLenient(true); // strip XSSI protection
        reader.beginArray();
        while (reader.hasNext()) {
            reader.beginObject();
            Change newChange = new Change();
            while (reader.hasNext()) {
                switch (reader.nextName()) {
                    case "_number":
                        newChange.setChangeId(reader.nextString());
                        break;
                    case "project":
                        newChange.setProject(reader.nextString());
                        break;
                    case "subject":
                        newChange.setSubject(reader.nextString());
                        break;
                    case "updated":
                        newChange.setLastUpdate(reader.nextString());
                        break;
                    default:
                        reader.skipValue();
                }
            }
            reader.endObject();
            // check if its a legit change
            if (newChange.isDeviceSpecific()) {
                changes.add(newChange);
            }
        }
        return changes;
    }
}
