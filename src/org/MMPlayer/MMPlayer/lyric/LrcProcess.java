package org.MMPlayer.MMPlayer.lyric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.util.Xml.Encoding;
import android.widget.SlidingDrawer;

public class LrcProcess {
    private ArrayList<LrcContent> lrcList; //List集合存放歌词内容对象
    private LrcContent mLrcContent;     //声明一个歌词内容对象

    /**
     * 无参构造函数用来实例化对象
     */
    public LrcProcess() {
        mLrcContent = new LrcContent();
        lrcList = new ArrayList<LrcContent>();
    }

    /**
     * 读取歌词
     *
     * @param path
     * @return
     */
    public ArrayList<LrcContent> readLRC(String path) {
        File f = new File(path.replace(".mp3", ".lrc"));
        try {
            //创建一个文件输入流对象
            FileInputStream fis = new FileInputStream(f);
            InputStreamReader isr = new InputStreamReader(fis, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String s = "";
            while ((s = br.readLine()) != null) {
                //替换字符
                s = s.replace("[", "");
                s = s.replace("]", "$");
                //分离“@”字符
                String splitLrcData[] = s.split("\\$");
                if (splitLrcData.length == 1) {
                    if (!(splitLrcData[0].length() == 8 && splitLrcData[0].contains(":") && splitLrcData[0].contains("."))) {
                        mLrcContent.setLrcTime(0);
                        mLrcContent.setLrcStr(splitLrcData[0]);
                        lrcList.add(mLrcContent);
                        mLrcContent = new LrcContent();
                    }
                }
                if (splitLrcData.length > 1) {
                    for (int i = 0; i < splitLrcData.length - 1; i++) {
                        if (!(splitLrcData[splitLrcData.length - 1].length() == 8 && splitLrcData[splitLrcData.length - 1].contains(":") && splitLrcData[splitLrcData.length - 1].contains("."))) {
                            mLrcContent.setLrcStr(splitLrcData[splitLrcData.length - 1]);
                            //处理歌词取得歌曲的时间
                            int lrcTime = time2Str(splitLrcData[i]);
                            mLrcContent.setLrcTime(lrcTime);
                            Log.i("TAG", "time：" + lrcTime + ";content:" + splitLrcData[splitLrcData.length - 1]);
                            //添加进列表数组
                            lrcList.add(mLrcContent);
                            //新创建歌词内容对象
                            mLrcContent = new LrcContent();
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            mLrcContent.setLrcStr("木有歌词文件，下载中...");
            mLrcContent.setLrcTime(0);
            lrcList.add(mLrcContent);
        } catch (IOException e) {
            e.printStackTrace();
            mLrcContent.setLrcStr("木有读取到歌词哦！");
            mLrcContent.setLrcTime(0);
            lrcList.add(mLrcContent);
        }
        return lrcList;
    }

    /**
     * 解析歌词时间
     * 歌词内容格式如下：
     * [00:02.32]陈奕迅
     * [00:03.43]好久不见
     * [00:05.22]歌词制作  王涛
     *
     * @param timeStr
     * @return
     */
    public int time2Str(String timeStr) {
        timeStr = timeStr.replace(":", ".");
        timeStr = timeStr.replace(".", "$");

        String timeData[] = timeStr.split("\\$"); //将时间分隔成字符串数组

        //分离出分、秒并转换为整型
        int currentTime;
        try {
            int minute = Integer.parseInt(timeData[0]);
            int second = Integer.parseInt(timeData[1]);
            int millisecond = Integer.parseInt(timeData[2]);

            //计算上一行与下一行的时间转换为毫秒数
            currentTime = (minute * 60 + second) * 1000 + millisecond * 10;
        } catch (Exception e) {
            currentTime = 0;
        }
        return currentTime;
    }
}
