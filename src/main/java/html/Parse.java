package html;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.function.Predicate;

public interface Parse {
    List<Post> list(String link, Predicate<Timestamp> until) throws IOException;
    Post detail(String link);
    List<String> resources();
}
