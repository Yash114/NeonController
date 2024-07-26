package com.gingertech.starbeam;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gingertech.starbeam.helpers.UserData;
import com.gingertech.starbeam.helpers.controllers.GenericCallbackv2;
import com.gingertech.starbeam.helpers.controllers.PremiumController;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class LaunchActivity extends Activity {

    public static int uiFlag = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LOW_PROFILE
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    public void initalizePurchases() {
        PremiumController premiumController = new PremiumController();
        premiumController.setup(LaunchActivity.this, new GenericCallbackv2() {
            @Override
            public void onChange(Object value) {

                if(value instanceof Boolean) {
                    UserData.isPremiumMode = (boolean) value;
                }

                if(value instanceof String) {
                    if(((String) value).equals("error")) {
                        launchError();
                    }
                }
            }
        });
    }

    boolean error = false;
    ViewGroup container;

    @Override
    protected void onStart() {
        super.onStart();
    }

    Boolean openPremium = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        openPremium = getIntent().getBooleanExtra("premium_page", false);

        setContentView(R.layout.logo_page);
        getWindow().getDecorView().setSystemUiVisibility(uiFlag);

        findViewById(R.id.cont).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainActivity = new Intent(LaunchActivity.this, MainActivity.class);
                mainActivity.putExtra("open_premium", openPremium);
                startActivity(mainActivity);
            }
        });

        try {
            setupAnimations();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        initalizePurchases();
    }

    private void setupAnimations() throws InterruptedException {

        container = findViewById(R.id.container);

        ArrayList<View> views = new ArrayList<>();

        int viewCount = container.getChildCount();

        for (int u = 0; u < viewCount; u++) {
            views.add(container.getChildAt(u));
        }

        int k = 0;
        for(View v : views) {
            animate(v, k, viewCount);
            k++;
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(!error) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            launchMainActivity();
                        }
                    });
                }
            }
        }, viewCount * 1000L + 2000);
    }

    private void animate(View view, int offset, int count) {
        ObjectAnimator a1 = ObjectAnimator.ofFloat(view, "scaleY", 0);

        a1.setDuration(1000L * (count - offset));
        a1.start();
    }



    private void launchMainActivity() {
        findViewById(R.id.cont).setVisibility(View.VISIBLE);

        Log.e("launch", "launched activity");
        Intent mainActivity = new Intent(this, MainActivity.class);
        mainActivity.putExtra("open_premium", openPremium);
        startActivity(mainActivity);

    }

    private void launchError() {
        error = true;
        Snackbar.make(container, getApplicationContext().getString(R.string.could_not_connect_to_the_internet_please_reconnect_and_restart_the_app), Snackbar.LENGTH_INDEFINITE).show();
    }

    public LaunchActivity() {

    }
}
