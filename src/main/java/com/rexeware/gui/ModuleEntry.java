package com.rexeware.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** ClickGUI'de gorunen bir modul satiri. */
public final class ModuleEntry {
    public final String name;
    public final BooleanSupplier getter;
    public final Consumer<Boolean> setter;
    public final List<Setting> settings = new ArrayList<>();

    public boolean expanded;
    public float toggleAnim;
    public float expandAnim;

    // Son cizimdeki konumlar (tiklama icin)
    public int rx, ry, rw, rh;
    public int sTop, sVisH;

    public ModuleEntry(String name, BooleanSupplier getter, Consumer<Boolean> setter) {
        this.name = name;
        this.getter = getter;
        this.setter = setter;
        this.toggleAnim = getter.getAsBoolean() ? 1f : 0f;
    }

    public ModuleEntry add(Setting setting) {
        settings.add(setting);
        return this;
    }
}
