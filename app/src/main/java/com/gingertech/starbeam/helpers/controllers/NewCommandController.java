package com.gingertech.starbeam.helpers.controllers;

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
import com.gingertech.starbeam.helpers.ButtonHandler;
import com.gingertech.starbeam.helpers.Command;
import com.gingertech.starbeam.helpers.LayoutClass;
import com.gingertech.starbeam.helpers.SaveClass;
import com.gingertech.starbeam.helpers.UserData;


public class NewCommandController extends ConstraintLayout {

    View root;

    Vibrator vibrator;

    public NewCommandController(@NonNull Context context) {
        super(context);

        init(context);
    }

    public NewCommandController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public NewCommandController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    public NewCommandController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context);
    }

    void init(Context context) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = inflater.inflate(R.layout.new_command_popup, this, true);

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

                EditText nameObj = root.findViewById(R.id.command_name);
                EditText descObj = root.findViewById(R.id.command_description);

                String name = nameObj.getText().toString();
                String descri = descObj.getText().toString();

                if(name.length() == 0 || descri.length() == 0) {
                    vibrator.vibrate(400);
                    Toast.makeText(context, R.string.Please_fill_out_both, Toast.LENGTH_LONG).show();
                    return;
                }

                ArraySet<String> commandNames = new ArraySet<>();
                for(Command command : UserData.Commands.values()) {
                    commandNames.add(command.name);
                }

                if(commandNames.contains(name)) {
                    vibrator.vibrate(400);
                    Toast.makeText(context, R.string.You_have_already_used, Toast.LENGTH_LONG).show();
                    return;
                }

                if(name.contains("<") ||name.contains(">") || name.contains(":") || name.contains("&")  || name.contains("?") || name.contains("\"")) {
                    vibrator.vibrate(400);
                    Toast.makeText(context, R.string.You_cannot_use_special, Toast.LENGTH_LONG).show();
                    return;
                }

                if(ButtonHandler.validate(name, ButtonID.Normal)) {
                    vibrator.vibrate(400);
                    Toast.makeText(context, R.string.This_is_a_reserved_name, Toast.LENGTH_LONG).show();
                    return;
                }

                if(!name.toLowerCase().equals(name)) {
                    vibrator.vibrate(400);
                    Toast.makeText(context, R.string.Command_names_must_be, Toast.LENGTH_LONG).show();
                    return;
                }

                root.setVisibility(GONE);

                Command command = new Command(name, descri);
                UserData.Commands.put(command.name, command);
                SaveClass.SaveCommands(context);

                SaveClass.SaveCommand(context, command);

                if(submitListener != null) {
                    submitListener.onChange(command);
                }
            }
        });
    }

    GenericCallback closeListener;
    public void setOnCloseListener(GenericCallback genericCallback) {
        closeListener = genericCallback;
    }

    GenericCallbackv2 submitListener;
    public void setOnSubmitListener(GenericCallbackv2 genericCallback) {
        submitListener = genericCallback;
    }
}
