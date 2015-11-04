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

import jaron.simpleserialization.SerializationData;
import jaron.simpleserialization.SerializationInputStream;
import jaron.simpleserialization.SerializationOutputStream;
import jaron.simpleserialization.SerializationTypes;

/**
 * Created by Muhammad Resna Rizki on 14/10/2015.
 */
public class BoatData extends SerializationData {
    public static float lat;
    public static float lng;

    public static float bearing;

    //public static int processed;

    public static boolean run;
    public static boolean completed;
    public static boolean gps;

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
            lat = latlng.get(0).getAsFloat();
            lng = latlng.get(1).getAsFloat();

            // Get Bearing
            bearing = jsonObject.getAsJsonPrimitive("bea").getAsFloat();

            run = (jsonObject.getAsJsonPrimitive("run").getAsInt() == 1);
            completed = (jsonObject.getAsJsonPrimitive("cmp").getAsInt() == 1);
            gps = (jsonObject.getAsJsonPrimitive("gps").getAsInt() == 1);

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

    @Override
    public void readData(SerializationInputStream input) {
        lat = input.readFloat();
        lng = input.readFloat();
        bearing = input.readFloat();
        //processed = input.readInteger();
        run = input.readBoolean();
        completed = input.readBoolean();
        gps = input.readBoolean();
        last_id_command = input.readInteger();
        left_motor = input.readInteger();
        right_motor = input.readInteger();
    }

    @Override
    public void writeData(SerializationOutputStream output) {
        output.writeFloat(lat);
        output.writeFloat(lng);
        output.writeFloat(bearing);
        //output.writeInteger(processed);
        output.writeBoolean(run);
        output.writeBoolean(completed);
        output.writeBoolean(gps);
        output.writeInteger(last_id_command);
        output.writeInteger(left_motor);
        output.writeInteger(right_motor);
    }

    @Override
    public int getDataSize() {
        int dataSize = SerializationTypes.SIZEOF_FLOAT; // lat
        dataSize += SerializationTypes.SIZEOF_FLOAT;    // lng
        dataSize += SerializationTypes.SIZEOF_FLOAT;    // bea
        //dataSize += SerializationTypes.SIZEOF_INTEGER;    // pro
        dataSize += SerializationTypes.SIZEOF_BOOLEAN;    // run
        dataSize += SerializationTypes.SIZEOF_BOOLEAN;    // completed
        dataSize += SerializationTypes.SIZEOF_BOOLEAN;    // gps
        dataSize += SerializationTypes.SIZEOF_INTEGER;    // idc
        dataSize += SerializationTypes.SIZEOF_INTEGER;    // lmt
        dataSize += SerializationTypes.SIZEOF_INTEGER;    // rmt
        return (dataSize);
    }
}

