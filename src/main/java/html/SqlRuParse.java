package html;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlRuParse {

    private static final Logger LOG = LoggerFactory.getLogger(SqlRuParse.class.getName());

    private class Post {
        String url;
        String description;
        String name;
        LocalDateTime date;

        public Post() {
        }

        public Post(String name, String url, String description, LocalDateTime date) {
            this.url = url;
            this.description = description;
            this.name = name;
            this.date = date;
        }

        @Override
        public String toString() {
            return "Post{" +
                    "url='" + url + '\'' +
                    ", description='" + description + '\'' +
                    ", name='" + name + '\'' +
                    ", date=" + date +
                    '}';
        }
    }

    public static void main(String[] args) throws Exception {
        SqlRuParse sqlRuParse = new SqlRuParse();
        int page;
        for (page = 1; page <= 5; page++) {
            Document doc = Jsoup.connect(String.format("https://www.sql.ru/forum/job-offers/%s", page)).get();
                Elements table = doc.getElementsByClass("forumTable").get(0).getElementsByTag("tr");
                for (int i = 4; i < table.size(); i++) {
                    String vacancy = table.get(i).getElementsByClass("postslisttopic").text();
                    Element href = table.get(i).getElementsByClass("postslisttopic").first().child(1);
                    String link = href.attr("href");
                    String date = table.get(i).getElementsByTag("td").get(5).text();
                    Post post = sqlRuParse.detail(link);
                    System.out.println(vacancy + " " + parseDate(date) + " " + post.toString());
                }
        }
    }


    private static LocalDateTime parseDate(String date) {

        String pattern = "d MM yy";
        DateFormat df = new SimpleDateFormat(pattern);

        date = date.replace("янв", "01");
        date = date.replace("фев", "02");
        date = date.replace("мар", "03");
        date = date.replace("апр", "04");
        date = date.replace("май", "05");
        date = date.replace("июн", "06");
        date = date.replace("июл", "07");
        date = date.replace("авг", "08");
        date = date.replace("сен", "09");
        date = date.replace("окт", "10");
        date = date.replace("ноя", "11");
        date = date.replace("дек", "12");

        if (date.contains("сегодня")) {
            Date currentDate = new Date();
            date = date.replace("сегодня", df.format(currentDate));

        } else if (date.contains("вчера")) {
            Instant now = Instant.now();
            Instant before = now.minus(Duration.ofDays(1));
            Date yesterday = Date.from(before);
            date = date.replace("вчера", df.format(yesterday));
        }
        DateTimeFormatter strToDate = DateTimeFormatter.ofPattern("d MM yy, HH:mm", Locale.forLanguageTag("ru"));
        LocalDateTime rsl = LocalDateTime.parse(date, strToDate);
        return rsl;
    }

    public Post detail(String url) {
        LOG.debug("Parse resources {}", url);
        Post post = null;
        try {
            Document doc = Jsoup.connect(url).get();
            Elements comments = doc.select(".msgTable");
            String description = comments.first().select(".msgBody").get(1).html();
            String name = comments.first().select(".messageHeader").text();
            String date = comments.last().select(".msgFooter").text();
            date = date.substring(0, date.indexOf('[') - 1);
            LOG.debug("Parsing completed");
            post = new Post(name, url, description, parseDate(date));
            return post;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return post;
    }

}

