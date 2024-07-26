package com.gingertech.starbeam.limelight.ui;

public interface GameGestures {
    //    @Override
    //    public void onUserLeaveHint() {
    //        super.onUserLeaveHint();
    //
    //        // PiP is only supported on Oreo and later, and we don't need to manually enter PiP on
    //        // Android S and later. On Android R, we will use onPictureInPictureRequested() instead.
    //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
    //            if (autoEnterPip) {
    //                try {
    //                    // This has thrown all sorts of weird exceptions on Samsung devices
    //                    // running Oreo. Just eat them and close gracefully on leave, rather
    //                    // than crashing.
    //                    requireActivity().enterPictureInPictureMode(getPictureInPictureParams(false));
    //                } catch (Exception e) {
    //                    e.printStackTrace();
    //                }
    //            }
    //        }
    //    }
    //
    //    @Override
    //    @TargetApi(Build.VERSION_CODES.R)
    //    public boolean onPictureInPictureRequested() {
    //        // Enter PiP when requested unless we're on Android 12 which supports auto-enter.
    //        if (autoEnterPip && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
    //            requireActivity().enterPictureInPictureMode(getPictureInPictureParams(false));
    //        }
    //        return true;
    //    }
    //
    //
    void onWindowFocusChanged(boolean hasFocus);

    void toggleKeyboard();
}
