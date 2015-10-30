package mapboat.roboboat_ysu.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;

/**
 * Created by Muhammad Resna Rizki on 14/10/2015.
 */
public class CommandData {
    public static double tlat;
    public static double tlng;

    public static boolean run;
    public static boolean aut = true;

    public static int idc = 0;
    public static int max = 100;

    public static int lmt = 0;
    public static int rmt = 0;

    public static String parseBoatData() {
        Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).create();
        return gson.toJson(new CommandData());
    }
}

