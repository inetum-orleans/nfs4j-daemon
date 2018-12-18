package world.gfi.nfs4j.config;

import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.nio.file.Path;

public class CustomRepresenter extends Representer {
    public CustomRepresenter() {
        this.representers.put(Path.class, new PathPresenter());
        this.addClassTag(Path.class, new Tag(Path.class));
    }

    private class PathPresenter implements Represent {
        @Override
        public Node representData(Object data) {
            String uri = data.toString();
            return representScalar(new Tag(Path.class), uri);
        }
    }
}
