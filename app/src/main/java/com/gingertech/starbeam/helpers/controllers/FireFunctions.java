package com.gingertech.starbeam.helpers.controllers;

import static com.gingertech.starbeam.helpers.controllers.ButtonID_data.ButtonID_BUTTON_IMAGE_TYPE_CONVERTER_inv;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.ArraySet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gingertech.starbeam.helpers.Command;
import com.gingertech.starbeam.helpers.LayoutClass;
import com.gingertech.starbeam.helpers.SaveClass;
import com.gingertech.starbeam.helpers.UserData;
import com.gingertech.starbeam.ui.layout.LayoutCreateFragment;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FireFunctions {

    enum Status {
        SUCCESS,
        ERROR_NO_USERNAME,
        ERROR_UNKNOWN,

    }

    public static class ImageUploadHelper {
        // Function to upload and compress an image by 25%
        public static UploadTask uploadAndCompressImage(String imageID, Bitmap image) {

            // Convert the compressed bitmap to a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 25, baos);
            byte[] imageData = baos.toByteArray();

            // Upload the compressed image to Firebase Storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child("UserContent/Layouts/ButtonImages/" + imageID);

            return storageRef.putBytes(imageData);
        }
    }

    public static Task<String> saveCode(Command command) {

        FirebaseFunctions mfunctions = FirebaseFunctions.getInstance();

        UserData.Username = "temp";

        if(UserData.Username != null) {
            if (UserData.Username.length() == 0) {
                return null;
            }
        } else {
            return null;
        }


        Map<String, Object> data = new HashMap<>();
        data.put("ownerUUID", "Yashua");
        data.put("functionDescription", command.description);
        data.put("functionCode", command.code);
        data.put("functionUUID", command.name);

        if(!command.dependancies.isEmpty()) {
            data.put("functionRefs", command.dependancies.toArray());
        } else {
            data.put("functionRefs", "");
        }


        return mfunctions
                .getHttpsCallable("saveFunction")
                .call(data)
                .continueWith(task -> {
                    // This continuation runs on either success or failure, but if the task
                    // has failed then getResult() will throw an Exception which will be
                    // propagated down.

                    if (!task.isSuccessful()) {
                        Exception e = task.getException();
                        Log.e("firefunc", "Error: " + e.getMessage());
                        // Handle the error here
                    } else {
                        Log.e("firefunc", (String) task.getResult().getData());
                    }

                    return "";
                });
    }

    public static Task<Map<String, Object>> getCode(String codeUUID) {

        FirebaseFunctions mfunctions = FirebaseFunctions.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("functionUUID", codeUUID);

        return mfunctions
                .getHttpsCallable("getFunction")
                .call(data)
                .continueWith(task -> (Map<String, Object>) task.getResult().getData());
    }

    public static CompletableFuture<Status> uploadButtonImages(Context c,  Map<String, String> buttonImageRefs) {


        int[] remainingUploads = new int[]{ buttonImageRefs.size() };

        CompletableFuture<Status> completableFuture = new CompletableFuture<>();

        for(Map.Entry<String, String> buttonImageRef : buttonImageRefs.entrySet()) {

            Bitmap bitmap = SaveClass.ReadImage(c, buttonImageRef.getKey());

            if(bitmap == null) {
                remainingUploads[0] -= 1;
                continue;
            }
            ImageUploadHelper.uploadAndCompressImage(buttonImageRef.getValue(), bitmap).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()) {
                        remainingUploads[0] -= 1;

                        if(remainingUploads[0] == 0) {
                            completableFuture.complete(Status.SUCCESS);
                        }
                    } else {
                        completableFuture.complete(Status.ERROR_UNKNOWN);

                    }
                }
            });

        }

        return completableFuture;
    }

    public static Task<String> saveController(Context c ,LayoutClass layoutClass) {

        FirebaseFunctions mfunctions = FirebaseFunctions.getInstance();

        String LayoutUUID = UUID.randomUUID().toString();

        HashMap<String, String> images = new HashMap<>();

        Map<String, Object> data = new HashMap<>();
        data.put("layoutUUID", LayoutUUID);
        data.put("ownerUUID", "temp");
        data.put("ownerName", "temp");
        data.put("layoutName", layoutClass.name);
        data.put("layoutDescription", layoutClass.description);
        data.put("isGyroActivated", layoutClass.gyroActivated);
        data.put("gyroSensitivity", layoutClass.gyroSensitivity);
        data.put("isTouchTrackpad", layoutClass.isTouchTrackpad);
        data.put("isGyroMouse", layoutClass.isGyroMouse);
        data.put("touchSensitivity", layoutClass.touchSensitivity);
        data.put("isTouchMouse", layoutClass.isTouchMouse);
        data.put("isSplit", layoutClass.isSplit);
        data.put("isSplitSwapped", layoutClass.isSplitSwapped);
        data.put("isMouseClickEnabled", layoutClass.isMouseClickEnabled);
        data.put("gyroActivated", layoutClass.gyroActivated);

        List<Map<String, Object>> buttonData = new ArrayList<>();

        Set<Command> commands = new ArraySet<>();
//        Set<Lay> layouts = new ArraySet<>();


        for (ButtonID_data b : layoutClass.buttons) {
            Map<String, Object> buttonMap = new HashMap<>();
            buttonMap.put("type", b.type);
            buttonMap.put("x", b.x);
            buttonMap.put("y", b.y);
            buttonMap.put("size", b.size);
            buttonMap.put("textSize", b.textSize);
            buttonMap.put("color", b.color);
            buttonMap.put("keybind", b.keybind);
            buttonMap.put("alpha", b.alpha);
            buttonMap.put("sensitivity", b.sensitivity);
            buttonMap.put("buttonNameVisible", b.buttonNameVisible);
            buttonMap.put("isHapticFeebackEnabled", b.isHapticFeebackEnabled);
            buttonMap.put("buttonName", b.buttonName);
            buttonMap.put("tintMode", b.tintMode);

//            Get all commands and store them as-well
            if(b.keybind.startsWith("\"") && b.keybind.endsWith("\"")) {
                String commandName = b.keybind.replace("\"", "");

                Command commandObj = UserData.Commands.get(commandName);

                if(commandObj != null) {

//                 Replace the name of the command with the UUID of the command
                   Command newCommand = new Command();
                   newCommand.code = commandObj.code;
                   newCommand.name = UUID.randomUUID().toString();
                   newCommand.description = commandObj.description;

                   commands.add(newCommand);

//                  Change the keybind of the button to the new command UUID
                   buttonMap.put("keybind", "\"" + newCommand.name +"\"");

//                   Change the name of the button to either the keybind or the default name (to obscure the UUID)
                   buttonMap.put("buttonName", Objects.equals(b.buttonName, "") ? commandObj.name : b.buttonName);

                }
            }

//            Temp for now
            if(Objects.equals(b.imageID, "")) {
                b.buttonImageType = ButtonID.BUTTON_IMAGE_TYPE.DEFAULT;
            } else {
                b.buttonImageType = ButtonID.BUTTON_IMAGE_TYPE.USER_PROVIDED;
            }

            buttonMap.put("buttonImageType", b.buttonImageType == ButtonID.BUTTON_IMAGE_TYPE.DEFAULT ? 0 :
                    b.buttonImageType == ButtonID.BUTTON_IMAGE_TYPE.APP_PROVIDED ? 1 : 2);

//            This button has a custom image
            if(b.buttonImageType == ButtonID.BUTTON_IMAGE_TYPE.USER_PROVIDED) {
                String imageID = UUID.randomUUID().toString();
                buttonMap.put("imageUUID" , imageID);

//                Store button ID as well as its layout UUID
                images.put(b.imageID, LayoutUUID + "/" + imageID);

            } else if(b.buttonImageType == ButtonID.BUTTON_IMAGE_TYPE.APP_PROVIDED) {

//                TODO: Implement app provded images
                buttonMap.put("imageUUID" , "0");

            } else {

                buttonMap.put("imageUUID", "");
            }

            buttonData.add(buttonMap);
        }

//        Save all command and add to the structure
        List<String> commandNames = new ArrayList<>();
        for(Command com : commands) {
            saveCode(com);
            commandNames.add(com.name);
        }

        data.put("commandDependencies", commandNames);


//        Upload all the images
        uploadButtonImages(c, images);

        data.put("buttons", buttonData);

        return mfunctions
                .getHttpsCallable("saveLayout")
                .call(data)
                .continueWith(task -> LayoutUUID);
    }

    public static Task<LayoutClass> getController (Context c, String layoutUUID, @Nullable List<String> prevDownloaded) {

        if(prevDownloaded == null) {
            prevDownloaded = new ArrayList<>();
            prevDownloaded.add(layoutUUID);
        }

        prevDownloaded.add(layoutUUID);

        FirebaseFunctions mfunctions = FirebaseFunctions.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("layoutUUID", layoutUUID);

        List<String> finalPrevDownloaded = prevDownloaded;
        return mfunctions
                .getHttpsCallable("getLayout")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, LayoutClass>() {

                    @Override
                    public LayoutClass then(@NonNull Task<HttpsCallableResult> task) throws Exception {

                        LayoutClass outputLayout = new LayoutClass();

                        if (task.isSuccessful()) {

                            HashMap<String, Object> g = (HashMap<String, Object>) task.getResult().getData();

                            Log.e("lol", g.get("result").toString());

                            if (!g.containsKey("result")) {
                                return null;
                            }


                            if (!(g.get("result") instanceof HashMap)) {
                                return null;
                            }

                            mMap d = new mMap((HashMap<String, Object>) g.get("result"));

                            if (d == null) {
                                return null;
                            }

                            outputLayout.isImported = true;
                            outputLayout.name = (String) d.getWithDefault("layoutName");
                            outputLayout.description = (String) d.getWithDefault("layoutDescription");
                            outputLayout.layoutUUID = (String) d.getWithDefault("layoutUUID");
                            outputLayout.ownerUUID = (String) d.getWithDefault("ownerUUID");
                            outputLayout.ownerName = (String) d.getWithDefault("ownerName");

                            outputLayout.touchSensitivity = Float.valueOf(d.getWithDefault("touchSensitivity", 0.5f).toString());
                            outputLayout.gyroSensitivity = Float.valueOf(d.getWithDefault("gyroSensitivity", 0.5f).toString());

                            outputLayout.isMouseClickEnabled = (boolean) d.getWithDefault("isMouseClickEnabled", true);
                            outputLayout.isTouchTrackpad = (boolean) d.getWithDefault("isTouchTrackpad", true);
                            outputLayout.isTouchMouse = (boolean) d.getWithDefault("isTouchMouse", true);
                            outputLayout.isSplit = (boolean) d.getWithDefault("isSplit", false);
                            outputLayout.isSplitSwapped = (boolean) d.getWithDefault("isSplitSwapped", false);
                            outputLayout.isGyroMouse = (boolean) d.getWithDefault("isSplitSwapped", true);

                            outputLayout.gyroActivated = (boolean) d.getWithDefault("gyroActivated", false);

                            List<String> commands = (List<String>) d.get("commandDependencies");

                            List<Object> buttonData = (List<Object>) d.get("buttons");
                            ArrayList<ButtonID_data> buttonIDData = new ArrayList<>();
                            if (buttonData != null) {

                                for (Object specificButtonInfo : buttonData) {

                                    mMap buttonInfo = new mMap((HashMap<String, Object>) specificButtonInfo);

                                    ButtonID_data buttonIDData1 = new ButtonID_data();

                                    buttonIDData1.textSize = Float.valueOf(buttonInfo.getWithDefault("textSize", -1).toString());
                                    buttonIDData1.tintMode = (int) buttonInfo.getWithDefault("tintMode", 1);

                                    buttonIDData1.addNewButton((int) buttonInfo.getWithDefault("type", 1), c);
                                    buttonIDData1.setPosition((int) buttonInfo.getWithDefault("x", 0),
                                            (int) buttonInfo.getWithDefault("y", 0));

                                    buttonIDData1.alpha = (int) buttonInfo.getWithDefault("alpha", 10);
                                    buttonIDData1.buttonImageType = ButtonID_BUTTON_IMAGE_TYPE_CONVERTER_inv((int) buttonInfo.getWithDefault("buttonImageType", 0));
                                    buttonIDData1.buttonName = (String) buttonInfo.getWithDefault("buttonName");
                                    buttonIDData1.buttonNameVisible = (Boolean) buttonInfo.getWithDefault("buttonNameVisible", true);
                                    buttonIDData1.setColor((int) buttonInfo.getWithDefault("color", -1));
                                    buttonIDData1.isHapticFeebackEnabled = (Boolean) buttonInfo.getWithDefault("isHapticFeebackEnabled", true);
                                    buttonIDData1.keybind = (String) buttonInfo.getWithDefault("keybind", " ");
                                    buttonIDData1.sensitivity = Float.valueOf(buttonInfo.getWithDefault("sensitivity", 0.5).toString());
                                    buttonIDData1.size = (int) buttonInfo.getWithDefault("size", 0);

                                    buttonIDData1.setImage((String) buttonInfo.getWithDefault("imageUUID", ""));

                                    if (buttonIDData1.buttonImageType == ButtonID.BUTTON_IMAGE_TYPE.USER_PROVIDED) {
                                        downloadRemoteImage(outputLayout.layoutUUID, buttonIDData1.imageID).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                                            @Override
                                            public void onComplete(@NonNull Task<byte[]> task) {

                                                if (task.isSuccessful()) {
                                                    byte[] result = task.getResult();
                                                    Bitmap bitmap = BitmapFactory.decodeByteArray(result, 0, result.length);

                                                    SaveClass.SaveImage(bitmap, buttonIDData1.imageID, c);
                                                }
                                            }
                                        });
                                    }

                                    buttonIDData.add(buttonIDData1);
                                }
                            }

                            outputLayout.buttons = buttonIDData;

                            if (commands != null &&
                                    PremiumController.hasPermission(PremiumController.Product.InfiniteButtons) && outputLayout.buttons.size() <= LayoutCreateFragment.maxButtons) {

                                for (String command : commands) {
                                    getCode(command).addOnCompleteListener(new OnCompleteListener<Map<String, Object>>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Map<String, Object>> task) {
                                            if (task.isSuccessful()) {

                                                HashMap<String, Object> g = (HashMap<String, Object>) task.getResult();

                                                Log.e("gg", g.toString());

                                                if (!g.containsKey("result")) {
                                                    return;
                                                }

                                                if (!(g.get("result") instanceof HashMap)) {
                                                    return;
                                                }

                                                mMap commandData = new mMap((HashMap<String, Object>) g.get("result"));

                                                if (commandData == null) {
                                                    return;
                                                }


                                                Command command1 = new Command();
//                                                command1.ownerUUID = (String) commandData.get("OwnerUUID");

                                                command1.UUID = (String) commandData.get("Name");
                                                command1.name = (String) commandData.get("Name");
                                                command1.description = (String) commandData.get("Description");
                                                command1.ownerName = (String) commandData.get("OwnerName");
                                                command1.updateCode((String) commandData.get("Code"));

                                                command1.isDownloaded = true;

                                                Object refs = commandData.get("Refs");
                                                if (refs instanceof List) {
                                                    List<String> refsList = (List<String>) refs;

                                                    for (String ref : refsList) {
                                                        if (!finalPrevDownloaded.contains(ref)) {
                                                            FireFunctions.getCode(ref).addOnCompleteListener(this);
                                                        }
                                                    }
                                                }

                                                Log.e("gg", (String) commandData.get("Code"));
                                                SaveClass.SaveCommand(c, command1);
                                                UserData.Commands.put(command1.name, command1);
                                                SaveClass.SaveCommands(c);

                                            }
                                        }
                                    });
                                }
                            }

                            return outputLayout;
                        }

                        // Not successful for some reason
                        try {
                            task.getResult();
                        } catch (RuntimeException e) {
                            Log.e("lol", e.getMessage());
                        }

                        Log.e("lol", "Not successful");

                        return null;
                    }
                });

    }

    static private Task<byte[]> downloadRemoteImage(String LayoutUUID, String ImageUUID) {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        StorageReference islandRef = storageRef.child("/UserContent/Layouts/ButtonImages/" + LayoutUUID + "/" + ImageUUID);

        final long ONE_MEGABYTE = 1024 * 1024;

        return islandRef.getBytes(ONE_MEGABYTE);

    }
}

