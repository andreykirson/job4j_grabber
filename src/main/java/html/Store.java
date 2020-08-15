package html;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

public interface Store {

    void save(Post post) throws IOException;

    List<Post> getAll();

    Post findById(String id);
}