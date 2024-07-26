package com.gingertech.starbeam.ui.layout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.LayoutClass;
import com.gingertech.starbeam.helpers.controllers.GenericCallbackv2;
import com.gingertech.starbeam.helpers.controllers.LaunchOverlayController;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

public class LayoutTestFragment extends Fragment {

    View root;

    GenericCallbackv2 changeFragmentCallback;
    LayoutClass layoutClass;

    LayoutTestFragment(GenericCallbackv2 changeFragmentCallback, LayoutClass layoutClass) {
        this.changeFragmentCallback = changeFragmentCallback;
        this.layoutClass = layoutClass;

    }

    public LayoutTestFragment() {
    }
    private MixpanelAPI mp;


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.layout_test_view, container, false);

        LaunchOverlayController controller = root.findViewById(R.id.testLayout);
        controller.setupWifi(null, null);
//        controller.makeButtons(true);

        mp = MixpanelAPI.getInstance(requireContext(), "054951ba4c264c6f804c798152383940", true);

        root.findViewById(R.id.close_test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    JSONObject props = new JSONObject();
                    props.put("title", "editButton");
                    mp.track("Button Clicked", props);
                } catch(JSONException j) {

                }

                if(changeFragmentCallback != null) {
                    changeFragmentCallback.onChange(LayoutRootPage.CREATEVIEW, layoutClass);

                }
            }
        });

        return root;

    }
}