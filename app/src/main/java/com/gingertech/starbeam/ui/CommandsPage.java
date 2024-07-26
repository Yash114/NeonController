package com.gingertech.starbeam.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.Command;
import com.gingertech.starbeam.helpers.SaveClass;
import com.gingertech.starbeam.helpers.UserData;
import com.gingertech.starbeam.helpers.controllers.CommandListItemController;
import com.gingertech.starbeam.helpers.controllers.GenericCallbackv2;
import com.gingertech.starbeam.helpers.controllers.MixPanel;
import com.gingertech.starbeam.helpers.controllers.NewCommandController;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class CommandsPage extends Fragment {

    View root;
    NewCommandController newCommandController;
    LinearLayout commandList;
    Command selectedCommand;

    GenericCallbackv2 hideUICallback;
    Vibrator vibrator;
    private MixpanelAPI mp;


    public CommandsPage(GenericCallbackv2 hideUICallback) {
        this.hideUICallback = hideUICallback;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.commands_fragment, container, false);

        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

        UserData.CurrentFragment = UserData.COMMANDS_LIST;

        mp = MixPanel.makeObj(requireContext());

        newCommandController = root.findViewById(R.id.newCommandPopup);
        commandList = root.findViewById(R.id.commandList);

        root.findViewById(R.id.addCommandButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newCommandController.setVisibility(View.VISIBLE);
                vibrator.vibrate(10);
            }
        });

        root.findViewById(R.id.deleteCommandPopup).findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(10);
                root.findViewById(R.id.deleteCommandPopup).setVisibility(View.GONE);
                vibrator.vibrate(10);
            }
        });

        root.findViewById(R.id.deleteCommandPopup).findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(10);
                for(int i = 0; i < commandList.getChildCount(); i++) {
                    CommandListItemController controller1 = (CommandListItemController) commandList.getChildAt(i);

                    if(controller1.command == selectedCommand) {
                        commandList.removeView(controller1);

                        SaveClass.DeleteCommand(requireContext(), controller1.command);

                        selectedCommand = null;

                        break;
                    }
                }

                root.findViewById(R.id.deleteCommandPopup).setVisibility(View.GONE);

            }
        });

        GenericCallbackv2 editCallback = new GenericCallbackv2() {

            @Override
            public void onChange(Object value) {

                vibrator.vibrate(10);

                if(hideUICallback != null) {
                    hideUICallback.onChange(-3);
                }

                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

                FragmentTransaction trans = fragmentManager.beginTransaction();

                trans.setCustomAnimations(R.anim.slide_down_in, R.anim.slide_down_out);

                trans.replace(R.id.nav_host_fragment, new CommandsEditorPage((Command) value, hideUICallback)).commit();
            }
        };

        GenericCallbackv2 deleteCallback = new GenericCallbackv2() {

            @Override
            public void onChange(Object value) {

                vibrator.vibrate(10);

                selectedCommand = (Command) value;
                root.findViewById(R.id.deleteCommandPopup).setVisibility(View.VISIBLE);
            }
        };

        if(hideUICallback != null) {
            hideUICallback.onChange(-4);
        }

        for(Command command : UserData.Commands.values()) {
//            SaveClass.GetCommand(requireContext(), command);
            CommandListItemController controller = new CommandListItemController(requireContext(), command);
            controller.setOnEditCallback(editCallback);
            controller.setOnDeleteCallback(deleteCallback);

            commandList.addView(controller);
        }


        newCommandController.setOnSubmitListener(new GenericCallbackv2() {
            @Override
            public void onChange(Object value) {
                vibrator.vibrate(10);


                if(value instanceof Command) {
                    CommandListItemController controller = new CommandListItemController(requireContext(), (Command) value);
                    controller.setOnEditCallback(editCallback);
                    controller.setOnDeleteCallback(deleteCallback);
                    commandList.addView(controller);
                }
            }
        });

        return root;
    }
}
