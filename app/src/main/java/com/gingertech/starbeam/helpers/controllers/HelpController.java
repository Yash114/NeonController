package com.gingertech.starbeam.helpers.controllers;

import static com.gingertech.starbeam.MainActivity.mFirebaseAnalytics;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gingertech.starbeam.MainActivity;
import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.UserData;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.HashMap;
import java.util.Objects;


public class HelpController extends RelativeLayout {

    class HelpPage {
        String helpPageText = "";
        int pointerRef = 0;
        int triggerRef = 0;
        int triggerRefAlt = 0;

        String link = "";
        int linkID = 0;

        String linkName = "";
        boolean trigger = false;

        HelpPage(String helpPageText, int pointerRef) {
            this.helpPageText = helpPageText;
            this.pointerRef = pointerRef;
        }

        HelpPage(String helpPageText, int pointerRef, boolean waitForClick) {
            this.helpPageText = helpPageText;
            this.pointerRef = pointerRef;
            this.trigger = waitForClick;
        }

        HelpPage(String helpPageText, int pointerRef, String linkName, String link) {
            this.helpPageText = helpPageText;
            this.pointerRef = pointerRef;
            this.link = link;
            this.linkName = linkName;
        }

        HelpPage(String helpPageText, int pointerRef, String linkName, int linkID) {
            this.helpPageText = helpPageText;
            this.pointerRef = pointerRef;
            this.linkID = linkID;
            this.linkName = linkName;
        }
    }

    private int pageIndex = -1;
    private int helpPage = -1;
    private final HashMap<Integer, HelpPage[]> helpAssignments = new HashMap<>();
    private TextView textView;
    private TextView nextButton;
    private TextView finishButton;

    private ImageView pointer;
    private TextView linkButton;
    private View container;

    private MixpanelAPI mp;

    class HelpPageIdentifier {
        int fragmentID = 0;
        boolean initialHelp = false;

        public HelpPageIdentifier(int fragmentID, boolean initialHelp) {
            this.fragmentID = fragmentID;
            this.initialHelp = initialHelp;
        }

        public HelpPageIdentifier(int fragmentID) {
            this.fragmentID = fragmentID;
        }
    }

    public HelpController(@NonNull Context context) {
        super(context);
        init(context);
    }

    public HelpController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HelpController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public HelpController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    ViewGroup root;
    public void init(Context c){


        LayoutInflater inflater = (LayoutInflater) c
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = (ViewGroup) inflater.inflate(R.layout.help_layout, this, true);

        heightMultiplier = root.getHeight() / 1440f;

        container = root.findViewById(R.id.container);
        textView = root.findViewById(R.id.textContent);
        nextButton = root.findViewById(R.id.nextButton);
        finishButton = root.findViewById(R.id.finishButton);

        pointer = root.findViewById(R.id.pointer);
        linkButton = root.findViewById(R.id.linkText);

        setupHelp();

        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if(helpAssignments.get(helpPage) != null) {
                    if (helpAssignments.get(helpPage).length - 2 == pageIndex) {
                        nextButton.setText(R.string.done);
                    } else {
                        nextButton.setText(R.string.next);
                    }
                }

                MixPanel.mpButtonTracking(mp, "forward_help");

                nextPage();
            }
        });

        finishButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                endHelp();
            }
        });


        linkButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                MixPanel.mpButtonTracking(mp, "help_link");

                if(helpAssignments.get(helpPage) != null) {

                    Bundle b = new Bundle();
                    b.putString("Link_Opened", helpAssignments.get(helpPage)[pageIndex].link);
                    mFirebaseAnalytics.logEvent("Help_Page_Navigated", b);

                    String link = helpAssignments.get(helpPage)[pageIndex].link;

                    if(link != null) {
                        try {
                            c.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
                        }
                        catch (ActivityNotFoundException e) {
                            Toast.makeText(getContext(), "Error Launching Discord", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    public void setupHelp() {

        helpAssignments.put(UserData.REMAP,
                new HelpPage[] {
                        new HelpPage(getContext().getString(R.string.Welcome_to_the_Remap_Page), 0),
                        new HelpPage(getContext().getString(R.string.redefined_controller_buttons), 0),
                        new HelpPage(getContext().getString(R.string.Tap_Save_once_you_are_done_), R.id.saveRemap),


                });

        helpAssignments.put(UserData.COMMANDS_LIST,
                new HelpPage[] {
                        new HelpPage(getContext().getString(R.string.Welcome_to_the_Command_List_Page), 0),
                        new HelpPage(getContext().getString(R.string.Click_On_The_New_Command), R.id.addCommandButton),
                        new HelpPage(getContext().getString(R.string.For_a_tutorial_please_watch), 0, getResources().getString(R.string.tutorial_video), "https://youtu.be/cVVC46h4cgE")
                });

        helpAssignments.put(UserData.COMMANDS_EDITOR,
                new HelpPage[] {
                        new HelpPage(getContext().getString(R.string.Welcome_to_the_Command_Editor), 0),
                        new HelpPage(getContext().getString(R.string.On_the_left_is_a_list_of_all), R.id.commandListScroll),
                        new HelpPage(getContext().getString(R.string.On_the_right_is_where_get), R.id.infoSegment),
                        new HelpPage(getContext().getString(R.string.Drag_commands_from_the), R.id.codeEditor),
                        new HelpPage(getContext().getString(R.string.To_use_the_command), 0),
                        new HelpPage(getContext().getString(R.string.For_a_tutorial_please_watch), 0, getResources().getString(R.string.tutorial_video), "https://youtu.be/cVVC46h4cgE")


                });

        helpAssignments.put(UserData.LAYOUTS_LIST_firsttime,
                new HelpPage[] {
                        new HelpPage(getContext().getString(R.string.Welcome_to_NEO_Stream), 0),
                        new HelpPage(getContext().getString(R.string.This_is_the_Controller), 0),
                        new HelpPage(getContext().getString(R.string.Click_on_PLAY_to_begin), R.id.playButton),
                });


        helpAssignments.put(UserData.PREMIUM,
                new HelpPage[] {
                        new HelpPage(getContext().getString(R.string.purchase_premium_features_here), 0),
                });

        helpAssignments.put(UserData.LAYOUTS_LIST,
                new HelpPage[] {
                        new HelpPage(getResources().getString(R.string.This_is_the_Layouts_Page),0),
                        new HelpPage(getContext().getString(R.string.this_is_the_layout_list_page_this_is_where_you_can_see_all_your_game_controllers), R.id.mainTitleText),
                        new HelpPage(getContext().getString(R.string.to_create_a_new_custom_layout_click_on_the_new_button), R.id.newButtonTag),
                        new HelpPage(getContext().getString(R.string.to_delete_a_layout_click_on_the_delete_button), R.id.deleteButton),
                        new HelpPage(getContext().getString(R.string.to_customize_a_layout_click_on_the_edit_button), R.id.editButton),
                        new HelpPage(getResources().getString(R.string.To_copy_a_layout_click), R.id.descriptionText),
                        new HelpPage(getContext().getString(R.string.after_selecting_the_layout_you_want_click_on_the_rocket_to_use_it_with_your_game), R.id.playButton)
                });


        helpAssignments.put(UserData.LAYOUTS_CREATE,
                new HelpPage[] {
                        new HelpPage(getResources().getString(R.string.This_is_the_Create_Page), 0),
                        new HelpPage(getContext().getString(R.string.to_learn_more_about_this_page_watch_this_video_tutorial), 0, getContext().getString(R.string.tutorial_video), "https://youtu.be/166qRqzTuIE"),
                });

        helpAssignments.put(UserData.LAYOUTS_CREATE_firsttime,
                new HelpPage[] {
                        new HelpPage(getResources().getString(R.string.This_is_the_Create_Page), 0),
                        new HelpPage(getResources().getString(R.string.When_you_are_ready), R.id.launch_button),
                        new HelpPage(getResources().getString(R.string.If_you_have_any_questions), 0, "Discord Server", "https://discord.gg/VbjxkKhTqh"),
                });

        helpAssignments.put(UserData.LAUNCH_COMPUTER_LIST,
                new HelpPage[] {
                        new HelpPage(getResources().getString(R.string.Welcome_to_WiFi_play),0),
                        new HelpPage(getResources().getString(R.string.Your_mobile_device_and_computer),0),
                        new HelpPage(getResources().getString(R.string.But_after_connecting_you),0),
                        new HelpPage(getResources().getString(R.string.If_you_are_having_trouble),0),
                        new HelpPage(getContext().getString(R.string.if_you_do_not_see_any_computer_simply_follow_this_tutorial_to_get_started), 0, getContext().getString(R.string.tutorial_video), "https://www.youtube.com/watch?v=zGOPhtT74WE"),
                });

        helpAssignments.put(UserData.LAUNCH_BLUETOOTH_PLAY,
                new HelpPage[] {
                        new HelpPage("Welcome to Bluetooth play!",0),
                        new HelpPage("After connecting you can control your PC games with an onscreen controller!",0),
                        new HelpPage("If you cannot connect, try removing your mobile device from your computer bluetooth list then retrying",0),


                });

        helpAssignments.put(UserData.LAUNCH_GAME_SELECT,
                new HelpPage[] {
                        new HelpPage(getContext().getString(R.string.this_is_the_game_list_page_this_is_where_you_can_see_all_the_games_you_can_launch), 0),
                        new HelpPage(getContext().getString(R.string.if_you_do_not_see_any_games_simply_add_your_desired_game_in_the_applications_tab_of_sunshine), 0),
                        new HelpPage(getContext().getString(R.string.if_you_see_a_game_click_on_it_to_start_playing), 0)
                });

        helpAssignments.put(UserData.LAUNCH_GAME_PLAY,
                new HelpPage[] {
                        new HelpPage(getContext().getString(R.string.this_is_the_launch_page_this_is_where_you_actually_play_your_games), 0),
                        new HelpPage(getContext().getString(R.string.the_layout_you_selected_within_the_layouts_list_page_will_appear_above_your_game), 0),
                        new HelpPage(getContext().getString(R.string.clicking_on_the_buttons_will_activate_whatever_keybinds_they_have_been_given), 0),
                        new HelpPage(getContext().getString(R.string.swiping_on_the_screen_will_move_the_mouse_cursor_like_a_trackpad), 0),
                        new HelpPage(getContext().getString(R.string.tapping_will_left_click_the_mouse), 0),
                        new HelpPage(getContext().getString(R.string.swiping_the_screen_with_2_fingers_will_scroll_the_screen), 0),
                        new HelpPage(getContext().getString(R.string.tapping_the_screen_with_3_fingers_will_show_the_keyboard), 0),
                        new HelpPage(getContext().getString(R.string.if_you_have_an_xbox_controller_simply_connect_to_it_and_neon_will_automatically_connect), 0),
                        new HelpPage(getContext().getString(R.string.if_the_screen_is_blank_you_may_have_to_manually_open_the_desired_application_on_your_computer), 0),
                        new HelpPage(getContext().getString(R.string.exiting_the_selected_game_will_end_the_connection), 0),
                        new HelpPage(getContext().getString(R.string.have_fun), 0),


                });

        helpAssignments.put(UserData.LAUNCH_GAME_PLAY_FirstTime,
                new HelpPage[] {
                        new HelpPage(getContext().getString(R.string.Welcome_to_NEON), 0),
                        new HelpPage(getContext().getString(R.string.Click_on_Desktop), 0),
                        new HelpPage(getContext().getString(R.string.have_fun), 0),

                });
    }

    ObjectAnimator a1;
    ObjectAnimator a2;
    ObjectAnimator a3;
    ObjectAnimator a4;

    public void setMixPanel(MixpanelAPI mp) {
        this.mp = mp;
    }

    float heightMultiplier = 0;

    private void nextPage() {

        pageIndex++;

        if(helpAssignments.get(helpPage) == null) { return; }

        if(pageIndex >= helpAssignments.get(helpPage).length) {
            endHelp_cont();
            return;
        }

        HashMap<String, String> addData = new HashMap<>();
        addData.put("help_text", helpAssignments.get(helpPage)[pageIndex].helpPageText);

        boolean hasLink = !Objects.equals(helpAssignments.get(helpPage)[pageIndex].link, "");
        addData.put("help_has_link", hasLink ? "yes" : "no");

        if(hasLink) {
            addData.put("help_link", helpAssignments.get(helpPage)[pageIndex].link);
        }

        MixPanel.mpEventTracking(mp, "help_page_forwarded", addData);

        textView.setText(helpAssignments.get(helpPage)[pageIndex].helpPageText);

        int pointerRef = helpAssignments.get(helpPage)[pageIndex].pointerRef;
        if(pointerRef != 0) {
            pointer.setVisibility(VISIBLE);

            View target = ((View) root.getParent()).findViewById(pointerRef);
            if (target != null) {

                int[] locationOnScreen = new int[2];
                target.getLocationInWindow(locationOnScreen);

                locationOnScreen[0] += target.getWidth() / 2 - pointer.getWidth() / 2;
                locationOnScreen[1] += target.getHeight() / 2 - pointer.getHeight() / 3;

                a1 = ObjectAnimator.ofFloat(pointer, "translationX", locationOnScreen[0]);

                a1.setDuration(300);
                a1.setInterpolator(new AccelerateDecelerateInterpolator());
                a1.start();

                a2 = ObjectAnimator.ofFloat(pointer, "translationY", locationOnScreen[1]);

                a2.setInterpolator(new AccelerateDecelerateInterpolator());
                a2.setDuration(250);
                a2.start();
            }

        } else {
            pointer.setVisibility(GONE);

            pointer.setX(0);
            pointer.setY(0);
        }

        if(!Objects.equals(helpAssignments.get(helpPage)[pageIndex].link, "")) {
            linkButton.setVisibility(VISIBLE);

            linkButton.setText(helpAssignments.get(helpPage)[pageIndex].linkName);
        } else {
            linkButton.setVisibility(GONE);
        }

        if(helpAssignments.get(helpPage)[pageIndex].trigger) {
            nextButton.setVisibility(GONE);

            if(helpAssignments.get(helpPage)[pageIndex].triggerRef != 0 ){
                pointerRef = helpAssignments.get(helpPage)[pageIndex].triggerRef;
            }

            View triggerObj = ((View) root.getParent()).findViewById(pointerRef);
            if (triggerObj != null) {

                triggerObj.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {

                        if(MotionEvent.ACTION_DOWN == motionEvent.getAction()) {
                            MixPanel.mpButtonTracking(mp, "help_action");


                            nextPage();
                            triggerObj.setOnTouchListener(null);
                        }

                        return false;
                    }
                });

                int triggerRefAlt = helpAssignments.get(helpPage)[pageIndex].triggerRefAlt;
                if(triggerRefAlt != 0) {
                    final View altView = findViewById(triggerRefAlt);

                    if(altView != null) {
                        altView.setOnTouchListener(new OnTouchListener() {
                            @Override
                            public boolean onTouch(View view, MotionEvent motionEvent) {

                                if (MotionEvent.ACTION_DOWN == motionEvent.getAction()) {
                                    nextPage();
                                    altView.setOnTouchListener(null);
                                }

                                return false;
                            }
                        });
                    }
                }
            }
        } else {
            nextButton.setVisibility(VISIBLE);
        }

        Bundle b = new Bundle();
        b.putString("Help_Index", String.valueOf(pageIndex));
        mFirebaseAnalytics.logEvent("Help_Page_Navigated", b);
    }

    public void beginHelp(int page) {

        endHelp_cont();
        endClickObj();

        pageIndex = -1;
        helpPage = page;

        container.setVisibility(VISIBLE);

        nextPage();

        Bundle b = new Bundle();
        b.putString("Fragment", String.valueOf(page));
        mFirebaseAnalytics.logEvent("Help_Page_Opened", b);
    }

    public void endClickObj() {
        if(a1 != null && a2 != null && a3 != null && a4 != null) {
            a1.end();
            a2.end();
            a3.end();
            a4.end();

            pointer.setVisibility(GONE);
        }
    }

    public void showClickObj(int rocketView) {
        View target = ((View) root.getParent()).findViewById(rocketView);

        if (target != null) {
            pointer.setVisibility(VISIBLE);

            int[] locationOnScreen = new int[2];
            target.getLocationOnScreen(locationOnScreen);

            locationOnScreen[0] += target.getWidth() / 2 - pointer.getWidth() / 2;
            locationOnScreen[1] += target.getHeight() / 2 - pointer.getHeight() / 3;

            a1 = ObjectAnimator.ofFloat(pointer, "translationX", locationOnScreen[0]);

            a1.setDuration(300);
            a1.setInterpolator(new AccelerateDecelerateInterpolator());
            a1.start();

            a2 = ObjectAnimator.ofFloat(pointer, "translationY", locationOnScreen[1]);

            a2.setInterpolator(new AccelerateDecelerateInterpolator());
            a2.setDuration(250);
            a2.start();

            a3 = ObjectAnimator.ofFloat(pointer, "translationX", locationOnScreen[0]);

            a3.setInterpolator(new AccelerateDecelerateInterpolator());
            a3.setRepeatCount(ValueAnimator.INFINITE);
            a3.setRepeatMode(ValueAnimator.REVERSE);
            a3.setStartDelay(250);
            a3.setDuration(2000);

            a3.start();

            a4 = ObjectAnimator.ofFloat(pointer, "translationY", locationOnScreen[1]);

            a4.setInterpolator(new AccelerateDecelerateInterpolator());
            a4.setRepeatCount(ValueAnimator.INFINITE);
            a4.setRepeatMode(ValueAnimator.REVERSE);
            a4.setStartDelay(200);
            a4.setDuration(2000);

            a4.start();
        } else {
            pointer.setVisibility(GONE);
        }


    }

    public void endHelp_cont() {

        container.setVisibility(GONE);
        pointer.setVisibility(GONE);

        pageIndex = -1;
        helpPage = -1;

        if(getContext() != null) {
            nextButton.setText(getContext().getText(R.string.next));
        }

    }

    public void endHelp() {

        container.setVisibility(GONE);
        pointer.setVisibility(GONE);

        pageIndex = -1;
        helpPage = -1;

        if(getContext() != null) {
            nextButton.setText(getContext().getText(R.string.next));
        }

        MainActivity.activeTutorial = false;

        MixPanel.mpEventTracking(mp, "help_end", null);


    }

}
