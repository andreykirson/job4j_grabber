package html;

import java.sql.Timestamp;
import java.util.Objects;

public class Post {
    private String url;
    private String description;
    private String name;
    private Timestamp date;
    private String id;

        public Post(String name, String url, String description, Timestamp date) {
            this.url = url;
            this.description = description;
            this.name = name;
            this.date = date;
        }

    public String getName() {
        return name;
    }

    public Timestamp getDate() {
        return date;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
        public String toString() {
            return "Post{"
                    +
                    "url='"
                    +
                    url
                    +
                    '\''
                    +
                    ", description='"
                    + description
                    +
                    '\''
                    +
                    ", name='" + name + '\''
                    +
                    ", date=" + date
                    +
                    '}';
        }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Post post = (Post) o;
        return Objects.equals(url, post.url)
                &&
                Objects.equals(name, post.name)
                &&
                Objects.equals(date, post.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, name, date);
    }
}
