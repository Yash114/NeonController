package com.gingertech.starbeam.limelight.ui;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gingertech.starbeam.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

@SuppressLint("ValidFragment")
public class AdapterFragment extends Fragment {
    private AdapterFragmentCallbacks callbacks;

    @SuppressLint("ValidFragment")
    public AdapterFragment(AdapterFragmentCallbacks callback) {
        this.callbacks = callback;
    }

    @SuppressLint("ValidFragment")
    public AdapterFragment(FragmentActivity callback) {
        this.callbacks = (AdapterFragmentCallbacks) callback;
    }

    public AdapterFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(callbacks == null) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        return inflater.inflate(callbacks.getAdapterFragmentLayoutId(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(callbacks != null) {
            callbacks.receiveAbsListView(getView().findViewById(R.id.fragmentView));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
