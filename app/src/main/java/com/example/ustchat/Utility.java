package com.example.ustchat;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.InputStream;
import java.net.URL;
import java.util.Random;

public class Utility {

    public static boolean enableNotification = true;

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

    public static String generateIntegerWithLeadingZeros(int maxBound, int numDigits) {
        Random rand = new Random();
        int randint = rand.nextInt(maxBound);
        return String.format("%0" + numDigits + "d", randint);
    }

    public static boolean isNightModeOn(Activity activity) {
        return (activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

}
