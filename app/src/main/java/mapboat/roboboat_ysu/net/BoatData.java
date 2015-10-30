package mapboat.roboboat_ysu.net;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringReader;

/**
 * Created by Muhammad Resna Rizki on 14/10/2015.
 */
public class BoatData {
    public static double lat;
    public static double lng;

    public static double bearing;

    public static boolean run;
    public static boolean completed;

    public static int last_id_command;

    public static int left_motor;
    public static int right_motor;

    public static void parseBoatData(String json) {
        if(! isJSONValid(json)) return;

        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);

        JsonElement jsonElement = new JsonParser().parse(reader);
        if(jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // Get LatLng
            JsonArray latlng = jsonObject.getAsJsonArray("pos");
            lat = latlng.get(0).getAsDouble();
            lng = latlng.get(1).getAsDouble();

            // Get Bearing
            bearing = jsonObject.getAsJsonPrimitive("bea").getAsDouble();

            run =  jsonObject.getAsJsonPrimitive("run").getAsBoolean();
            completed =  jsonObject.getAsJsonPrimitive("cmp").getAsBoolean();

            last_id_command = jsonObject.getAsJsonPrimitive("idc").getAsInt();

            if(jsonObject.has("lmt")) {
                left_motor = jsonObject.getAsJsonPrimitive("lmt").getAsInt();
            }

            if(jsonObject.has("rmt")) {
                right_motor = jsonObject.getAsJsonPrimitive("rmt").getAsInt();
            }
        }
    }

    private static boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }
}

