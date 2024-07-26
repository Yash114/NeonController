package com.gingertech.starbeam.helpers.controllers;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gingertech.starbeam.R;

public class PremiumSelection extends ConstraintLayout {

    ViewGroup root;
    OnGenericCallbackv2 selectedCallback;
    public PremiumController.SubscriptionOffer subscriptionOffer;
    int primaryColor;
    public String offerID;

    boolean selected = false;

    TypedArray typedArrayAttributes;


    public String price;
    String discount;
    boolean bestPrice;

    public PremiumSelection(@NonNull Context context) {
        super(context);
        init(context);
    }

    public PremiumSelection(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        typedArrayAttributes = context.obtainStyledAttributes(attrs, R.styleable.PremiumSelection, 0, 0);
        price = typedArrayAttributes.getString(R.styleable.PremiumSelection_Price);
        discount = typedArrayAttributes.getString(R.styleable.PremiumSelection_Discount);
        bestPrice = typedArrayAttributes.getBoolean(R.styleable.PremiumSelection_bestDeal, false);
        offerID = typedArrayAttributes.getString(R.styleable.PremiumSelection_OfferID);

        typedArrayAttributes.recycle();
        init(context);
    }

    public PremiumSelection(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        typedArrayAttributes = context.obtainStyledAttributes(attrs, R.styleable.PremiumSelection, 0, 0);
        price = typedArrayAttributes.getString(R.styleable.PremiumSelection_Price);
        discount = typedArrayAttributes.getString(R.styleable.PremiumSelection_Discount);
        bestPrice = typedArrayAttributes.getBoolean(R.styleable.PremiumSelection_bestDeal, false);
        offerID = typedArrayAttributes.getString(R.styleable.PremiumSelection_OfferID);

        typedArrayAttributes.recycle();
        init(context);
    }

    public PremiumSelection(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        typedArrayAttributes = context.obtainStyledAttributes(attrs, R.styleable.PremiumSelection, 0, 0);
        price = typedArrayAttributes.getString(R.styleable.PremiumSelection_Price);
        discount = typedArrayAttributes.getString(R.styleable.PremiumSelection_Discount);
        bestPrice = typedArrayAttributes.getBoolean(R.styleable.PremiumSelection_bestDeal, false);
        offerID = typedArrayAttributes.getString(R.styleable.PremiumSelection_OfferID);

        typedArrayAttributes.recycle();
        init(context);
    }

    void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = (ViewGroup) inflater.inflate(R.layout.premium_selection_view, this, true);

        primaryColor = context.getColor(R.color.primary);

        if(selectedCallback != null) {
            selectedCallback.onChange(this);
        }

        ((TextView) root.findViewById(R.id.bestValueTag)).setVisibility(bestPrice ? VISIBLE : GONE);
        ((TextView) root.findViewById(R.id.price)).setText("??? " + price);
        ((TextView) root.findViewById(R.id.savings)).setText(discount);

        selected(selected);
        root.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedCallback != null) {
                    selectedCallback.onChange(PremiumSelection.this);
                }

                selected = true;
                selected(selected);
            }
        });
    }

    public void selected(boolean selected) {
        ((TextView) root.findViewById(R.id.bestValueTag)).setTextColor(selected ? Color.BLACK : primaryColor);
        ((TextView) root.findViewById(R.id.price)).setTextColor(selected ? Color.BLACK : primaryColor);
        ((TextView) root.findViewById(R.id.savings)).setTextColor(selected ? Color.BLACK : primaryColor);
        post(new Runnable() {
            @Override
            public void run() {
                root.setBackgroundResource(selected ? R.drawable.golden : R.drawable.black_border);
            }
        });

    }

    public void setSelectedCallback(OnGenericCallbackv2 genericCallbackv2) {
        selectedCallback = genericCallbackv2;
    }

    public void setOffer(PremiumController.SubscriptionOffer offer) {
        subscriptionOffer = offer;

        String displayPrice = offer.formattedPrice + " " + price;

        ((TextView) root.findViewById(R.id.price)).setText(displayPrice);

    }
}
