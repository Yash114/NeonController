package com.gingertech.starbeam.ui;

import static com.gingertech.starbeam.MainActivity.uiFlag;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.UserData;
import com.gingertech.starbeam.helpers.controllers.GenericCallback;
import com.gingertech.starbeam.helpers.controllers.GenericCallbackv2;

import java.util.Random;

public class HomePage extends Fragment {

    View root;

    GenericCallbackv2 callbackv2;
    public HomePage(GenericCallbackv2 callbackv2) {
        this.callbackv2 = callbackv2;
    }

    public HomePage() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        requireActivity().getWindow().getDecorView().setSystemUiVisibility(uiFlag);

        root = inflater.inflate(R.layout.home_page, container, false);

        return root;
    }
}
