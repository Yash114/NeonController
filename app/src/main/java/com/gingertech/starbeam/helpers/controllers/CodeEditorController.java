package com.gingertech.starbeam.helpers.controllers;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

import com.gingertech.starbeam.helpers.Command;

import androidx.annotation.Nullable;

public class CodeEditorController extends LinearLayout {

    public CodeEditorController(Context context) {
        super(context);
        init(context);
    }

    public CodeEditorController(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CodeEditorController(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public CodeEditorController(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    void init(Context context) {
        this.setOrientation(VERTICAL);
    }

    public void setCommands(Context c, Command command) {
        for(Command.MainCode s : command.MainCodeBlocks) {

            MainCodeBlock codeBlock = new MainCodeBlock(c);
            codeBlock.setBlockType(0, s.Command);
            codeBlock.addCommandContainers(s.commandContainers);
            this.addView(codeBlock);
        }
    }

    public void setOnSelectCallback(GenericCallbackv2 selectCallback) {

        final int count = this.getChildCount();
        for (int i = 0; i < count; i++) {
            ((MainCodeBlock) this.getChildAt(i)).setOnSelectCallback(new GenericCallbackv2() {
                @Override
                public void onChange(Object value) {
                    selectCallback.onChange(value);
                }
            });
        }
    }
}
