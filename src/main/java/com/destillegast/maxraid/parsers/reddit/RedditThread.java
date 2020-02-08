package com.destillegast.maxraid.parsers.reddit;

/**
 * Created by DeStilleGast 6-2-2020
 */
public class RedditThread {
    private String kind;
    private RedditThreadData data;

    public String getKind() { return kind; }
    public void setKind(String value) { this.kind = value; }

    public RedditThreadData getData() { return data; }
    public void setData(RedditThreadData value) { this.data = value; }
}
