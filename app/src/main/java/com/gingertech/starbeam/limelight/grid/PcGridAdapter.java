package com.gingertech.starbeam.limelight.grid;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.gingertech.starbeam.R;
import com.gingertech.starbeam.limelight.PcView;
import com.gingertech.starbeam.limelight.nvstream.http.ComputerDetails;
import com.gingertech.starbeam.limelight.nvstream.http.PairingManager;
import com.gingertech.starbeam.limelight.preferences.PreferenceConfiguration;
import com.gingertech.starbeam.ui.launch.LaunchComputerList;

import java.util.Collections;
import java.util.Comparator;

public class PcGridAdapter extends GenericGridAdapter<LaunchComputerList.ComputerObject> {

    public PcGridAdapter(Context context, PreferenceConfiguration prefs) {
        super(context, getLayoutIdForPreferences(prefs));
    }

    private static int getLayoutIdForPreferences(PreferenceConfiguration prefs) {
        return R.layout.pc_grid_item;
    }

    public void updateLayoutWithPreferences(Context context, PreferenceConfiguration prefs) {
        // This will trigger the view to reload with the new layout
        setLayoutId(getLayoutIdForPreferences(prefs));
    }

    public void addComputer(LaunchComputerList.ComputerObject computer) {
        itemList.add(computer);
        sortList();
    }

    private void sortList() {
        Collections.sort(itemList, new Comparator<LaunchComputerList.ComputerObject>() {
            @Override
            public int compare(LaunchComputerList.ComputerObject lhs, LaunchComputerList.ComputerObject rhs) {
                return lhs.details.name.toLowerCase().compareTo(rhs.details.name.toLowerCase());
            }
        });
    }

    public boolean removeComputer(LaunchComputerList.ComputerObject computer) {
        return itemList.remove(computer);
    }

    @Override
    public void populateView(View parentView, ImageView imgView, ProgressBar prgView, TextView txtView, ImageView overlayView, LaunchComputerList.ComputerObject obj) {
        imgView.setImageResource(R.drawable.pc_icon);
        imgView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.secondary)));
        if (obj.details.state == ComputerDetails.State.ONLINE) {
            imgView.setAlpha(1.0f);
        }
        else {
            imgView.setAlpha(0.4f);
        }

        if (obj.details.state == ComputerDetails.State.UNKNOWN) {
            prgView.setVisibility(View.VISIBLE);
        }
        else {
            prgView.setVisibility(View.INVISIBLE);
        }

        txtView.setText(obj.details.name);
        if (obj.details.state == ComputerDetails.State.ONLINE) {
            txtView.setAlpha(1.0f);
        }
        else {
            txtView.setAlpha(0.4f);
        }

        if (obj.details.state == ComputerDetails.State.OFFLINE) {
            overlayView.setImageResource(R.drawable.warning_icon);
            overlayView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.yellow)));
            overlayView.setAlpha(0.4f);
            overlayView.setVisibility(View.VISIBLE);
        }
        // We must check if the status is exactly online and unpaired
        // to avoid colliding with the loading spinner when status is unknown
        else if (obj.details.state == ComputerDetails.State.ONLINE &&
                obj.details.pairState == PairingManager.PairState.NOT_PAIRED) {
            overlayView.setImageResource(R.drawable.lock_icon);
            overlayView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.secondary)));

            overlayView.setAlpha(1.0f);
            overlayView.setVisibility(View.VISIBLE);
        }
        else {
            overlayView.setImageResource(R.drawable.play_icon);
            overlayView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.secondary)));
            overlayView.setVisibility(View.GONE);
        }
    }
}
