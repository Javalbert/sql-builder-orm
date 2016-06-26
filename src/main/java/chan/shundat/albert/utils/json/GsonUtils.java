package chan.shundat.albert.utils.json;

import java.io.IOException;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.util.ISO8601Utils;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public final class GsonUtils {
	public static Boolean getBoolean(JsonObject obj, String memberName) {
        JsonElement booleanElement = obj.get(memberName);
        return !isJsonElementNull(booleanElement)
                ? booleanElement.getAsBoolean()
                : null;
    }

	public static Date getDateFromLong(JsonObject obj, String memberName) {
        JsonElement dateElement = obj.get(memberName);
        return !isJsonElementNull(dateElement)
                ? new Date(dateElement.getAsLong())
                : null;
    }
	
    public static Double getDouble(JsonObject obj, String memberName) {
        JsonElement doubleElement = obj.get(memberName);
        return !isJsonElementNull(doubleElement)
                ? doubleElement.getAsDouble()
                : null;
    }
    
    public static Integer getInteger(JsonObject obj, String memberName) {
        JsonElement intElement = obj.get(memberName);
        return !isJsonElementNull(intElement)
                ? intElement.getAsInt()
                : null;
    }
	
    public static Date getIso8601Date(JsonObject obj, String memberName) {
        JsonElement dateElement = obj.get(memberName);
        if (isJsonElementNull(dateElement)) {
        	return null;
        }
        try {
			return ISO8601Utils.parse(dateElement.getAsString(), new ParsePosition(0));
		} catch (ParseException e) {}
		return null;
    }

	public static JsonArray getJsonArray(JsonObject obj, String memberName) {
		JsonElement arrayElement = obj.get(memberName);
		return !isJsonElementNull(arrayElement) 
				? arrayElement.getAsJsonArray()
				: null;
	}

    public static String getString(JsonObject obj, String memberName) {
        JsonElement stringElement = obj.get(memberName);
        return !isJsonElementNull(stringElement)
                ? stringElement.getAsString()
                : null;
    }
	
	public static Gson newGson() {
		return new GsonBuilder()
				.serializeNulls()
				.registerTypeAdapter(Date.class, new Iso8601DateAdapter())
				.create();
	}
	
	private static boolean isJsonElementNull(JsonElement element) {
        return element == null || element.isJsonNull();
    }
	
	public static class Iso8601DateAdapter extends TypeAdapter<Date> {
		@Override
		public void write(JsonWriter out, Date value) throws IOException {
			if (value == null) {
				out.nullValue();
				return;
			}
			
			out.value(ISO8601Utils.format(value, true, TimeZone.getDefault()));
		}

		@Override
		public Date read(JsonReader in) throws IOException {
			if (in.peek() == JsonToken.NULL) {
				in.nextNull();
				return null;
			}
			
			try {
				return ISO8601Utils.parse(in.nextString(), new ParsePosition(0));
			} catch (ParseException e) {}
			return null;
		}
	}
	
	private GsonUtils() {}
}