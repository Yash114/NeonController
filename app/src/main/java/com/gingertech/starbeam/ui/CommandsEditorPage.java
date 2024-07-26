package com.gingertech.starbeam.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.ArraySet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.gingertech.starbeam.MainActivity;
import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.ButtonHandler;
import com.gingertech.starbeam.helpers.Command;
import com.gingertech.starbeam.helpers.SaveClass;
import com.gingertech.starbeam.helpers.UserData;
import com.gingertech.starbeam.helpers.controllers.ButtonID;
import com.gingertech.starbeam.helpers.controllers.CodeBlock;
import com.gingertech.starbeam.helpers.controllers.CodeEditorController;
import com.gingertech.starbeam.helpers.controllers.CommandContainer;
import com.gingertech.starbeam.helpers.controllers.FireFunctions;
import com.gingertech.starbeam.helpers.controllers.GenericCallbackv2;
import com.gingertech.starbeam.helpers.controllers.MainCodeBlock;
import com.gingertech.starbeam.helpers.controllers.MixPanel;
import com.gingertech.starbeam.helpers.controllers.PremiumCardController;
import com.gingertech.starbeam.helpers.controllers.PremiumController;
import com.gingertech.starbeam.helpers.controllers.mEditTextValid;
import com.gingertech.starbeam.helpers.controllers.mScroll;
import com.google.android.material.snackbar.Snackbar;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class CommandsEditorPage extends Fragment {

    View root;

    LinearLayout CommandBlockList;
    CodeEditorController CodeEditor;
    mScroll lockableScroll;
    InputMethodManager imm;

    CommandContainer[] commandBlocks;
    Command command;

    TextView commandDescription, parameterTitle, valueTitle;
    EditText parameterInput;
    LinearLayout keybindInput;

    CodeBlock selectedCodeBlock;

    TextView blockoutText;

    GenericCallbackv2 hideUICallback;

    Vibrator vibrator;

    View commandNameEdit;

    boolean saving = false;

    private MixpanelAPI mp;

    public CommandsEditorPage(Command command, GenericCallbackv2 genericCallbackv2) {
        this.command = command;
        this.hideUICallback = genericCallbackv2;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        UserData.CurrentFragment = UserData.COMMANDS_EDITOR;
        mp = MixPanel.makeObj(requireContext());

        root = inflater.inflate(R.layout.command_editor_fragment, container, false);

        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

        ((TextView) root.findViewById(R.id.CommandName)).setText(command.name);

        imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        root.findViewById(R.id.nameEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText nameObj = root.findViewById(R.id.command_name);
                EditText descObj = root.findViewById(R.id.command_description);

                nameObj.setText(command.name);
                descObj.setText(command.description);

                commandNameEdit.setVisibility(View.VISIBLE);
            }
        });

        commandNameEdit = root.findViewById(R.id.commandNameEdit);

        commandNameEdit.findViewById(R.id.xbutton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                commandNameEdit.setVisibility(View.GONE);
            }
        });
        commandNameEdit.findViewById(R.id.submitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText nameObj = root.findViewById(R.id.command_name);
                EditText descObj = root.findViewById(R.id.command_description);

                String name = nameObj.getText().toString();
                String descri = descObj.getText().toString();

                if(name.length() == 0 || descri.length() == 0) {
                    vibrator.vibrate(400);
                    Toast.makeText(requireContext(), R.string.Please_fill_out_both, Toast.LENGTH_LONG).show();
                    return;
                }

                ArraySet<String> commandNames = new ArraySet<>();
                for(Command command : UserData.Commands.values()) {
                    commandNames.add(command.name);
                }

                if(commandNames.contains(name)) {
                    vibrator.vibrate(400);
                    Toast.makeText(requireContext(), R.string.You_have_already_used, Toast.LENGTH_LONG).show();
                    return;
                }

                if(name.contains("<") ||name.contains(">") || name.contains(":") || name.contains("&")  || name.contains("?") || name.contains("\"")) {
                    vibrator.vibrate(400);
                    Toast.makeText(requireContext(), R.string.You_cannot_use_special, Toast.LENGTH_LONG).show();
                    return;
                }

                if(ButtonHandler.validate(name, ButtonID.Normal)) {
                    vibrator.vibrate(400);
                    Toast.makeText(requireContext(), R.string.This_is_a_reserved_name, Toast.LENGTH_LONG).show();
                    return;
                }

                if(!name.toLowerCase().equals(name)) {
                    vibrator.vibrate(400);
                    Toast.makeText(requireContext(), R.string.Command_names_must_be, Toast.LENGTH_LONG).show();
                    return;
                }

                command.name = nameObj.getText().toString();
                command.description = descObj.getText().toString();

                SaveClass.SaveCommand(requireContext(), command);

                UserData.Commands.put(command.name, command);
                SaveClass.SaveCommands(requireContext());

                ((TextView) root.findViewById(R.id.CommandName)).setText(command.name);

                Toast.makeText(requireContext(), R.string.Saved, Toast.LENGTH_SHORT).show();

                commandNameEdit.setVisibility(View.GONE);

                vibrator.vibrate(100);

            }
        });

        root.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedCodeBlock == null) { return; };
                if(selectedCodeBlock.defaultOnly) { return; };

                MixPanel.mpEventTracking(mp, "Deleted_Command", null);

                ViewGroup parent = ((ViewGroup) selectedCodeBlock.getParent());
                if(parent != null) {
                    parent.removeView(selectedCodeBlock);
                }

            }
        });
        root.findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(10);

                MixPanel.mpEventTracking(mp, "Edited_Code_Editor", null);

                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

                FragmentTransaction trans = fragmentManager.beginTransaction();

                trans.setCustomAnimations(R.anim.slide_up_in, R.anim.slide_up_out);

                trans.replace(R.id.nav_host_fragment, new CommandsPage(hideUICallback)).commit();
            }
        });

        root.findViewById(R.id.saveButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MainCodeBlock m;
                StringBuilder allCode = new StringBuilder();
                for (int i = 0; i < CodeEditor.getChildCount(); i++) {
                    m = (MainCodeBlock) CodeEditor.getChildAt(i);
                    allCode.append(m.toString()).append("\n");
                }

                command.updateCode(allCode.toString());

                SaveClass.SaveCommand(requireContext(), command);
//                FireFunctions.saveCode(command);

                vibrator.vibrate(10);

                if (!saving) {

                    HashMap<String, String> codeWritten = new HashMap<>();
                    codeWritten.put("codeWritten", command.code);
                    codeWritten.put("codeName", command.name);
                    codeWritten.put("codeDescription", command.description);

                    MixPanel.mpEventTracking(mp, "Saved_Commands", codeWritten);

                    Toast.makeText(requireContext(), R.string.Saved, Toast.LENGTH_SHORT).show();

                    saving = true;

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            saving = false;
                        }
                    }, 1000);
                }
            }
        });

        blockoutText = root.findViewById(R.id.blockoutText);

        commandDescription = root.findViewById(R.id.description);

        parameterTitle = root.findViewById(R.id.parameterTitle);
        valueTitle = root.findViewById(R.id.valuesTitle);

        parameterInput = root.findViewById(R.id.parameter);
        keybindInput = root.findViewById(R.id.keybinds);


        ((EditText) keybindInput.getChildAt(0)).setOnEditorActionListener(new myOnEditorAction());

        commandBlocks = new CommandContainer[]{
                new CommandContainer(requireContext(), CommandContainer.TAP),
                new CommandContainer(requireContext(), CommandContainer.PRESS),
                new CommandContainer(requireContext(), CommandContainer.RELEASE),
                new CommandContainer(requireContext(), CommandContainer.DELAY),
                new CommandContainer(requireContext(), CommandContainer.eGYRO),
                new CommandContainer(requireContext(), CommandContainer.dGYRO),

                new CommandContainer(requireContext(), CommandContainer.eTRACKPAD),

                new CommandContainer(requireContext(), CommandContainer.sLAYOUT),
                new CommandContainer(requireContext(), CommandContainer.rLAYOUT),
                new CommandContainer(requireContext(), CommandContainer.sTEXT),
                new CommandContainer(requireContext(), CommandContainer.rTEXT),

                new CommandContainer(requireContext(), CommandContainer.aMouse),
                new CommandContainer(requireContext(), CommandContainer.rMouse),

                new CommandContainer(requireContext(), CommandContainer.eMouseClick),
                new CommandContainer(requireContext(), CommandContainer.dMouseClick),

                new CommandContainer(requireContext(), CommandContainer.COMMAND),
        };


        lockableScroll = root.findViewById(R.id.commandListScroll);

        CodeEditor = root.findViewById(R.id.codeEditor);
        command.setCode(requireContext());

        CodeEditor.setCommands(requireContext(), command);

        MainCodeBlock m;
        for (int i = 0; i < CodeEditor.getChildCount(); i++) {
            m = (MainCodeBlock) CodeEditor.getChildAt(i);
            m.setOnSelectCallback(new GenericCallbackv2() {
                @Override
                public void onChange(Object value) {
                    CommandsEditorPage.this.onChange((CodeBlock) value);
                    vibrator.vibrate(10);
                }
            });

            m.setOnMovingCallback(new GenericCallbackv2() {
                @Override
                public void onChange(Object value) {

                    ((mScroll) root.findViewById(R.id.codeScroll)).setScrollingEnabled(!(boolean) value);
                }
            });
        }

        CommandBlockList = root.findViewById(R.id.commands);

        parameterInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    if (selectedCodeBlock == null) {
                        return true;
                    }

                    try {
                        int parameterValue = Integer.parseInt(textView.getText().toString());
                        String returnError = selectedCodeBlock.setParameter(parameterValue);
                        if (returnError != null) {
                            Toast.makeText(requireContext(), returnError, Toast.LENGTH_SHORT).show();
                            textView.setText("0");
                        }
                    } catch (NumberFormatException e) {

                    }

                    hideKeyboardAndUI();
                }

                return false;
            }
        });

        CommandBlockList.removeAllViews();
        for (CommandContainer s : commandBlocks) {
            CodeBlock codeBlock = new CodeBlock(requireContext());
            codeBlock.defaultOnly();
            codeBlock.setCommand(requireContext(), s.type);
            codeBlock.setOnTouchListener(new View.OnTouchListener() {

                CodeBlock v;

                float x, y;

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                        CodeBlock codeblock = (CodeBlock) view;

                        if((codeblock.commandContainer.type == CommandContainer.eGYRO || codeblock.commandContainer.type == CommandContainer.dGYRO) && !PremiumController.hasPermission(PremiumController.Product.MotionControls)) {
                            needsPremiumGyro();
                            return true;
                        }

                        v = new CodeBlock(requireContext());
                        v.setCommand(requireContext(), codeblock.commandContainer.type);

                        x = motionEvent.getX();
                        y = motionEvent.getY();

                        ((ViewGroup) root).addView(v);

                        v.setVisibility(View.GONE);

                        int[] location = {0, 0};
                        view.getLocationOnScreen(location);

                        v.setX(location[0]);
                        v.setY(location[1]);

                        onChange(codeBlock);

                        commandDescription.setText(codeblock.commandContainer.description);
                        parameterTitle.setText(codeblock.commandContainer.parameterDescription);
                        valueTitle.setText(codeblock.commandContainer.valueDescription);

                        blockoutText.setVisibility(View.GONE);

                        valueTitle.setVisibility(View.GONE);
                        parameterTitle.setVisibility(View.GONE);

                        parameterInput.setVisibility(View.GONE);
                        keybindInput.setVisibility(View.GONE);

                        keybindInput.removeAllViews();

                        mEditTextValid child = new mEditTextValid(requireContext(), codeblock.commandContainer);
                        child.getEditText().setText("");
                        child.getEditText().setOnEditorActionListener(new myOnEditorAction());
                        keybindInput.addView(child);

                        parameterInput.setText("0");

                    }

                    if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {

                        if(v != null) {

                            float X = motionEvent.getX() - x;
                            float Y = motionEvent.getY() - y;

                            if (Math.abs(X) > 20) {
                                lockableScroll.setScrollingEnabled(false);
                                v.setVisibility(View.VISIBLE);
                            }

                            if (!lockableScroll.isScrollable()) {

                                x = motionEvent.getX();
                                y = motionEvent.getY();

                                v.setX(v.getX() + X);
                                v.setY(v.getY() + Y);

                                getClosestCodeBlock(v);
                            }
                        }
                    }

                    if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {

                        if(v != null) {
                            if (!lockableScroll.isScrollable()) {

                                MainCodeBlock mainCodeBlock = getClosestCodeBlock(v);

                                if (mainCodeBlock != null) {
                                    selectedCodeBlock = mainCodeBlock.place(v);
                                }

                                ((ViewGroup) root).removeView(v);

                                lockableScroll.setScrollingEnabled(true);

                            }
                            v = null;

                        }

                    }

                    return true;
                }
            });

            CommandBlockList.addView(codeBlock);

        }

        return root;
    }

    private @Nullable MainCodeBlock getClosestCodeBlock(CodeBlock codeBlock) {

        MainCodeBlock closest = null;

        for (int i = 0; i < CodeEditor.getChildCount(); i++) {
            MainCodeBlock mainCodeBlock = (MainCodeBlock) CodeEditor.getChildAt(i);

            int[] location = {0, 0};
            int[] locationBlock = {0, 0};

            mainCodeBlock.getLocationOnScreen(location);
            codeBlock.getLocationOnScreen(locationBlock);

            if (within(mainCodeBlock, locationBlock)) {
                closest = mainCodeBlock;
            }
        }

        if (closest != null) {
            closest.highlight(codeBlock);
        } else {
//            closest.highlight(null);
        }

        for (int i = 0; i < CodeEditor.getChildCount(); i++) {
            MainCodeBlock mainCodeBlock = (MainCodeBlock) CodeEditor.getChildAt(i);

            if (closest != null) {
                if (!Objects.equals(mainCodeBlock.code, closest.code)) {
                    mainCodeBlock.highlight(null);
                }
            } else {
                mainCodeBlock.highlight(null);
            }
        }

        return closest;
    }

    public static boolean within(MainCodeBlock mainCodeBlock, int[] blockLocation) {

        int[] location = {0, 0};

        mainCodeBlock.getLocationOnScreen(location);

        if (location[0] <= blockLocation[0] && location[1] <= blockLocation[1]) {
            return location[0] + mainCodeBlock.getWidth() >= blockLocation[0] && location[1] + mainCodeBlock.getHeight() >= blockLocation[1];
        }

        return false;

    }

    public static boolean within(CodeBlock mainCodeBlock, int[] blockLocation) {

        int[] location = {0, 0};

        mainCodeBlock.getLocationOnScreen(location);

        if (location[0] <= blockLocation[0] && location[1] <= blockLocation[1]) {
            return location[0] + mainCodeBlock.getWidth() >= blockLocation[0] && location[1] + mainCodeBlock.getHeight() >= blockLocation[1];
        }

        return false;

    }

    private void hideKeyboardAndUI() {

        if (imm != null) {
            imm.hideSoftInputFromWindow(root.getWindowToken(), 0);
        }

        requireActivity().getWindow().getDecorView().setSystemUiVisibility(MainActivity.uiFlag);
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        requireActivity().getCurrentFocus().clearFocus();
    }


    class myOnEditorAction implements TextView.OnEditorActionListener {

        boolean createdNewView = false;

        mEditTextValid view;

        public myOnEditorAction(mEditTextValid view, boolean createdNewView) {
            this.view = view;
            this.createdNewView = createdNewView;
        }

        public myOnEditorAction(){

        }

        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            if (i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_ACTION_NEXT) {

                hideKeyboardAndUI();

                if(selectedCodeBlock.commandContainer.multipleKeybinds) {

                    if (textView.getText().toString().length() > 0 && !createdNewView) {

                        createdNewView = true;
                        mEditTextValid child = new mEditTextValid(requireContext(), selectedCodeBlock.commandContainer);
                        child.getEditText().setOnEditorActionListener(new myOnEditorAction(child, false));
                        keybindInput.addView(child);

                    } else if(createdNewView && textView.getText().toString().length() == 0){

                        createdNewView = false;

                        if(view != null) {
                            keybindInput.removeView(view);
                        }

                    }
                }

                if (selectedCodeBlock != null) {
                    selectedCodeBlock.commandContainer.values.clear();

                    for (int k = 0; k < keybindInput.getChildCount(); k++) {
                        mEditTextValid editText = (mEditTextValid) keybindInput.getChildAt(k);

                        if (editText.getEditText().getText().length() != 0) {
                            selectedCodeBlock.commandContainer.values.add(editText.getEditText().getText().toString().replace(" ", ""));
                        }
                    }

                    selectedCodeBlock.update();
                }
            }

            return true;
        }
    }

    void needsPremiumGyro() {
        vibrator.vibrate(100);
        Snackbar r = Snackbar.make(root, R.string.You_need_to_upgrade_to_use_gyroscope, Snackbar.LENGTH_INDEFINITE);
        r.setAction(R.string.Dismiss, view -> {
        });

        r.show();
    }

    void onChange(@Nullable CodeBlock selectedCodeBlock) {


        blockoutText.setVisibility(selectedCodeBlock == null ? View.VISIBLE : View.GONE);

        if (selectedCodeBlock == null) {
            return;
        }


        for (int i = 0; i < CodeEditor.getChildCount(); i++) {
            ((MainCodeBlock) CodeEditor.getChildAt(i)).deselectAll();
        }

        for (int i = 0; i < CommandBlockList.getChildCount(); i++) {
            ((CodeBlock) CommandBlockList.getChildAt(i)).deselect();
        }


        this.selectedCodeBlock = selectedCodeBlock;
        this.selectedCodeBlock.select();

        keybindInput.removeAllViews();

        mEditTextValid child;


        for (String keybinds : this.selectedCodeBlock.commandContainer.values) {
            child = new mEditTextValid(requireContext(), this.selectedCodeBlock.commandContainer);
            child.getEditText().setText(keybinds);
            child.getEditText().setOnEditorActionListener(new myOnEditorAction(child, selectedCodeBlock.commandContainer.multipleKeybinds));
            keybindInput.addView(child);
        }

        if(selectedCodeBlock.commandContainer.multipleKeybinds){
            child = new mEditTextValid(requireContext(), this.selectedCodeBlock.commandContainer);
            child.getEditText().setOnEditorActionListener(new myOnEditorAction(child, false));

            keybindInput.addView(child);
        } else if (this.selectedCodeBlock.commandContainer.values.isEmpty()) {
            child = new mEditTextValid(requireContext(), this.selectedCodeBlock.commandContainer);
            child.getEditText().setOnEditorActionListener(new myOnEditorAction(child, true));

            keybindInput.addView(child);
        }
        commandDescription.setText(selectedCodeBlock.commandContainer.description);
        parameterTitle.setText(selectedCodeBlock.commandContainer.parameterDescription);
        valueTitle.setText(selectedCodeBlock.commandContainer.valueDescription);

        valueTitle.setVisibility(selectedCodeBlock.commandContainer.hasValues ? View.VISIBLE : View.GONE);
        parameterTitle.setVisibility(selectedCodeBlock.commandContainer.hasParameter ? View.VISIBLE : View.GONE);

        parameterInput.setVisibility(selectedCodeBlock.commandContainer.hasParameter ? View.VISIBLE : View.GONE);
        keybindInput.setVisibility(selectedCodeBlock.commandContainer.hasValues ? View.VISIBLE : View.GONE);

        parameterInput.setText(String.valueOf(selectedCodeBlock.commandContainer.parameter));

        vibrator.vibrate(10);

        root.findViewById(R.id.delete).setAlpha(selectedCodeBlock.defaultOnly ? 0.3f : 1);
    }
}
