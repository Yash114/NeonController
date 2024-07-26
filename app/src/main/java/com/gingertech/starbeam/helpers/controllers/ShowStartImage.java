package com.gingertech.starbeam.helpers.controllers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gingertech.starbeam.R;

public class ShowStartImage extends Fragment {

    View root;


    public ShowStartImage() {

    }


    @Nullable
    @java.lang.Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.start_image, container, false);

        return root;

    }
}
