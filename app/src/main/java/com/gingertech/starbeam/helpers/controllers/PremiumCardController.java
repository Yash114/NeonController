package com.gingertech.starbeam.helpers.controllers;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.Image;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.billingclient.api.ProductDetails;
import com.gingertech.starbeam.R;

public class PremiumCardController extends LinearLayout {

    View root;
    TypedArray typedArrayAttributes;

    public ProductDetails productDetails;
    public String ItemID;
    String Title;
    String Content;
    boolean purchased;
    int imageRef;

    public PremiumCardController(@NonNull Context context) {
        super(context);

        init(context);
    }

    public PremiumCardController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        typedArrayAttributes = context.obtainStyledAttributes(attrs, R.styleable.PremiumCardController, 0, 0);
        ItemID = typedArrayAttributes.getString(R.styleable.PremiumCardController_ItemID);
        imageRef = typedArrayAttributes.getResourceId(R.styleable.PremiumCardController_Image, 0);

        typedArrayAttributes.recycle();
        init(context);
    }

    public PremiumCardController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        typedArrayAttributes = context.obtainStyledAttributes(attrs, R.styleable.PremiumCardController, 0, 0);
        ItemID = typedArrayAttributes.getString(R.styleable.PremiumCardController_ItemID);
        imageRef = typedArrayAttributes.getResourceId(R.styleable.PremiumCardController_Image, 0);

        typedArrayAttributes.recycle();
        init(context);
    }

    public PremiumCardController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        typedArrayAttributes = context.obtainStyledAttributes(attrs, R.styleable.PremiumCardController, 0, 0);
        ItemID = typedArrayAttributes.getString(R.styleable.PremiumCardController_ItemID);
        imageRef = typedArrayAttributes.getResourceId(R.styleable.PremiumCardController_Image, 0);

        typedArrayAttributes.recycle();
        init(context);
    }

    void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = inflater.inflate(R.layout.premium_card, this, true);

        root.findViewById(R.id.backgroundView).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(setOnSelectedListener != null) {
                    setOnSelectedListener.onChange(productDetails);
                }
            }
        });

    }

    GenericCallbackv2 setOnSelectedListener;
    public void setOnSelectedListener(GenericCallbackv2 onTouchListener) {
        this.setOnSelectedListener = onTouchListener;
    }

    public void setDetails(PremiumController.OneTimePurchaseOffer productDetails) {

        ((TextView) root.findViewById(R.id.title)).setText(productDetails.Title);
        ((TextView) root.findViewById(R.id.content)).setText(productDetails.Description);
        ((ImageView) root.findViewById(R.id.image)).setImageResource(imageRef);
        ((TextView) root.findViewById(R.id.purchase)).setText("Get Now!");

        setPurchased(productDetails.isPurchased);

        this.productDetails = productDetails.productDetails;
    }

    private void setPurchased(boolean purchased) {

        root.findViewById(R.id.purchase).setVisibility(purchased ? GONE : VISIBLE);
        root.findViewById(R.id.alreadyHave).setVisibility(!purchased ? GONE : VISIBLE);

    }
}
