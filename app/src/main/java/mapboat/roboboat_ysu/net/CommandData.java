package mapboat.roboboat_ysu.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;

import jaron.simpleserialization.SerializationData;
import jaron.simpleserialization.SerializationInputStream;
import jaron.simpleserialization.SerializationOutputStream;
import jaron.simpleserialization.SerializationTypes;

/**
 * Created by Muhammad Resna Rizki on 14/10/2015.
 */
public class CommandData extends SerializationData {
    public static float tlat;
    public static float tlng;

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

    @Override
    public void readData(SerializationInputStream input) {
        tlat = input.readFloat();
        tlng = input.readFloat();
        run = input.readBoolean();
        aut = input.readBoolean();
        idc = input.readInteger();
        max = input.readInteger();
        lmt = input.readInteger();
        rmt = input.readInteger();
    }

    @Override
    public void writeData(SerializationOutputStream output) {
        output.writeFloat(tlat);
        output.writeFloat(tlng);
        output.writeBoolean(run);
        output.writeBoolean(aut);
        output.writeInteger(idc);
        output.writeInteger(max);
        output.writeInteger(lmt);
        output.writeInteger(rmt);
    }

    @Override
    public int getDataSize() {
        int dataSize = SerializationTypes.SIZEOF_FLOAT; // tlat
        dataSize += SerializationTypes.SIZEOF_FLOAT;    // tlng
        dataSize += SerializationTypes.SIZEOF_BOOLEAN;    // run
        dataSize += SerializationTypes.SIZEOF_BOOLEAN;    // aut
        dataSize += SerializationTypes.SIZEOF_INTEGER;    // idc
        dataSize += SerializationTypes.SIZEOF_INTEGER;    // max
        dataSize += SerializationTypes.SIZEOF_INTEGER;    // lmt
        dataSize += SerializationTypes.SIZEOF_INTEGER;    // rmt
        return (dataSize);
    }
}

