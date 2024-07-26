package com.gingertech.starbeam.ui.layout;

import static android.content.ContentValues.TAG;

import static com.gingertech.starbeam.MainActivity.mFirebaseAnalytics;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.gingertech.starbeam.MainActivity;
import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.ButtonHandler;
import com.gingertech.starbeam.helpers.LayoutClass;
import com.gingertech.starbeam.helpers.SaveClass;
import com.gingertech.starbeam.helpers.UserData;
import com.gingertech.starbeam.helpers.controllers.ButtonID;
import com.gingertech.starbeam.helpers.controllers.ButtonID_data;
import com.gingertech.starbeam.helpers.controllers.FireFunctions;
import com.gingertech.starbeam.helpers.controllers.MixPanel;
import com.gingertech.starbeam.helpers.controllers.OnGenericCallbackv2;
import com.gingertech.starbeam.helpers.controllers.PremiumController;
import com.gingertech.starbeam.helpers.controllers.mCheckBox;
import com.gingertech.starbeam.helpers.controllers.ColorSliderController;
import com.gingertech.starbeam.helpers.controllers.DrawerController_CreateLayout;
import com.gingertech.starbeam.helpers.controllers.GenericCallbackv2;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class LayoutCreateFragment extends Fragment {

    public static final int maxButtons = 7;

    private String rightScreenKeybind = "";
    private String leftScreenKeybind = "";

    int buttonCount = 0;

    View root;

    GenericCallbackv2 changeFragmentCallback;
    LayoutClass currentLayout;
    ViewGroup buttonContainer;

    ArrayList<ButtonID> controlButtons = new ArrayList<>();

    ButtonID selectedButton = null;

    boolean gyroActivated;
    boolean isTouchTrackpad;
    boolean isMouseClick;

    boolean isGyroMouse;
    boolean isTouchMouse;
    boolean useSplitTouch;
    boolean swapSides;
    boolean invertGyro;

    boolean hapticEnabled;
    boolean joyTouchActivateEnable;
    boolean verticalScrollEnable;

    float gyroSensitivity;
    float touchSensitivity;
    float gyroThreshold;

    float slideSensitivity = 0.5f;
    String newTitle = "";
    String newDescription = "";

    boolean unsavedChanges = false;

    boolean drawerOpen = true;

    InputMethodManager imm;

    Vibrator vibrator;

    EditText editNameText;

    int baseColor;

    boolean shownSnackbar = false;

    ViewGroup openedPopup;

    View premiumPopup;

    View bottomObjects;

    TextView RemainingButtonsText;
    TextView layoutName;

    BitmapDrawable selectedImage;
    int selectedButtonTintMode = 0;
    Color selectedButtonColor = new Color();
    float selectedButtonAlpha = 1;

    boolean helpPopupShown = false;

    mCheckBox showNameCheck;
    View closePopup;

    public static WindowManager.LayoutParams layoutParams;
    public View inflatedView;
    boolean isFocused = false;
    static boolean overlayActivated = false;
    static boolean xVisible = false;

    LinearLayout swipeSenseGroup, joystickTouchDetectGroup, verticalScrollGroup;
    Slider swipeSense;

    LinearLayout KeybindHeader;
    AutoCompleteTextView KeybindEdit;

    AutoCompleteTextView leftTextArea, rightTextArea;

//    B proceedTutorialFlag = 0;

    GenericCallbackv2 onDeleteButton = new GenericCallbackv2() {
        @Override
        public void onChange(Object value) {
            super.onChange(value);

            deactivate_selected_button();
        }
    };


    LayoutCreateFragment(GenericCallbackv2 changeFragmentCallback, LayoutClass layoutClass) {
        this.changeFragmentCallback = changeFragmentCallback;
        this.currentLayout = layoutClass;
    }

    LayoutCreateFragment(GenericCallbackv2 changeFragmentCallback) {
        this.changeFragmentCallback = changeFragmentCallback;
    }

    public LayoutCreateFragment() {
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);


    }

    private void hideKeyboardAndUI() {

        if (imm != null) {
            imm.hideSoftInputFromWindow(root.getWindowToken(), 0);
        }
        requireActivity().getWindow().getDecorView().setSystemUiVisibility(MainActivity.uiFlag);
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void openPremiumPopup() {
        premiumPopup.setVisibility(View.VISIBLE);
    }

    private void updateButtonsCount() {

        String up = getResources().getString(R.string.Remaining_Buttons) + " ∞";

        if (!PremiumController.hasPermission(PremiumController.Product.InfiniteButtons)) {
            up = getResources().getString(R.string.Remaining_Buttons) + " " + (maxButtons - buttonCount);

            if (maxButtons - buttonCount == 0) {
                RemainingButtonsText.setTextColor(Color.RED);
            } else {
                RemainingButtonsText.setTextColor(requireActivity().getColor(R.color.primary));
            }
        }

        RemainingButtonsText.setText(up);
    }

    private void setupViews() {

        bottomObjects = root.findViewById(R.id.bottomOptions);
        RemainingButtonsText = root.findViewById(R.id.RemainingButtonsText);

        mFirebaseAnalytics.logEvent("Layout_Create_Fragment", new Bundle());

        UserData.CurrentFragment = UserData.LAYOUTS_CREATE;

        joystickTouchDetectGroup = root.findViewById(R.id.joystickTouchRecognition);

        verticalScrollGroup = root.findViewById(R.id.verticalScrollGroup);

        mCheckBox verticalScrollCheck = verticalScrollGroup.findViewById(R.id.verticalScrollCheck);
        verticalScrollCheck.setOnClickAction(new OnGenericCallbackv2() {
            @Override
            public void onChange(Object value) {
                if (selectedButton == null) {
                    return;
                }
                verticalScrollEnable = !verticalScrollEnable;
            }

            @Override
            public void onChange(Object value, Object value2) {

            }

            @Override
            public void onChange(Object value, Object value2, Object value3) {

            }
        });
        mCheckBox joystickTouchActivationCheck = joystickTouchDetectGroup.findViewById(R.id.joystickTouchActivationCheck);
        joystickTouchActivationCheck.setOnClickAction(new OnGenericCallbackv2() {
            @Override
            public void onChange(Object value) {
                if (selectedButton == null) {
                    return;
                }
                joyTouchActivateEnable = !joyTouchActivateEnable;
            }

            @Override
            public void onChange(Object value, Object value2) {

            }

            @Override
            public void onChange(Object value, Object value2, Object value3) {

            }
        });


        swipeSenseGroup = root.findViewById(R.id.swipeSenseGroup);
        swipeSense = root.findViewById(R.id.swipeSense);
        swipeSense.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                if (selectedButton == null) {
                    return;
                }

                if (selectedButton.type == ButtonID.Joy || selectedButton.type == ButtonID.Scroll) {
                    slideSensitivity = slider.getValue();
                }

                setUnsavedChanges();

            }
        });

        gyroActivated = currentLayout.gyroActivated;
        gyroSensitivity = currentLayout.gyroSensitivity;
        gyroThreshold = currentLayout.gyroThreshold;
        touchSensitivity = currentLayout.touchSensitivity;
        isTouchTrackpad = currentLayout.isTouchTrackpad;
        isMouseClick = currentLayout.isMouseClickEnabled;
        isGyroMouse = currentLayout.isGyroMouse;
        isTouchMouse = currentLayout.isTouchMouse;

        ((mCheckBox) root.findViewById(R.id.hapticToggle)).setOnClickAction(new OnGenericCallbackv2() {
            @Override
            public void onChange(Object value) {
                if (selectedButton == null) {
                    return;
                }
                hapticEnabled = !hapticEnabled;
            }

            @Override
            public void onChange(Object value, Object value2) {

            }

            @Override
            public void onChange(Object value, Object value2, Object value3) {

            }
        });

        View resetPopup = root.findViewById(R.id.resetPopup);
        resetPopup.findViewById(R.id.yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentLayout.resetDefaults(getContext());
                currentLayout.save(requireContext());
                UserData.setCurrentLayout(requireContext(), currentLayout);

                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction tr = fragmentManager.beginTransaction();
                tr.replace(R.id.nav_host_fragment, new LayoutCreateFragment(changeFragmentCallback, currentLayout)).commitAllowingStateLoss();

                MixPanel.mpButtonTracking(mp, "reset_confirmation");

            }
        });

        resetPopup.findViewById(R.id.no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MixPanel.mpButtonTracking(mp, "reset_close");
                resetPopup.setVisibility(View.GONE);
            }
        });

        closePopup = root.findViewById(R.id.closePopup);
        closePopup.findViewById(R.id.yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MixPanel.mpButtonTracking(mp, "close_confirmation");

                if (changeFragmentCallback != null) {

                    deactivate_selected_button();
                    changeFragmentCallback.onChange(LayoutRootPage.LISTVIEW);
                    buttonContainer.removeAllViews();
                }

                MainActivity.helpController.endHelp();
            }
        });

        closePopup.findViewById(R.id.no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closePopup.setVisibility(View.GONE);
                MixPanel.mpButtonTracking(mp, "close_close");

            }
        });

        root.findViewById(R.id.resetButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MixPanel.mpButtonTracking(mp, "reset_open");

                resetPopup.setVisibility(View.VISIBLE);
            }
        });

        DrawerController_CreateLayout drawer = root.findViewById(R.id.buttonDrawer);

        ImageView drawerToggle = root.findViewById(R.id.buttonDrawerToggle);

        buttonContainer = root.findViewById(R.id.container);

        vibrator = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);

        baseColor = ContextCompat.getColor(requireContext(), R.color.primary);

        showNameCheck = root.findViewById(R.id.showName);

        KeybindEdit = root.findViewById(R.id.setKeyBindEdit);

        ArrayList<String> autoComplete = new ArrayList<>();
        UserData.Commands.forEach((a, b) -> autoComplete.add("\"" + a + "\""));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, autoComplete);

        KeybindEdit.setAdapter(adapter);

        KeybindHeader = root.findViewById(R.id.keybindHeader);

        editNameText = root.findViewById(R.id.setButtonName);


        imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        KeybindEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (selectedButton != null) {
                    root.findViewById(R.id.validID)
                            .setActivated(ButtonHandler.validate(KeybindEdit.getText().toString(), selectedButton.type));

                    if (KeybindEdit.getText().toString().equals("gyro_toggle") && !PremiumController.hasPermission(PremiumController.Product.MotionControls)) {
                        vibrator.vibrate(100);
                        Snackbar r = Snackbar.make(root, R.string.You_need_to_upgrade_to_use_gyroscope, Snackbar.LENGTH_INDEFINITE);
                        r.setAction(R.string.view, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                UserData.selectedPremiumObject = PremiumController.Product.MotionControls;
                                changeFragmentCallback.onChange(-3, UserData.PREMIUM);
                            }
                        });

                        r.show();
                    }
                }
            }
        });


        KeybindEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {

                    if (selectedButton == null) {
                        return true;
                    }

                    int selectedHandleType = selectedButton.type;

                    root.findViewById(R.id.validID)
                            .setActivated(ButtonHandler.validate(KeybindEdit.getText().toString(), selectedHandleType));

                    hideKeyboardAndUI();

                    setUnsavedChanges();
                }

                return true;
            }
        });

        editNameText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {

                    if (selectedButton == null) {
                        return true;
                    }

                    hideKeyboardAndUI();

                    setUnsavedChanges();
                }

                return true;
            }
        });

        boolean[] scaled = {false};
        ScaleListener scaleListener = new ScaleListener();
        scaleListener.onScale = new OnGenericCallbackv2() {
            @Override
            public void onChange(Object value) {
                setUnsavedChanges();

                scaled[0] = true;
            }

            @Override
            public void onChange(Object value, Object value2) {

            }

            @Override
            public void onChange(Object value, Object value2, Object value3) {

            }
        };

        ScaleGestureDetector mScaleGestureDetector = new ScaleGestureDetector(getContext(), scaleListener);


        buttonContainer.setOnTouchListener(new View.OnTouchListener() {

            boolean disselect_override = false;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                mScaleGestureDetector.onTouchEvent(motionEvent);

                if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {

                    if (!disselect_override) {
                        deactivate_selected_button();
                    }

                    disselect_override = false;

                } else if (scaled[0]) {
                    disselect_override = true;
                }
                scaled[0] = false;

                return true;
            }
        });
        showNameCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedButton == null) {
                    return;
                }

                MixPanel.mpButtonTracking(mp, "show_name_toggle");

                selectedButton.buttonNameVisible = showNameCheck.activated;
            }
        });

        drawerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (getContext() == null) {
                    return;
                }
                MixPanel.mpButtonTracking(mp, "button_drawer_toggle");

                drawer.toggleDrawer();
                drawerOpen = !drawerOpen;

                drawerToggle.setImageDrawable(ContextCompat.getDrawable(requireContext(), drawerOpen ? R.drawable.left_icon : R.drawable.plus_icon));

                ObjectAnimator a2 = ObjectAnimator.ofFloat(drawerToggle, "translationX", drawerOpen ? 0 : -drawer.getWidth());
                a2.setDuration(500);
                a2.start();

                vibrator.vibrate(10);
            }
        });

        drawer.setCallback(new GenericCallbackv2() {
            @Override
            public void onChange(Object value) {

                drawerOpen = false;
                drawerToggle.setImageDrawable(ContextCompat.getDrawable(requireContext(), drawerOpen ? R.drawable.left_icon : R.drawable.plus_icon));


                ObjectAnimator a2 = ObjectAnimator.ofFloat(drawerToggle, "translationX", drawerOpen ? 0 : -drawer.getWidth());
                a2.setDuration(500);
                a2.start();

                if (controlButtons.size() + 1 >= maxButtons && !PremiumController.hasPermission(PremiumController.Product.InfiniteButtons)) {

                    Bundle b = new Bundle();
                    b.putString("title", "6_buttons_requirement");
                    mFirebaseAnalytics.logEvent("Message", b);

                    vibrator.vibrate(500);
                    Snackbar r = Snackbar.make(root, "For more buttons you must upgrade", Snackbar.LENGTH_INDEFINITE);
                    r.setAction(R.string.view, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            UserData.selectedPremiumObject = PremiumController.Product.InfiniteButtons;

                            changeFragmentCallback.onChange(-3, UserData.PREMIUM);
                        }
                    });

                    r.show();
                    return;
                }

                addNewButton((int) value, requireContext());
            }
        });


        if(!currentLayout.buttons.isEmpty()) {
        root.postDelayed(new Runnable() {
            @Override
            public void run() {
                    drawerToggle.callOnClick();
            }
        }, 100);
        }

        root.findViewById(R.id.exitKeybindButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MixPanel.mpButtonTracking(mp, "keybind_exit");

                imm.hideSoftInputFromWindow(root.getWindowToken(), 0);
                exit();
            }
        });

        root.findViewById(R.id.applyKeybindButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MixPanel.mpButtonTracking(mp, "keybind_apply");

                apply();
            }
        });


        root.findViewById(R.id.exitInfoButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MixPanel.mpButtonTracking(mp, "info_exit");


                exit();
            }
        });

        root.findViewById(R.id.applyTextureButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MixPanel.mpButtonTracking(mp, "texture_apply");

                apply();
            }
        });


        root.findViewById(R.id.exitTextureButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MixPanel.mpButtonTracking(mp, "texture_edit");

                exit();
            }
        });

        root.findViewById(R.id.sampleImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MixPanel.mpButtonTracking(mp, "image_edit");

                pickImage();

                ImageView v = (ImageView) view;
                v.setAlpha(255 * selectedButtonAlpha / 10);

                if (selectedButtonTintMode == 0) {
                    v.setImageTintMode(PorterDuff.Mode.MULTIPLY);
                    v.setImageTintList(ColorStateList.valueOf(selectedButton.color));

                    ((TextView) root.findViewById(R.id.screenButton)).setTextColor(Color.BLACK);
                    ((TextView) root.findViewById(R.id.multiplyButton)).setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary));
                } else {
                    v.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                    v.setColorFilter(selectedButton.color);

                    ((TextView) root.findViewById(R.id.multiplyButton)).setTextColor(Color.BLACK);
                    ((TextView) root.findViewById(R.id.screenButton)).setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary));
                }
            }
        });

        root.findViewById(R.id.tintToggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView v = root.findViewById(R.id.sampleImageView);
                MixPanel.mpButtonTracking(mp, "tint_toggle");

                if (((SwitchCompat) view).isChecked()) {
                    //Is Screen Tint Mode
                    selectedButtonTintMode = 1;

                    v.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                    v.setColorFilter(selectedButtonColor.toArgb());
                } else {
                    //Is not Screen tint mode
                    selectedButtonTintMode = 0;

                    v.clearColorFilter();
                    v.setImageTintMode(PorterDuff.Mode.MULTIPLY);
                    v.setImageTintList(ColorStateList.valueOf(selectedButtonColor.toArgb()));
                }
            }
        });

        root.findViewById(R.id.resetTextureButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MixPanel.mpButtonTracking(mp, "texture_reset");


                if(selectedButton != null) {
                    selectedButton.resetImage(requireContext());
                    selectedButton.resetImage(requireContext());
                }

                exit();
            }
        });

        setupTopButtons();
    }

    private MixpanelAPI mp;
    DisplayMetrics displayMetrics;
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.layout_creation_view, container, false);
        displayMetrics = new DisplayMetrics();

        mp = MixPanel.makeObj(requireContext());
        MixPanel.mpEventTracking(mp, "Layout_Create_Fragment_opened", null);

        pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        try {
                            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                            Bitmap i = BitmapFactory.decodeStream(inputStream);
                            BitmapDrawable image = new BitmapDrawable(i);

                            selectedImage = image;

                            ImageView v = root.findViewById(R.id.sampleImageView);
                            v.setImageBitmap(image.getBitmap());

                            if (selectedButtonTintMode == 0) {
                                v.setImageTintMode(PorterDuff.Mode.MULTIPLY);
                                v.setImageTintList(ColorStateList.valueOf(selectedButtonColor.toArgb()));
                            } else {
                                v.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                                v.setColorFilter(selectedButtonColor.toArgb());
                            }

                            ((ImageView) root.findViewById(R.id.sampleImageView)).setImageTintList(ColorStateList.valueOf(selectedButton.color));

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        MixPanel.mpEventTracking(mp, "image_return", null);
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });

        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.widthPixels;
        width = displayMetrics.heightPixels * displayMetrics.widthPixels;

        layoutParams = new WindowManager.LayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;

        layoutParams.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;

        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        layoutParams.format = PixelFormat.TRANSPARENT;
        layoutParams.gravity = Gravity.TOP;

        if (currentLayout == null) {

            ArraySet<String> layoutNames = new ArraySet<>();
            for (LayoutClass layouts : UserData.Layouts) {
                layoutNames.add(layouts.name);
            }

            String newLayoutName = getResources().getString(R.string.tutorial) + " ";
            String newLayoutDescription = getResources().getString(R.string.tutorial_controller);
            int layoutNameindex = 0;

            while (layoutNames.contains((newLayoutName + layoutNameindex))) {
                layoutNameindex++;
            }

            newLayoutName += layoutNameindex;
            newLayoutDescription += layoutNameindex;
            currentLayout = new LayoutClass(newLayoutName, newLayoutDescription, requireContext());
            currentLayout.makeDefaultLayout(requireContext());

            UserData.Layouts.add(currentLayout);
            currentLayout.save(requireContext());
            UserData.setCurrentLayout(requireContext(), currentLayout);
        }

        if (changeFragmentCallback != null) {
            changeFragmentCallback.onChange(currentLayout.name);
        }

        int numOfViews = currentLayout.buttons.size();
        final int[] indexViews = {0};
        final int viewsPerBatch = 1;
        Runnable r = new Runnable() {
            public void run() {

                //Dont update anything if the fragment has been detached
                if (getActivity() == null) {
                    return;
                }

                if (LayoutCreateFragment.this.currentLayout.buttons.size() > 0) {

                    for (int i = 0; i < viewsPerBatch; i++) {
                        if (i + indexViews[0] == numOfViews) {
                            View loadingView = root.findViewById(R.id.layoutLoading);
                            if(loadingView != null) {
                                loadingView.setVisibility(View.GONE);
                            }
                            return;
                        }

                        ButtonID button = new ButtonID(requireContext());


                        button.setData(requireContext(), LayoutCreateFragment.this.currentLayout.buttons.get(indexViews[0] + i));


                        button.setOnTouchListener(new TouchListen(button));
                        controlButtons.add(button);
                        buttonContainer.addView(button);
                    }

                    indexViews[0] += viewsPerBatch;
                    root.post(this);
                } else {

                    root.findViewById(R.id.layoutLoading).setVisibility(View.GONE);

                }

                buttonCount = currentLayout.buttons.size();
                updateButtonsCount();
            }
        };

        setupViews();

        root.postDelayed(r, 1000);

        root.postDelayed(new Runnable() {
            @Override
            public void run() {
//                if (getActivity() != null && !helpPopupShown && UserData.openCount < 3) {
//                    View v = getActivity().findViewById(R.id.bottomOptions);
//                    if(v != null) {
//                        helpPopupShown = true;
//                        Snackbar r = Snackbar.make(v, UserData.openCount == 0 ? "First Time? Get Help!" : "Need Help?", Snackbar.LENGTH_INDEFINITE);
//                        r.setAction(UserData.openCount == 0 ? "OK" : "Yes!", new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                MixPanel.mpEventTracking(mp, "Create_Video_Opened!", null);
//
//                                if (getActivity() != null) {
//                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/166qRqzTuIE")));
//                                }
//                            }
//                        });
//
//                        r.show();
//                    }
//                }

                if(UserData.timesConnected < 1) {
                    ObjectAnimator a = ObjectAnimator.ofFloat(root.findViewById(R.id.launch_button), "alpha", 0.5f);

                    a.setDuration(500);
                    a.setRepeatCount(ValueAnimator.INFINITE);
                    a.setRepeatMode(ValueAnimator.REVERSE);
                    a.start();
                }

            }
        }, 1000);

        return root;

    }

    int PICK_PHOTO_FOR_BUTTON = 3456789;
    int height;
    int width;

    public void UpdateOverlay() {

        final ViewGroup c = root.findViewById(R.id.back);
        final Handler handler = new Handler();

        inflatedView = View.inflate(getContext(), R.layout.keyboardtoggle, c);

        inflatedView.findViewById(R.id.LL).setOnTouchListener(new View.OnTouchListener() {

            final int[] values = {-width, 0, width};
            int current = 1;

            int xposB;
            int yposB;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {

                    handler.postDelayed(exitLongPress, 2500);

                    xposB = Math.round(event.getX());
                    yposB = Math.round(event.getY());
                }

                if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                    handler.removeCallbacks(exitLongPress);

                    if (Math.abs(event.getX() - xposB) > 150) {

                        if (event.getX() - xposB > 0) {

                            if (current != 2) {
                                current += 1;
                                layoutParams.x = values[current];
                            }

                        } else {

                            if (current != 0) {
                                current -= 1;
                                layoutParams.x = values[current];
                            }

                        }

                    } else {

                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                        if (isFocused) {

                            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                            MainActivity.WM.updateViewLayout(inflatedView, layoutParams);
                        } else {

                            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                            MainActivity.WM.updateViewLayout(inflatedView, layoutParams);
                        }

                        isFocused = !isFocused;
                    }
                    MainActivity.WM.updateViewLayout(inflatedView, layoutParams);
//                    MainActivity.vibrate(5);

                }
                return false;
            }

            final Runnable exitLongPress = new Runnable() {
                public void run() {
//                    MainActivity.vibrate(50);
                    xVisible = false;

                    imm.showInputMethodPicker();
                    imm.hideSoftInputFromWindow(root.getWindowToken(), 0);
                    layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                    MainActivity.WM.updateViewLayout(inflatedView, layoutParams);
                    MainActivity.WM.removeView(inflatedView);
                    overlayActivated = false;
                    mFirebaseAnalytics.logEvent("exited_normal_overlay", new Bundle());


//                    MainActivity.vibrate(5);
                }
            };
        });

        inflatedView.findViewById(R.id.back).setBackgroundColor(Color.TRANSPARENT);
    }


    public void pickImage() {

        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    boolean processingFlag = false;

    void setupTopButtons() {

        gyroActivated = currentLayout.gyroActivated;
        useSplitTouch = currentLayout.isSplit;
        swapSides = currentLayout.isSplitSwapped;
        invertGyro = currentLayout.invertGyro;

        View SettingPopup = root.findViewById(R.id.settingPopup);

        ((mCheckBox) SettingPopup.findViewById(R.id.swapTouchscreenControlsToggle)).setActivated(swapSides);
        ((mCheckBox) SettingPopup.findViewById(R.id.useSplitTouch)).setActivated(useSplitTouch);

        SettingPopup.findViewById(R.id.gyro).setActivated(currentLayout.gyroActivated && PremiumController.hasPermission(PremiumController.Product.MotionControls));
        SettingPopup.findViewById(R.id.invertGyro).setActivated(invertGyro);

        ((mCheckBox) SettingPopup.findViewById(R.id.gyro)).clickable = PremiumController.hasPermission(PremiumController.Product.MotionControls);
        SettingPopup.findViewById(R.id.swapTouchscreenControls).setVisibility(useSplitTouch ? View.VISIBLE : View.GONE);

        rightScreenKeybind = currentLayout.rightTrackpadKeybind;
        leftScreenKeybind = currentLayout.leftTrackpadKeybind;

        ((TextView) root.findViewById(R.id.leftSide)).setText(leftScreenKeybind);
        ((TextView) root.findViewById(R.id.rightSide)).setText(rightScreenKeybind);

        leftTextArea = root.findViewById(R.id.leftTextArea);
        rightTextArea = root.findViewById(R.id.rightTextArea);

        leftTextArea.setText(leftScreenKeybind);
        rightTextArea.setText(rightScreenKeybind);

        ArrayList<String> autoComplete = new ArrayList<>();
        autoComplete.addAll(Arrays.asList(ButtonHandler.keyCodesForMoveables));
        autoComplete.addAll(Arrays.asList(ButtonHandler.keyCodesForNormal));
        autoComplete.remove("mouse_wheel");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, autoComplete);

        leftTextArea.setAdapter(adapter);
        rightTextArea.setAdapter(adapter);

        SettingPopup.findViewById(R.id.ShareControllerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MixPanel.mpButtonTracking(mp, "shared_layout_click");

                vibrator.vibrate(10);

                if(currentLayout.layoutUUID != null && !currentLayout.layoutUUID.equals("")) {
                    ((TextView) SettingPopup.findViewById(R.id.LayoutIDView)).setText(currentLayout.layoutUUID);

                } else {

                    root.findViewById(R.id.save_button).callOnClick();

                    Toast.makeText(requireContext(), R.string.Generating_Sharable_Layout_ID, Toast.LENGTH_SHORT).show();
                    FireFunctions.saveController(requireContext(), currentLayout).addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {

                            if(task.isSuccessful()) {

                                vibrator.vibrate(10);

                                currentLayout.layoutUUID = task.getResult();

                                HashMap<String, String> data = new HashMap<>();
                                data.put("UUID", currentLayout.layoutUUID);
                                data.put("layout_name", currentLayout.name);

                                MixPanel.mpEventTracking(mp, "shared_layout_complete", data);

                                Context context = getContext();

                                if(context != null) {
                                    currentLayout.save(requireContext());

                                    Toast.makeText(context, R.string.Share_this_code_to_share_your, Toast.LENGTH_SHORT).show();
                                    ((TextView) SettingPopup.findViewById(R.id.LayoutIDView)).setText(currentLayout.layoutUUID);

                                }

                            } else {
                                MixPanel.mpEventTracking(mp, "shared_layout_failed", null);

                                vibrator.vibrate(100);
                                Toast.makeText(requireContext(), R.string.Error_Creating_Layout_ID, Toast.LENGTH_LONG).show();

                            }

                        }
                    });
                }
            }
        });

        ((mCheckBox) SettingPopup.findViewById(R.id.gyro)).setOnClickAction(new OnGenericCallbackv2() {

            @Override
            public void onChange(Object value) {
                if (!PremiumController.hasPermission(PremiumController.Product.MotionControls)) {
                    vibrator.vibrate(100);
                    Snackbar r = Snackbar.make(root, R.string.You_need_to_upgrade_to_use_gyroscope, Snackbar.LENGTH_INDEFINITE);
                    r.setAction("View", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            UserData.selectedPremiumObject = PremiumController.Product.MotionControls;

                            changeFragmentCallback.onChange(-3, UserData.PREMIUM);
                        }
                    });

                    r.show();

                    return;
                }

                gyroActivated = ((mCheckBox) SettingPopup.findViewById(R.id.gyro)).activated;
                MixPanel.mpEventTracking(mp, gyroActivated ? "Use_Gyro" : "de-Use_Gyro", null);


                setUnsavedChanges();
            }

            @Override
            public void onChange(Object value, Object value2) {

            }

            @Override
            public void onChange(Object value, Object value2, Object value3) {

            }
        });

        ((Slider) SettingPopup.findViewById(R.id.touchSense)).setValue(currentLayout.touchSensitivity);
        SettingPopup.findViewById(R.id.touchSense).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    touchSensitivity = ((Slider) SettingPopup.findViewById(R.id.touchSense)).getValue();
                    setUnsavedChanges();
                }
                return false;
            }
        });


        SettingPopup.findViewById(R.id.gyroSense).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    gyroSensitivity = ((Slider) SettingPopup.findViewById(R.id.gyroSense)).getValue();
                    setUnsavedChanges();
                }
                return false;
            }
        });

        SettingPopup.findViewById(R.id.gyroThresh).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    gyroThreshold = ((Slider) SettingPopup.findViewById(R.id.gyroThresh)).getValue();
                    setUnsavedChanges();
                }
                return false;
            }
        });

        ((mCheckBox) SettingPopup.findViewById(R.id.gyroMouse)).setOnClickAction(new OnGenericCallbackv2() {

            @Override
            public void onChange(Object value) {
                if (!PremiumController.hasPermission(PremiumController.Product.MotionControls)) {
                    vibrator.vibrate(100);
                    Snackbar r = Snackbar.make(root, R.string.You_need_to_upgrade_to_use_gyroscope, Snackbar.LENGTH_INDEFINITE);
                    r.setAction("View", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            UserData.selectedPremiumObject = PremiumController.Product.MotionControls;

                            changeFragmentCallback.onChange(-3, UserData.PREMIUM);
                        }
                    });

                    r.show();

                    return;
                }

                isGyroMouse = ((mCheckBox) SettingPopup.findViewById(R.id.gyroMouse)).activated;
                setUnsavedChanges();

                MixPanel.mpEventTracking(mp, isGyroMouse ? "Activated_Gyro_Mouse" : "de-Activated_Gyro_Mouse", null);

            }

            @Override
            public void onChange(Object value, Object value2) {

            }

            @Override
            public void onChange(Object value, Object value2, Object value3) {

            }
        });
        SettingPopup.findViewById(R.id.gyroMouse).setActivated(currentLayout.isGyroMouse);


        SettingPopup.findViewById(R.id.touchMouse).setActivated(currentLayout.isTouchMouse);


        ((mCheckBox) SettingPopup.findViewById(R.id.mouseClick)).setOnClickAction(new OnGenericCallbackv2() {

            @Override
            public void onChange(Object value) {
                isMouseClick = ((mCheckBox) SettingPopup.findViewById(R.id.mouseClick)).activated;
                setUnsavedChanges();
            }

            @Override
            public void onChange(Object value, Object value2) {

            }

            @Override
            public void onChange(Object value, Object value2, Object value3) {

            }
        });

        ((mCheckBox) SettingPopup.findViewById(R.id.invertGyro)).setOnClickAction(new OnGenericCallbackv2() {

            @Override
            public void onChange(Object value) {
                invertGyro = ((mCheckBox) SettingPopup.findViewById(R.id.invertGyro)).activated;
                setUnsavedChanges();
            }

            @Override
            public void onChange(Object value, Object value2) {

            }

            @Override
            public void onChange(Object value, Object value2, Object value3) {

            }
        });


        SettingPopup.findViewById(R.id.mouseClick).setActivated(currentLayout.isMouseClickEnabled);

        ((mCheckBox) SettingPopup.findViewById(R.id.mouseTrackpad)).setOnClickAction(new OnGenericCallbackv2() {

            @Override
            public void onChange(Object value) {
                isTouchTrackpad = ((mCheckBox) SettingPopup.findViewById(R.id.mouseTrackpad)).activated;
                setUnsavedChanges();
            }

            @Override
            public void onChange(Object value, Object value2) {

            }

            @Override
            public void onChange(Object value, Object value2, Object value3) {

            }
        });


        SettingPopup.findViewById(R.id.mouseTrackpad).setActivated(currentLayout.isTouchTrackpad);

        ((EditText) SettingPopup.findViewById(R.id.title)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {

                    hideKeyboardAndUI();

                    if(textView.getText().length() < 1) {
                        Toast.makeText(requireContext(), R.string.Please_add_a_layout_title, Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    newTitle = textView.getText().toString();

                    setUnsavedChanges();

                }

                return true;
            }
        });

        ((EditText) SettingPopup.findViewById(R.id.description)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {

                    hideKeyboardAndUI();

                    if(textView.getText().length() < 1) {
                        Toast.makeText(requireContext(), R.string.Please_add_a_layout_description, Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    newDescription = textView.getText().toString();
                    setUnsavedChanges();
                }

                return true;
            }
        });

        SettingPopup.findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                rightScreenKeybind = rightTextArea.getText().toString();
                leftScreenKeybind = leftTextArea.getText().toString();

                ((TextView) root.findViewById(R.id.leftSide)).setText(leftScreenKeybind);
                ((TextView) root.findViewById(R.id.rightSide)).setText(rightScreenKeybind);

                SettingPopup.setVisibility(View.GONE);
            }
        });

        root.findViewById(R.id.optionsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EditText) SettingPopup.findViewById(R.id.title)).setText(currentLayout.name);
                ((EditText) SettingPopup.findViewById(R.id.description)).setText(currentLayout.description);

                SettingPopup.findViewById(R.id.gyro).setActivated(gyroActivated && PremiumController.hasPermission(PremiumController.Product.MotionControls));
                SettingPopup.findViewById(R.id.gyroStuff).setVisibility(PremiumController.hasPermission(PremiumController.Product.MotionControls) ? View.VISIBLE : View.GONE);

                ((Slider) SettingPopup.findViewById(R.id.gyroSense)).setValue(gyroSensitivity);
                ((Slider) SettingPopup.findViewById(R.id.touchSense)).setValue(touchSensitivity);
                ((Slider) SettingPopup.findViewById(R.id.gyroThresh)).setValue(gyroThreshold);

                SettingPopup.setVisibility(View.VISIBLE);
            }
        });

        root.findViewById(R.id.close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(unsavedChanges) {
                    closePopup.setVisibility(View.VISIBLE);

                } else {

                    MixPanel.mpButtonTracking(mp, "close_create_fragment");

                    Bundle b = new Bundle();
                    b.putString("name", "Close_Button");
                    mFirebaseAnalytics.logEvent("Create_Button_Click", b);

                    if (changeFragmentCallback != null) {

                        deactivate_selected_button();
                        changeFragmentCallback.onChange(LayoutRootPage.LISTVIEW);
                        buttonContainer.removeAllViews();
                    }

                    MainActivity.helpController.endHelp();
                }

                vibrator.vibrate(10);

            }
        });

        root.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                unsavedChanges = false;

                vibrator.vibrate(50);

                    MixPanel.mpButtonTracking(mp, "save_button");

                    if (processingFlag) {
                        return;
                    }

                    processingFlag = true;
                    root.findViewById(R.id.layoutLoading).setVisibility(View.VISIBLE);

                    Bundle b = new Bundle();
                    b.putString("name", "Save_Button");
                    mFirebaseAnalytics.logEvent("Create_Button_Click", b);

                    deactivate_selected_button();

                    ArrayList<ButtonID_data> buttonData = new ArrayList<>();
                    for (ButtonID buttonID : controlButtons) {
                        ButtonID_data bData = buttonID.getData(requireContext());
                        buttonData.add(bData);
                    }

                    currentLayout.buttons = buttonData;
                    currentLayout.gyroActivated = gyroActivated;
                    currentLayout.gyroSensitivity = gyroSensitivity;
                    currentLayout.gyroThreshold = gyroThreshold;

                    currentLayout.touchSensitivity = touchSensitivity;
                    currentLayout.isTouchTrackpad = isTouchTrackpad;
                    currentLayout.isGyroMouse = isGyroMouse;
                    currentLayout.isTouchMouse = isTouchMouse;
                    currentLayout.isMouseClickEnabled = isMouseClick;

                    currentLayout.isSplit = useSplitTouch;
                    currentLayout.isSplitSwapped = swapSides;
                    currentLayout.invertGyro = invertGyro;

                    currentLayout.rightTrackpadKeybind = rightScreenKeybind;
                    currentLayout.leftTrackpadKeybind = leftScreenKeybind;

                    Log.i("get", rightScreenKeybind);

                    if (!newTitle.equals("")) {
                        currentLayout.name = newTitle;
                    }
                    if (!newDescription.equals("")) {
                        currentLayout.description = newDescription;
                    }

                    changeFragmentCallback.onChange(newTitle);

                    currentLayout.save(requireContext());

//                    Toast.makeText(requireContext(), R.string.Saved_Layout, Toast.LENGTH_LONG).show();

                    UserData.layoutsEdited += 1;
                    SaveClass.SaveFlags(requireContext());

                    root.findViewById(R.id.layoutLoading).setVisibility(View.GONE);

                    processingFlag = false;
                }

        });

        root.findViewById(R.id.test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MixPanel.mpButtonTracking(mp, "test_button");


                Bundle b = new Bundle();
                b.putString("name", "test_button");
                mFirebaseAnalytics.logEvent("Create_Button_Click", b);

                if (changeFragmentCallback != null) {

                    deactivate_selected_button();

                    ArrayList<ButtonID_data> buttonData = new ArrayList<>();
                    for(ButtonID buttonID : controlButtons) {
                        ButtonID_data bData = buttonID.getData(requireContext());
                        buttonData.add(bData);
                    }

                    LayoutClass testLayout = new LayoutClass(currentLayout.name, requireContext());
                    testLayout.buttons = buttonData;
                    UserData.testLayout = testLayout;

                    changeFragmentCallback.onChange(LayoutRootPage.TESTVIEW, testLayout);
                    buttonContainer.removeAllViews();
                }

                MainActivity.helpController.endHelp();
            }
        });

        root.findViewById(R.id.test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        if(UserData.timesConnected < 1) {
            ObjectAnimator a = ObjectAnimator.ofFloat(root.findViewById(R.id.launch_button), "translationY", 16);

//            a.setDuration(250);
//            a.setRepeatCount(ValueAnimator.INFINITE);
//            a.setRepeatMode(ValueAnimator.REVERSE);
//            a.start();
        }

        root.findViewById(R.id.launch_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MixPanel.mpButtonTracking(mp, "launch_button");

                if(processingFlag) { return; }
                root.findViewById(R.id.save_button).performClick();

                root.findViewById(R.id.layoutLoading).setVisibility(View.VISIBLE);

                processingFlag = true;

                vibrator.vibrate(10);

                Bundle b = new Bundle();
                b.putString("name", "launch_button");
                mFirebaseAnalytics.logEvent("Create_Button_Click", b);

                UserData.layoutsEdited += 1;
                SaveClass.SaveFlags(requireContext());

                MainActivity.helpController.endHelp();
                root.findViewById(R.id.layoutLoading).setVisibility(View.GONE);

                processingFlag = false;

                if(UserData.isNativeMode) {
                    if (changeFragmentCallback != null) {
                        changeFragmentCallback.onChange(LayoutRootPage.LAUNCHVIEW);
                    }
                } else {

                    String list = imm.getEnabledInputMethodList().toString();

                    if (!Settings.canDrawOverlays(getContext())) {
                        MixPanel.mpEventTracking(getContext(), "ACTION_MANAGE_OVERLAY_PERMISSION", null);

                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + requireContext().getPackageName()));
                        startActivityForResult(intent, 616);


                    } else if (!list.contains(".keyboardController")) {

                        MixPanel.mpEventTracking(getContext(), "ACTION_INPUT_METHOD_SETTINGS", null);

                        Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
                        startActivityForResult(intent, 161);
                    } else {

                        if (!overlayActivated) {

                            MixPanel.mpEventTracking(getContext(), "Controller_Overlay_Launched", null);

                            overlayActivated = true;
//                            deactivate_selected_button();

                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    imm.showInputMethodPicker();

                                }
                            }, 250);


                            Toast.makeText(requireContext(), getResources().getText(R.string.summon_overlay_snack), Toast.LENGTH_LONG).show();
                            mFirebaseAnalytics.logEvent("created_basic_overlay", new Bundle());

//                            keybindButton.setClickable(false);

                            UpdateOverlay();
                            MainActivity.WM.addView(inflatedView, layoutParams);
//
                            vibrator.vibrate(5);

                            requireActivity().finishAffinity();

                        }
                    }
                }
            }
        });

        root.findViewById(R.id.trashButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedButton != null) {

                    root.findViewById(R.id.deleteButtonPopup).setVisibility(View.VISIBLE);
                }
            }
        });

        root.findViewById(R.id.deleteButtonPopup).findViewById(R.id.yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle b = new Bundle();
                b.putString("name", "trash_button");
                mFirebaseAnalytics.logEvent("Create_Button_Click", b);
                root.findViewById(R.id.deleteButtonPopup).setVisibility(View.GONE);

                MixPanel.mpButtonTracking(mp, "trash_button");

                HashMap<String, String> addData = new HashMap<>();
                addData.put("total_buttons_active", Integer.toString(controlButtons.size()));

                MixPanel.mpEventTracking(mp, "deleted_button", addData);

                if(selectedButton != null) {
                    selectedButton.remove();
                    selectedButton = null;

                    root.findViewById(R.id.trashButton).setAlpha(0.3f);

                    setUnsavedChanges();
                }

                vibrator.vibrate(10);

                ObjectAnimator a3 = ObjectAnimator.ofFloat(bottomObjects, "translationY", selectedButton == null ? 0 : -bottomObjects.getHeight());
                a3.setDuration(250);
                a3.start();

                buttonCount -= 1;
                updateButtonsCount();
            }
        });

        root.findViewById(R.id.deleteButtonPopup).findViewById(R.id.no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                root.findViewById(R.id.deleteButtonPopup).setVisibility(View.GONE);
            }
        });

        root.findViewById(R.id.keybindButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle b = new Bundle();
                b.putString("name", "keybind_button");
                mFirebaseAnalytics.logEvent("Create_Button_Click", b);

                MixPanel.mpButtonTracking(mp, "keybind_open");

                if(selectedButton == null) {
                    vibrator.vibrate(30);
                    return;
                }

                if(openedPopup == null) {
                    openedPopup = root.findViewById(R.id.keybindPopup);
                    openedPopup.setVisibility(View.VISIBLE);
                }

                editNameText.setText(selectedButton.buttonName);

                slideSensitivity = selectedButton.sensitivity;
                hapticEnabled = selectedButton.isHapticFeebackEnabled;
                root.findViewById(R.id.hapticToggle).setActivated(hapticEnabled);


                swipeSense.setValue(slideSensitivity);

                if(selectedButton.type == ButtonID.Scroll || selectedButton.type == ButtonID.Joy) {
                    swipeSenseGroup.setVisibility(View.VISIBLE);
                } else {
                    swipeSenseGroup.setVisibility(View.GONE);
                }

                if(selectedButton.type == ButtonID.Joy) {
                    joystickTouchDetectGroup.setVisibility(View.VISIBLE);
                    joyTouchActivateEnable = selectedButton.joystickTouchActivate;

                    ((mCheckBox) joystickTouchDetectGroup.findViewById(R.id.joystickTouchActivationCheck))
                            .setActivated(joyTouchActivateEnable);
                }

                if(selectedButton.type == ButtonID.Scroll) {
                    verticalScrollGroup.setVisibility(View.VISIBLE);
                    verticalScrollEnable = selectedButton.verticalScroll;

                    ((mCheckBox) verticalScrollGroup.findViewById(R.id.verticalScrollCheck))
                            .setActivated(verticalScrollEnable);
                }

                KeybindEdit.setText(selectedButton.keybind);
                root.findViewById(R.id.validID)
                        .setActivated(ButtonHandler.validate(KeybindEdit.getText().toString(), selectedButton.type));

                vibrator.vibrate(10);
            }
        });


        root.findViewById(R.id.textureButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MixPanel.mpButtonTracking(mp, "texture_open");

//                if(!UserData.isPremiumMode) {
//                    showPremiumPopup("Button texture customization requires premium");
//                    return;
//                }

                isStoragePermissionGranted();
                isReadingPermissionGranted();

                Bundle b = new Bundle();
                b.putString("name", "image_button");
                mFirebaseAnalytics.logEvent("Create_Button_Click", b);

                if(selectedButton == null) {
                    vibrator.vibrate(30);
                    return;
                }

                if(openedPopup == null) {
                    openedPopup = root.findViewById(R.id.imagePopup);
                    openedPopup.setVisibility(View.VISIBLE);
                }

                ImageView imageView = root.findViewById(R.id.sampleImageView);

                imageView.setImageDrawable(selectedButton.imageView.getDrawable().mutate().getConstantState().newDrawable());

                if ( selectedButton.tintMode == 0) {
                    imageView.setImageTintMode(PorterDuff.Mode.MULTIPLY);
                    imageView.setImageTintList(ColorStateList.valueOf(selectedButton.color));
                } else {
                    imageView.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                    imageView.setColorFilter(selectedButton.color);
                }

                ((SwitchCompat) root.findViewById(R.id.tintToggle)).setChecked(selectedButton.tintMode == 1);
                selectedButtonTintMode = selectedButton.tintMode;

                if(selectedButton.buttonImage != null) {
                    selectedImage = new BitmapDrawable(selectedButton.buttonImage);
                }
                selectedButtonColor = Color.valueOf(selectedButton.color);
                selectedButtonAlpha = selectedButton.alpha / 10f;

                imageView.setImageAlpha((int) (255 * selectedButtonAlpha));

                ColorSliderController colorSliderController = root.findViewById(R.id.color_selector);
                colorSliderController.setColor(Color.valueOf(selectedButton.color), selectedButtonAlpha);
                ((ColorSliderController) root.findViewById(R.id.color_selector)).setOnColorChangeListener(new GenericCallbackv2() {

                    @Override
                    public void onChange(Object value) {
                    }

                    @Override
                    public void onChange(Object value, Object value2) {
                        selectedButtonColor = (Color) value;
                        selectedButtonAlpha = (float) value2;

                        if ( selectedButtonTintMode == 0) {

                            imageView.setImageTintMode(PorterDuff.Mode.MULTIPLY);
                            imageView.setImageTintList(ColorStateList.valueOf(selectedButtonColor.toArgb()));
                            imageView.setImageAlpha((int) (selectedButtonAlpha * 255));

                        } else {

                            imageView.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                            imageView.setColorFilter(selectedButtonColor.toArgb());
                            imageView.setImageAlpha((int) (selectedButtonAlpha * 255));

                        }

                    }

                    @Override
                    public void onChange(Object value, Object value2, Object value3) {

                    }
                });



                vibrator.vibrate(10);
            }
        });

        root.findViewById(R.id.infoButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MixPanel.mpButtonTracking(mp, "info_open");


                if(selectedButton == null) {
                    vibrator.vibrate(30);
                    return;
                }

                if(openedPopup == null) {
                    openedPopup = root.findViewById(R.id.infoPopup);
                    openedPopup.setVisibility(View.VISIBLE);
                }

                TextView buttonDescriptionTitle = root.findViewById(R.id.infoTitle);
                TextView firstDescript = root.findViewById(R.id.infoText);
                TextView secondDescript = root.findViewById(R.id.infoModif);

                String s = "", s1 = "", s2 = "";

                if (selectedButton.type == ButtonID.Normal) {
                    s = getResources().getString(R.string.button);
                    s1 = getResources().getString(R.string.button_discript);
                    s2 = getResources().getString(R.string.button_mod);

                } else if (selectedButton.type == ButtonID.Sticky) {

                    s = getResources().getString(R.string.stickybutton);
                    s1 = getResources().getString(R.string.stickybutton_discript);
                    s2 = getResources().getString(R.string.stickybutton_mod);

                } else if (selectedButton.type == ButtonID.Cycle) {

                    s = getResources().getString(R.string.cyclebutton);
                    s1 = getResources().getString(R.string.cyclebutton_discript);
                    s2 = getResources().getString(R.string.cyclebutton_mod);

                } else if (selectedButton.type == ButtonID.Joy) {

                    s = getResources().getString(R.string.joybutton);
                    s1 = getResources().getString(R.string.joybutton_discript);
                    s2 = getResources().getString(R.string.joybutton_mod);


                } else if (selectedButton.type == ButtonID.Scroll) {

                    s = getResources().getString(R.string.scrollbutton);
                    s1 = getResources().getString(R.string.scrollbutton_discript);
                    s2 = getResources().getString(R.string.scrollbutton_mod);

                }

                String sss = getResources().getString(R.string.Description);
                buttonDescriptionTitle.setText(sss + s);
                firstDescript.setText(s1);
                secondDescript.setText(s2);

                vibrator.vibrate(10);
            }
        });
    }

    void addNewButton(int type, Context context) {

        ButtonID newButton = new ButtonID(context);
        newButton.addNewButton(type, context);
        newButton.setPosition(500, 500);

        setUnsavedChanges();

        buttonContainer.addView(newButton);

        activate_selected_button(newButton);

        newButton.setOnTouchListener(new TouchListen(newButton));

        HashMap<String, String> addData = new HashMap<>();

        addData.put("button_type", Integer.toString(newButton.type));
        addData.put("total_buttons_active", Integer.toString(controlButtons.size()));

        MixPanel.mpEventTracking(mp, "added_new_button", addData);

        newButton.setTintMode(0);

        buttonCount += 1;
        updateButtonsCount();

        vibrator.vibrate(10);
    }

    public void deactivate_selected_button() {

        if (selectedButton != null) {

            root.findViewById(R.id.trashButton).setAlpha(0.3f);

            selectedButton.updateTransformations();

            controlButtons.add(selectedButton);

            a1.cancel();
            a2.cancel();

            selectedButton.setScaleX(1);
            selectedButton.setScaleY(1);

            selectedButton = null;

            ObjectAnimator a3 = ObjectAnimator.ofFloat(bottomObjects, "translationY", selectedButton == null ? 0 : -bottomObjects.getHeight());
            a3.setDuration(250);
            a3.start();
        }
    }

    ObjectAnimator a1;
    ObjectAnimator a2;

    public void activate_selected_button(ButtonID b) {

        vibrator.vibrate(10);

        deactivate_selected_button();
        root.findViewById(R.id.trashButton).setAlpha(1);

        selectedButton = b;
//        selectedButton.setColor(selectedButton.color == -1 ? ContextCompat.getColor(requireContext(), R.color.primary) : selectedButton.color);
        controlButtons.remove(selectedButton);

        a1 = ObjectAnimator.ofFloat(b, "scaleY", 1.1f);

        a1.setDuration(200);
        a1.setRepeatCount(ValueAnimator.INFINITE);
        a1.setRepeatMode(ValueAnimator.REVERSE);
        a1.start();

        a2 = ObjectAnimator.ofFloat(b, "scaleX", 1.1f);

        a2.setDuration(400);
        a2.setRepeatCount(ValueAnimator.INFINITE);
        a2.setRepeatMode(ValueAnimator.REVERSE);
        a2.start();

        ArrayList<String> codes = new ArrayList<>();

        if(selectedButton.type == ButtonID.Joy) {
            codes.addAll(Arrays.asList(ButtonHandler.keycodesForJoy));
        } else if(selectedButton.type == ButtonID.Scroll || selectedButton.type == ButtonID.Cycle) {
            codes.addAll(Arrays.asList(ButtonHandler.keycodesForScrollandCycle));
        } else {
            codes.addAll(Arrays.asList(ButtonHandler.keyCodesForNormal));
        }

        for(String commandName : UserData.Commands.keySet()) {
            codes.add("\"" + commandName + "\"");
        }

        KeybindEdit.setVisibility(selectedButton.isCommand ? View.GONE : View.VISIBLE);

        KeybindHeader.setVisibility(selectedButton.isCommand ? View.GONE : View.VISIBLE);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, codes);

        KeybindEdit.setAdapter(adapter);

        ObjectAnimator a3 = ObjectAnimator.ofFloat(bottomObjects, "translationY", selectedButton == null ? 0 : -bottomObjects.getHeight());
        a3.setDuration(250);
        a3.start();

    }

    private void exit() {

        vibrator.vibrate(10);

        if(openedPopup == root.findViewById(R.id.keybindPopup)) {

            if (!ButtonHandler.validate(KeybindEdit.getText().toString(), selectedButton.type)) {
                if (isContextAvailable()) {
                    Toast.makeText(requireContext(), R.string.invalid_keybind, Toast.LENGTH_LONG).show();
                }
            }
        }

        openedPopup.setVisibility(View.GONE);
        openedPopup = null;

        imm.hideSoftInputFromWindow(root.getWindowToken(), 0);

        selectedImage = null;
        selectedButtonTintMode = 0;
        selectedButtonAlpha = 255;
        selectedButtonColor = Color.valueOf(Color.WHITE);

        hideKeyboardAndUI();

        selectedButton.updateText();


    }

    private void apply() {
        vibrator.vibrate(10);

        if(selectedButton == null) {
            return;
        }

        if(openedPopup == root.findViewById(R.id.keybindPopup)) {

            if (!ButtonHandler.validate(KeybindEdit.getText().toString(), selectedButton.type)) {
                if (isContextAvailable()) {
                    Toast.makeText(requireContext(), R.string.invalid_keybind, Toast.LENGTH_LONG).show();
                }
            } else {

                if(KeybindEdit.getText().toString().equals("gyro_toggle") && !PremiumController.hasPermission(PremiumController.Product.MotionControls)) {
                    vibrator.vibrate(100);
                    Snackbar r = Snackbar.make(root, R.string.You_need_to_upgrade_to_use_gyroscope, Snackbar.LENGTH_INDEFINITE);
                    r.setAction(R.string.view, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            changeFragmentCallback.onChange(-3, UserData.PREMIUM);
                        }
                    });

                    r.show();

                    selectedButton.setKeybind("");
                } else {
                    selectedButton.setKeybind(KeybindEdit.getText().toString());
                }
            }

            selectedButton.buttonName = editNameText.getText().toString();
            selectedButton.buttonNameVisible = showNameCheck.activated;
            selectedButton.sensitivity = slideSensitivity;
            selectedButton.isHapticFeebackEnabled = hapticEnabled;
            selectedButton.joystickTouchActivate = joyTouchActivateEnable;
            selectedButton.verticalScroll = verticalScrollEnable;

        } else {

            selectedButton.setImage(selectedImage, requireContext());
            selectedButton.setTintMode(selectedButtonTintMode);
            selectedButton.setColor(selectedButtonColor.toArgb());
            selectedButton.setAlpha_((int) (selectedButtonAlpha * 10));

            selectedImage = null;
            selectedButtonTintMode = 0;
            selectedButtonAlpha = 0;
            selectedButtonColor = Color.valueOf(Color.WHITE);

        }

        openedPopup.setVisibility(View.GONE);
        openedPopup = null;

        hideKeyboardAndUI();

        selectedButton.updateText();

        setUnsavedChanges();

    }

    class TouchListen implements View.OnTouchListener {

        final Handler handler = new Handler();
        int x = 0;
        int y = 0;
        boolean Action_Up_Override = false;
        boolean Action_Move_Override = false;
        boolean Select_Override = false;

        ButtonID buttonID;
        ScaleGestureDetector mScaleGestureDetector;
        TouchListen(ButtonID buttonID) {
            mScaleGestureDetector  = new ScaleGestureDetector(getContext(), new ScaleListener());
            this.buttonID = buttonID;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getPointerCount() == 1) {

                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {

                    x = (int) event.getX();
                    y = (int) event.getY();
                    Action_Up_Override = false;
                    Action_Move_Override = false;

                    Select_Override = false;
                    if(selectedButton == null || selectedButton != buttonID) {
                        activate_selected_button((ButtonID) v);
                        Select_Override = true;
                    }

                    handler.postDelayed(mLongPressed, ViewConfiguration.getLongPressTimeout());
                }

                if (event.getActionMasked() == MotionEvent.ACTION_MOVE && (selectedButton == v) && !Action_Move_Override) {

                    float X = event.getX() - x;
                    float Y = event.getY() - y;
                    double distance = Math.sqrt(Math.pow(X, 2) + Math.pow(Y, 2));

                    if (distance > 10) {
                        setUnsavedChanges();
                        handler.removeCallbacks(mLongPressed);
                        Action_Up_Override = true;

                        if (selectedButton.getX() + X + (float) selectedButton.getLayoutParams().width / 2 > 0 && selectedButton.getX() + X < buttonContainer.getWidth() - (float) selectedButton.getLayoutParams().width / 2) {
                            selectedButton.setX(selectedButton.getX() + X);
                        }

                        if (selectedButton.getY() + Y + (float) selectedButton.getLayoutParams().height / 2 > 0 && selectedButton.getY() + Y < buttonContainer.getHeight() - (float) selectedButton.getLayoutParams().height / 2) {
                            selectedButton.setY(selectedButton.getY() + Y);
                        }
                    }
                }

                if ((event.getActionMasked() == MotionEvent.ACTION_CANCEL || event.getActionMasked() == MotionEvent.ACTION_UP) && !Action_Up_Override) {

                    float X = event.getX() - x;
                    float Y = event.getY() - y;
                    double distance = Math.sqrt(Math.pow(X, 2) + Math.pow(Y, 2));

                    handler.removeCallbacks(mLongPressed);

                    if (distance < 50) {

                        if (selectedButton == (v)) {
                            if(!Select_Override) {
                                deactivate_selected_button();
                            }
                        } else {
                            activate_selected_button((ButtonID) v);
                        }

                    }

                    x = 0;
                    y = 0;

                }
            } else{
                if(mScaleGestureDetector.onTouchEvent(event)) {
                    handler.removeCallbacks(mLongPressed);
                    Action_Up_Override = true;
                    Action_Move_Override = true;
                }
            }


            return false;
        }

        Runnable mLongPressed = new Runnable() {
            public void run() {

                if(selectedButton == null || selectedButton != buttonID) {
                    activate_selected_button(buttonID);
                    return;
                }

                if(getContext() != null && PremiumController.hasPermission(PremiumController.Product.InfiniteButtons)) {
                    duplicateButton(getContext());

                }
            }
        };

    }

    class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        float size;
        float size_last;
        float xx;
        float yy;
        float mScaleFactor = 1.0f;

        OnGenericCallbackv2 onScale;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            if (selectedButton != null) {
                size = selectedButton.getLayoutParams().width;
                size_last = size;
                xx = selectedButton.getX();
                yy = selectedButton.getY();
                mScaleFactor = 1;
            }
            return super.onScaleBegin(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {

            if (selectedButton != null) {
                mScaleFactor *= scaleGestureDetector.getScaleFactor();
//                mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

                if ((mScaleFactor > 1 && size * mScaleFactor < buttonContainer.getHeight()) || (mScaleFactor <= 1 && (size * mScaleFactor) > (buttonContainer.getHeight() / 12f))) {

                    if(onScale != null) {
                        onScale.onChange(null);
                    }
                    setUnsavedChanges();

                    ViewGroup.LayoutParams params = selectedButton.getLayoutParams();

                    size_last = size * mScaleFactor;

                    params.width = (int) (size * mScaleFactor);

                    if (selectedButton.type == ButtonID.Scroll) {

                        params.height = (int) (size * mScaleFactor * 0.6f);
                    } else {

                        params.height = (int) (size * mScaleFactor);
                    }

                    selectedButton.setX(xx - (size_last - size) / 2);

                    if (selectedButton.type == ButtonID.Scroll) {

                        selectedButton.setY(yy - (size_last - size) * 0.6f / 2);
                    } else {

                        selectedButton.setY(yy - (size_last - size) / 2);
                    }

                    selectedButton.requestLayout();

                }
            }
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);

            mScaleFactor = 1;

        }
    }

    private void duplicateButton(Context context) {

        if(controlButtons.size() >= maxButtons && !PremiumController.hasPermission(PremiumController.Product.InfiniteButtons)) {

            Bundle b = new Bundle();
            b.putString("title", "6_buttons_requirement");
            mFirebaseAnalytics.logEvent("Message", b);

            vibrator.vibrate(500);
            Snackbar r = Snackbar.make(root, R.string.For_more_buttons_you_must_upgrade, Snackbar.LENGTH_INDEFINITE);
            r.setAction(R.string.view, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changeFragmentCallback.onChange(-3, UserData.PREMIUM);
                }
            });

            r.show();
            return;
        }

        ButtonID selectedRef = selectedButton;
        deactivate_selected_button();

        ButtonID newButton = new ButtonID(context);

        ButtonID_data data = selectedRef.getData(context);

        newButton.setData_dupl(context, data);

        newButton.setPosition((int) (newButton.x * 0.95f), (int) (newButton.y * 0.95f));

        setUnsavedChanges();

        buttonContainer.addView(newButton);

        activate_selected_button(newButton);

        newButton.setOnTouchListener(new TouchListen(newButton));

        HashMap<String, String> addData = new HashMap<>();

        addData.put("button_type", Integer.toString(newButton.type));
        addData.put("total_buttons_active", Integer.toString(controlButtons.size()));

        MixPanel.mpEventTracking(mp, "added_new_button", addData);

        newButton.setTintMode(0);
    }

    private boolean isContextAvailable(){

        boolean success = true;

        try { requireContext(); }
        catch (IllegalStateException e) {
            success = false;
        }

        return success;
    }

    public void isReadingPermissionGranted() {

        hideKeyboardAndUI();

        if (requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG,"Permission is granted");
        } else {

            Log.v(TAG,"Permission is revoked");
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        }

    }

    public void isStoragePermissionGranted() {
        hideKeyboardAndUI();

        if (requireContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG,"Permission is granted");
        } else {

            Log.v(TAG,"Permission is revoked");
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }
    }

    private void setUnsavedChanges() {
        unsavedChanges = true;
    }
}
