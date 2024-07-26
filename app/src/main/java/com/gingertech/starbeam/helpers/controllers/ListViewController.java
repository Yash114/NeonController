package com.gingertech.starbeam.helpers.controllers;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Layout;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.gingertech.starbeam.MainActivity;
import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.LayoutClass;
import com.gingertech.starbeam.helpers.LayoutGroupClass;
import com.gingertech.starbeam.helpers.UserData;

import java.util.ArrayList;
import java.util.Objects;

public class ListViewController extends HorizontalScrollView {

    final static int DELETE = 0;
    final static int EDIT = 1;
    final static int LAUNCH = 2;
    final static int DUPLICATE = 3;


    ViewGroup root;
    LinearLayout container;

    public int itemHeight = 0;

    ArrayList<LayoutClass> layouts = new ArrayList<>();
    ArrayList<LayoutGroupClass> groups = new ArrayList<>();


    Object selected;

    boolean editable = false;
    TypedArray typedArrayAttributes;


    public ListViewController(Context context) {
        super(context);
        init(context);
    }

    public ListViewController(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        typedArrayAttributes = context.obtainStyledAttributes(attrs, R.styleable.ListViewController, 0, 0);
        editable = typedArrayAttributes.getBoolean(R.styleable.ListViewController_editable, true);



        init(context);
    }

    public ListViewController(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        typedArrayAttributes = context.obtainStyledAttributes(attrs, R.styleable.ListViewController, 0, 0);
        editable = typedArrayAttributes.getBoolean(R.styleable.ListViewController_editable, true);

        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = (ViewGroup) inflater.inflate(R.layout.list_view, this, true);

        container = root.findViewById(R.id.scroll_container);

    }

    public void setSelected(LayoutClass layout) {
        if (layout == null) { return; }

        selected = layout;
    }

    public void setData() {

        int layoutCount = 0;
        this.layouts = UserData.Layouts;
        this.groups = UserData.LayoutGroups;

        int selectedIndex = 1;
        LayoutPreviewTabController selectedView = null;
        ArrayList<LayoutPreviewTabController> lockedLayouts = new ArrayList<>();
        int index = 0;

        if(this.layouts == null || this.groups == null) { return; }

        if(root != null) {
            container.removeAllViews();

            if(editable) {
                LayoutPreviewTabController newLayoutPreviewTabController = new LayoutPreviewTabController(root.getContext());
                newLayoutPreviewTabController.setClickedCallback(new GenericCallback().setOnGenericCallback(new OnGenericCallback() {
                    @Override
                    public void onChange(Object value) {
                        if (newCallback != null) {
                            newCallback.fire(null);
                        }
                    }
                }));

                container.addView(newLayoutPreviewTabController);
            }

            for (LayoutClass layout : layouts) {

                boolean lockedView = false;

                if(!UserData.isPremiumMode) {
                    if(!Objects.equals(layout.name, "Keyboard") && !layout.name.equals("Xbox")) {
                        layoutCount += 1;
                    }

                    if(layoutCount > MainActivity.maxLayouts && !Objects.equals(layout.name, "Keyboard") && !layout.name.equals("Xbox")) {
                        lockedView = true;
                    }
                }

                LayoutPreviewTabController nListItemViewController = new LayoutPreviewTabController(root.getContext(), lockedView);

                if(layout.isLayoutInGroup) { continue; }

                nListItemViewController.setEditable(editable);
                nListItemViewController.setup(layout, getContext());
                container.addView(nListItemViewController);
                if(lockedView) {
                    lockedLayouts.add(nListItemViewController);
                }

                if(selected == layout) {
                    nListItemViewController.setClick(true);
                    selectedIndex = index;
                    selectedView = nListItemViewController;
                }

                nListItemViewController.setClickedCallback(new GenericCallback().setOnGenericCallback(new OnGenericCallback() {
                    @Override
                    public void onChange(Object value) {

                        if(value == null) {
                            listItemSelectListener.fire(null);
                            return;
                        }

                        if(value instanceof Integer) {

                            int buttonAction = (int) value;

                            if(buttonAction == ListViewController.DELETE) {

                                if(deleteCallback != null) {
                                    deleteCallback.fire(null);
                                }
                            } else if(buttonAction == ListViewController.EDIT) {

                                if(editCallback != null) {
                                    editCallback.fire(null);
                                }

                            } else if(buttonAction == ListViewController.LAUNCH) {
                                if(onLaunchListener != null) {
                                    onLaunchListener.fire(null);
                                }
                            } else if(buttonAction == ListViewController.DUPLICATE) {
                                if(onLaunchListener != null) {
                                    duplicateCallback.fire(null);
                                }
                            }
                            return;
                        }

                        //add to a group
                        if(value instanceof Boolean) {
                            if(addToGroupCallback != null) {
                                addToGroupCallback.fire(nListItemViewController.layout);
                            }

                            return;
                        }

                        if(value != selected) {
                            UserData.currentGroupID = "";

                            listItemSelectListener.fire(value);
                            selected = value;

                            nListItemViewController.setClick(true);

                            //set not clicked for all child views besides the last one
                            //because it is the add button
                            int i = container.getChildCount();
                            for (int k = 0; k < i; k++) {
                                View child = container.getChildAt(k);
                                if (child != nListItemViewController) {
                                    if(child instanceof LayoutPreviewTabController) {
                                        LayoutPreviewTabController c = ((LayoutPreviewTabController) child);
                                        c.setClick(false);
                                        c.showAddToGroup(false);

                                    } else if(child instanceof LayoutGroupView) {
                                        ((LayoutGroupView) child).setClick(false);

                                    }
                                } else {
                                    ((LayoutPreviewTabController) child).showAddToGroup(false);
                                }

                            }
                        }
                    }
                }));


                if(itemHeight == 0) {
                    itemHeight = nListItemViewController.getHeight();
                }

                index++;
            }

            for (LayoutGroupClass group : groups) {


                boolean lockedView = !UserData.isPremiumMode;

                LayoutGroupView nListItemViewController = new LayoutGroupView(root.getContext());

                if(Objects.equals(((LayoutClass) selected).groupID, group.groupID)) {
                    nListItemViewController.setClick(true);
                }

                container.addView(nListItemViewController);
                nListItemViewController.setup(group, getContext());

                if(itemHeight == 0) {
                    itemHeight = nListItemViewController.getHeight();
                }

                nListItemViewController.setClickedCallback(new GenericCallback().setOnGenericCallback(new OnGenericCallback() {
                    @Override
                    public void onChange(Object value) {

                        UserData.currentGroupID = group.groupID;

                        if(value instanceof Integer) {

                            int v = (Integer) value;

                            //Remove From Group
                            if(v == -1) {
                                UserData.currentLayout.removeFromGroup(getContext());
                                setData();

                                int i = container.getChildCount();
                                for (int k = 0; k < i; k++) {
                                    View child = container.getChildAt(k);
                                    if (child != nListItemViewController) {
                                        if (child instanceof LayoutPreviewTabController) {

                                            LayoutPreviewTabController c = (LayoutPreviewTabController) child;

                                        }
                                    }

                                }

                                return;
                            }
                        }

                        if(value != selected) {

                            if(nListItemViewController.groupClass.layoutClasses.isEmpty()) {

                                int i = container.getChildCount();
                                for (int k = 0; k < i; k++) {
                                    View child = container.getChildAt(k);
                                    if (child != nListItemViewController) {
                                        if (child instanceof LayoutPreviewTabController) {

                                            LayoutPreviewTabController c = ((LayoutPreviewTabController) child);
                                            c.showAddToGroup(true);

                                        } else if (child instanceof LayoutGroupView) {
                                            ((LayoutGroupView) child).setClick(false);

                                        }
                                    }

                                }

                            } else {

                                listItemSelectListener.fire(value);
                                selected = value;

                                nListItemViewController.setClick(true);

                                //set not clicked for all child views besides the last one
                                //because it is the add button
                                int i = container.getChildCount();
                                for (int k = 0; k < i; k++) {
                                    View child = container.getChildAt(k);
                                    if (child != nListItemViewController) {
                                        if (child instanceof LayoutPreviewTabController) {

                                            LayoutPreviewTabController c = ((LayoutPreviewTabController) child);
                                            c.setClick(false);
                                            c.showAddToGroup(true);

                                        } else if (child instanceof LayoutGroupView) {
                                            ((LayoutGroupView) child).setClick(false);

                                        }
                                    }

                                }
                            }
                        }
                    }

                }));

                index++;
            }

            if(selectedView != null) {
                container.removeView(selectedView);
                container.addView(selectedView, 1);
            }

            if(!lockedLayouts.isEmpty()) {
                for(LayoutPreviewTabController l : lockedLayouts) {
                    container.removeView(l);
                    container.addView(l);
                }
            }
        }
    }

    public LayoutClass selectFirst() {
        LayoutPreviewTabController layout = (LayoutPreviewTabController) container.getChildAt(1);

        if(layout != null) {
            layout.toggleClick();
            return layout.layout;
        }

        return null;
    }

    GenericCallback listItemSelectListener;
    GenericCallback editCallback;
    GenericCallback deleteCallback;
    GenericCallback newCallback;
    GenericCallback onLaunchListener;
    GenericCallback duplicateCallback;
    GenericCallback addToGroupCallback;


    public void setOnGroupAddListener(GenericCallback addToGroupCallback) {
        this.addToGroupCallback = addToGroupCallback;
    }
    public void setOnListSelectedListener(GenericCallback listItemSelectListener) {
        this.listItemSelectListener = listItemSelectListener;
    }

    public void setOnLaunchListener(GenericCallback listItemSelectListener) {
        this.onLaunchListener = listItemSelectListener;
    }
    public void setEditCallback(GenericCallback clickedCallback) {
        this.editCallback = clickedCallback;
    }

    public void setDeleteCallback(GenericCallback clickedCallback) {
        this.deleteCallback = clickedCallback;
    }

    public void setDuplicateCallback(GenericCallback clickedCallback) {
        this.duplicateCallback = clickedCallback;
    }

    public void setNewCallback(GenericCallback clickedCallback) {
        this.newCallback = clickedCallback;
    }
}
