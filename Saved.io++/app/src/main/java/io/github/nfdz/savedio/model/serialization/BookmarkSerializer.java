/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio.model.serialization;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.github.nfdz.savedio.R;
import io.github.nfdz.savedio.model.Bookmark;

/** This class has methods to ease bookmark serialization. Serialization format is JSON. */
public class BookmarkSerializer {

    /**
     * Serializes given bookmark.
     * @param bm
     * @return String JSON object.
     * @throws SerializationException
     */
    public static String serialize(Bookmark bm) throws SerializationException {
        try {
            JSONObject bmJson = new JSONObject();
            bmJson.put(bm.FIELD_TITLE, bm.getTitle());
            bmJson.put(bm.FIELD_LIST, bm.getListName());
            bmJson.put(bm.FIELD_URL, bm.getUrl());
            bmJson.put(bm.FIELD_NOTE, bm.getNotes());
            return bmJson.toString();
        } catch (JSONException e) {
            throw new SerializationException(R.string.serialize_json_error);
        }
    }

    /**
     * Serializes given bookmark list.
     * @param bookmarks
     * @return String JSON array.
     * @throws SerializationException
     */
    public static String serialize(List<Bookmark> bookmarks) throws SerializationException {
        if (bookmarks.isEmpty()) {
            throw new SerializationException(R.string.serialize_empty_error);
        }
        JSONArray array = new JSONArray();
        for (Bookmark bm : bookmarks) {
            array.put(serialize(bm));
        }
        return array.toString();
    }

    /**
     * Deserializes given bookmark.
     * @param serializedBm
     * @return Bookmark (it is not a realm object)
     * @throws SerializationException
     */
    public static Bookmark deserializeBookmark(String serializedBm) throws SerializationException {
        try {
            JSONObject bmJson = new JSONObject(serializedBm);
            Bookmark bm = new Bookmark();
            bm.setTitle(bmJson.has(bm.FIELD_TITLE) ? bmJson.getString(bm.FIELD_TITLE) : "");
            bm.setListName(bmJson.has(bm.FIELD_LIST) ? bmJson.getString(bm.FIELD_LIST) : "");
            bm.setUrl(bmJson.getString(bm.FIELD_URL)); // mandatory field
            bm.setNotes(bmJson.has(bm.FIELD_NOTE) ? bmJson.getString(bm.FIELD_NOTE) : "");
            return bm;
        } catch (JSONException e) {
            throw new SerializationException(R.string.deserialize_json_error);
        }
    }

    /**
     * Deserializes given bookmarks.
     * @param serializedBms
     * @return List of Bookmark (it is not a realm object)
     * @throws SerializationException
     */
    public static List<Bookmark> deserializeBookmarks(String serializedBms) throws SerializationException {
        try {
            List<Bookmark> bookmarks = new ArrayList<>();
            JSONArray array = new JSONArray(serializedBms);
            int length = array.length();
            if (length <= 0) {
                throw new SerializationException(R.string.deserialize_empty_error);
            }
            for (int i = 0; i < length; i++) {
                bookmarks.add(deserializeBookmark(array.getString(i)));
            }
            return bookmarks;
        } catch (JSONException e) {
            throw new SerializationException(R.string.deserialize_json_array_error);
        }
    }
}
