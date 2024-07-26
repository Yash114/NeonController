package com.gingertech.starbeam.helpers;

import android.content.Context;
import android.util.ArraySet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class LayoutGroupClass extends LayoutClass {

    //Already has a name and description in super class
    public List<LayoutClass> layoutClasses = new ArrayList<>();

    public LayoutGroupClass(String name, String description) {

        this.name = name;
        this.description = description;
        this.groupID = UUID.randomUUID().toString();
    }

    public LayoutGroupClass(String name, String description, ArraySet<LayoutClass> layoutClasses) {

        this.name = name;
        this.description = description;
        this.groupID = UUID.randomUUID().toString();

        this.layoutClasses.addAll(layoutClasses);
    }

    LayoutGroupClass(String name, String description, Set<String> layoutClassNames, String uuid) {

        this.name = name;
        this.description = description;
        this.groupID = uuid;

        for(String layoutName : layoutClassNames) {
            Optional<LayoutClass> l = UserData.Layouts.stream().filter(s -> Objects.equals(s.name, layoutName)).findFirst();
            l.ifPresent(layoutClass -> this.layoutClasses.add(layoutClass));
        }
    }

    public void save(Context c) {
        SaveClass.SaveLayoutGroup(c, this);
    }
}
