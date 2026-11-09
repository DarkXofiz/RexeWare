package com.rexeware.gui;

import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

/** Bir modulun ayari. Sag tikla acilan panelde gorunur. */
public abstract class Setting {
    public final String name;
    public int rx, ry, rw, rh;

    protected Setting(String name) {
        this.name = name;
    }

    public abstract int height();

    public static final class Bool extends Setting {
        public final BooleanSupplier getter;
        public final Consumer<Boolean> setter;
        public float anim = -1f;

        public Bool(String name, BooleanSupplier getter, Consumer<Boolean> setter) {
            super(name);
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public int height() {
            return 18;
        }

        public void toggle() {
            setter.accept(!getter.getAsBoolean());
        }
    }

    public static final class Slider extends Setting {
        public final double min;
        public final double max;
        public final double step;
        public final int decimals;
        public final DoubleSupplier getter;
        public final DoubleConsumer setter;

        public Slider(String name, double min, double max, double step, int decimals,
                      DoubleSupplier getter, DoubleConsumer setter) {
            super(name);
            this.min = min;
            this.max = max;
            this.step = step;
            this.decimals = decimals;
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public int height() {
            return 24;
        }

        public double fraction() {
            double f = (getter.getAsDouble() - min) / (max - min);
            return Math.max(0.0, Math.min(1.0, f));
        }

        public void setFromMouse(double mouseX) {
            double t = Math.max(0.0, Math.min(1.0, (mouseX - rx) / (double) rw));
            double raw = min + t * (max - min);
            double snapped = Math.round((raw - min) / step) * step + min;
            snapped = Math.max(min, Math.min(max, snapped));
            double scale = Math.pow(10, decimals);
            setter.accept(Math.round(snapped * scale) / scale);
        }

        public String display() {
            return String.format(Locale.ROOT, "%." + decimals + "f", getter.getAsDouble());
        }
    }
}
