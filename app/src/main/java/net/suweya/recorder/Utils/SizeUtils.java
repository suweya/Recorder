package net.suweya.recorder.Utils;

import android.content.res.Resources;

/**
 * Created by suweya on 2015/5/27.
 */
public class SizeUtils {

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
