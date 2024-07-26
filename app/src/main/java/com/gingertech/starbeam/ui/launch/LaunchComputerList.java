package com.gingertech.starbeam.ui.launch;

import static android.content.Context.MODE_PRIVATE;

import static com.gingertech.starbeam.MainActivity.mFirebaseAnalytics;
import static com.gingertech.starbeam.MainActivity.uiFlag;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.SaveClass;
import com.gingertech.starbeam.helpers.UserData;
import com.gingertech.starbeam.helpers.controllers.GenericCallbackv2;
import com.gingertech.starbeam.helpers.controllers.MixPanel;
import com.gingertech.starbeam.limelight.AppView;
import com.gingertech.starbeam.limelight.LimeLog;
import com.gingertech.starbeam.limelight.binding.PlatformBinding;
import com.gingertech.starbeam.limelight.binding.crypto.AndroidCryptoProvider;
import com.gingertech.starbeam.limelight.computers.ComputerManagerListener;
import com.gingertech.starbeam.limelight.computers.ComputerManagerService;
import com.gingertech.starbeam.limelight.grid.PcGridAdapter;
import com.gingertech.starbeam.limelight.grid.assets.DiskAssetLoader;
import com.gingertech.starbeam.limelight.nvstream.http.ComputerDetails;
import com.gingertech.starbeam.limelight.nvstream.http.NvApp;
import com.gingertech.starbeam.limelight.nvstream.http.NvHTTP;
import com.gingertech.starbeam.limelight.nvstream.http.PairingManager;
import com.gingertech.starbeam.limelight.nvstream.wol.WakeOnLanSender;
import com.gingertech.starbeam.limelight.preferences.GlPreferences;
import com.gingertech.starbeam.limelight.preferences.PreferenceConfiguration;
import com.gingertech.starbeam.limelight.ui.AdapterFragment;
import com.gingertech.starbeam.limelight.ui.AdapterFragmentCallbacks;
import com.gingertech.starbeam.limelight.utils.Dialog;
import com.gingertech.starbeam.limelight.utils.ServerHelper;
import com.gingertech.starbeam.limelight.utils.ShortcutHelper;
import com.gingertech.starbeam.limelight.utils.UiHelper;
import com.gingertech.starbeam.ui.ManualComputerJoin_Neon;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class LaunchComputerList extends Fragment implements AdapterFragmentCallbacks {

    private LinearLayout noPcFoundLayout;
    private PcGridAdapter pcGridAdapter;
    private ShortcutHelper shortcutHelper;
    private ComputerManagerService.ComputerManagerBinder managerBinder;
    private boolean freezeUpdates, runningPolling, inForeground, completeOnCreateCalled;

    private GenericCallbackv2 returnCallback;
    private int fragmentID = 0;

    private View root;

    public LaunchComputerList(@Nullable GenericCallbackv2 returnCallback) {
        this.returnCallback = returnCallback;
    }

    public LaunchComputerList(@Nullable GenericCallbackv2 returnCallback, int fragmentID) {
        this.returnCallback = returnCallback;
        this.fragmentID = fragmentID;
    }

    public LaunchComputerList() {}

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            final ComputerManagerService.ComputerManagerBinder localBinder =
                    ((ComputerManagerService.ComputerManagerBinder)binder);

            // Wait in a separate thread to avoid stalling the UI
            new Thread() {
                @Override
                public void run() {

                    if(getActivity() == null) {
                        return;
                    }

                    // Wait for the binder to be ready
                    localBinder.waitForReady();

                    // Now make the binder visible
                    managerBinder = localBinder;

                    try {

                        // Start updates
                        startComputerUpdates();

                        // Force a keypair to be generated early to avoid discovery delays
                        new AndroidCryptoProvider(requireActivity()).getClientCertificate();

                    } catch (IllegalStateException ignored) {

                    }
                }
            }.start();
        }

        public void onServiceDisconnected(ComponentName className) {
            managerBinder = null;
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Only reinitialize views if completeOnCreate() was called
        // before requireActivity() callback. If it was not, completeOnCreate() will
        // handle initializing views with the config change accounted for.
        // requireActivity() is not prone to races because both callbacks are invoked
        // in the main thread.
        if (completeOnCreateCalled) {
            // Reinitialize views just in case orientation changed
            initializeViews();
        }
    }

    private final static int PAIR_ID = 2;
    private final static int UNPAIR_ID = 3;
    private final static int WOL_ID = 4;
    private final static int DELETE_ID = 5;
    private final static int RESUME_ID = 6;
    private final static int QUIT_ID = 7;
    private final static int VIEW_DETAILS_ID = 8;
    private final static int FULL_APP_LIST_ID = 9;
    private final static int TEST_NETWORK_ID = 10;
    private void initializeViews() {

//        requireActivity().setContentView(R.layout.activity_pc_view);

//        UiHelper.notifyNewRootView(requireActivity());

//         Allow floating expanded PiP overlays while browsing PCs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireActivity().setShouldDockBigOverlays(false);
        }

//         Set default preferences if we've never been run
        PreferenceManager.setDefaultValues(requireActivity(), R.xml.preferences, false);

        //Set the correct layout for the PC grid
        pcGridAdapter.updateLayoutWithPreferences(requireActivity(), PreferenceConfiguration.readPreferences(requireActivity()));

        //This replaces everything

        try {
            if (root.findViewById(R.id.pcFragmentContainerNEON) != null) {
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.pcFragmentContainerNEON, new AdapterFragment(this));
                transaction.commitAllowingStateLoss();

            } else {
                Toast.makeText(getContext(), R.string.there_was_an_error, Toast.LENGTH_LONG).show();

            }

        } catch (IllegalArgumentException argumentException) {
            Toast.makeText(getContext(), R.string.there_was_an_error, Toast.LENGTH_LONG).show();
        }

//        TextView setupGuideButton = root.findViewById(R.id.setupGuideButton);
        noPcFoundLayout = root.findViewById(R.id.no_pc_found_layout);

//        root.findViewById(R.id.videoButton).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                MixPanel.mpButtonTracking(mp, "connect_tutorial_video");
//
//                try {
//                    requireContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/zGOPhtT74WE")));
//                } catch (ActivityNotFoundException e) {
//                    Toast.makeText(getContext(), "Error: Unable to launch youtube", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

        root.findViewById(R.id.viewTutorial).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MixPanel.mpButtonTracking(mp, "connect_tutorial_video");

                vibrator.vibrate(10);

                try {
                    requireContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=zGOPhtT74WE")));
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getContext(), R.string.Unable_to_launch_youtube, Toast.LENGTH_SHORT).show();
                }

            }
        });

        if (pcGridAdapter.getCount() == 0) {

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {

                    if(getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (pcGridAdapter.getCount() == 0) {

                                    root.findViewById(R.id.no_pc_found_layout).setVisibility(View.VISIBLE);
                                    MixPanel.mpEventTracking(mp, "computer_not_found", null);


                                } else {
                                    root.findViewById(R.id.no_pc_found_layout).setVisibility(View.INVISIBLE);
                                    MixPanel.mpEventTracking(mp, "computer_found", null);

                                }
                            }
                        });
                    }
                }
            }, 500);



            MixPanel.mpEventTracking(mp, "computer_not_found", null);

        }  else {


            root.findViewById(R.id.pcs_loading).setVisibility(View.GONE);

            root.findViewById(R.id.no_pc_found_layout).setVisibility(View.GONE);

//            noPcFoundLayout.setVisibility(View.GONE);

            MixPanel.mpEventTracking(mp, "computer_found", null);

        }


        root.findViewById(R.id.videoButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(getContext() == null){ return; }

                UserData.openedVideo = true;

                MixPanel.mpButtonTracking(mp, "connect_tutorial_video");
                requireContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/zGOPhtT74WE")));

                if(UserData.timesConnected == 0) {
                    UserData.back_from_connect_without_connecting = true;
                }

            }
        });

        pcGridAdapter.notifyDataSetChanged();

    }

    private MixpanelAPI mp;
    private ViewGroup pcFragmentContainerNEON;

    Vibrator vibrator;

    Handler reloadHander = new Handler();
    boolean reloadTimer = false;
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        UserData.CurrentFragment = UserData.LAUNCH_GAME_PLAY;

        UserData.PlayMode = UserData.WIFI;
        SaveClass.SaveFlags(getContext());

        mp = MixPanel.makeObj(requireContext());

        root = inflater.inflate(R.layout.activity_pc_view_aethr, container, false);
        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);

        root.findViewById(R.id.noPC).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MixPanel.mpEventTracking(mp, "Open_Share_Popup", null);

                root.findViewById(R.id.arcadeEmailPopup).setVisibility(View.VISIBLE);
                vibrator.vibrate(10);

            }
        });

        root.findViewById(R.id.restartButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                vibrator.vibrate(100);
//
//                if(reloadTimer) { return; }
//
//                MixPanel.mpEventTracking(mp, "Reload_Computer_List", null);

                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction trans = fragmentManager.beginTransaction();
                trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                trans.replace(R.id.nav_host_fragment, new LaunchComputerList(returnCallback)).commit();


                completeOnCreate();

//                reloadTimer = true;
//                reloadHander.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        reloadTimer = false;
//                    }
//                }, 5000);
            }
        });

        root.findViewById(R.id.restartButton2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(100);

                if(reloadTimer) { return; }

                MixPanel.mpEventTracking(mp, "Reload_Computer_List", null);

                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction trans = fragmentManager.beginTransaction();
                trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                trans.replace(R.id.nav_host_fragment, new LaunchComputerList(returnCallback)).commit();


                completeOnCreate();

                reloadTimer = true;
                reloadHander.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        reloadTimer = false;
                    }
                }, 5000);

            }
        });


        root.findViewById(R.id.manualIP).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(10);

                MixPanel.mpEventTracking(mp, "Open_Manual_PC", null);

                startActivity(new Intent(requireActivity(), ManualComputerJoin_Neon.class));

            }
        });
        root.findViewById(R.id.arcadeEmailPopup).findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                root.findViewById(R.id.arcadeEmailPopup).setVisibility(View.GONE);
                vibrator.vibrate(10);

            }
        });

        root.findViewById(R.id.arcadeEmailPopup).findViewById(R.id.submitEmail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HashMap<String, String> data = new HashMap<>();

                EditText emailField = root.findViewById(R.id.arcadeEmailPopup).findViewById(R.id.emailField);
                data.put("Email", emailField.getText().toString());
                MixPanel.mpEventTracking(mp, "Email_For_NeonArcade", data);

                root.findViewById(R.id.arcadeEmailPopup).setVisibility(View.GONE);

                vibrator.vibrate(10);

            }
        });

        try {
            pcFragmentContainerNEON = root.findViewById(R.id.pcFragmentContainerNEON);
        } catch (IllegalArgumentException e) {
            Toast.makeText(getContext(), R.string.there_was_an_error, Toast.LENGTH_LONG).show();
        }

        mFirebaseAnalytics.logEvent("Launch_Computer_Fragment", new Bundle());


        MixPanel.mpEventTracking(mp, "Launch_Computer_Fragment_opened", null);

        // Assume we're in the foreground when created to avoid a race
        // between binding to CMS and onResume()
        inForeground = true;


        // Create a GLSurfaceView to fetch GLRenderer unless we have
        // a cached result already.
        final GlPreferences glPrefs = GlPreferences.readPreferences(requireActivity());
        if (!glPrefs.savedFingerprint.equals(Build.FINGERPRINT) || glPrefs.glRenderer.isEmpty()) {
            GLSurfaceView surfaceView = new GLSurfaceView(requireActivity());

            surfaceView.setRenderer(new GLSurfaceView.Renderer() {
                @Override
                public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
                    // Save the GLRenderer string so we don't need to do requireActivity() next time
                    glPrefs.glRenderer = gl10.glGetString(GL10.GL_RENDERER);
                    glPrefs.savedFingerprint = Build.FINGERPRINT;
                    glPrefs.writePreferences();

                    LimeLog.info("Fetched GL Renderer: " + glPrefs.glRenderer);

                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                ((ViewGroup) root.findViewById(R.id.pcFragmentContainerNEON)).removeView(surfaceView);
                            } catch (IllegalArgumentException e) {
//                                Toast.makeText(getContext(), "There was an error.", Toast.LENGTH_LONG).show();

                            }

                            //Ensures the fragment is still attached
                            if(getActivity() != null) {
                                completeOnCreate();
                            } else {
                                //Fragment was likely destroyed
                                Context c = getContext();
                                if(c != null) {
                                    Toast.makeText(c, "There was an error.", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
                }

                @Override
                public void onSurfaceChanged(GL10 gl10, int i, int i1) {
                }

                @Override
                public void onDrawFrame(GL10 gl10) {
                }


            });


            if(pcFragmentContainerNEON != null) {
                pcFragmentContainerNEON.addView(surfaceView);
            } else {
                Toast.makeText(getContext(), "There was an error.", Toast.LENGTH_LONG).show();
            }

        } else {

            LimeLog.info("Cached GL Renderer: " + glPrefs.glRenderer);
            completeOnCreate();

        }

        return root;
    }

    private void completeOnCreate() {

        UserData.CurrentFragment = UserData.LAUNCH_COMPUTER_LIST;
        completeOnCreateCalled = true;

        shortcutHelper = new ShortcutHelper(requireActivity());

//        UiHelper.setLocale(requireActivity());

        // Bind to the computer manager service
        requireActivity().bindService(new Intent(requireActivity(), ComputerManagerService.class), serviceConnection,
                Service.BIND_AUTO_CREATE);

        pcGridAdapter = new PcGridAdapter(requireActivity(), PreferenceConfiguration.readPreferences(requireActivity()));

        initializeViews();
    }

    private void startComputerUpdates() {
        // Only allow polling to start if we're bound to CMS, polling is not already running,
        // and our activity is in the foreground.
        if (managerBinder != null && !runningPolling && inForeground) {
            freezeUpdates = false;
            managerBinder.startPolling(new ComputerManagerListener() {
                @Override
                public void notifyComputerUpdated(final ComputerDetails details) {
                    if (!freezeUpdates) {
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateComputer(details);
                            }
                        });
                    }
                }
            });
            runningPolling = true;
        }
    }

    private void stopComputerUpdates(boolean wait) {
        if (managerBinder != null) {
            if (!runningPolling) {
                return;
            }

            freezeUpdates = true;

            managerBinder.stopPolling();

            if (wait) {
                managerBinder.waitForPollingStopped();
            }

            runningPolling = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (managerBinder != null) {
            requireActivity().unbindService(serviceConnection);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Display a decoder crash notification if we've returned after a crash
        UiHelper.showDecoderCrashDialog(requireActivity());

        inForeground = true;
        startComputerUpdates();

        requireActivity().getWindow().getDecorView().setSystemUiVisibility(uiFlag);

    }

    @Override
    public void onPause() {
        super.onPause();

        inForeground = false;
        stopComputerUpdates(false);
    }

    @Override
    public void onStop() {
        super.onStop();

        Dialog.closeDialogs();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        stopComputerUpdates(false);

        // Call superclass
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        LaunchComputerList.ComputerObject computer = (LaunchComputerList.ComputerObject) pcGridAdapter.getItem(info.position);

        // Add a header with PC status details
        menu.clearHeader();
        String headerTitle = computer.details.name + " - ";
        switch (computer.details.state)
        {
            case ONLINE:
                headerTitle += getResources().getString(R.string.pcview_menu_header_online);
                break;
            case OFFLINE:
                menu.setHeaderIcon(R.drawable.ic_pc_offline);
                headerTitle += getResources().getString(R.string.pcview_menu_header_offline);
                break;
            case UNKNOWN:
                headerTitle += getResources().getString(R.string.pcview_menu_header_unknown);
                break;
        }

        menu.setHeaderTitle(headerTitle);

        // Inflate the context menu
        if (computer.details.state == ComputerDetails.State.OFFLINE ||
                computer.details.state == ComputerDetails.State.UNKNOWN) {
//            menu.add(Menu.NONE, WOL_ID, 1, getResources().getString(R.string.pcview_menu_send_wol));
        }
        else if (computer.details.pairState != PairingManager.PairState.PAIRED) {
            menu.add(Menu.NONE, PAIR_ID, 1, getResources().getString(R.string.pcview_menu_pair_pc));
        }
        else {
//            if (computer.details.runningGameId != 0) {
//                menu.add(Menu.NONE, RESUME_ID, 1, getResources().getString(R.string.applist_menu_resume));
//                menu.add(Menu.NONE, QUIT_ID, 2, getResources().getString(R.string.applist_menu_quit));
//            }


//            menu.add(Menu.NONE, FULL_APP_LIST_ID, 4, getResources().getString(R.string.pcview_menu_app_list));
        }

        menu.add(Menu.NONE, WOL_ID, 1, getResources().getString(R.string.pcview_menu_send_wol));
        menu.add(Menu.NONE, TEST_NETWORK_ID, 5, getResources().getString(R.string.pcview_menu_test_network));
        menu.add(Menu.NONE, DELETE_ID, 6, getResources().getString(R.string.pcview_menu_delete_pc));
        menu.add(Menu.NONE, VIEW_DETAILS_ID, 7,  getResources().getString(R.string.pcview_menu_details));
    }

//    @Override
//    public void onContextMenuClosed(Menu menu) {
//        // For some reason, requireActivity() gets called again _after_ onPause() is called on requireActivity() activity.
//        // startComputerUpdates() manages requireActivity() and won't actual start polling until the activity
//        // returns to the foreground.
//        startComputerUpdates();
//    }

    private void doPair(final ComputerDetails computer) {
        if (computer.state == ComputerDetails.State.OFFLINE || computer.activeAddress == null) {
            Toast.makeText(requireActivity(), getResources().getString(R.string.pair_pc_offline), Toast.LENGTH_SHORT).show();
            return;
        }
        if (managerBinder == null) {
            Toast.makeText(requireActivity(), getResources().getString(R.string.error_manager_not_running), Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(requireActivity(), getResources().getString(R.string.pairing), Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {

                if(getContext() == null) { return; }
                if(getActivity() == null) { return; }

                NvHTTP httpConn;
                String message;
                boolean success = false;

                boolean noContext = false;
                try {
                    // Stop updates and wait while pairing
                    stopComputerUpdates(true);

                    httpConn = new NvHTTP(ServerHelper.getCurrentAddressFromComputer(computer),
                            computer.httpsPort, managerBinder.getUniqueId(), computer.serverCert,
                            PlatformBinding.getCryptoProvider(requireActivity()));
                    if (httpConn.getPairState() == PairingManager.PairState.PAIRED) {
                        // Don't display any toast, but open the app list
                        message = null;
                        success = true;
                    }
                    else {
                        final String pinStr = PairingManager.generatePinString();

                        // Spin the dialog off in a thread because it blocks

                        Dialog.displayDialog(requireActivity(), getResources().getString(R.string.pair_pairing_title),
                                getResources().getString(R.string.pair_pairing_msg)+" "+pinStr, false);

                        PairingManager pm = httpConn.getPairingManager();

                        PairingManager.PairState pairState = pm.pair(httpConn.getServerInfo(true), pinStr);
                        if (pairState == PairingManager.PairState.PIN_WRONG) {
                            message = getResources().getString(R.string.pair_incorrect_pin);
                        }
                        else if (pairState == PairingManager.PairState.FAILED) {
                            if (computer.runningGameId != 0) {
                                message = getResources().getString(R.string.pair_pc_ingame);
                            }
                            else {
                                message = getResources().getString(R.string.pair_fail);
                            }
                        }
                        else if (pairState == PairingManager.PairState.ALREADY_IN_PROGRESS) {
                            message = getResources().getString(R.string.pair_already_in_progress);
                        }
                        else if (pairState == PairingManager.PairState.PAIRED) {
                            // Just navigate to the app view without displaying a toast
                            message = null;
                            success = true;

                            // Pin requireActivity() certificate for later HTTPS use
                            managerBinder.getComputer(computer.uuid).serverCert = pm.getPairedCert();

                            // Invalidate reachability information after pairing to force
                            // a refresh before reading pair state again
                            managerBinder.invalidateStateForComputer(computer.uuid);
                        }
                        else {
                            // Should be no other values
                            message = null;
                        }
                    }
                } catch (UnknownHostException e) {
                    message = getResources().getString(R.string.error_unknown_host);
                } catch (FileNotFoundException e) {
                    message = getResources().getString(R.string.error_404);
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                    message = e.getMessage();
                } catch (IllegalStateException e) {
                    Log.e("contentError", "Could not get resources");
                    message = "error";
                    noContext = true;
                }

                Dialog.closeDialogs();

                if(!noContext) {
                        final String toastMessage = message;
                        final boolean toastSuccess = success;

                        try {
                            requireActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    if (getContext() == null) {
                                        return;
                                    }
                                    if (getActivity() == null) {
                                        return;
                                    }

                                    if (toastMessage != null) {
                                        Toast.makeText(requireActivity(), toastMessage, Toast.LENGTH_LONG).show();
                                    }

                                    if (toastSuccess) {
                                        // Open the app list after a successful pairing attempt
                                        doAppList(computer, true, false);
                                    } else {
                                        // Start polling again if we're still in the foreground
                                        startComputerUpdates();
                                    }
                                }
                            });
                        } catch (IllegalStateException e) {

                        }
                    }
            }
        }).start();
    }

    private void doWakeOnLan(final ComputerDetails computer) {
        if (computer.state == ComputerDetails.State.ONLINE) {
            Toast.makeText(requireActivity(), getResources().getString(R.string.wol_pc_online), Toast.LENGTH_SHORT).show();
            return;
        }

        if (computer.macAddress == null) {
            Toast.makeText(requireActivity(), getResources().getString(R.string.wol_no_mac), Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String message;
                try {
                    WakeOnLanSender.sendWolPacket(computer);
                    message = getResources().getString(R.string.wol_waking_msg);
                } catch (IOException e) {
                    message = getResources().getString(R.string.wol_fail);
                }

                final String toastMessage = message;
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(requireActivity(), toastMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }

    private void doUnpair(final ComputerDetails computer) {
        if (computer.state == ComputerDetails.State.OFFLINE || computer.activeAddress == null) {
            Toast.makeText(requireActivity(), getResources().getString(R.string.error_pc_offline), Toast.LENGTH_SHORT).show();
            return;
        }
        if (managerBinder == null) {
            Toast.makeText(requireActivity(), getResources().getString(R.string.error_manager_not_running), Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(requireActivity(), getResources().getString(R.string.unpairing), Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                NvHTTP httpConn;
                String message;
                try {
                    httpConn = new NvHTTP(ServerHelper.getCurrentAddressFromComputer(computer),
                            computer.httpsPort, managerBinder.getUniqueId(), computer.serverCert,
                            PlatformBinding.getCryptoProvider(requireActivity()));
                    if (httpConn.getPairState() == PairingManager.PairState.PAIRED) {
                        httpConn.unpair();
                        if (httpConn.getPairState() == PairingManager.PairState.NOT_PAIRED) {
                            message = getResources().getString(R.string.unpair_success);
                        }
                        else {
                            message = getResources().getString(R.string.unpair_fail);
                        }
                    }
                    else {
                        message = getResources().getString(R.string.unpair_error);
                    }
                } catch (UnknownHostException e) {
                    message = getResources().getString(R.string.error_unknown_host);
                } catch (FileNotFoundException e) {
                    message = getResources().getString(R.string.error_404);
                } catch (XmlPullParserException | IOException e) {
                    message = e.getMessage();
                    e.printStackTrace();
                }

                final String toastMessage = message;
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(requireActivity(), toastMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }

    private void doAppList(ComputerDetails computer, boolean newlyPaired, boolean showHiddenGames) {
        if (computer.state == ComputerDetails.State.OFFLINE) {
            Toast.makeText(requireActivity(), getResources().getString(R.string.error_pc_offline), Toast.LENGTH_SHORT).show();
            return;
        }
        if (managerBinder == null) {
            Toast.makeText(requireActivity(), getResources().getString(R.string.error_manager_not_running), Toast.LENGTH_LONG).show();
            return;
        }

//        Intent i = new Intent(requireActivity(), AppView.class);
//        i.putExtra(AppView.NAME_EXTRA, computer.name);
//        i.putExtra(AppView.UUID_EXTRA, computer.uuid);
//        i.putExtra(AppView.NEW_PAIR_EXTRA, newlyPaired);
//        i.putExtra(AppView.SHOW_HIDDEN_APPS_EXTRA, showHiddenGames);
//        startActivity(i);


        //To avoid java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if(returnCallback != null) {
                    Object ret = -5;
                    returnCallback.onChange(ret);
                }

                if(getActivity() == null) {

                    if(returnCallback != null) {
                        returnCallback.onChange(UserData.LAUNCH_COMPUTER_LIST);
                    }

                    return;
                }

                // Perform your Fragment transaction here
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

                FragmentTransaction trans = fragmentManager.beginTransaction();
                trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                trans.replace(R.id.nav_host_fragment, new LaunchGameList(computer.name, computer.uuid, newlyPaired, showHiddenGames, returnCallback)).commitAllowingStateLoss();
            }
        }, 100); // Delay in milliseconds
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final LaunchComputerList.ComputerObject computer = (LaunchComputerList.ComputerObject) pcGridAdapter.getItem(info.position);
        switch (item.getItemId()) {
            case PAIR_ID:
                doPair(computer.details);
                return true;

            case UNPAIR_ID:
                doUnpair(computer.details);
                return true;

            case WOL_ID:
                doWakeOnLan(computer.details);
                return true;

            case DELETE_ID:
                if (ActivityManager.isUserAMonkey()) {
                    LimeLog.info("Ignoring delete PC request from monkey");
                    return true;
                }
                UiHelper.displayDeletePcConfirmationDialog(requireActivity(), computer.details, new Runnable() {
                    @Override
                    public void run() {
                        if (managerBinder == null) {
                            Toast.makeText(requireActivity(), getResources().getString(R.string.error_manager_not_running), Toast.LENGTH_LONG).show();
                            return;
                        }
                        removeComputer(computer.details);
                    }
                }, null);
                return true;

            case FULL_APP_LIST_ID:
                doAppList(computer.details, false, true);
                return true;

            case RESUME_ID:
                if (managerBinder == null) {
                    Toast.makeText(requireActivity(), getResources().getString(R.string.error_manager_not_running), Toast.LENGTH_LONG).show();
                    return true;
                }

                ServerHelper.doStart(requireActivity(), new NvApp("app", computer.details.runningGameId, false), computer.details, managerBinder);
                return true;

            case QUIT_ID:
                if (managerBinder == null) {
                    Toast.makeText(requireActivity(), getResources().getString(R.string.error_manager_not_running), Toast.LENGTH_LONG).show();
                    return true;
                }

                // Display a confirmation dialog first
                UiHelper.displayQuitConfirmationDialog(requireActivity(), new Runnable() {
                    @Override
                    public void run() {
                        ServerHelper.doQuit(requireActivity(), computer.details,
                                new NvApp("app", 0, false), managerBinder, null);
                    }
                }, null);
                return true;

            case VIEW_DETAILS_ID:
                Dialog.displayDialog(requireActivity(), getResources().getString(R.string.title_details), computer.details.toString(), false);
                return true;

            case TEST_NETWORK_ID:
                ServerHelper.doNetworkTest(requireActivity());
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    private void removeComputer(ComputerDetails details) {
        managerBinder.removeComputer(details);

        new DiskAssetLoader(requireActivity()).deleteAssetsForComputer(details.uuid);

        // Delete hidden games preference value
        requireActivity().getSharedPreferences(AppView.HIDDEN_APPS_PREF_FILENAME, MODE_PRIVATE)
                .edit()
                .remove(details.uuid)
                .apply();

        for (int i = 0; i < pcGridAdapter.getCount(); i++) {
            LaunchComputerList.ComputerObject computer = (LaunchComputerList.ComputerObject) pcGridAdapter.getItem(i);

            if (details.equals(computer.details)) {
                // Disable or delete shortcuts referencing requireActivity() PC
                shortcutHelper.disableComputerShortcut(details,
                        getResources().getString(R.string.scut_deleted_pc));

                pcGridAdapter.removeComputer(computer);
                pcGridAdapter.notifyDataSetChanged();

                if (pcGridAdapter.getCount() == 0) {
                    // Show the "Discovery in progress" view
//                    noPcFoundLayout.setVisibility(View.VISIBLE);
                    MixPanel.mpEventTracking(requireContext(), "no_computer_found", null);

                    root.findViewById(R.id.no_pc_found_layout).setVisibility(View.VISIBLE);

                }

                break;
            }
        }
    }

    private void updateComputer(ComputerDetails details) {
        LaunchComputerList.ComputerObject existingEntry = null;

        for (int i = 0; i < pcGridAdapter.getCount(); i++) {
            LaunchComputerList.ComputerObject computer = (LaunchComputerList.ComputerObject) pcGridAdapter.getItem(i);

            // Check if requireActivity() is the same computer
            if (details.uuid.equals(computer.details.uuid)) {
                existingEntry = computer;
                break;
            }
        }

        // Add a launcher shortcut for requireActivity() PC
        if (details.pairState == PairingManager.PairState.PAIRED) {
            shortcutHelper.createAppViewShortcutForOnlineHost(details);
        }

        if (existingEntry != null) {
            // Replace the information in the existing entry
            existingEntry.details = details;
        }
        else {
            // Add a new entry
            pcGridAdapter.addComputer(new LaunchComputerList.ComputerObject(details));

            // Remove the "Discovery in progress" view
//            noPcFoundLayout.setVisibility(View.INVISIBLE);
            root.findViewById(R.id.no_pc_found_layout).setVisibility(View.GONE);

//            root.findViewById(R.id.pcs_loading).setVisibility(View.GONE);



            MixPanel.mpEventTracking(requireContext(), "computer_found", null);

        }

        // Notify the view that the data has changed
        pcGridAdapter.notifyDataSetChanged();
    }

    @Override
    public int getAdapterFragmentLayoutId() {
        return R.layout.pc_grid_view;
    }


    //Todo this is where i click on the PC
    @Override
    public void receiveAbsListView(AbsListView listView) {
        listView.setAdapter(pcGridAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
                                    long id) {
                LaunchComputerList.ComputerObject computer = (LaunchComputerList.ComputerObject) pcGridAdapter.getItem(pos);
                if (computer.details.state == ComputerDetails.State.UNKNOWN ||
                        computer.details.state == ComputerDetails.State.OFFLINE) {
                    // Open the context menu if a PC is offline or refreshing
                    requireActivity().openContextMenu(arg1);
                } else if (computer.details.pairState != PairingManager.PairState.PAIRED) {
                    // Pair an unpaired machine by default
                    doPair(computer.details);
                } else {
                    doAppList(computer.details, false, false);
                }
            }
        });
        UiHelper.applyStatusBarPadding(listView);
        registerForContextMenu(listView);
    }

    public static class ComputerObject {
        public ComputerDetails details;

        public ComputerObject(ComputerDetails details) {
            if (details == null) {
                throw new IllegalArgumentException("details must not be null");
            }
            this.details = details;
        }

        @Override
        public String toString() {
            return details.name;
        }
    }

}
