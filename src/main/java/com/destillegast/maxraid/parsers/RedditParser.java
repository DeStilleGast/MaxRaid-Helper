package com.destillegast.maxraid.parsers;

import com.destillegast.maxraid.parsers.reddit.ChildData;
import com.destillegast.maxraid.parsers.reddit.RedditThread;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Objects;

/**
 * Created by DeStilleGast 6-2-2020
 */
public class RedditParser {

    public static ChildData getThreadContent(String threadId){
        try{
            return Objects.requireNonNull(parseThread(threadId)).getData().getChildren().get(0).getData();
        }catch (Exception ex){
            return null;
        }
    }

    public static RedditThread parseThread(String threadId){
        String base = "https://api.reddit.com/api/info/?id=t3_";

        ObjectMapper om = new ObjectMapper();
        try {
            String json = Jsoup.connect(base + threadId).ignoreContentType(true).get().text();

            om.enable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
//            om.enable(JsonParser.Feature.IGNORE_UNDEFINED);
            return om.readValue(json, RedditThread.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
