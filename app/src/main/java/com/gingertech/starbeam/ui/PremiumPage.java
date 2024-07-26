package com.gingertech.starbeam.ui;

import static com.gingertech.starbeam.MainActivity.mFirebaseAnalytics;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.billingclient.api.ProductDetails;
import com.gingertech.starbeam.MainActivity;
import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.UserData;
import com.gingertech.starbeam.helpers.controllers.GenericCallbackv2;
import com.gingertech.starbeam.helpers.controllers.MixPanel;
import com.gingertech.starbeam.helpers.controllers.PremiumCardController;
import com.gingertech.starbeam.helpers.controllers.PremiumController;
import com.gingertech.starbeam.helpers.controllers.PremiumSelection;
import com.gingertech.starbeam.ui.launch.LaunchGameList;
import com.gingertech.starbeam.ui.layout.LayoutListFragment;
import com.gingertech.starbeam.ui.layout.LayoutRootPage;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.ArrayList;
import java.util.HashMap;


public class PremiumPage extends Fragment {

    View root;
    PremiumController premiumController;
    HashMap<String, PremiumController.OneTimePurchaseOffer> offers = new HashMap<>();

    PremiumController.Product launchProduct;

    public PremiumPage() {}

    public PremiumPage(PremiumController premiumController) {
        this.premiumController = premiumController;
    }

    MixpanelAPI mp;

    @Override
    public void onResume() {
        super.onResume();
        premiumController.queryPuchases();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.premium_layout_v2, container, false);

        ViewGroup premiumOptions = root.findViewById(R.id.productOptions);
        mp = MixPanel.makeObj(requireContext());
        MixPanel.mpEventTracking(mp, "Opened_Premium_Page", null);


        if(premiumController != null) {
            offers = premiumController.getOptions();

            if(UserData.selectedPremiumObject != null) {
                launchProduct = UserData.selectedPremiumObject;
                UserData.selectedPremiumObject = null;

                if (offers.containsKey(PremiumController.productToID.get(launchProduct))) {
                    PremiumController.purchasePremium_onetime(requireActivity(), offers.get(PremiumController.productToID.get(launchProduct)).productDetails, new GenericCallbackv2() {
                        @Override
                        public void onChange(Object value) {
                            if (value instanceof Boolean) {
                                Log.e("billing", "updated");

                                for (int i = 0; i < premiumOptions.getChildCount(); i++) {
                                    PremiumCardController premiumSelection = (PremiumCardController) premiumOptions.getChildAt(i);

                                    PremiumController.OneTimePurchaseOffer offer = offers.get(premiumSelection.ItemID);

                                    if (offer != null) {
                                        premiumSelection.setOnSelectedListener(this);
                                        premiumSelection.setDetails(offer);
                                    }
                                }
                            }
                        }

                    });
                }
            }
        }

        if(premiumController == null) {

            Toast.makeText(getContext(), R.string.Please_verify_your_connection, Toast.LENGTH_LONG).show();

            return root;
        }

        GenericCallbackv2 selectedCallback = new GenericCallbackv2() {
            @Override
            public void onChange(Object value) {
                super.onChange(value);

                if(value instanceof ProductDetails) {
                    PremiumController.purchasePremium_onetime(requireActivity(), (ProductDetails) value, new GenericCallbackv2() {
                        @Override
                        public void onChange(Object value) {
                            if(value instanceof Boolean) {
                                Log.e("billing", "updated");

                                for(int i = 0; i < premiumOptions.getChildCount(); i++) {
                                    PremiumCardController premiumSelection = (PremiumCardController) premiumOptions.getChildAt(i);

                                    if(offers != null) {
                                        PremiumController.OneTimePurchaseOffer offer = offers.get(premiumSelection.ItemID);

                                        if (offer != null) {
                                            premiumSelection.setOnSelectedListener(this);
                                            premiumSelection.setDetails(offer);
                                        }
                                    }
                                }
                            }
                        }
                    });

                    HashMap<String, String> d = new HashMap<>();

                    d.put("Selected_Product", ((ProductDetails) value).getProductId());

                    MixPanel.mpEventTracking(mp, "Clicked_Purchase", d);
                }
            }
        };

        for(int i = 0; i < premiumOptions.getChildCount(); i++) {
            PremiumCardController premiumSelection = (PremiumCardController) premiumOptions.getChildAt(i);

            if(offers != null) {
                if (offers.containsKey(premiumSelection.ItemID)) {
                    PremiumController.OneTimePurchaseOffer offer = offers.get(premiumSelection.ItemID);

                    if (offer != null) {
                        premiumSelection.setOnSelectedListener(selectedCallback);
                        premiumSelection.setDetails(offer);
                    }
                }
            }
        }

        return root;

    }
}
