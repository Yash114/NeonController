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

public class SimpleLayoutsView extends Fragment {

    View root;
    LayoutClass selected;

    RelativeLayout relativeLayout;


    public SimpleLayoutsView() {

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.simple_layouts_page, container, false);

        selected = UserData.currentLayout;

        ListViewController recyclerView = root.findViewById(R.id.recycleView);

        relativeLayout = root.findViewById(R.id.container);

        recyclerView.setSelected(selected);
        recyclerView.setData();

        recyclerView.setOnListSelectedListener(new GenericCallback().setOnGenericCallback(new OnGenericCallback() {
            @Override
            public void onChange(Object value) {
                selected = (LayoutClass) value;
                UserData.setCurrentLayout(requireContext(), selected);
            }
        }));

        return root;

    }
}
