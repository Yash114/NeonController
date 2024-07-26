package com.gingertech.starbeam.helpers.controllers;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.gingertech.starbeam.R;
import com.gingertech.starbeam.ui.CommandsEditorPage;

import java.util.ArrayList;

public class MainCodeBlock extends RelativeLayout {

    public final static int ONBLOCK = 0;
    public final static int COMMANDBLOCK = 1;

    public int type = ONBLOCK;

    public String code;
    private final boolean highlighted = false;

    private GenericCallbackv2 movingBlockCallback;

    ViewGroup root;
    LinearLayout codeBlockList;

    CodeBlock tempBlock;
    int tempBlockClosest = -1;


    public MainCodeBlock(Context context) {
        super(context);
        init(context);
    }

    public MainCodeBlock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MainCodeBlock(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public MainCodeBlock(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    void init(Context context) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = (ViewGroup) inflater.inflate(R.layout.main_code_block, this, true);
        codeBlockList = root.findViewById(R.id.codeBlocks);
    }

    void addCommandContainers(ArrayList<CommandContainer> code) {

        for(CommandContainer cc : code) {

            CodeBlock codeBlock = new CodeBlock(getContext(), cc);
            codeBlock.setOnTouchListener(new CodeBlockTouch());
            codeBlockList.addView(codeBlock);
        }
    }

    public void setBlockType(int type, String Command) {
        code = Command + ": [";
        ((TextView) root.findViewById(R.id.command)).setText(code);
    }

    public void highlight(@Nullable CodeBlock codeBlock) {

        if(codeBlock == null && tempBlock != null) {
            codeBlockList.removeView(tempBlock);
            tempBlock = null;

            if(selectCallback != null) {
                selectCallback.onChange(null);
            }

        } else if(codeBlock != null) {

            if(tempBlock == null) {

                tempBlock = new CodeBlock(getContext(), codeBlock.commandContainer.type);
                tempBlockClosest = -1;
                tempBlock.select();
                codeBlockList.addView(tempBlock, 0);

                if(selectCallback != null) {
                    selectCallback.onChange(tempBlock);
                }
            }

            CodeBlock block;
            int[] location = {0,0};
            double distance = 1000;
            int closestIndex = 0;

            for(int i = 0; i < codeBlockList.getChildCount(); i++) {

                block = (CodeBlock) codeBlockList.getChildAt(i);
                block.getLocationOnScreen(location);

                double d = Math.sqrt(Math.pow(location[0] - codeBlock.getX(), 2) + Math.pow(location[1] - codeBlock.getY(), 2));
                if(d < distance) {
                    distance = d;
                    closestIndex = i;
                }
            }
            if(codeBlockList.getChildAt(closestIndex) != tempBlock) {
                codeBlockList.removeView(tempBlock);
                tempBlockClosest = closestIndex;
                tempBlock.select();
                codeBlockList.addView(tempBlock, tempBlockClosest);
            }

        }
    }

    public void highlight_local(@Nullable CodeBlock codeBlock) {

        if(codeBlock == null && tempBlock != null) {
            codeBlockList.removeView(tempBlock);
            tempBlock = null;

            if(selectCallback != null) {
                selectCallback.onChange(null);
            }

        } else if(codeBlock != null) {

            CodeBlock block;
            int[] location = {0,0};
            double distance = 1000;
            int closestIndex = -1;

            for(int i = 0; i < codeBlockList.getChildCount(); i++) {

                block = (CodeBlock) codeBlockList.getChildAt(i);
                location[0] = (int) block.getX();
                location[1] = (int) block.getY();

                double d = Math.sqrt(Math.pow(location[0] - codeBlock.getX(), 2) + Math.pow(location[1] - codeBlock.getY(), 2));
                if(d < distance && d < 200) {
                    distance = d;
                    closestIndex = i;
                }
            }
            if(closestIndex == -1) {
                codeBlockList.removeView(tempBlock);
                tempBlock = null;
                tempBlockClosest = -1;
                return;
            }

            if(tempBlockClosest == closestIndex) {
                return;
            }

            if(tempBlock != null) {
                codeBlockList.removeView(tempBlock);
            }


            tempBlockClosest = closestIndex;

            tempBlock = new CodeBlock(getContext(), codeBlock.commandContainer.type);

            if (selectCallback != null) {
                selectCallback.onChange(tempBlock);
            }

            tempBlock.select();
            codeBlockList.addView(tempBlock, tempBlockClosest);

        }
    }

    public CodeBlock place(@Nullable CodeBlock codeBlock) {

        CodeBlock c = null;

        if(codeBlock != null) {

            for(int i = 0; i < codeBlockList.getChildCount(); i++) {
                ((CodeBlock) codeBlockList.getChildAt(i)).deselect();
            }

            c = new CodeBlock(getContext(), codeBlock.commandContainer);

            c.setOnSelectedListener(new GenericCallbackv2() {
                @Override
                public void onChange(Object value) {

                    if(selectCallback != null) {
                        selectCallback.onChange(value);
                    }

                    for(int i = 0; i < codeBlockList.getChildCount(); i++) {

                        if(value != codeBlockList.getChildAt(i)) {
                            ((CodeBlock) codeBlockList.getChildAt(i)).deselect();
                        }
                    }
                }
            });

            if(selectCallback != null) {
                selectCallback.onChange(c);
            }

            c.setOnTouchListener(new CodeBlockTouch());
            codeBlockList.addView(c, tempBlockClosest);
            c.select();


        } else {
            if(selectCallback != null) {
                selectCallback.onChange(null);
            }
        }

        if(tempBlock != null) {
            codeBlockList.removeView(tempBlock);
        }

        tempBlock = null;

        if(movingBlockCallback != null) {
            movingBlockCallback.onChange(false);
        }

        tempBlockClosest = -1;

        return c;
    }


    GenericCallbackv2 selectCallback;
    public void setOnSelectCallback(GenericCallbackv2 onSelectCallback) {
        this.selectCallback = onSelectCallback;
    }

    public void deselectAll() {
        for(int i = 0; i < codeBlockList.getChildCount(); i++) {
            ((CodeBlock) codeBlockList.getChildAt(i)).deselect();
        }
    }

    public String toString() {
        LinearLayout codeBlocks = root.findViewById(R.id.codeBlocks);
        StringBuilder output = new StringBuilder();

        output.append(code).append("\n");

        for(int i = 0; i < codeBlocks.getChildCount(); i++){
            CodeBlock b = (CodeBlock) codeBlocks.getChildAt(i);

            if(b.getVisibility() == VISIBLE) {
                output.append("\t\t\t\t").append(b.commandContainer.getText()).append("\n");
            }
        }

        output.append("]").append("\n");

        return output.toString();
    }

    class CodeBlockTouch implements OnTouchListener {

        float x, y;
        boolean isMoving = false;
        CodeBlock codeBlock, newCodeBlock;


        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                codeBlock = (CodeBlock) view;

                x = motionEvent.getX();
                y = motionEvent.getY();
                isMoving = false;

            }
            if(motionEvent.getAction() == MotionEvent.ACTION_MOVE) {

                float X = motionEvent.getX() - x;
                float Y = motionEvent.getY() - y;

                if (Math.sqrt( Math.pow(X, 2) + Math.pow(Y, 2) ) > 20 && !isMoving) {

                    view.setVisibility(GONE);

                    isMoving = true;

                    newCodeBlock = new CodeBlock(getContext(), codeBlock.commandContainer);

                    root.addView(newCodeBlock);
                    newCodeBlock.setX(view.getX());
                    newCodeBlock.setY(view.getY());

                    movingBlockCallback.onChange(true);
                }

                if(isMoving) {
                    x = motionEvent.getX();
                    y = motionEvent.getY();

                    newCodeBlock.setX(newCodeBlock.getX() + X);
                    newCodeBlock.setY(newCodeBlock.getY() + Y);

                    highlight_local(newCodeBlock);
                }
            }

            if(motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {

                if(isMoving) {
                    root.removeView(newCodeBlock);
                    root.removeView(view);

                    if(tempBlockClosest != -1) {
                        place(codeBlock);
                    }
                } else {

                    if(selectCallback != null) {
                        selectCallback.onChange(codeBlock);
                    }

                    codeBlock.select();
                }

                movingBlockCallback.onChange(false);

            }

            return true;
        }
    }

    public void setOnMovingCallback(GenericCallbackv2 onMovingCallback) {
        movingBlockCallback = onMovingCallback;
    }
}
