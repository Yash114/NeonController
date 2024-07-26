package com.gingertech.starbeam.helpers.controllers;

import static com.gingertech.starbeam.helpers.controllers.RemapClass.AutoCorrectType.SCALAR;
import static com.gingertech.starbeam.helpers.controllers.RemapClass.AutoCorrectType.VECTOR;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.ButtonHandler;
import com.gingertech.starbeam.helpers.UserData;

import java.util.ArrayList;
import java.util.Arrays;

public class RemapListItemController extends ConstraintLayout {

    public View root;

    Remap thisRemap;

    public RemapListItemController(@NonNull Context context, Remap remap) {
        super(context);

        thisRemap = remap;
        init(context);
    }

    public RemapListItemController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RemapListItemController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public RemapListItemController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    ArrayList<String> autoComplete = new ArrayList<>();

    TextView keybindTitle;
    AutoCompleteTextView keybindValue;

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = inflater.inflate(R.layout.controller_assignment_list_item, this, true);

        if(thisRemap == null) { return; }

        keybindTitle = root.findViewById(R.id.controllerButtonName);

        keybindTitle.setText(thisRemap.controllerKey.name());

        keybindValue = root.findViewById(R.id.controllerButtonValue);

        switch (thisRemap.correctType) {
            case VECTOR:
                autoComplete.addAll(Arrays.asList(ButtonHandler.keycodesForJoystick));
                break;

            case SCALAR:
                UserData.Commands.forEach((a, b) -> autoComplete.add("\"" + a + "\""));
                autoComplete.addAll(Arrays.asList(ButtonHandler.keyCodesForNormal));
                break;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, autoComplete);

        keybindValue.setAdapter(adapter);
        keybindValue.setHint("eg: " + (thisRemap.equals(VECTOR) ? "mouse" : "space"));
        keybindValue.setText(thisRemap.controllerButtonKeybind);
    }

    public Remap getRemap() {
        return new Remap(thisRemap.controllerKey, keybindValue.getText().toString());
    }
}
