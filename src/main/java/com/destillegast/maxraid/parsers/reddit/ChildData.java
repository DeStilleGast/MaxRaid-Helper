package com.destillegast.maxraid.parsers.reddit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by DeStilleGast 6-2-2020
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChildData {

    private String subreddit;
    private String selftext;
    @JsonProperty("author_fullname")
    private String authorFullname;
    @JsonProperty("title")
    private String title;

    public String getSubreddit() { return subreddit; }

    public String getSelftext() { return selftext; }

    public String getAuthorFullname() { return authorFullname; }
}
