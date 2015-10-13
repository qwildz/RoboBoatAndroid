package mapboat.roboboat_ysu.net.Utils;

import android.util.Log;

import mapboat.roboboat_ysu.net.roboboat.BuildConfig;

/**
 * Created by Wildz on 10/13/2015.
 */
public class LogHelper {

    public static void simpleLog(String tag, String message) {

        if (BuildConfig.DEBUG) {
            Log.d(tag, message);
        }

    }

}
