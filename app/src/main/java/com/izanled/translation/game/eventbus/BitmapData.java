package com.izanled.translation.game.eventbus;

import android.graphics.Bitmap;

public class BitmapData {

    private Bitmap bitmap;

    public BitmapData(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
