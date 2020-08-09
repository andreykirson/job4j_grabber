package html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class SqlRuParse {
    public static void main(String[] args) throws Exception {
        Document doc = Jsoup.connect("https://www.sql.ru/forum/job-offers").get();
        Elements table = doc.getElementsByClass("forumTable").get(0).getElementsByTag("tr");
        for (int i = 5; i < table.size(); i++) {
            String vacancy = table.get(i).getElementsByClass("postslisttopic").text();;
            String date = table.get(i).getElementsByTag("td").get(5).text();

            SimpleDateFormat formatter = new SimpleDateFormat("d MMM yy", new Locale("ru"));
            String[] monthAndYear = date.split(",");
            Date dateMontYear = formatter.parse(monthAndYear[0]);
            System.out.println(dateMontYear);
        }
    }
}
