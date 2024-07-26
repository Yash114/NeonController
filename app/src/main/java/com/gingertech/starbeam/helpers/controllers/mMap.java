package com.gingertech.starbeam.helpers.controllers;

import java.util.HashMap;

public class mMap extends HashMap<String, Object> {

    mMap(HashMap<String, Object> other) {
        this.putAll(other);
    }

    public Object getWithDefault(String key, Object defaultValue) {
        return this.containsKey(key) ? this.get(key) : defaultValue;
    }

    public Object getWithDefault(String key) {
        return this.containsKey(key) ? this.get(key) : "~";
    }
}
