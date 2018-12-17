package io.github.toilal.nsf4j.api;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathConverter implements JsonDeserializer<Path>, JsonSerializer<Path> {
    @Override
    public Path deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return Paths.get(json.getAsString());
    }

    @Override
    public JsonElement serialize(Path src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }
}
