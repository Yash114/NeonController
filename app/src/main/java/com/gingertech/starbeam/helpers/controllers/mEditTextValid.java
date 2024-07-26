package com.gingertech.starbeam.helpers.controllers;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.ButtonHandler;
import com.gingertech.starbeam.helpers.UserData;

import java.util.ArrayList;
import java.util.Arrays;

public class mEditTextValid extends LinearLayout {

    View root;

    CommandContainer forCommand;

    ArrayList<String> autoComplete = new ArrayList<>();

    public mEditTextValid(Context context, CommandContainer forCommand) {
        super(context);
        this.forCommand = forCommand;
        init(context);
    }

    public mEditTextValid(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public mEditTextValid(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public mEditTextValid(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = inflater.inflate(R.layout.m_edit_text, this, true);
        AutoCompleteTextView autoCompleteTextView = root.findViewById(R.id.editText);
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateValidation();
            }
        });

        switch (forCommand.keybindType) {
            case (CommandContainer.AUTO_NORMAL):
                autoComplete.addAll(Arrays.asList(ButtonHandler.keyCodesForNormal));
                break;

            case (CommandContainer.AUTO_LAYOUTS):
                UserData.Layouts.forEach(l -> autoComplete.add(l.name));
                break;

            case (CommandContainer.AUTO_COMMANDS):
                UserData.Commands.forEach((a,b) -> autoComplete.add(a));
                break;

            case (CommandContainer.AUTO_MOVEABLES):
                autoComplete.addAll(Arrays.asList(ButtonHandler.keyCodesForMoveables));
                break;

            case (CommandContainer.AUTO_NONE):
                break;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, autoComplete);

        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setHint("eg: " + forCommand.valueHint);

        root.findViewById(R.id.cb).setActivated(false);

    }

    public AutoCompleteTextView getEditText() {
        return root.findViewById(R.id.editText);
    }

    public void updateValidation() {

        boolean b = forCommand.keybindType != CommandContainer.AUTO_NONE && autoComplete.contains(getEditText().getText().toString());

        root.findViewById(R.id.cb).setActivated(b);
    }

}
