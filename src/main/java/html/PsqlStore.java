package html;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PsqlStore implements Store, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(PsqlStore.class.getName());
    private static final String INSERT_REQUEST = "insert into data_model(name, created, text, link) values(?, ?, ?, ?) returning id;";
    private static final String FIND_BY_ID_REQUEST = "select * from data_model where id = ?;";
    private static final String GET_ALL = "select * from data_model;";

    private ConfigManager configManager;

    private Connection connection;

    public PsqlStore(Connection connection) {
        this.connection = connection;
    }

    public PsqlStore(ConfigManager configManager) throws IOException {
        this.configManager = configManager;
        this.initConnection();
        this.initTable();
    }

    private void initConnection() {
        try {
            Class.forName(configManager.get("driver-class-name"));
            connection = DriverManager.getConnection(
                    configManager.get("url"),
                    configManager.get("username"),
                    configManager.get("password")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initTable() throws IOException {
        String sql = String.join(" ", Files.readAllLines(Path.of("./src/main/java/db/DataModel.sql")));
        try (Statement stm = connection.createStatement()) {
            stm.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


        @Override
        public void save(Post post) {
            LOG.debug("Save Post name: {}, created_date: {}", post.getName(), post.getDate());
            try (PreparedStatement ps = connection.prepareStatement(INSERT_REQUEST)) {
                ps.setString(1, post.getName());
                ps.setTimestamp(2, post.getDate());
                ps.setString(3, post.getDescription());
                ps.setString(4, post.getUrl());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        LOG.debug("Key retrieved");
                        post.setId(String.valueOf(rs.getInt(1)));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            LOG.debug("Saving complete");
        }

        @Override
        public List<Post> getAll() {
            LOG.debug("Find all");
            List<Post> result = new ArrayList<>();
            try (PreparedStatement ps = connection.prepareStatement(GET_ALL)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(new Post(
                                rs.getString("name"),
                                rs.getString("link"),
                                rs.getString("text"),
                                rs.getTimestamp("created")
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
        public Post findById(String id) {
        LOG.debug("Find by id {}", id);
            Post result = null;
            try (PreparedStatement ps = connection.prepareStatement(FIND_BY_ID_REQUEST)) {
                ps.setInt(1, Integer.parseInt(id));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Post p = new Post(
                                rs.getString("name"),
                                rs.getString("link"),
                                rs.getString("text"),
                                rs.getTimestamp("created")
                        );
                        p.setId(rs.getString("id"));
                        result = p;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            LOG.debug("Found post name: {}", result);
            return result;
        }

    @Override
    public Timestamp lastItem() {
        LOG.debug("Retrieve last date");
        Timestamp time = Timestamp.valueOf(LocalDateTime.MAX);
        try (Statement st = connection.createStatement()) {
            try (ResultSet resultSet = st.executeQuery("select max(created) from data_model;")) {
                if (resultSet.next()) {
                    Timestamp dbTime = resultSet.getTimestamp(1);
                    if (dbTime != null) {
                        time = dbTime;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOG.debug("Retrieved date: {}", time);
        return time;
    }
    @Override
        public void close() throws Exception {
            if (connection != null) {
                connection.close();
            }
        }
    }
