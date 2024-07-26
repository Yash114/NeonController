package com.gingertech.starbeam.helpers.controllers;

import android.content.Context;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gingertech.starbeam.R;

import java.util.ArrayList;
import java.util.Set;

public class RemapListController extends RelativeLayout {

    public RemapListController(@NonNull Context context) {
        super(context);
        init(context);
    }

    public RemapListController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RemapListController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public RemapListController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    View root;
    RemapClass remapClass;
    public void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = inflater.inflate(R.layout.remap_list_view, this, true);
    }

    public void setRemap(Context c, RemapClass remapClass) {

        this.remapClass = remapClass;

        LinearLayout layout = root.findViewById(R.id.controllerAssignments);
        layout.removeAllViews();

        for(Remap remap : remapClass.assignments) {
            layout.addView(new RemapListItemController(c, remap));
        }
    }

    public RemapClass getRemap() {

        ArrayList<Remap> remapList = new ArrayList<>();

        LinearLayout layout = root.findViewById(R.id.controllerAssignments);
        int childCount = layout.getChildCount();

        for(int remapIndex = 0; remapIndex < childCount; remapIndex++) {

            RemapListItemController v = (RemapListItemController) layout.getChildAt(remapIndex);

            remapList.add(v.getRemap());
        }

        return new RemapClass(remapList);
    }
}
