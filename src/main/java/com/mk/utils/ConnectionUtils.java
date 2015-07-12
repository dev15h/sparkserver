package com.mk.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by margish on 6/14/15.
 */
public class ConnectionUtils {
    public static Document connectionRetry(String url, Integer attempt){
        if (attempt > 3) return null;
        Document doc = null;
        try {
            System.out.println("Attempt " + attempt + " for connecting url: " + url);
            doc = Jsoup.connect(url).timeout(10000).get();
        } catch (Exception e){
            try {
                Thread.sleep(5000L);
                doc = connectionRetry(url, attempt+1);
            } catch (InterruptedException ie) {
                e.printStackTrace();
                doc = connectionRetry(url, attempt+1);
                return doc;
            }
        }
        return doc;

    }
}
