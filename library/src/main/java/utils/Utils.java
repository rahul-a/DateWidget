package utils;

import android.content.Context;
import android.os.Build;
import android.util.TypedValue;

import com.example.library.R;

/**
 * Created by priyabratapatnaik on 03/11/15.
 */
public class Utils {
    /**
     * Gets the colorAccent from the current context, if possible/available
     * @param context The context to use as reference for the color
     * @return the accent color of the current context
     */
    public static int getAccentColorFromThemeIfAvailable(Context context) {
        TypedValue typedValue = new TypedValue();
        // First, try the android:colorAccent
        if (Build.VERSION.SDK_INT >= 21) {
            context.getTheme().resolveAttribute(android.R.attr.colorAccent, typedValue, true);
            return typedValue.data;
        }
        // Next, try colorAccent from support lib
        int colorAccentResId = context.getResources().getIdentifier("colorAccent", "attr", context.getPackageName());
        if (colorAccentResId != 0 && context.getTheme().resolveAttribute(colorAccentResId, typedValue, true)) {
            return typedValue.data;
        }
        // Return the value in mdtp_accent_color
        return context.getColor(R.color.mdtp_accent_color);
    }
}
