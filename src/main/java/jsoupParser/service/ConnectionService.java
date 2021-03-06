package jsoupParser.service;

import jsoupParser.cookies.Cookies;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.logging.Logger;


public class ConnectionService {

    private static final Logger logger = Logger.getLogger(ConnectionService.class.getName());
    private static volatile   long lastConnectionTime = 0;
    private static long pauseBtwConnection;
    private Document htmlPage;
    private volatile static Cookies cookies;


    public ConnectionService(){
        int connectionLimit = 70;
        pauseBtwConnection= 60*1000/ connectionLimit;
    }

    public Document getDocument(String url){

        Connection connection = Jsoup.connect(url);
        this.setBrowserHeaders(connection);


        Connection.Response response;
        try {

            response=connection.execute();

            htmlPage = response.parse();
            cookies.addNewCookies(response.cookies());


        } catch (IOException ex){logger.warning("Error occurred while parsing "+url);}

        htmlPage = this.removeCaptcha(htmlPage);

        return htmlPage;
    }


    private Document removeCaptcha(Document htmlPage){
        Element element = htmlPage.select("div.form.form_state_image.form_error_no.form_audio_yes.i-bem").first();


        return htmlPage;
    }

    private void setBrowserHeaders(Connection connection){
        connection.validateTLSCertificates(true)
                 .cookies(cookies.getCookies())
                .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+ xml, application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Accept-Encoding", "gzip, deflate, sdch, br")
                .header("Accept-Language","ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4")
                .header("Cache-Control","no-cache")
                .header("Connection","keep-alive")
                .header("DNT","1")
                .header("Host", "auto.ru")
                .header("Pragma","no-cache")
                .header("Upgrade-Insecure-Requests","1")
                .followRedirects(true);

    }

    private synchronized boolean allowConnection(){

        boolean allowConnection = false;

        if(lastConnectionTime == 0){
            lastConnectionTime = System.currentTimeMillis();
        }

        long afterLastConnection = (int)(System.currentTimeMillis() - lastConnectionTime);

        if(afterLastConnection<pauseBtwConnection){

        }

        return allowConnection;
    }

    public static void setCookies(Cookies cookies) {
        ConnectionService.cookies = cookies;
    }
}
