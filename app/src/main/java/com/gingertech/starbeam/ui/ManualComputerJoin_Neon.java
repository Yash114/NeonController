package com.gingertech.starbeam.ui;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.gingertech.starbeam.MainActivity;
import com.gingertech.starbeam.R;
import com.gingertech.starbeam.limelight.computers.ComputerManagerService;
import com.gingertech.starbeam.limelight.nvstream.http.ComputerDetails;
import com.gingertech.starbeam.limelight.nvstream.http.NvHTTP;
import com.gingertech.starbeam.limelight.nvstream.jni.MoonBridge;
import com.gingertech.starbeam.limelight.utils.Dialog;
import com.gingertech.starbeam.limelight.utils.ServerHelper;
import com.gingertech.starbeam.limelight.utils.SpinnerDialog;
import com.gingertech.starbeam.limelight.utils.UiHelper;

public class ManualComputerJoin_Neon extends Activity {
        private TextView hostText;
        private ComputerManagerService.ComputerManagerBinder managerBinder;
        private final java.util.concurrent.LinkedBlockingQueue<String> computersToAdd = new java.util.concurrent.LinkedBlockingQueue<>();
        private Thread addThread;
        private final ServiceConnection serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, final IBinder binder) {
                managerBinder = ((ComputerManagerService.ComputerManagerBinder)binder);
                startAddThread();
            }

            public void onServiceDisconnected(ComponentName className) {
                joinAddThread();
                managerBinder = null;
            }
        };

        private boolean isWrongSubnetSiteLocalAddress(String address) {
            try {
                java.net.InetAddress targetAddress = java.net.InetAddress.getByName(address);
                if (!(targetAddress instanceof java.net.Inet4Address) || !targetAddress.isSiteLocalAddress()) {
                    return false;
                }

                // We have a site-local address. Look for a matching local interface.
                for (java.net.NetworkInterface iface : java.util.Collections.list(java.net.NetworkInterface.getNetworkInterfaces())) {
                    for (java.net.InterfaceAddress addr : iface.getInterfaceAddresses()) {
                        if (!(addr.getAddress() instanceof java.net.Inet4Address) || !addr.getAddress().isSiteLocalAddress()) {
                            // Skip non-site-local or non-IPv4 addresses
                            continue;
                        }

                        byte[] targetAddrBytes = targetAddress.getAddress();
                        byte[] ifaceAddrBytes = addr.getAddress().getAddress();

                        // Compare prefix to ensure it's the same
                        boolean addressMatches = true;
                        for (int i = 0; i < addr.getNetworkPrefixLength(); i++) {
                            if ((ifaceAddrBytes[i / 8] & (1 << (i % 8))) != (targetAddrBytes[i / 8] & (1 << (i % 8)))) {
                                addressMatches = false;
                                break;
                            }
                        }

                        if (addressMatches) {
                            return false;
                        }
                    }
                }

                // Couldn't find a matching interface
                return true;
            } catch (Exception e) {
                // Catch all exceptions because some broken Android devices
                // will throw an NPE from inside getNetworkInterfaces().
                e.printStackTrace();
                return false;
            }
        }

        private java.net.URI parseRawUserInputToUri(String rawUserInput) {
            try {
                // Try adding a scheme and parsing the remaining input.
                // This handles input like 127.0.0.1:47989, [::1], [::1]:47989, and 127.0.0.1.
                java.net.URI uri = new java.net.URI("moonlight://" + rawUserInput);
                if (uri.getHost() != null && !uri.getHost().isEmpty()) {
                    return uri;
                }
            } catch (java.net.URISyntaxException ignored) {}

            try {
                // Attempt to escape the input as an IPv6 literal.
                // This handles input like ::1.
                java.net.URI uri = new java.net.URI("moonlight://[" + rawUserInput + "]");
                if (uri.getHost() != null && !uri.getHost().isEmpty()) {
                    return uri;
                }
            } catch (java.net.URISyntaxException ignored) {}

            return null;
        }

        private void doAddPc(String rawUserInput) throws InterruptedException {
            boolean wrongSiteLocal = false;
            boolean invalidInput = false;
            boolean success;
            int portTestResult;

            SpinnerDialog dialog = SpinnerDialog.displayDialog(this, getResources().getString(R.string.title_add_pc),
                    getResources().getString(R.string.msg_add_pc), false);

            try {
                ComputerDetails details = new ComputerDetails();

                // Check if we parsed a host address successfully
                java.net.URI uri = parseRawUserInputToUri(rawUserInput);
                if (uri != null && uri.getHost() != null && !uri.getHost().isEmpty()) {
                    String host = uri.getHost();
                    int port = uri.getPort();

                    // If a port was not specified, use the default
                    if (port == -1) {
                        port = NvHTTP.DEFAULT_HTTP_PORT;
                    }

                    details.manualAddress = new ComputerDetails.AddressTuple(host, port);
                    success = managerBinder.addComputerBlocking(details);
                    if (!success){
                        wrongSiteLocal = isWrongSubnetSiteLocalAddress(host);
                    }
                } else {
                    // Invalid user input
                    success = false;
                    invalidInput = true;
                }
            } catch (InterruptedException e) {
                // Propagate the InterruptedException to the caller for proper handling
                dialog.dismiss();
                throw e;
            } catch (IllegalArgumentException e) {
                // This can be thrown from OkHttp if the host fails to canonicalize to a valid name.
                // https://github.com/square/okhttp/blob/okhttp_27/okhttp/src/main/java/com/squareup/okhttp/HttpUrl.java#L705
                e.printStackTrace();
                success = false;
                invalidInput = true;
            }

            // Keep the SpinnerDialog open while testing connectivity
            if (!success && !wrongSiteLocal && !invalidInput) {
                // Run the test before dismissing the spinner because it can take a few seconds.
                portTestResult = MoonBridge.testClientConnectivity(ServerHelper.CONNECTION_TEST_SERVER, 443,
                        MoonBridge.ML_PORT_FLAG_TCP_47984 | MoonBridge.ML_PORT_FLAG_TCP_47989);
            } else {
                // Don't bother with the test if we succeeded or the IP address was bogus
                portTestResult = MoonBridge.ML_TEST_RESULT_INCONCLUSIVE;
            }

            dialog.dismiss();

            if (invalidInput) {
                Dialog.displayDialog(this, getResources().getString(R.string.conn_error_title), getResources().getString(R.string.addpc_unknown_host), false);
            }
            else if (wrongSiteLocal) {
                Dialog.displayDialog(this, getResources().getString(R.string.conn_error_title), getResources().getString(R.string.addpc_wrong_sitelocal), false);
            }
            else if (!success) {
                String dialogText;
                if (portTestResult != MoonBridge.ML_TEST_RESULT_INCONCLUSIVE && portTestResult != 0)  {
                    dialogText = getResources().getString(R.string.nettest_text_blocked);
                }
                else {
                    dialogText = getResources().getString(R.string.addpc_fail);
                }
                Dialog.displayDialog(this, getResources().getString(R.string.conn_error_title), dialogText, false);
            }
            else {
                ManualComputerJoin_Neon.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ManualComputerJoin_Neon.this, getResources().getString(R.string.addpc_success), Toast.LENGTH_LONG).show();

                        if (!isFinishing()) {
                            // Close the activity
                            ManualComputerJoin_Neon.this.finish();
                        }
                    }
                });
            }

        }

        private void startAddThread() {
            addThread = new Thread() {
                @Override
                public void run() {
                    while (!isInterrupted()) {
                        try {
                            String computer = computersToAdd.take();
                            doAddPc(computer);
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                }
            };
            addThread.setName("UI - AddComputerManually");
            addThread.start();
        }

        private void joinAddThread() {
            if (addThread != null) {
                addThread.interrupt();

                try {
                    addThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();

                    // InterruptedException clears the thread's interrupt status. Since we can't
                    // handle that here, we will re-interrupt the thread to set the interrupt
                    // status back to true.
                    Thread.currentThread().interrupt();
                }

                addThread = null;
            }
        }

        @Override
        protected void onStop() {
            super.onStop();

            Dialog.closeDialogs();
            SpinnerDialog.closeDialogs(this);
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();

            if (managerBinder != null) {
                joinAddThread();
                unbindService(serviceConnection);
            }
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            UiHelper.setLocale(this);

            setContentView(R.layout.activity_add_computer_manually_neon);

            UiHelper.notifyNewRootView(this);

            this.hostText = findViewById(R.id.hostTextView);
            hostText.setImeOptions(EditorInfo.IME_ACTION_DONE);
            hostText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    if (actionId == EditorInfo.IME_ACTION_DONE ||
                            (keyEvent != null &&
                                    keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                                    keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                        getWindow().getDecorView().setSystemUiVisibility(MainActivity.uiFlag);
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        return handleDoneEvent();
                    }
                    else if (actionId == EditorInfo.IME_ACTION_PREVIOUS) {
                        // This is how the Fire TV dismisses the keyboard
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(hostText.getWindowToken(), 0);

                        getWindow().getDecorView().setSystemUiVisibility(MainActivity.uiFlag);
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        return false;
                    }


                    getWindow().getDecorView().setSystemUiVisibility(MainActivity.uiFlag);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    return false;
                }
            });

            findViewById(R.id.addPcButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handleDoneEvent();
                }
            });

            // Bind to the ComputerManager service
            bindService(new Intent(ManualComputerJoin_Neon.this,
                    ComputerManagerService.class), serviceConnection, Service.BIND_AUTO_CREATE);
        }

        // Returns true if the event should be eaten
        private boolean handleDoneEvent() {
            String hostAddress = hostText.getText().toString().trim();

            if (hostAddress.length() == 0) {
                Toast.makeText(ManualComputerJoin_Neon.this, getResources().getString(R.string.addpc_enter_ip), Toast.LENGTH_LONG).show();
                return true;
            }

            computersToAdd.add(hostAddress);
            return false;
        }

    }
