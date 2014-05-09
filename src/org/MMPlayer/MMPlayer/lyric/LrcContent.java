package org.MMPlayer.MMPlayer.lyric;

import java.util.Comparator;

public class LrcContent implements Comparable<LrcContent>{
    private String lrcStr;  //歌词内容
    private int lrcTime;    //歌词当前时间
    public String getLrcStr() {
        return lrcStr;
    }
    public void setLrcStr(String lrcStr) {
        this.lrcStr = lrcStr;
    }
    public int getLrcTime() {
        return lrcTime;
    }
    public void setLrcTime(int lrcTime) {
        this.lrcTime = lrcTime;
    }

    @Override
    public int compareTo(LrcContent lrcContent) {
        return getLrcTime()-lrcContent.getLrcTime();
    }
}
