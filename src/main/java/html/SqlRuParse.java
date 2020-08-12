package html;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.Jsoup;

public class SqlRuParse {

    public static void main(String[] args) throws Exception {
        SqlRuParse sqlRuParse = new SqlRuParse();
        int page;
        for (page = 1; page <= 5; page++) {
            Document doc = Jsoup.connect(String.format("https://www.sql.ru/forum/job-offers/%s", page)).get();
                Elements table = doc.getElementsByClass("forumTable").get(0).getElementsByTag("tr");
                for (int i = 1; i < table.size(); i++) {
                    String vacancy = table.get(i).getElementsByClass("postslisttopic").text();
                    String date = table.get(i).getElementsByTag("td").get(5).text();
                    System.out.println(vacancy + " " + sqlRuParse.parseDate(date));
                }
        }
    }


    private LocalDateTime parseDate(String date) {

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
}

