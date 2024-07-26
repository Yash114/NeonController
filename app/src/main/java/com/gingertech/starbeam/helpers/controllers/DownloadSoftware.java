package com.gingertech.starbeam.helpers.controllers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.LayoutClass;
import com.gingertech.starbeam.helpers.UserData;
import com.gingertech.starbeam.helpers.controllers.GenericCallback;
import com.gingertech.starbeam.helpers.controllers.OnGenericCallback;

public class DownloadSoftware extends Fragment {

    View root;
    LayoutClass selected;

    RelativeLayout relativeLayout;


    public DownloadSoftware() {

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.download_software, container, false);


        return root;

    }
}