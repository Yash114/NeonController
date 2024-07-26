package com.gingertech.starbeam.helpers;

public class mColor {

    public int red = 0;
    public int green = 0;
    public int blue = 0;
    public int alpha = 0;

    public static mColor WHITE = new mColor(255, 255, 255);
    public static mColor BLACK = new mColor(0, 0, 0);

    public static mColor RED = new mColor(255, 0, 0);
    public static mColor GREEN = new mColor(0, 255, 0);
    public static mColor BLUE = new mColor(0, 0, 255);

    public static mColor GRAY = new mColor(150, 150, 150);
    public static mColor DKGRAY = new mColor(40, 40, 40);

    public mColor(){}
    public mColor(int red, int blue, int green) {
        this.red = red;
        this.blue = blue;
        this.green = green;
        this.alpha = 255;
    }

    public mColor(int red, int blue, int green, int alpha) {
        this.red = red;
        this.blue = blue;
        this.green = green;
        this.alpha = alpha;
    }

    public void updateColor(int red, int blue, int green, int alpha) {
        this.red = red;
        this.blue = blue;
        this.green = green;
        this.alpha = alpha;
    }

    public void updateColor(int red, int blue, int green) {
        this.red = red;
        this.blue = blue;
        this.green = green;
        this.alpha = 255;
    }

    public int getColor() {
        return this.alpha << 24 + this.red << 16 + this.green << 8 + this.blue;
    }

    public mColor(int argb) {
        this.alpha = (argb >> 24) & 255;
        this.red = argb >> 16;
        this.green = (argb >> 8) & 255;
        this.blue = argb & 255;
    }
}
