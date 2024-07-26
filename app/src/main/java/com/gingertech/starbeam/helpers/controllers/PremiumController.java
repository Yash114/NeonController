package com.gingertech.starbeam.helpers.controllers;

import static androidx.core.content.ContextCompat.getSystemService;

import static org.slf4j.MDC.put;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.gingertech.starbeam.helpers.SaveClass;
import com.gingertech.starbeam.helpers.UserData;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class PremiumController {

    public enum Product {
        MotionControls,
        CommandEditor,
        InfiniteButtons,
        AllAccess,
        UnlimitedSaves
    }

    public boolean acknowledgePurchaseUnsuccessful = false;
    List<QueryProductDetailsParams.Product> queryProductDetailsParamsList = new ArrayList<>();
    QueryProductDetailsParams queryProductDetailsParams;

    static ProductDetails premiumSubcriptionDetails;
    static ProductDetails premiumSubcriptionDetails_pre;

    static ProductDetails premiumSubcriptionDetails_new;

    static ProductDetails premiumOneTimerDetails;

    static public HashMap<String, OneTimePurchaseOffer> products = new HashMap<>();
    public static Set<String> purchasedProductsList = new ArraySet<>();

    public String oneTimerPurchase = "premium_one_time";

    GenericCallbackv2 onPurchasesUpdated;
    static BillingClient billingClient;


    static String[] inAppProductList = {
            "motion_controls",
            "command_editor",
            "infinite_buttons",
            "premium_one_time",
            "unlimited_saves"
    };

    public static HashMap<Product, String> productToID = new HashMap<>() {{
        put(Product.MotionControls, "motion_controls");
        put(Product.CommandEditor, "command_editor");
        put(Product.InfiniteButtons, "infinite_buttons");
        put(Product.AllAccess, "premium_one_time");
        put(Product.UnlimitedSaves, "unlimited_saves");
    }};

    public static void purchasePremium(Activity activity) {

        if(premiumSubcriptionDetails == null) { return; }

        List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                List.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                .setProductDetails(premiumSubcriptionDetails)
                                // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                // for a list of offers that are available to the user
                                .setOfferToken(premiumSubcriptionDetails.getSubscriptionOfferDetails().get(0).getOfferToken())
                                .build()
                );

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        //Launches the billing view
        BillingResult billingResult = billingClient.launchBillingFlow(activity, billingFlowParams);

        if(billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            //Tell the user there was an issue
            Log.i("billing", "there was a purchasing error");
        }
    }

    public static void purchasePremium_onetime(Activity activity, ProductDetails product, GenericCallbackv2 onDone) {

        Log.e("proo", "purchased a thing " + product.getProductId());
        {
            if (premiumOneTimerDetails == null) {
                return;
            }

            List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                    List.of(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(product)
                                    .build()
                    );

            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build();


            BillingResult billingResult = billingClient.launchBillingFlow(activity, billingFlowParams);

            if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                //Tell the user there was an issue
                Log.i("billing", "there was a purchasing error");
            }
        }
    }

    public static void purchasePremium_new(Activity activity, PremiumController.SubscriptionOffer subscriptionOffer) {

        if(premiumSubcriptionDetails_new == null) { return; }

        List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                List.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                .setProductDetails(premiumSubcriptionDetails_new)
                                // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                // for a list of offers that are available to the user
                                .setOfferToken(subscriptionOffer.token)
                                .build()
                );

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        //Launches the billing view
        BillingResult billingResult = billingClient.launchBillingFlow(activity, billingFlowParams);

        if(billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            //Tell the user there was an issue
            Log.i("billing", "there was a purchasing error");
        }
    }

    private final PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
            Log.e("billing", "purchase was updated");

            {
                //If there was a purchase
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (Purchase purchase : purchases) {
                        for(String l : purchase.getProducts()) {
                            purchasedProductsList.add(l);
                        }

                        for(String l : purchase.getProducts()) {
                            OneTimePurchaseOffer offer = products.get(l);
                            if (offer != null) {
                                offer.isPurchased = true;
                                products.put(l, offer);
                            }
                        }

                        handlePurchase(purchase);
                    }

                    onPurchasesUpdated.onChange(true, true);

                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                    // Handle an error caused by a user cancelling the purchase flow.
//                onPurchasesUpdated.onChange(false, "User cancelled the premium free trial");


                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {

                    Log.e("billing", "Item already owned!");
                    onPurchasesUpdated.onChange(true, true);

                    if (purchases != null) {
                        //The user already owns this product so give it to them
                        for (Purchase purchase : purchases) {
                             purchasedProductsList.addAll(purchase.getProducts());

                            for(String l : purchase.getProducts()) {
                                OneTimePurchaseOffer offer = products.get(l);
                                if (offer != null) {
                                    offer.isPurchased = true;
                                    products.put(l, offer);
                                }
                            }

                            handlePurchase(purchase);
                        }


                    } else {

                        onPurchasesUpdated.onChange("error");
                    }
                }
            }
        }
    };

    public void handlePurchase(Purchase purchase) {

        if (!purchase.isAcknowledged()) {
            AcknowledgePurchaseParams acknowledgePurchaseParams =
                    AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();
            billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                @Override
                public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                    if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                    }
                }
            });
        }

    }



    public void setup(final Activity activity, GenericCallbackv2 onPurchasesUpdated) {

        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean connected = activeNetwork != null;

        if(!connected) {
            onPurchasesUpdated.onChange("error");
        }

        billingClient = BillingClient.newBuilder(activity)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases(
                        PendingPurchasesParams.newBuilder()
                                .enableOneTimeProducts()
                                .build())
                .build();

        this.onPurchasesUpdated = onPurchasesUpdated;
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    Log.e("billing", "billing setup!");

                    //New Subscription Stuff
                    {

                        QueryProductDetailsParams queryProductDetailsParams =
                                QueryProductDetailsParams.newBuilder()
                                        .setProductList(
                                                List.of(
                                                        QueryProductDetailsParams.Product.newBuilder()
                                                                .setProductId("new_neon_premium")
                                                                .setProductType(BillingClient.ProductType.SUBS)
                                                                .build()))
                                        .build();

                        billingClient.queryProductDetailsAsync(
                                queryProductDetailsParams,
                                new ProductDetailsResponseListener() {
                                    public void onProductDetailsResponse(BillingResult billingResult,
                                                                         List<ProductDetails> productDetailsList) {

                                        if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                                            onPurchasesUpdated.onChange("error");
                                            return;

                                        }

                                        //
                                        if (productDetailsList.isEmpty()) {
                                            onPurchasesUpdated.onChange("error");
                                            return;
                                        }

                                        //Successfully connected and found product
                                        premiumSubcriptionDetails_new = productDetailsList.get(0);
                                        Log.i("product_details", premiumSubcriptionDetails.getTitle());

                                        //Find previous purchases
                                        queryPuchases();
                                    }
                                }
                        );
                    }

                    //Subscription Stuff
                    {

                        QueryProductDetailsParams queryProductDetailsParams =
                                QueryProductDetailsParams.newBuilder()
                                        .setProductList(
                                                List.of(
                                                        QueryProductDetailsParams.Product.newBuilder()
                                                                .setProductId("premium_subscription_monthly")
                                                                .setProductType(BillingClient.ProductType.SUBS)
                                                                .build()))
                                        .build();

                        billingClient.queryProductDetailsAsync(
                                queryProductDetailsParams,
                                new ProductDetailsResponseListener() {
                                    public void onProductDetailsResponse(BillingResult billingResult,
                                                                         List<ProductDetails> productDetailsList) {

                                        if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                                            onPurchasesUpdated.onChange("error");
                                            return;

                                        }

                                        //
                                        if (productDetailsList.isEmpty()) {
                                            onPurchasesUpdated.onChange("error");
                                            return;
                                        }

                                        //Successfully connected and found product
                                        premiumSubcriptionDetails = productDetailsList.get(0);
                                        Log.i("product_details", premiumSubcriptionDetails.getTitle());

                                        //Find previous purchases
                                        queryPuchases();
                                    }
                                }
                        );

                        QueryProductDetailsParams queryProductDetailsParams2 =
                                QueryProductDetailsParams.newBuilder()
                                        .setProductList(
                                                List.of(
                                                        QueryProductDetailsParams.Product.newBuilder()
                                                                .setProductId("premium_subscription")
                                                                .setProductType(BillingClient.ProductType.SUBS)
                                                                .build()))
                                        .build();

                        billingClient.queryProductDetailsAsync(
                                queryProductDetailsParams2,
                                new ProductDetailsResponseListener() {
                                    public void onProductDetailsResponse(BillingResult billingResult,
                                                                         List<ProductDetails> productDetailsList) {

                                        if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                                            onPurchasesUpdated.onChange("error");
                                            return;

                                        }

                                        //
                                        if (productDetailsList.isEmpty()) {
                                            onPurchasesUpdated.onChange("error");
                                            return;
                                        }

                                        //Successfully connected and found product
                                        premiumSubcriptionDetails_pre = productDetailsList.get(0);
                                        Log.i("product_details", premiumSubcriptionDetails_pre.getTitle());

                                        //Find previous purchases
                                        queryPuchases();
                                    }
                                }
                        );
                    }

                    // One time purchase
                    {

                        for(String prodID : inAppProductList) {
                            queryProductDetailsParamsList.add(QueryProductDetailsParams.Product.newBuilder()
                                    .setProductId(prodID)
                                    .setProductType(BillingClient.ProductType.INAPP)
                                    .build());
                        }

                        queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                                .setProductList(queryProductDetailsParamsList)
                                .build();

                        billingClient.queryProductDetailsAsync(queryProductDetailsParams, new ProductDetailsResponseListener() {
                            @Override
                            public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull final List<ProductDetails> list) {


                                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                                    PremiumController.products = new HashMap<>();

                                    for(ProductDetails p : list ) {
                                        products.put(p.getProductId(), new OneTimePurchaseOffer(p));
                                        Log.e("billing", p.getProductId());

                                    }


                                    ProductDetails correctOne = list.get(0);
                                    for (ProductDetails d : list) {

                                        if (d.getProductId().equals(oneTimerPurchase)) {
                                            correctOne = d;
                                        }
                                    }

                                    PremiumController.premiumOneTimerDetails = correctOne;

                                    queryPuchases();

                                }
                            }

                        });


                    }

                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.e("billing", "billing disconnected!");
            }
        });


    }

    public void queryPuchases() {

        //Subscriptions
        QueryPurchasesParams.Builder params = QueryPurchasesParams.newBuilder();
        params.setProductType(BillingClient.ProductType.SUBS);

        billingClient.queryPurchasesAsync(params.build(), new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {

                if(billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                    Log.i("billing", "unable to retrieve previous purchases");
                    onPurchasesUpdated.onChange("error");
                    return;
                }

                if(list.isEmpty()) {
                    Log.i("billing", "No sub Purchases found!");
                    return;

                }


                for(Purchase p : list) {
                    if(p.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {

                        purchasedProductsList.add("premium_one_time");
                        onPurchasesUpdated.onChange(true);
                        Log.i("billing", "previous subscription found");

                        if(!p.isAcknowledged()) {

                            AcknowledgePurchaseParams acknowledgePurchaseParams =
                                    AcknowledgePurchaseParams.newBuilder()
                                            .setPurchaseToken(p.getPurchaseToken())
                                            .build();
                            billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                                @Override
                                public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                                    onPurchasesUpdated.onChange(true);
                                }
                            });
                        }

                    }
                }
            }
        });

        //Inapp purchase
        QueryPurchasesParams.Builder params2 = QueryPurchasesParams.newBuilder();
        params2.setProductType(BillingClient.ProductType.INAPP);

        billingClient.queryPurchasesAsync(params2.build(), new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {

                if(billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                    Log.i("billing", "unable to retrieve previous purchases");
                    onPurchasesUpdated.onChange("error");
                    return;
                }

                if(list.isEmpty()) {
                    Log.i("billing", "No In-app Purchases found!");
                    return;

                }

                boolean wasPurchased = false;
                for(Purchase p : list) {
                    if(p.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {

                        ConsumeParams c = ConsumeParams.newBuilder()
                                .setPurchaseToken(p.getPurchaseToken())
                                .build();

//                        billingClient.consumeAsync(c, new ConsumeResponseListener() {
//                            @Override
//                            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
//
//                            }
//                        });

                        for(String l : p.getProducts()) {

                            Log.i("billing", l);

                            purchasedProductsList.add(l);
                            OneTimePurchaseOffer offer = products.get(l);
                            if(offer != null) {
                                offer.isPurchased = true;
                                products.put(l, offer);
                            }
                        }

                        onPurchasesUpdated.onChange(true);
                        Log.i("billing", "previous purchase found");

                        if(!p.isAcknowledged()) {

                            AcknowledgePurchaseParams acknowledgePurchaseParams =
                                    AcknowledgePurchaseParams.newBuilder()
                                            .setPurchaseToken(p.getPurchaseToken())
                                            .build();
                            billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                                @Override
                                public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                        if (onPurchasesUpdated != null) {
                                            onPurchasesUpdated.onChange(true);
                                        }
                                    } else {
                                        //Try again when the user resumes the app
                                        acknowledgePurchaseUnsuccessful = true;

                                        if (onPurchasesUpdated != null) {
//                                            onPurchasesUpdated.onChange(true);
                                        }
                                    }
                                }
                            });
                        }

                    }
                }
            }
        });


    }

    public class OneTimePurchaseOffer {
        boolean isPurchased = false;
        String Title;
        String Description;
        String FormattedPrice;
        String ID;

        public ProductDetails productDetails;

        OneTimePurchaseOffer(ProductDetails productDetails) {
            this.productDetails = productDetails;
            Title = productDetails.getName();
            Description = productDetails.getDescription();
            FormattedPrice = productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice();
            ID = productDetails.getProductId();
        }

    }

    public class SubscriptionOffer {

        public String formattedPrice =" ";
        String sku = "";
        String token = "";
        SubscriptionOffer(ProductDetails.SubscriptionOfferDetails productDetails) {
            formattedPrice = productDetails.getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
            sku = productDetails.getBasePlanId();
            token = productDetails.getOfferToken();

            Log.i("product_details", toString());
        }

        @NonNull
        @Override
        public String toString() {
            return sku + ": " + formattedPrice;
        }
    }

    public HashMap<String, OneTimePurchaseOffer> getOptions() {
        return products;
    }

    public static boolean hasPermission(PremiumController.Product product) {

        if(!UserData.isPremiumMode) { return false; }
        if(purchasedProductsList.contains("premium_one_time")) {  return true; }

        if(product == Product.MotionControls && purchasedProductsList.contains("motion_controls")) {
            return true;
        }

        if(product == Product.CommandEditor && purchasedProductsList.contains("command_editor")) {
            return true;
        }

        if(product == Product.InfiniteButtons && purchasedProductsList.contains("infinite_buttons")) {
            return true;
        }

        return product == Product.UnlimitedSaves && purchasedProductsList.contains("unlimited_saves");
    }
}
