package com.gingertech.starbeam.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.SaveClass;
import com.gingertech.starbeam.helpers.UserData;
import com.gingertech.starbeam.helpers.controllers.RemapClass;
import com.gingertech.starbeam.helpers.controllers.RemapListController;

public class RemapPage extends Fragment {

    View root;

    Vibrator vibrator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        UserData.CurrentFragment = UserData.REMAP;

        root = inflater.inflate(R.layout.remap_page, container, false);

        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);

        RemapListController remapListController = root.findViewById(R.id.remapListView);

        remapListController.setRemap(getContext(), UserData.mRemap);

        root.findViewById(R.id.saveRemap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(10);
                UserData.mRemap = remapListController.getRemap();
                SaveClass.SaveRemap(getContext(), UserData.mRemap);

                Toast.makeText(requireContext(), R.string.Saved, Toast.LENGTH_SHORT).show();
            }
        });

        root.findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(100);
                UserData.mRemap = new RemapClass();
                SaveClass.SaveRemap(getContext(), UserData.mRemap);
                remapListController.setRemap(getContext(), UserData.mRemap);

                Toast.makeText(requireContext(), R.string.Saved, Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

}
