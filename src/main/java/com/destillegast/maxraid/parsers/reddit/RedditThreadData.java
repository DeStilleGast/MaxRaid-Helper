package com.destillegast.maxraid.parsers.reddit;

import java.util.List;

/**
 * Created by DeStilleGast 6-2-2020
 */
public class RedditThreadData {
    private String modhash;
    private long dist;
    private List<Child> children;
    private Object after;
    private Object before;

    public String getModhash() { return modhash; }
    public void setModhash(String value) { this.modhash = value; }

    public long getDist() { return dist; }
    public void setDist(long value) { this.dist = value; }

    public List<Child> getChildren() { return children; }
    public void setChildren(List<Child> value) { this.children = value; }

    public Object getAfter() { return after; }
    public void setAfter(Object value) { this.after = value; }

    public Object getBefore() { return before; }
    public void setBefore(Object value) { this.before = value; }
}
