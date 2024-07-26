package com.gingertech.starbeam.helpers.controllers;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.Command;

public class CommandListItemController extends ConstraintLayout{

    View root;

    boolean selected = false;
    boolean slided = false;

    public Command command;

    public CommandListItemController(@NonNull Context context, Command command) {
        super(context);

        this.command = command;
        init(context);
    }

    public CommandListItemController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CommandListItemController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public CommandListItemController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void init(Context context) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = inflater.inflate(R.layout.command_list_item, this, true);

        ((TextView) root.findViewById(R.id.command_name)).setText(command.name);
        ((TextView) root.findViewById(R.id.command_description)).setText(command.description);
        root.findViewById(R.id.downloaded).setVisibility(command.isDownloaded ? VISIBLE : GONE);


        root.findViewById(R.id.editButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editCallback != null) {
                    editCallback.onChange(command);
                }
            }
        });

        root.findViewById(R.id.deleteButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(deleteCallback != null) {
                    deleteCallback.onChange(command);
                }
            }
        });
    }


    GenericCallbackv2 editCallback;
    GenericCallbackv2 deleteCallback;

    public void setOnEditCallback(GenericCallbackv2 selectedListener) {
        this.editCallback = selectedListener;
    }

    public void setOnDeleteCallback(GenericCallbackv2 selectedListenerCallback) {
        this.deleteCallback = selectedListenerCallback;
    }

}
