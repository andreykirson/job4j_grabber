package html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SqlRuParse {
    public static void main(String[] args) throws Exception {
        int page;
        for (page = 1; page <= 3; page++) {
            Document doc = Jsoup.connect(String.format("https://www.sql.ru/forum/job-offers/%s", page)).get();
                Elements table = doc.getElementsByClass("forumTable").get(0).getElementsByTag("tr");
                for (int i = 4; i < table.size(); i++) {
                    String vacancy = table.get(i).getElementsByClass("postslisttopic").text();
                    String date = table.get(i).getElementsByTag("td").get(5).text();
                    System.out.println(vacancy + " " + date);
                }
        }
    }
}

