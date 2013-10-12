package org.MMPlayer.MMPlayer.cut;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ID3V1 {
    
    private String title;
    private String artist="<UnKnown>";
    private String album="<UnKnown>";
    private String year="<UnKnown>";
    private File file;
    public ID3V1(File file) {
        this.file = file;
    }
    public void initialize(){
        try {
            //可以随机访问文件的任意部分
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            //跳到ID3V1开始的位置
            raf.seek(raf.length() - 128);
            byte[] tag = new byte[3];
            //读取Header
            raf.read(tag);
            if (!new String(tag).equals("TAG")) {
                Log.i("TAG","No ID3V1 found");
            }
            byte[] tags = new byte[125];
            raf.read(tags);
            //逐一读取ID3V1中的各个字段
            readTag(tags);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void readTag(byte[] array) {
        title = new String(array, 0, 30).trim();
        artist = new String(array, 30, 30).trim();
        album = new String(array, 60, 30).trim();
        year = new String(array, 90, 4);
        if(artist==null){
            artist="<UnKnown>";
        }
        if(album==null){
            album="<UnKnown>";
        }
        if(year==null){
            year="<UnKnown>";
        }
    }
    public String toString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("标题:"+title+"\n");
        buffer.append("歌手:"+artist+"\n");
        buffer.append("专辑:"+album+"\n");
        buffer.append("年代:"+year+"\n");
        return buffer.toString();
    }
}