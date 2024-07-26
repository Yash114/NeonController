package com.gingertech.starbeam.ui.launch;

import static android.content.Context.MODE_PRIVATE;

import static com.gingertech.starbeam.MainActivity.mFirebaseAnalytics;
import static com.gingertech.starbeam.MainActivity.uiFlag;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.gingertech.starbeam.MainActivity;
import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.UserData;
import com.gingertech.starbeam.helpers.controllers.GenericCallback;
import com.gingertech.starbeam.helpers.controllers.GenericCallbackv2;
import com.gingertech.starbeam.helpers.controllers.MixPanel;
import com.gingertech.starbeam.helpers.controllers.OnGenericCallbackv2;
import com.gingertech.starbeam.limelight.AppView;
import com.gingertech.starbeam.limelight.Game;
import com.gingertech.starbeam.limelight.LimeLog;
import com.gingertech.starbeam.limelight.computers.ComputerManagerListener;
import com.gingertech.starbeam.limelight.computers.ComputerManagerService;
import com.gingertech.starbeam.limelight.grid.AppGridAdapter;
import com.gingertech.starbeam.limelight.nvstream.http.ComputerDetails;
import com.gingertech.starbeam.limelight.nvstream.http.NvApp;
import com.gingertech.starbeam.limelight.nvstream.http.NvHTTP;
import com.gingertech.starbeam.limelight.nvstream.http.PairingManager;
import com.gingertech.starbeam.limelight.preferences.PreferenceConfiguration;
import com.gingertech.starbeam.limelight.ui.AdapterFragment;
import com.gingertech.starbeam.limelight.ui.AdapterFragmentCallbacks;
import com.gingertech.starbeam.limelight.utils.CacheHelper;
import com.gingertech.starbeam.limelight.utils.Dialog;
import com.gingertech.starbeam.limelight.utils.ServerHelper;
import com.gingertech.starbeam.limelight.utils.ShortcutHelper;
import com.gingertech.starbeam.limelight.utils.SpinnerDialog;
import com.gingertech.starbeam.limelight.utils.UiHelper;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.security.cert.CertificateEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class LaunchGameList extends Fragment implements AdapterFragmentCallbacks {

    View root;

    public LaunchGameList(){}

    private MixpanelAPI mp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        UserData.CurrentFragment = UserData.LAUNCH_GAME_SELECT;

        mFirebaseAnalytics.logEvent("Launch_Game_Fragment", new Bundle());

        mp = MixPanel.makeObj(requireContext());

        MixPanel.mpEventTracking(mp, "Launch_Game_Fragment_opened", null);

        root = inflater.inflate(R.layout.activity_app_view_aethr, container, false);

        try {
            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } catch (NullPointerException ignored) {
        }
        // Assume we're in the foreground when created to avoid a race
        // between binding to CMS and onResume()
        inForeground = true;

        shortcutHelper = new ShortcutHelper(requireActivity());

        UiHelper.setLocale(requireActivity());

        // Allow floating expanded PiP overlays while browsing apps
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireActivity().setShouldDockBigOverlays(false);
        }

//        UiHelper.notifyNewRootView(requireActivity());

        SharedPreferences hiddenAppsPrefs = getActivity().getSharedPreferences(HIDDEN_APPS_PREF_FILENAME, MODE_PRIVATE);
        for (String hiddenAppIdStr : hiddenAppsPrefs.getStringSet(uuidString, new HashSet<String>())) {
            hiddenAppIds.add(Integer.parseInt(hiddenAppIdStr));
        }


        // Bind to the computer manager service
        requireActivity().bindService(new Intent(this.requireActivity(), ComputerManagerService.class), serviceConnection,
                Service.BIND_AUTO_CREATE);

        TextView label = root.findViewById(R.id.appListText);
        requireActivity().setTitle(computerName);
        label.setText(R.string.Pick_your_game);

        return root;
    }

    private AppGridAdapter appGridAdapter;
    private ShortcutHelper shortcutHelper;

    private ComputerDetails computer;
    private ComputerManagerService.ApplistPoller poller;
    private SpinnerDialog blockingLoadSpinner;
    private String lastRawApplist;
    private int lastRunningAppId;
    private boolean suspendGridUpdates;
    private boolean inForeground;
    private final HashSet<Integer> hiddenAppIds = new HashSet<>();

    private final static int START_OR_RESUME_ID = 1;
    private final static int QUIT_ID = 2;
    private final static int START_WITH_QUIT = 4;
    private final static int VIEW_DETAILS_ID = 5;
    private final static int CREATE_SHORTCUT_ID = 6;
    private final static int HIDE_APP_ID = 7;

    public final static String HIDDEN_APPS_PREF_FILENAME = "HiddenApps";

    private String uuidString;
    private boolean showHiddenApps;
    private String computerName;
    private boolean newPairExtra;

    private GenericCallbackv2 returnCallback;

    public LaunchGameList(String name, String uuid, boolean pair, boolean hidden, @Nullable GenericCallbackv2 returnCallback) {
        this.computerName = name;
        this.uuidString = uuid;
        this.newPairExtra = pair;
        this.showHiddenApps = hidden;

        this.returnCallback = returnCallback;
    }

    private ComputerManagerService.ComputerManagerBinder managerBinder;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            final ComputerManagerService.ComputerManagerBinder localBinder =
                    ((ComputerManagerService.ComputerManagerBinder)binder);

            // Wait in a separate thread to avoid stalling the UI
            new Thread() {
                @Override
                public void run() {
                    // Wait for the binder to be ready
                    localBinder.waitForReady();

                    // Get the computer object
                    computer = localBinder.getComputer(uuidString);
                    if (computer == null) {

                        if(returnCallback != null) {
                            returnCallback.onChange(UserData.LAUNCH_COMPUTER_LIST);
                        }

                        return;
                    }

                    // Add a launcher shortcut for this PC (forced, since this is user interaction)
                    shortcutHelper.createAppViewShortcut(computer, true, newPairExtra);
                    shortcutHelper.reportComputerShortcutUsed(computer);

                    try {
                        appGridAdapter = new AppGridAdapter(requireContext(),
                                PreferenceConfiguration.readPreferences(requireContext()),
                                computer, localBinder.getUniqueId(),
                                showHiddenApps);
                    } catch (Exception e) {
                        e.printStackTrace();

                        if(returnCallback != null) {
                            returnCallback.onChange(UserData.LAUNCH_COMPUTER_LIST);
                        }

                        return;
                    }

                    appGridAdapter.updateHiddenApps(hiddenAppIds, true);

                    // Now make the binder visible. We must do this after appGridAdapter
                    // is set to prevent us from reaching updateUiWithServerinfo() and
                    // touching the appGridAdapter prior to initialization.
                    managerBinder = localBinder;

                    // Load the app grid with cached data (if possible).
                    // This must be done _before_ startComputerUpdates()
                    // so the initial serverinfo response can update the running
                    // icon.
                    populateAppGridWithCache();

                    // Start updates
                    startComputerUpdates();

                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (requireActivity().isFinishing() || requireActivity().isChangingConfigurations()) {
                                return;
                            }

                            // Despite my best efforts to catch all conditions that could
                            // cause the activity to be destroyed when we try to commit
                            // I haven't been able to, so we have this try-catch block.
                            try {

                                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.appFragmentContainer, new AdapterFragment(LaunchGameList.this));
                                transaction.commitAllowingStateLoss();

                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            }
                        }
                    });
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

        // If appGridAdapter is initialized, let it know about the configuration change.
        // If not, it will pick it up when it initializes.
        if (appGridAdapter != null) {
            // Update the app grid adapter to create grid items with the correct layout
            appGridAdapter.updateLayoutWithPreferences(requireContext(), PreferenceConfiguration.readPreferences(requireContext()));

            try {
                // Reinflate the app grid itself to pick up the layout change
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.appFragmentContainer, new AdapterFragment(LaunchGameList.this));
                transaction.commitAllowingStateLoss();

            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private void startComputerUpdates() {
        // Don't start polling if we're not bound or in the foreground
        if (managerBinder == null || !inForeground) {
            return;
        }

        managerBinder.startPolling(new ComputerManagerListener() {
            @Override
            public void notifyComputerUpdated(final ComputerDetails details) {
                // Do nothing if updates are suspended
                if (suspendGridUpdates) {
                    return;
                }

                // Don't care about other computers
                if (!details.uuid.equalsIgnoreCase(uuidString)) {
                    return;
                }

                if (details.state == ComputerDetails.State.OFFLINE) {
                    // The PC is unreachable now
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Display a toast to the user and quit the activity
                            Toast.makeText(requireContext(), getResources().getText(R.string.lost_connection), Toast.LENGTH_SHORT).show();
//                            requireActivity().finish();
                        }
                    });

                    return;
                }

                // Close immediately if the PC is no longer paired
                if (details.state == ComputerDetails.State.ONLINE && details.pairState != PairingManager.PairState.PAIRED) {
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Disable shortcuts referencing this PC for now
                            shortcutHelper.disableComputerShortcut(details,
                                    getResources().getString(R.string.scut_not_paired));

                            // Display a toast to the user and quit the activity
                            Toast.makeText(requireContext(), getResources().getText(R.string.scut_not_paired), Toast.LENGTH_SHORT).show();
//                            requireActivity().finish();
                        }
                    });

                    return;
                }

                // App list is the same or empty
                if (details.rawAppList == null || details.rawAppList.equals(lastRawApplist)) {

                    // Let's check if the running app ID changed
                    if (details.runningGameId != lastRunningAppId) {
                        // Update the currently running game using the app ID
                        lastRunningAppId = details.runningGameId;
                        updateUiWithServerinfo(details);
                    }

                    return;
                }

                lastRunningAppId = details.runningGameId;
                lastRawApplist = details.rawAppList;

                try {
                    updateUiWithAppList(NvHTTP.getAppListByReader(new StringReader(details.rawAppList)));
                    updateUiWithServerinfo(details);

                    if (blockingLoadSpinner != null) {
                        blockingLoadSpinner.dismiss();
                        blockingLoadSpinner = null;
                    }
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }
            }
        });

        if (poller == null) {
            poller = managerBinder.createAppListPoller(computer);
        }
        poller.start();
    }

    private void stopComputerUpdates() {
        if (poller != null) {
            poller.stop();
        }

        if (managerBinder != null) {
            managerBinder.stopPolling();
        }

        if (appGridAdapter != null) {
            appGridAdapter.cancelQueuedOperations();
        }
    }

    private void updateHiddenApps(boolean hideImmediately) {
        HashSet<String> hiddenAppIdStringSet = new HashSet<>();

        for (Integer hiddenAppId : hiddenAppIds) {
            hiddenAppIdStringSet.add(hiddenAppId.toString());
        }

        requireActivity().getSharedPreferences(HIDDEN_APPS_PREF_FILENAME, MODE_PRIVATE)
                .edit()
                .putStringSet(uuidString, hiddenAppIdStringSet)
                .apply();

        appGridAdapter.updateHiddenApps(hiddenAppIds, hideImmediately);
    }

    private void populateAppGridWithCache() {
        try {
            // Try to load from cache
            lastRawApplist = CacheHelper.readInputStreamToString(CacheHelper.openCacheFileForInput(requireActivity().getCacheDir(), "applist", uuidString));
            List<NvApp> applist = NvHTTP.getAppListByReader(new StringReader(lastRawApplist));
            updateUiWithAppList(applist);
            LimeLog.info("Loaded applist from cache");
        } catch (IOException | XmlPullParserException e) {
            if (lastRawApplist != null) {
                LimeLog.warning("Saved applist corrupted: "+lastRawApplist);
                e.printStackTrace();
            }
            LimeLog.info("Loading applist from the network");
            // We'll need to load from the network
            loadAppsBlocking();
        }
    }

    private void loadAppsBlocking() {
        blockingLoadSpinner = SpinnerDialog.displayDialog(requireActivity(), getResources().getString(R.string.applist_refresh_title),
                getResources().getString(R.string.applist_refresh_msg), true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        SpinnerDialog.closeDialogs(requireActivity());
        Dialog.closeDialogs();

        if (managerBinder != null) {
            requireActivity().unbindService(serviceConnection);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().getWindow().getDecorView().setSystemUiVisibility(uiFlag);

        // Display a decoder crash notification if we've returned after a crash
        UiHelper.showDecoderCrashDialog(requireActivity());

        inForeground = true;
        startComputerUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();

        inForeground = false;
        stopComputerUpdates();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        AppView.AppObject selectedApp = (AppView.AppObject) appGridAdapter.getItem(info.position);

        menu.setHeaderTitle(selectedApp.app.getAppName());

        if (lastRunningAppId != 0) {
            if (lastRunningAppId == selectedApp.app.getAppId()) {
                menu.add(Menu.NONE, START_OR_RESUME_ID, 1, getResources().getString(R.string.applist_menu_resume));
                menu.add(Menu.NONE, QUIT_ID, 2, getResources().getString(R.string.applist_menu_quit));
            }
            else {
                menu.add(Menu.NONE, START_WITH_QUIT, 1, getResources().getString(R.string.applist_menu_quit_and_start));
            }
        }

        // Only show the hide checkbox if this is not the currently running app or it's already hidden
        if (lastRunningAppId != selectedApp.app.getAppId() || selectedApp.isHidden) {
            MenuItem hideAppItem = menu.add(Menu.NONE, HIDE_APP_ID, 3, getResources().getString(R.string.applist_menu_hide_app));
            hideAppItem.setCheckable(true);
            hideAppItem.setChecked(selectedApp.isHidden);
        }

        menu.add(Menu.NONE, VIEW_DETAILS_ID, 4, getResources().getString(R.string.applist_menu_details));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Only add an option to create shortcut if box art is loaded
            // and when we're in grid-mode (not list-mode).
            ImageView appImageView = info.targetView.findViewById(R.id.grid_image);
            if (appImageView != null) {
                // We have a grid ImageView, so we must be in grid-mode
                BitmapDrawable drawable = (BitmapDrawable)appImageView.getDrawable();
                if (drawable != null && drawable.getBitmap() != null) {
                    // We have a bitmap loaded too
                    menu.add(Menu.NONE, CREATE_SHORTCUT_ID, 5, getResources().getString(R.string.applist_menu_scut));
                }
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final AppView.AppObject app = (AppView.AppObject) appGridAdapter.getItem(info.position);

        HashMap<String, Object> connData = new HashMap<>();

        connData.put("host", computer.activeAddress.address);
        connData.put("port", computer.activeAddress.port);
        connData.put("httpsPort", computer.httpsPort);
        connData.put("appName", app.app.getAppName());
        connData.put("appId", app.app.getAppId());
        connData.put("uniqueId", managerBinder.getUniqueId());
        connData.put("uuid", computer.uuid);
        connData.put("appSupportsHdr", app.app.isHdrSupported());
        connData.put("pcName", computer.name);

        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction trans = fragmentManager.beginTransaction();
        trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        try {
            if (computer.serverCert != null) {
                connData.put("derCertData", computer.serverCert.getEncoded());
            }
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }

        switch (item.getItemId()) {
            case START_WITH_QUIT:
                // Display a confirmation dialog first
                UiHelper.displayQuitConfirmationDialog(requireActivity(), new Runnable() {
                    @Override
                    public void run() {
//                        ServerHelper.doStart(requireActivity(), app.app, computer, managerBinder);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                trans.replace(R.id.nav_host_fragment, new LaunchPlayScreen(connData, returnCallback)).commit();
                            }
                        }).start();

                    }
                }, null);
                return true;

            case START_OR_RESUME_ID:
                // Resume is the same as start for us
//                ServerHelper.doStart(requireActivity(), app.app, computer, managerBinder);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        trans.replace(R.id.nav_host_fragment, new LaunchPlayScreen(connData, returnCallback)).commit();
                    }
                }).start();



                return true;

            case QUIT_ID:
                // Display a confirmation dialog first
                UiHelper.displayQuitConfirmationDialog(requireActivity(), new Runnable() {
                    @Override
                    public void run() {
                        suspendGridUpdates = true;
                        ServerHelper.doQuit(requireActivity(), computer,
                                app.app, managerBinder, new Runnable() {
                                    @Override
                                    public void run() {
                                        // Trigger a poll immediately
                                        suspendGridUpdates = false;
                                        if (poller != null) {
                                            poller.pollNow();
                                        }
                                    }
                                });
                    }
                }, null);
                return true;

            case VIEW_DETAILS_ID:
                Dialog.displayDialog(requireActivity(), getResources().getString(R.string.title_details), app.app.toString(), false);
                return true;

            case HIDE_APP_ID:
                if (item.isChecked()) {
                    // Transitioning hidden to shown
                    hiddenAppIds.remove(app.app.getAppId());
                }
                else {
                    // Transitioning shown to hidden
                    hiddenAppIds.add(app.app.getAppId());
                }
                updateHiddenApps(false);
                return true;

            case CREATE_SHORTCUT_ID:
                ImageView appImageView = info.targetView.findViewById(R.id.grid_image);
                Bitmap appBits = ((BitmapDrawable)appImageView.getDrawable()).getBitmap();
                if (!shortcutHelper.createPinnedGameShortcut(computer, app.app, appBits)) {
                    Toast.makeText(requireContext(), getResources().getString(R.string.unable_to_pin_shortcut), Toast.LENGTH_LONG).show();
                }
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    private void updateUiWithServerinfo(final ComputerDetails details) {
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean updated = false;

                // Look through our current app list to tag the running app
                for (int i = 0; i < appGridAdapter.getCount(); i++) {
                    AppView.AppObject existingApp = (AppView.AppObject) appGridAdapter.getItem(i);

                    // There can only be one or zero apps running.
                    if (existingApp.isRunning &&
                            existingApp.app.getAppId() == details.runningGameId) {
                        // This app was running and still is, so we're done now
                        return;
                    }
                    else if (existingApp.app.getAppId() == details.runningGameId) {
                        // This app wasn't running but now is
                        existingApp.isRunning = true;
                        updated = true;
                    }
                    else if (existingApp.isRunning) {
                        // This app was running but now isn't
                        existingApp.isRunning = false;
                        updated = true;
                    }
                    else {
                        // This app wasn't running and still isn't
                    }
                }

                if (updated) {
                    appGridAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void updateUiWithAppList(final List<NvApp> appList) {

        if(getActivity() == null) {
            return;
        }
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                boolean updated = false;

                // First handle app updates and additions
                for (NvApp app : appList) {
                    boolean foundExistingApp = false;

                    // Try to update an existing app in the list first
                    for (int i = 0; i < appGridAdapter.getCount(); i++) {
                        AppView.AppObject existingApp = (AppView.AppObject) appGridAdapter.getItem(i);
                        if (existingApp.app.getAppId() == app.getAppId()) {
                            // Found the app; update its properties
                            if (!existingApp.app.getAppName().equals(app.getAppName())) {
                                existingApp.app.setAppName(app.getAppName());
                                updated = true;
                            }

                            foundExistingApp = true;
                            break;
                        }
                    }

                    if (!foundExistingApp) {
                        // This app must be new
                        appGridAdapter.addApp(new AppView.AppObject(app));

                        // We could have a leftover shortcut from last time this PC was paired
                        // or if this app was removed then added again. Enable those shortcuts
                        // again if present.
                        shortcutHelper.enableAppShortcut(computer, app);

                        updated = true;
                    }
                }

                // Next handle app removals
                int i = 0;
                while (i < appGridAdapter.getCount()) {
                    boolean foundExistingApp = false;
                    AppView.AppObject existingApp = (AppView.AppObject) appGridAdapter.getItem(i);

                    // Check if this app is in the latest list
                    for (NvApp app : appList) {
                        if (existingApp.app.getAppId() == app.getAppId()) {
                            foundExistingApp = true;
                            break;
                        }
                    }

                    // This app was removed in the latest app list
                    if (!foundExistingApp) {
                        shortcutHelper.disableAppShortcut(computer, existingApp.app, "App removed from PC");
                        appGridAdapter.removeApp(existingApp);
                        updated = true;

                        // Check this same index again because the item at i+1 is now at i after
                        // the removal
                        continue;
                    }

                    // Move on to the next item
                    i++;
                }

                if (updated) {
                    appGridAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public int getAdapterFragmentLayoutId() {
        return PreferenceConfiguration.readPreferences(requireContext()).smallIconMode ?
                R.layout.app_grid_view_small : R.layout.app_grid_view;
    }

    @Override
    public void receiveAbsListView(AbsListView listView) {
        listView.setAdapter(appGridAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
                                    long id) {
                AppView.AppObject app = (AppView.AppObject) appGridAdapter.getItem(pos);

                // Only open the context menu if something is running, otherwise start it
//                if (lastRunningAppId != 0) {
//                    requireActivity().openContextMenu(arg1);
//                } else {
//                    ServerHelper.doStart(requireActivity(), app.app, computer, managerBinder);

                    HashMap<String, Object> connData = new HashMap<>();

                    connData.put("host", computer.activeAddress.address);
                    connData.put("port", computer.activeAddress.port);
                    connData.put("httpsPort", computer.httpsPort);
                    connData.put("appName", app.app.getAppName());
                    connData.put("appId", app.app.getAppId());
                    connData.put("uniqueId", managerBinder.getUniqueId());
                    connData.put("uuid", computer.uuid);
                    connData.put("appSupportsHdr", app.app.isHdrSupported());
                    connData.put("pcName", computer.name);

                    try {
                        if (computer.serverCert != null) {
                            connData.put("derCertData", computer.serverCert.getEncoded());
                        }
                    } catch (CertificateEncodingException e) {
                        e.printStackTrace();
                    }

                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    FragmentTransaction trans = fragmentManager.beginTransaction();
                    trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

                    trans.replace(R.id.nav_host_fragment, new LaunchPlayScreen(connData, returnCallback)).commit();
//                }
            }
        });
        UiHelper.applyStatusBarPadding(listView);
        registerForContextMenu(listView);
        listView.requestFocus();
    }

    public static class AppObject {
        public final NvApp app;
        public boolean isRunning;
        public boolean isHidden;

        public AppObject(NvApp app) {
            if (app == null) {
                throw new IllegalArgumentException("app must not be null");
            }
            this.app = app;
        }

        @Override
        public String toString() {
            return app.getAppName();
        }
    }
}
