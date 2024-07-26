package com.gingertech.starbeam.helpers.controllers;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gingertech.starbeam.MainActivity;
import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.UserData;

import java.util.Timer;
import java.util.TimerTask;

public class OnboardingTutorialController extends ConstraintLayout {
    public OnboardingTutorialController(@NonNull Context context) {
        super(context);
        init(context);
    }

    public OnboardingTutorialController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public OnboardingTutorialController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public OnboardingTutorialController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    View root;

    ImageView mainImage;
    TextView textTitleView;
    TextView textSubTitleView;

    ImageView backButton;
    ImageView forwardButton;
    LinearLayout progressSelectors;
    LinearLayout textGroup;

    int currentPage = 0;
    int maxPages = 5;

    ColorStateList highlightedColor;
    ColorStateList notHighlightedColor;

    boolean transitioning = false;
    String[] textList1 = {
            "Play PC Games on Mobile",
            "Step 1.",
            "Step 2.",
            "Step 3.",
            "That's It!"
    };

    String[] textList2_t1 = {
            "in 3 Easy Steps!",
            "Install PC Streaming Software",
            "Connect your PC and Phone",
            "Customize Your Controller",
            "Play your Favorite Games!"
    };

    String[] textList2_t2 = {
            "in 3 Steps!",
            "Install PC Streaming Software",
            "Connect your PC and Phone",
            "Customize Your Controller",
    };
    int[] images_t1 = {
            R.drawable.onboarding_1,
            R.drawable.onboarding_5,
            R.drawable.onboarding_2,
            R.drawable.onboarding_3,
            R.drawable.onboarding_4,

    };

    int[] images_t2 = {
            R.drawable.onboarding_1,
            R.drawable.onboarding_5,
            R.drawable.onboarding_2,
            R.drawable.onboarding_3,
            R.drawable.onboarding_4,
    };

    Vibrator vibrator;

    void init(Context context) {

        highlightedColor = ColorStateList.valueOf(context.getColor(R.color.secondary));
        notHighlightedColor = ColorStateList.valueOf(context.getColor(R.color.primary));

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = inflater.inflate(R.layout.onboarding_tutorial_popup, this, true);

        mainImage = root.findViewById(R.id.imageView);
        textTitleView = root.findViewById(R.id.titleText);
        textSubTitleView = root.findViewById(R.id.subTitleText);
        textGroup = root.findViewById(R.id.textGroup);

        backButton = root.findViewById(R.id.back);
        forwardButton = root.findViewById(R.id.forward);
        progressSelectors = root.findViewById(R.id.linearLayout);

        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentPage != 0) {
                    currentPage--;
                    updatePages();

                    vibrator.vibrate(10);

                } else {

                    vibrator.vibrate(100);
                }
            }
        });

        forwardButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if(currentPage == maxPages - 1) {
                    root.setVisibility(GONE);

                    resetViews();

                    if(onFinishCallback != null) {
                        onFinishCallback.onChange(null);
                    }

                    return;
                }

                if(currentPage < maxPages - 1 && !transitioning) {
                    currentPage++;
                    updatePages();
                }

                vibrator.vibrate(10);
            }
        });

        updatePages();
    }

    void updatePages() {

        transitioning = true;

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                transitioning = false;
            }
        }, 750);

        resetViews();
        animateViews();

        ((ImageView) progressSelectors.getChildAt(currentPage + 1)).setImageTintList(highlightedColor);

        if(currentPage > 0) {
            ((ImageView) progressSelectors.getChildAt(currentPage)).setImageTintList(notHighlightedColor);
        }

        if(currentPage < maxPages - 1) {
            ((ImageView) progressSelectors.getChildAt(currentPage + 2)).setImageTintList(notHighlightedColor);
        }

        mainImage.setImageResource(images_t1[currentPage]);
        textTitleView.setText(textList1[currentPage]);
        textSubTitleView.setText(textList2_t1[currentPage]);

    }


    ObjectAnimator a1;
    ObjectAnimator a2;
    ObjectAnimator a3;

    void resetViews() {
        mainImage.setX(mainImage.getX() + 20);
        textGroup.setX(textGroup.getX() - 20);
        ((View) ((View) textSubTitleView.getParent()).getParent()).setAlpha(0);
    }

    void animateViews() {
        a1 = ObjectAnimator.ofFloat(mainImage, "translationX", -20);
        a1.setDuration(500);
        a1.start();

        a2 = ObjectAnimator.ofFloat(textGroup, "translationX", 20);
        a2.setDuration(500);
        a2.start();

        a3 = ObjectAnimator.ofFloat(textGroup.getParent(), "alpha", 1);
        a3.setDuration(500);
        a3.start();
    }

    GenericCallbackv2 onFinishCallback;
    public void setOnFinish(GenericCallbackv2 genericCallbackv2) {
        this.onFinishCallback = genericCallbackv2;
    }
}
