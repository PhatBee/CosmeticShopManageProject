package vn.phatbee.cosmesticshopapp.retrofit;


import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
        if (value != null) {
            out.value(value.format(formatter));
        } else {
            out.nullValue();
        }
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String str = in.nextString();
        return str != null && !str.isEmpty() ? LocalDateTime.parse(str, formatter) : null;
    }

}
