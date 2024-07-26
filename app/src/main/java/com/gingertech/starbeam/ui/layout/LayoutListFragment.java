package com.gingertech.starbeam.ui.layout;

import static com.gingertech.starbeam.MainActivity.mFirebaseAnalytics;
import static com.gingertech.starbeam.MainActivity.uiFlag;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;

import com.gingertech.starbeam.MainActivity;
import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.LayoutClass;
import com.gingertech.starbeam.helpers.LayoutGroupClass;
import com.gingertech.starbeam.helpers.SaveClass;
import com.gingertech.starbeam.helpers.UserData;
import com.gingertech.starbeam.helpers.controllers.FireFunctions;
import com.gingertech.starbeam.helpers.controllers.GenericCallback;
import com.gingertech.starbeam.helpers.controllers.LayoutGroupView;
import com.gingertech.starbeam.helpers.controllers.ListViewController;
import com.gingertech.starbeam.helpers.controllers.MixPanel;
import com.gingertech.starbeam.helpers.controllers.OnGenericCallback;
import com.gingertech.starbeam.helpers.controllers.OnGenericCallbackv2;
import com.gingertech.starbeam.helpers.controllers.PopupController;
import com.gingertech.starbeam.helpers.controllers.PremiumController;
import com.gingertech.starbeam.ui.PremiumPage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class LayoutListFragment extends Fragment {
    View root;
    LayoutClass selected;
    OnGenericCallbackv2 changeFragmentCallback;
    View deleteButton, playButton, editButton;
    boolean isFocused = false;
    static boolean overlayActivated = false;
    static boolean xVisible = false;

    Vibrator vibrator;
    InputMethodManager imm;

    public static WindowManager.LayoutParams layoutParams;

    public View inflatedView;

    public LayoutListFragment(){

    }

    public LayoutListFragment(OnGenericCallbackv2 changeFragmentCallback) {
        this.changeFragmentCallback = changeFragmentCallback;
    }

    private void openPremiumPopup() {
        vibrator.vibrate(100);
        Snackbar r = Snackbar.make(root, R.string.You_need_to_upgrade, Snackbar.LENGTH_INDEFINITE);
        r.setAction(R.string.view, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeFragmentCallback.onChange(-3, UserData.PREMIUM);
            }
        });

        r.show();
    }
    ViewGroup premiumPopup;

    void showPremiumPopup() {
        vibrator.vibrate(100);
        Snackbar r = Snackbar.make(root, R.string.You_need_to_upgrade, Snackbar.LENGTH_INDEFINITE);
        r.setAction(R.string.view, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserData.selectedPremiumObject = PremiumController.Product.UnlimitedSaves;

                changeFragmentCallback.onChange(-3, UserData.PREMIUM);
            }
        });

        r.show();
    }

    private MixpanelAPI mp;

    int height;
    int width;
    DisplayMetrics displayMetrics = new DisplayMetrics();

    EditText layoutIDImport;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.layout_page, container, false);

        vibrator = (Vibrator) root.getContext().getSystemService(Context.VIBRATOR_SERVICE);

        mFirebaseAnalytics.logEvent("Layout_List_Fragment", new Bundle());

        mp = MixPanel.makeObj(requireContext());

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

        MixPanel.mpEventTracking(mp, "Layout_List_Fragment_opened", null);

        ListViewController recyclerView = root.findViewById(R.id.recycleView);
        final ViewGroup c = root.findViewById(R.id.back);
        inflatedView = View.inflate(getContext(), R.layout.keyboardtoggle, c);

        selected = UserData.currentLayout;

        recyclerView.setSelected(selected);
        recyclerView.setData();

        imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        deleteButton = root.findViewById(R.id.deleteButton);
        editButton = root.findViewById(R.id.editButton);
        playButton = root.findViewById(R.id.playButton);

        layoutIDImport = ((EditText) root.findViewById(R.id.layoutID));
        root.findViewById(R.id.layoutImport).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                vibrator.vibrate(10);

                Toast.makeText(requireContext(), R.string.Importing_Layout, Toast.LENGTH_SHORT).show();

                String layoutID = layoutIDImport.getText().toString();
                layoutIDImport.setText("");

                HashMap<String, String> data = new HashMap<>();
                data.put("UUID", layoutID);

                MixPanel.mpEventTracking(mp, "import_layout_clicked", data);

                FireFunctions.getController(getContext(), layoutID, null).addOnCompleteListener(new OnCompleteListener<LayoutClass>() {
                    @Override
                    public void onComplete(@NonNull Task<LayoutClass> task) {

                        if(!task.isSuccessful()) {
                            MixPanel.mpEventTracking(mp, "import_layout_fail", data);

                            vibrator.vibrate(100);

                            if(getContext() == null) {
                                return;
                            }

                            Toast.makeText(requireContext(), R.string.Could_not_retrieve_layout, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        LayoutClass l = task.getResult();

                        if(l == null) {
                            vibrator.vibrate(100);
                            MixPanel.mpEventTracking(mp, "import_layout_fail", data);

                            Toast.makeText(requireContext(), R.string.Could_not_retrieve_layout, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ArrayMap layoutData = new ArrayMap();
                        layoutData.put("Import Name", l.name);
                        layoutData.put("Import Button Length", l.buttons.size());
                        layoutData.put("Import ID", l.layoutUUID);
                        layoutData.put("Import Name", l.name);

                        if(!PremiumController.hasPermission(PremiumController.Product.InfiniteButtons) && l.buttons.size() > LayoutCreateFragment.maxButtons) {
                            vibrator.vibrate(100);

                            MixPanel.mpEventTracking(mp, "import_need_upgrade", data);

                            Snackbar r = Snackbar.make(root, R.string.You_need_to_upgrade, Snackbar.LENGTH_INDEFINITE);
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

                        if(PremiumController.hasPermission(PremiumController.Product.UnlimitedSaves)) {
                            recyclerView.setSelected(l);
                        }

                        MixPanel.mpEventTracking(mp, "import_layout_success", data);


                        Context c = getActivity();
                        if(c != null) {
                            Toast.makeText(c, R.string.Got_it, Toast.LENGTH_SHORT).show();

                            l.UpdateOldNames();

                            UserData.Layouts.add(l);
                            UserData.currentLayout = l;
                            selected = l;

                            l.save(c);

                            recyclerView.setData();
                        }

                    }
                });
            }
        });

        inflatedView = View.inflate(getContext(), R.layout.keyboardtoggle, c);
//        inflatedView.setAlpha((float) ColorOrganizer.SettingValues.get("toggleOpacity") / 100);

        inflatedView.findViewById(R.id.LL).setOnTouchListener(new View.OnTouchListener() {

            final int[] values = {-width, 0, width};
            int current = 1;

            int xposB;
            int yposB;

            Handler handler = new Handler();

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {

                    handler.postDelayed(exitLongPress, ViewConfiguration.getLongPressTimeout());

                    xposB = Math.round(event.getX());
                    yposB = Math.round(event.getY());
//                    keybindButton.clearFocus();

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


                        isFocused = !isFocused;

                        if (isFocused) {

                            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                            MainActivity.WM.updateViewLayout(inflatedView, layoutParams);

                        } else {
                            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                            MainActivity.WM.updateViewLayout(inflatedView, layoutParams);

                        }

                    }
                    vibrator.vibrate(100);

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

//
//                    MainActivity.vibrate(5);
                }
            };
        });

        root.findViewById(R.id.deletePopup).findViewById(R.id.deleteButtonFromPopup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MixPanel.mpButtonTracking(mp, "delete_layout_confirmation");

                root.findViewById(R.id.deletePopup).setVisibility(View.GONE);

                if(!Objects.equals(UserData.currentLayoutName, UserData.currentLayout.name)) {
                    UserData.currentLayoutName = UserData.currentLayout.name;
                }

                if(!Objects.equals(UserData.currentLayoutName, "Default") && !Objects.equals(UserData.currentLayoutName, "default xbox") ) {

                    SaveClass.DeleteLayout(UserData.currentLayout, requireContext());

                    UserData.Layouts.remove(UserData.currentLayout);

                    SaveClass.SaveCurrentLayout(requireContext());
                    SaveClass.SaveLayouts(requireContext());

                    recyclerView.setData();

                    UserData.setCurrentLayout(requireContext(), recyclerView.selectFirst());

                    vibrator.vibrate(10);

                } else {

                    Toast.makeText(requireContext(), R.string.cannot_delete_the_default_layouts_please_choose_another, Toast.LENGTH_SHORT).show();
                    vibrator.vibrate(400);

                }
            }
        });

        root.findViewById(R.id.deletePopup).findViewById(R.id.returnFromDeletePopup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MixPanel.mpButtonTracking(mp, "delete_layout_close");

                root.findViewById(R.id.deletePopup).setVisibility(View.GONE);
                vibrator.vibrate(10);

            }
        });

        recyclerView.setOnListSelectedListener(new GenericCallback().setOnGenericCallback(new OnGenericCallback() {
            @Override
            public void onChange(Object value) {

                if(value == null) {
                    openPremiumPopup();
                    return;
                }


                try {
                    JSONObject props = new JSONObject();
                    props.put("title", ((LayoutClass) value).name);
                    mp.track("Button Clicked", props);
                } catch (JSONException j) {

                }

                MixPanel.mpButtonTracking(mp, "user_layout_" + ((LayoutClass) value).name);

                selected = (LayoutClass) value;
                UserData.setCurrentLayout(requireContext(), selected);
            }
        }));

        recyclerView.setDeleteCallback(new GenericCallback().setOnGenericCallback(new OnGenericCallback() {

            @Override
            public void onChange(Object value) {
                deleteSelectedLayout();
            }
        }));

        recyclerView.setEditCallback(new GenericCallback().setOnGenericCallback(new OnGenericCallback() {

            @Override
            public void onChange(Object value) {
                editSelectedLayout();
            }
        }));

        recyclerView.setNewCallback(new GenericCallback().setOnGenericCallback(new OnGenericCallback() {

            @Override
            public void onChange(Object value) {
                createNewLayout();
            }
        }));
        recyclerView.setOnGroupAddListener(new GenericCallback().setOnGenericCallback(new OnGenericCallback() {
            @Override
            public void onChange(Object value) {

                LayoutClass layout = (LayoutClass) value;

                LayoutGroupClass groupClass = null;

                if(UserData.currentGroupID.equals("")) { return; }

                for(LayoutGroupClass g : UserData.LayoutGroups) {
                    if(UserData.currentGroupID.equals(g.groupID)) {
                        groupClass = g;
                        break;
                    }
                }

                if(groupClass != null) {

                    groupClass.layoutClasses.add(layout);
                    groupClass.save(requireContext());

                    layout.isLayoutInGroup = true;
                    layout.groupID = groupClass.groupID;

                    layout.save(requireContext());

                    recyclerView.setData();

                } else {

                    Log.i("group", "unable to add to group");
                }
            }
        }));


        recyclerView.setDuplicateCallback(new GenericCallback().setOnGenericCallback(new OnGenericCallback() {

            @Override
            public void onChange(Object value) {
                MixPanel.mpButtonTracking(mp, "new_layout_open");


                if(!PremiumController.hasPermission(PremiumController.Product.UnlimitedSaves) && UserData.Layouts.size() > MainActivity.maxLayouts) {
                        showPremiumPopup();
                    return;
                }

                if(getContext() == null) {
                    return;
                }

                Toast.makeText(getContext(), R.string.Created_a_Layout_Copy, Toast.LENGTH_SHORT).show();

                LayoutClass copyLayout = new LayoutClass(UserData.currentLayoutName + "~", getContext());
                copyLayout.copyLayout(UserData.currentLayout);

                moveToCreateLayout(copyLayout);
                UserData.Layouts.add(copyLayout);
                copyLayout.save(requireContext());
                UserData.setCurrentLayout(requireContext(), copyLayout);

                MixPanel.mpEventTracking(mp, "duplicate_layout_created", null);

                MainActivity.helpController.endHelp();

                vibrator.vibrate(10);

            }
        }));

        recyclerView.setOnLaunchListener(new GenericCallback().setOnGenericCallback(new OnGenericCallback() {

            @Override
            public void onChange(Object value) {

                MixPanel.mpEventTracking(mp, "view_edit_from_list", null);
                editSelectedLayout();

            }
        }));


        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                vibrator.vibrate(10);

                if (changeFragmentCallback != null) {
                    changeFragmentCallback.onChange(LayoutRootPage.LAUNCHVIEW);

                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSelectedLayout();
                vibrator.vibrate(10);
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editSelectedLayout();
                vibrator.vibrate(10);
            }
        });

        ((PopupController) root.findViewById(R.id.createPopup)).setOnSubmitListener(new GenericCallback().setOnGenericCallback(new OnGenericCallback() {
            @Override
            public void onChange(Object value) {

                if(value instanceof LayoutClass && !(value instanceof LayoutGroupClass)) {
                    LayoutClass layout = (LayoutClass) value;

                    moveToCreateLayout(layout);
                    UserData.Layouts.add(layout);
                    layout.save(requireContext());
                    UserData.setCurrentLayout(requireContext(), layout);

                    MixPanel.mpEventTracking(mp, "new_layout_created", null);

                } else if(value instanceof LayoutGroupClass) {

                    LayoutGroupClass group = (LayoutGroupClass) value;
                    group.save(requireContext());

                    MixPanel.mpEventTracking(mp, "new_layout_group_created", null);

                    recyclerView.setData();
                }

                vibrator.vibrate(10);

            }
        }));

        ((PopupController) root.findViewById(R.id.createPopup)).setOnCloseListener(new GenericCallback().setOnGenericCallback(new OnGenericCallback() {
            @Override
            public void onChange(Object value) {
                requireActivity().getWindow().getDecorView().setSystemUiVisibility(uiFlag);

                MixPanel.mpButtonTracking(mp, "new_layout_close");
                vibrator.vibrate(10);


            }
        }));

        return root;
    }

    private void moveToCreateLayout(LayoutClass layoutClass) {

        requireActivity().getWindow().getDecorView().setSystemUiVisibility(uiFlag);

        if(changeFragmentCallback != null) {
            changeFragmentCallback.onChange(LayoutRootPage.CREATEVIEW, layoutClass);
        }
    }

    private void editSelectedLayout(){
        MixPanel.mpButtonTracking(mp, "edit_layout_open");

        if(selected == null) {
            vibrator.vibrate(400);
            return;
        }

        MainActivity.helpController.endHelp_cont();

        if(changeFragmentCallback != null) {
            changeFragmentCallback.onChange(LayoutRootPage.CREATEVIEW, selected);
        }
    }

    private void deleteSelectedLayout() {
        MixPanel.mpButtonTracking(mp, "delete_layout_open");


        if(selected == null) {
            vibrator.vibrate(400);
            return;
        }

        root.findViewById(R.id.deletePopup).setVisibility(View.VISIBLE);
    }

    private void createNewLayout() {
        MixPanel.mpButtonTracking(mp, "new_layout_open");

        if(!PremiumController.hasPermission(PremiumController.Product.UnlimitedSaves) && UserData.Layouts.size() >= MainActivity.maxLayouts) {
            showPremiumPopup();
            return;
        }

        root.findViewById(R.id.createPopup).setVisibility(View.VISIBLE);
        MainActivity.helpController.endHelp();
    }
}
