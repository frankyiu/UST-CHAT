package com.example.ustchat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.InputStream;
import java.net.URL;

public class Utility {

    // ref: https://stackoverflow.com/questions/7361976/how-to-create-a-drawable-from-a-stream-without-resizing-it
    public static Drawable loadImageFromUrl(String url) {
        try {
            InputStream inputStream = (InputStream) new URL(url).getContent();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            bitmap.setDensity(Bitmap.DENSITY_NONE);
            Drawable drawable = new BitmapDrawable(bitmap);
            inputStream.close();
            return drawable;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
