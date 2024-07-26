package com.gingertech.starbeam.helpers.controllers;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.gingertech.starbeam.R;

import java.util.ArrayList;

public class CodeBlock extends RelativeLayout {

    public CommandContainer commandContainer;

    int type = -1;

    boolean selected = false;
    public boolean defaultOnly = true;

    CommandContainer ref;

    int selectColor, normalColor;


    ViewGroup root;


    public CodeBlock(Context context) {
        super(context);

        init(context);
    }

    public CodeBlock(Context context, int type) {
        super(context);

        this.type = type;
        init(context);
    }

    public CodeBlock(Context context, CommandContainer reference) {
        super(context);

        this.ref = reference;
        init(context);
    }


    public CodeBlock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CodeBlock(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    void init(Context context) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        selectColor = context.getColor(R.color.light_secondary);
        normalColor = context.getColor(R.color.primary);

        root = (ViewGroup) inflater.inflate(R.layout.code_block, this, true);

        if(ref != null) {
            this.commandContainer = new CommandContainer(context, ref);
            update();
        }

        if(type != -1) {
            this.commandContainer = new CommandContainer(context, type);
            update();
        }


        root.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if(selectedCallback != null) {

                    selectedCallback.onChange(CodeBlock.this);
                }

                selected = !selected;

                ((TextView) root.findViewById(R.id.command)).setTextColor(selected ? Color.RED : Color.BLACK);
            }
        });
    }

    public void setCommand(Context context, int type) {

        commandContainer = new CommandContainer(context, type);

        ((TextView) root.findViewById(R.id.command)).setText(commandContainer.command);

        TextView parameterField = root.findViewById(R.id.parameter);
        if(commandContainer.hasParameter) {
            parameterField.setVisibility(VISIBLE);
            parameterField.setText(String.valueOf(commandContainer.getParameter(true)));
        } else {
            parameterField.setVisibility(GONE);
        }

        TextView valuesField = root.findViewById(R.id.values);
        if(commandContainer.hasValues) {
            valuesField.setVisibility(VISIBLE);

            valuesField.setText(commandContainer.getValues(true));

        } else {
            valuesField.setVisibility(GONE);
        }

    }

    public void deselect() {
        selected = false;
        root.findViewById(R.id.background).setBackgroundTintList(ColorStateList.valueOf(normalColor));

    }

    public void select() {
        selected = true;
        root.findViewById(R.id.background).setBackgroundTintList(ColorStateList.valueOf(selectColor));
    }

    GenericCallbackv2 selectedCallback;
    public void setOnSelectedListener(GenericCallbackv2 genericCallbackv2) {
        selectedCallback = genericCallbackv2;
    }

    public String setParameter(int parameter) {
        if(!this.commandContainer.hasParameter) { return getResources().getString(R.string.command_doesnt_take_any_parameters); }

        if(!(this.commandContainer.maxParameter >= parameter) || parameter < 0) { return getResources().getString(R.string.value_out_of_bounds); }

        commandContainer.parameter = parameter;

        TextView parameterField = root.findViewById(R.id.parameter);
        if(commandContainer.hasParameter) {
            parameterField.setVisibility(VISIBLE);
            parameterField.setText(String.valueOf(commandContainer.getParameter(false)));
        } else {
            parameterField.setVisibility(GONE);
        }

        return null;
    }

    public void update() {

        ((TextView) root.findViewById(R.id.command)).setText(commandContainer.command + ":");

        TextView parameterField = root.findViewById(R.id.parameter);
        if(commandContainer.hasParameter) {
            parameterField.setVisibility(VISIBLE);
            parameterField.setText(String.valueOf(commandContainer.getParameter(false)));
        } else {
            parameterField.setVisibility(GONE);
        }

        TextView valuesField = root.findViewById(R.id.values);
        if(commandContainer.hasValues) {
            valuesField.setVisibility(VISIBLE);

            valuesField.setText(commandContainer.getValues(false));

        } else {
            valuesField.setVisibility(GONE);
        }

        defaultOnly = false;

    }

    public void defaultOnly() {
        defaultOnly = true;
    }

}
