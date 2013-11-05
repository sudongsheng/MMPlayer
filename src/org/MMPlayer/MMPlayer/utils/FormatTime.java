package org.MMPlayer.MMPlayer.utils;

public class FormatTime {
    public String formatTime(Long time) {
        String min = time / (1000 * 60) + "";
        String sec = null;
        if (time % (1000 * 60) / 100 % 10 >= 5
                && time % (1000 * 60) / 1000 != 59) {
            sec = time % (1000 * 60) / 1000 + 1 + "";
        } else if (time % (1000 * 60) / 100 % 10 >= 5
                && time % (1000 * 60) / 1000 == 59) {
            min = time / (1000 * 60) + 1 + "";
            sec = time % (1000 * 60) / 1000 + "";
        } else {
            sec = time % (1000 * 60) / 1000 + "";
        }
        if (min.length() < 2) {
            min = "0" + time / (1000 * 60) + "";
        }
        if (sec.length() < 2) {
            sec = "0" + sec + "";
        }
        return min + ":" + sec;
    }
}
