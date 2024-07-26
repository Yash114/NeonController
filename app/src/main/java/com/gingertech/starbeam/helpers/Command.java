package com.gingertech.starbeam.helpers;

import android.content.Context;
import android.util.ArraySet;
import android.util.Log;

import androidx.annotation.Nullable;

import com.gingertech.starbeam.helpers.SaveClass;
import com.gingertech.starbeam.helpers.controllers.CommandContainer;
import com.gingertech.starbeam.helpers.controllers.GenericCallbackv2;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.UUID;

public class Command {

    public String name, description, code, UUID;

    public String ownerName, ownerUUID;

    public boolean isDownloaded = false;

    public ArraySet<String> dependancies = new ArraySet<>();


//    public static String[] commands = {"onPress", "whilePressed", "onRelease"};
    public static String[] commands = {"onPress", "onRelease"};
    public ArrayList<MainCode> MainCodeBlocks = new ArrayList<>();

    MainCode onPressBlock = null;
    MainCode whilePressedBlock = null;
    MainCode onReleaseBlock = null;


    @Override
    public boolean equals(@Nullable Object obj) {
        return Objects.equals(this.name, name);
    }

    public class MainCode {
        public String Command = "";
        public ArrayList<CommandContainer> commandContainers = new ArrayList<>();

        MainCode(String command) {
            this.Command = command;
        }
    }

    public Command(){}

    public Command(Context c, String UUID) {
        HashMap<String, String> command = SaveClass.GetCommand(c, UUID);
        this.code = command.get("code");
        this.name = command.get("name");
        this.description = command.get("description");
        this.isDownloaded = Objects.equals(command.get("isExternal"), "1");
        this.UUID = UUID;

        this.setCode(c);
    }


    public Command(String name, String description) {
        this.name = name;
        this.description = description;
        this.UUID = java.util.UUID.randomUUID().toString();
    }

    public void setCode(Context c) {

        if(this.code == null || this.code.length() <= 1) {

            StringBuilder stringBuilder = new StringBuilder();

            for(String command : commands) {
                MainCode mainCode = new MainCode(command);
                MainCodeBlocks.add(mainCode);
                stringBuilder.append(command).append(": [\n]").append("\n");
            }

            code = stringBuilder.toString();
            return;
        }

        MainCodeBlocks.clear();
        String inputString = code.replace("\t", "");

        int prevIndex = 0;

        for(String command : commands) {

            MainCode mainCode = new MainCode(command);
            MainCodeBlocks.add(mainCode);

            String beginMarker = command + ": [";
            String endMarker = "]";

            int beginIndex = inputString.indexOf(beginMarker, prevIndex);
            int endIndex = inputString.indexOf(endMarker, beginIndex);

            if (beginIndex == -1 || endIndex == -1) {
                return;
            }

            String codeBlockString = inputString.substring(beginIndex + beginMarker.length(), endIndex);
            String[] codeBlocks = codeBlockString.split("\n");

            for(String codeblock : codeBlocks) {

                CommandContainer rr = Line2Block(c, codeblock);
                if(rr != null) {
                    mainCode.commandContainers.add(rr);
                }
            }

            prevIndex = endIndex;

        }


    }

    private static CommandContainer Line2Block(Context c, String s) {

        int type = -1;

        for(int i = 0; i < CommandContainer.commands.length; i++) {

            if(!s.contains(":")) { continue; }

            String command = s.substring(0, s.indexOf(":"));
            if(command.equals(CommandContainer.commands[i])) {
                type = i;
                break;
            }
        }

        if(type == -1) { return null; }

        CommandContainer block  = new CommandContainer(c, type);

        if(block.hasParameter) {
            int startPara = s.indexOf("(") + 1;
            int endPara = s.indexOf(")");

            String parameter = s.substring(startPara, endPara);
            block.parameter = Integer.parseInt(parameter);
        }

        if(block.hasValues) {
            int startVal = s.indexOf("{") + 1;
            int endVal = s.indexOf("}");

            if(endVal != -1) {
                String[] values = s.substring(startVal, endVal).split(" ");

                Arrays.stream(values).filter(ss -> !ss.isEmpty()).forEach(ss -> block.values.add(ss));
            }
        }

        return block;
    }

    public void run(GenericCallbackv2 execute, boolean buttonState) {

        //if the button state is down run the onPress and whilePressed functions
        if(buttonState) {
            execute.onChange(true);

            runPress(execute);
        } else {
            runRelease(execute);
        }

    }

    private void setMainCodeBlocks() {

        if (onPressBlock == null || whilePressedBlock == null || onReleaseBlock == null) {
            for (MainCode mainCodeBlock : MainCodeBlocks) {
                if (mainCodeBlock.Command.equals("onPress")) {
                    onPressBlock = mainCodeBlock;
                }

                if (mainCodeBlock.Command.equals("whilePressed")) {
                    whilePressedBlock = mainCodeBlock;
                }

                if (mainCodeBlock.Command.equals("onRelease")) {
                    onReleaseBlock = mainCodeBlock;
                }
            }
        }
    }

    private void runPress(GenericCallbackv2 execute) {

        setMainCodeBlocks();

        if (onPressBlock == null || onReleaseBlock == null) {
            return;
        }

        Thread thread = new Thread(() -> onPressBlock.commandContainers.forEach(c -> c.run(execute)));
        thread.start();
    }

    private void runRelease(GenericCallbackv2 execute) {

        setMainCodeBlocks();

        if (onPressBlock == null || onReleaseBlock == null) {
            return;
        }

        new Thread(() -> onReleaseBlock.commandContainers.forEach(c -> c.run(execute))).start();

        execute.onChange(false);

    }

    public void updateCode(String code) {

        this.code = code;

        updateDependencies();
    }

    private void updateDependencies() {
        dependancies.clear();

        for(MainCode mainCodeBlocks : MainCodeBlocks) {
            for(CommandContainer commandContainer : mainCodeBlocks.commandContainers) {
                if(commandContainer.type != CommandContainer.COMMAND) { continue; }

                dependancies.add(commandContainer.values.get(0));
            }
        }
    }
}
