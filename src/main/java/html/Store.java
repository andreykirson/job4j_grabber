package html;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

public interface Store {

    void save(Post post) throws IOException;

    List<Post> getAll();

    Post findById(String id);

    Timestamp lastItem();
}