package com.gingertech.starbeam.ui.layout;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.gingertech.starbeam.MainActivity;
import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.LayoutClass;
import com.gingertech.starbeam.helpers.UserData;
import com.gingertech.starbeam.helpers.controllers.GenericCallback;
import com.gingertech.starbeam.helpers.controllers.GenericCallbackv2;
import com.gingertech.starbeam.helpers.controllers.OnGenericCallback;
import com.gingertech.starbeam.helpers.controllers.OnGenericCallbackv2;
import com.gingertech.starbeam.helpers.controllers.PremiumController;

public class LayoutRootPage extends Fragment {

    final public static int LISTVIEW = 0;
    final public static int CREATEVIEW = 1;
    final public static int LAUNCHVIEW = 2;
    final public static int TESTVIEW = 3;

    public int currentView = LISTVIEW;

    View root;

    FragmentManager fragmentManager;
    GenericCallbackv2 changeFragmentCallback;
    GenericCallbackv2 callback;
    GenericCallbackv2 premiumPageCallback;

    LayoutClass currentLayout;

    int proceedToFragment = -1;


    public LayoutRootPage(GenericCallbackv2 callback) {
        this.changeFragmentCallback = callback;
    }

    public LayoutRootPage(GenericCallbackv2 callback, int fragment) {
        this.changeFragmentCallback = callback;

        if(fragment == LayoutRootPage.TESTVIEW) {
            proceedToFragment = -1;
            return;
        }

        proceedToFragment = fragment;

    }

    public LayoutRootPage(){

    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.layout_page, container, false);

        fragmentManager = requireActivity().getSupportFragmentManager();

        callback = new GenericCallbackv2() {
            @Override
            public void onChange(Object value, Object value2) {

                if((int) value == -1) {
                    changeFragmentCallback.onChange(value);
                    return;
                }

                if((int) value == -2) {
                    changeFragmentCallback.onChange(value);
                    return;
                }

                if((int) value == -3) {
                    changeFragmentCallback.onChange(value2);
                    return;
                }

                changeView((int) value, (LayoutClass) value2);
            }

            @Override
            public void onChange(Object value) {
                if(value instanceof String) {
                    changeFragmentCallback.onChange(UserData.currentLayout.name);
                } else {
                    changeView((int) value, null);
                }
            }

        };

        premiumPageCallback = new GenericCallbackv2() {
            @Override
            public void onChange(Object value) {
//                PremiumController.purchasePremium_onetime(requireActivity());

            }
        };
//
//        FragmentTransaction trans = fragmentManager.beginTransaction();
//        trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        switch (proceedToFragment) {

            case (LayoutRootPage.CREATEVIEW):
                changeView(LayoutRootPage.CREATEVIEW, UserData.currentLayout);
                break;

            case (LayoutRootPage.LISTVIEW):
            case (-1):
            default:
                changeFragmentCallback.onChange("Controller List");

                FragmentTransaction trans = fragmentManager.beginTransaction();
                trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                trans.replace(R.id.nav_host_fragment, new LayoutListFragment(callback)).commit();

                UserData.CurrentFragment = UserData.LAYOUTS_LIST;
        }

        return root;
    }

    void changeView(int newView, @Nullable LayoutClass layoutClass) {
        FragmentTransaction trans = fragmentManager.beginTransaction();

        if(currentView == newView) { return; }

        switch (newView) {

            case (LISTVIEW):
                changeFragmentCallback.onChange("Controller List");

                trans.setCustomAnimations(R.anim.slide_up_in, R.anim.slide_up_out);
                trans.replace(R.id.nav_host_fragment, new LayoutListFragment(callback)).commit();

                //Activates Drawer Button
                UserData.CurrentFragment = UserData.LAYOUTS_LIST;
                changeFragmentCallback.onChange(-2);

                break;

            case (CREATEVIEW):

                if(UserData.CurrentFragment == UserData.LAYOUTS_TEST) {
                    trans.setCustomAnimations(R.anim.slide_up_in, R.anim.slide_up_out);
                } else {
                    trans.setCustomAnimations(R.anim.slide_down_in, R.anim.slide_down_out);
                }

                trans.replace(R.id.nav_host_fragment, new LayoutCreateFragment(callback, layoutClass)).commit();

                //Activates Drawer Button
                UserData.CurrentFragment = UserData.LAYOUTS_CREATE;
                changeFragmentCallback.onChange(-1);
                break;

            case (LAUNCHVIEW):
                if(changeFragmentCallback != null) {
                    changeFragmentCallback.onChange("Play");

                    changeFragmentCallback.onChange(MainActivity.PLAY);

                    //deActivates Drawer Button
                    UserData.CurrentPage = MainActivity.PLAY;
                    changeFragmentCallback.onChange(-2);
                }
                break;

            case (TESTVIEW):
                if(changeFragmentCallback != null) {
                    trans.setCustomAnimations(R.anim.slide_down_in, R.anim.slide_down_out);
                    trans.replace(R.id.nav_host_fragment, new LayoutTestFragment(callback, layoutClass)).commit();

                    //Activates Drawer Button
                    UserData.CurrentFragment = UserData.LAYOUTS_TEST;
                    changeFragmentCallback.onChange(-1);
                }
                break;
        }

        currentView = newView;
    }

}
