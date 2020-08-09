package html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class SqlRuParse {
    public static void main(String[] args) throws Exception {
        Document doc = Jsoup.connect("https://www.sql.ru/forum/job-offers").get();
        Elements table = doc.getElementsByClass("forumTable").get(0).getElementsByTag("tr");
        for (int i = 4; i < table.size(); i++) {
            String vacancy = table.get(i).getElementsByClass("postslisttopic").text();;
            String date = table.get(i).getElementsByTag("td").get(5).text();
            System.out.println(vacancy + " " + date);
        }
    }
}
