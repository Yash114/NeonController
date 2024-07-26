package com.gingertech.starbeam.helpers.controllers;

import android.content.Context;
import android.media.Image;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gingertech.starbeam.R;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.Timer;
import java.util.TimerTask;

public class TutorialViewController extends ConstraintLayout {
    public TutorialViewController(Context context) {
        super(context);

        init(context);
    }

    public TutorialViewController(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public TutorialViewController(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    public TutorialViewController(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context);
    }

    ViewGroup root;

    TextView stepTitle;
    TextView stepDescription;
    ImageView imageView;
    TextView backButton;
    TextView nextButton;
    TextView doneButton;



    int[] tutorialImages = {
            R.drawable.tutorial_1,
            R.drawable.tutorial_2,
            R.drawable.tutorial_3,
            R.drawable.tutorial_4,
            R.drawable.tutorial_5,
//            R.drawable.tutorial_6,

    };
    String[] headerText = {
            getContext().getString(R.string.step_1),
            getContext().getString(R.string.step_2),
            getContext().getString(R.string.step_3),
            getContext().getString(R.string.step_4),
            getContext().getString(R.string.step_5),
//            getContext().getString(R.string.step_6)
    };

    String[] bodyText = {
            "Set both devices to the same WiFi network",
            "Go to www.neoncontroller.app on your PC",
            "Download and Install the Neon PC Host",
            "Reload mobile device and tap on ayour computer",
            "Enter pairing PIN and Play!"
    };

    int currentTutorialPage = 0;
    int totalPages = 5;

    MixpanelAPI mp;

    public void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = (ViewGroup) inflater.inflate(R.layout.tutorial_view, this, true);

        mp = MixPanel.makeObj(context);

        stepTitle = root.findViewById(R.id.stepTitle);
        stepDescription = root.findViewById(R.id.stepDescription);
        imageView = root.findViewById(R.id.imageView);

        nextButton = root.findViewById(R.id.next);
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(transitioning) { return; }

                MixPanel.mpButtonTracking(mp, "connect_tutorial_next");
                if(currentTutorialPage == totalPages - 1) { return; }

                currentTutorialPage += 1;

                updatePage();
            }
        });

        backButton = root.findViewById(R.id.back);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                MixPanel.mpButtonTracking(mp, "connect_tutorial_back");
                if(currentTutorialPage == 0) { return; }
                currentTutorialPage -= 1;


                updatePage();
            }
        });

        doneButton = root.findViewById(R.id.done);
        doneButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

//                if(transitioning) { return; }
                MixPanel.mpButtonTracking(mp, "connect_tutorial_done");
                currentTutorialPage = 0;

                if(onFinishedListener != null) {
                    onFinishedListener.onChange(null);
                    currentTutorialPage = 0;
                }

                updatePage();
            }
        });
        updatePage();

    }

    boolean transitioning = true;

    public void updatePage() {

        transitioning = true;

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                transitioning = false;
            }
        }, 1500);

        stepTitle.setText(headerText[currentTutorialPage]);
        stepDescription.setText(bodyText[currentTutorialPage]);
        imageView.setImageResource(tutorialImages[currentTutorialPage]);

        backButton.setVisibility(currentTutorialPage == 0 ? INVISIBLE : VISIBLE);
        nextButton.setVisibility(currentTutorialPage == totalPages - 1 ? GONE : VISIBLE);
        doneButton.setVisibility(currentTutorialPage == totalPages - 1 ? VISIBLE : GONE);

    }

    GenericCallbackv2 onFinishedListener;

    public void setOnFinishListener(GenericCallbackv2 genericCallbackv2) {
        onFinishedListener = genericCallbackv2;
    }
}
