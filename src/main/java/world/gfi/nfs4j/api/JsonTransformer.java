package world.gfi.nfs4j.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.ResponseTransformer;

import java.nio.file.Path;

public class JsonTransformer implements ResponseTransformer {
    public static Gson gson = new GsonBuilder().registerTypeAdapter(Path.class, new PathConverter()).create();

    @Override
    public String render(Object model) {
        return gson.toJson(model);
    }

}
