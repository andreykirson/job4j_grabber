package html;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(PsqlStore.class.getName());
    private static final String INSERT_REQUEST = "insert into data_model(name, created, text, link) values(?, ?, ?, ?) returning id;";
    private static final String FIND_BY_ID_REQUEST = "select * from items where id = ?;";
    private static final String GET_ALL = "select * from data_model;";

    private static Connection cn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
            cn = DriverManager.getConnection(
                    cfg.getProperty("url"),
                    cfg.getProperty("username"),
                    cfg.getProperty("password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        }

        @Override
        public void save (Post post) throws IOException {
            LOG.debug("Save Post name: {}, created_date: {}", post.name, post.date);
            try (PreparedStatement ps = cn.prepareStatement(INSERT_REQUEST)) {
                ps.setString(1, post.name);
                ps.setDate(2, post.date);
                ps.setString(3, post.description);
                ps.setString(4, post.url);
                ps.addBatch();
                ps.executeBatch();
            } catch (Exception e) {
                LOG.debug("Something went wrong", e);
            }
        }

        @Override
        public List<Post> getAll () {
            LOG.debug("Find all");
            List<Post> result = new ArrayList<>();
            try (PreparedStatement ps = cn.prepareStatement(GET_ALL)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(new Post(
                                rs.getString("name"),
                                rs.getString("link"),
                                rs.getString("text"),
                                rs.getDate("created")
                        ));
                    }
                }
                LOG.debug("Selecting complete. Found posts: {}", result.size());
            } catch (Exception e) {
                LOG.error("Something went wrong", e);
            }
            LOG.debug("Found {} Found posts", result.size());
            return result;
        }

        @Override
        public Post findById (String id){
            LOG.debug("Find by id {}", id);
            Post result = null;
            try (PreparedStatement ps = cn.prepareStatement(FIND_BY_ID_REQUEST)) {
                ps.setInt(1, Integer.parseInt(id));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        result = new Post(
                                rs.getString("name"),
                                rs.getString("link"),
                                rs.getString("text"),
                                rs.getDate("created")
                        );
                    }
                }
                LOG.debug("Selecting complete. Found post name: {}", result.name);
            } catch (Exception e) {
                LOG.error("Something went wrong", e);
            }
            LOG.debug("Found post name: {}", result.name);
            return result;
        }

        @Override
        public void close () throws Exception {
            if (cn != null) {
                cn.close();
            }
        }

    private Properties getConfig() {
        try (InputStream in = PsqlStore.class.getClassLoader().getResourceAsStream("grabber.properties")) {
            Properties config = new Properties();
            config.load(in);
            return config;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    }
}
