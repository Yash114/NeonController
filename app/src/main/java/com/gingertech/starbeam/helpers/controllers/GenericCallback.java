package com.gingertech.starbeam.helpers.controllers;

public class GenericCallback {

    private OnGenericCallback onGenericCallback;

    public GenericCallback setOnGenericCallback(OnGenericCallback listener) {
        onGenericCallback = listener;
        return this;
    }

    public void fire(Object newValue) {
        onGenericCallback.onChange(newValue);
    }

}
