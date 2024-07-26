package com.gingertech.starbeam.helpers.controllers;

import static com.gingertech.starbeam.MainActivity.uiFlag;

import android.content.Context;
import android.os.Vibrator;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.LayoutClass;
import com.gingertech.starbeam.helpers.LayoutGroupClass;
import com.gingertech.starbeam.helpers.SaveClass;
import com.gingertech.starbeam.helpers.UserData;

import java.util.Locale;

public class PopupController extends ConstraintLayout {
    public PopupController(@NonNull Context context) {
        super(context);
        init(context);
    }

    public PopupController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PopupController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public PopupController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    Vibrator vibrator;

    ViewGroup root;
    void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = (ViewGroup) inflater.inflate(R.layout.new_layout_popup, this, true);

        vibrator = (Vibrator) root.getContext().getSystemService(Context.VIBRATOR_SERVICE);

        root.findViewById(R.id.xbutton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(closeListener != null) {
                    closeListener.fire(null);
                }

                root.setVisibility(GONE);

            }
        });

        root.findViewById(R.id.createButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText nameObj = root.findViewById(R.id.layout_name);
                EditText descObj = root.findViewById(R.id.layout_descritp);

                String name = nameObj.getText().toString();
                String descri = descObj.getText().toString();

                if(name.length() == 0 || descri.length() == 0) {
                    vibrator.vibrate(400);
                    Toast.makeText(context, R.string.Please_fill_out_both, Toast.LENGTH_LONG).show();
                    return;
                }

                ArraySet<String> layoutNames = new ArraySet<>();
                for(LayoutClass layouts : UserData.Layouts) {
                    layoutNames.add(layouts.name);
                }

                if(layoutNames.contains(name)) {
                    vibrator.vibrate(400);
                    Toast.makeText(context, R.string.You_have_already_used, Toast.LENGTH_LONG).show();
                    return;
                }

                if(name.contains(" ")) {
                    vibrator.vibrate(400);
                    Toast.makeText(context, R.string.Please_do_not_use_spaces, Toast.LENGTH_LONG).show();
                    return;
                }

                if(submitListener != null) {
                    submitListener.fire(new LayoutClass(name, descri, PopupController.this.getContext()));
                }

                root.setVisibility(GONE);

                UserData.layoutsCreated += 1;

                if(getContext() != null) {
                    SaveClass.SaveFlags(getContext());
                }

            }
        });

        root.findViewById(R.id.createGroup).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText nameObj = root.findViewById(R.id.layout_name);
                EditText descObj = root.findViewById(R.id.layout_descritp);

                String name = nameObj.getText().toString();
                String descri = descObj.getText().toString();

                if(name.length() == 0 || descri.length() == 0) {
                    vibrator.vibrate(400);
                    Toast.makeText(context, R.string.Please_fill_out_both, Toast.LENGTH_LONG).show();
                    return;
                }

                ArraySet<String> layoutNames = new ArraySet<>();
                for(LayoutClass layouts : UserData.Layouts) {
                    layoutNames.add(layouts.name);
                }

                if(layoutNames.contains(name)) {
                    vibrator.vibrate(400);
                    Toast.makeText(context, R.string.You_have_already_used, Toast.LENGTH_LONG).show();
                    return;
                }

                if(name.contains(" ")) {
                    vibrator.vibrate(400);
                    Toast.makeText(context, R.string.Please_do_not_use_spaces, Toast.LENGTH_LONG).show();
                    return;
                }

                if(submitListener != null) {
                    submitListener.fire(new LayoutGroupClass(name, descri));
                }

                root.setVisibility(GONE);

                UserData.layoutsCreated += 1;

                if(getContext() != null) {
                    SaveClass.SaveFlags(getContext());
                }

            }
        });
    }

    GenericCallback closeListener;
    public void setOnCloseListener(GenericCallback genericCallback) {
        closeListener = genericCallback;
    }

    GenericCallback submitListener;
    public void setOnSubmitListener(GenericCallback genericCallback) {
        submitListener = genericCallback;
    }
}
