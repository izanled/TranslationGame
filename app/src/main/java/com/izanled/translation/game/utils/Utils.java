package com.izanled.translation.game.utils;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Utils {
    public static boolean SaveBitmapToFilePathPNG(Bitmap bitmap, String strFilePath) {
        if (bitmap != null && strFilePath != null && strFilePath.length() >= 1) {
            File fileCacheItem = new File(strFilePath);
            FileOutputStream out = null;

            try {
                if (fileCacheItem != null) {
                    fileCacheItem.createNewFile();
                    out = new FileOutputStream(fileCacheItem);
                    if (out != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        return true;
                    }
                }

                return true;
            } catch (Exception var13) {
                var13.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException var12) {
                    var12.printStackTrace();
                    return false;
                }

            }

            return false;
        } else {
            return false;
        }
    }
}
