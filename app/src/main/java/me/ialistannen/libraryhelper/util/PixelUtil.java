package me.ialistannen.libraryhelper.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Utility functions to help with pixel units.
 */
public class PixelUtil {

  /**
   * @param context The context to use
   * @param dp The amount of dp to convert
   * @return The dp in pixels
   */
  public static int dpToPixels(Context context, int dp) {
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
  }

}
