package html;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlRuParse implements Parse {

    private static final String RESOURCE = "https://www.sql.ru/forum/job-offers";
    private static final Logger LOG = LoggerFactory.getLogger(SqlRuParse.class.getName());


    private static Timestamp parseDate(String date) {

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
        Timestamp timestamp = Timestamp.valueOf(rsl);
        return timestamp;
    }

    @Override
    public List<Post> list(String url, Predicate<Timestamp> until) {
        List<Post> listPosts = new ArrayList<>();
        SqlRuParse sqlRuParse = new SqlRuParse();
        String postLink = null;
        LOG.debug("Parse: {}", url);
            try {
                Document doc = Jsoup.connect(url).get();
                Elements table = doc.getElementsByClass("forumTable").get(0).getElementsByTag("tr");
                for (int i = 4; i < table.size(); i++) {
                    String vacancy = table.get(i).getElementsByClass("postslisttopic").text();
                    Element date = table.get(i).child(5);
                    Timestamp createDate = parseDate(date.text());
                    LOG.debug("Parse vacansy: {}", vacancy);
                    if (until.test(createDate)) {
                        System.out.println("Вакансия с такой датой уже есть!!!!!");
                        System.out.println("LocalDateTime.MAX " + Timestamp.valueOf(LocalDateTime.MAX));
                        System.out.println("createDate " + createDate);
                        break;
                    }
                    LOG.debug("Parse vacansy: {}", vacancy);
                    Element href = table.get(i).getElementsByTag("a").first();
                    postLink = href.attr("href");
                    Post post = sqlRuParse.detail(postLink);
                    listPosts.add(post);
                    System.out.println(createDate);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return listPosts;
    }

    public Post detail(String url) {
        LOG.debug("Parse resources {}", url);
        Post post = null;
        try {
            Document doc = Jsoup.connect(url).get();
            Elements comments = doc.select(".msgTable");
            String description = comments.first().select(".msgBody").get(1).text();
            String name = comments.first().select(".messageHeader").text();
            String date = comments.last().select(".msgFooter").text();
            date = date.substring(0, date.indexOf('[') - 1);
            LOG.debug("Parsing completed");
            post = new Post(name, url, description, parseDate(date));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public List<String> resources() {
        List<String> list = new LinkedList<>();
        for (int page = 1; page <= 2; page++) {
            list.add(String.format("%s/%s", RESOURCE, page));
        }
        return list;
    }

}

