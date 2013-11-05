package org.MMPlayer.MMPlayer.cut;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class ID3V2 {

    private File file;
    private int tagSize = -1;
    //存储ID3V2的帧，比如TALB等
    private Map<String, byte[]> tags = new HashMap<String, byte[]>();

    public ID3V2(File file) {
        this.file = file;
    }

    public void initialize() throws Exception {
        if (file == null)
            Log.i("TAG", "MP3 file is not found");
        FileInputStream is = new FileInputStream(file);
        byte[] header = new byte[10];
        is.read(header);
        //判断是否是合法的ID3V2头
        if (header[0] != 'I' || header[1] != 'D' || header[2] != '3') {
            Log.i("TAG", "not invalid mp3 ID3 tag");
        } else {
            //计算ID3V2的帧大小
            tagSize = (header[9] & 0xff) + ((header[8] & 0xff) << 7)
                    + ((header[7] & 0xff) << 14) + ((header[6] & 0xff) << 21);
            int pos = 10;
            while (pos < tagSize) {
                byte[] tag = new byte[10];
                //读取ID3V2的帧头，如果tag[0]=0，则跳出循环，结束解析ID3V2
                is.read(tag);
                if (tag[0] == 0) {
                    break;
                }
                String tagName = new StringBuffer().append((char) tag[0]).append(
                        (char) tag[1]).append((char) tag[2]).append((char) tag[3])
                        .toString();
                //计算ID3V2帧的大小，不包括前面的帧头大小
                int length = ((tag[4] & 0xff) << 24) + ((tag[5] & 0xff) << 16)
                        + ((tag[6] & 0xff) << 8) + tag[7];
                Log.i("TAG", length + "length");
                try {
                    byte[] data = new byte[length];
                    is.read(data);
                    //将帧头和帧体存储在HashMap中
                    tags.put(tagName, data);
                } catch (OutOfMemoryError error) {
                }
                pos = pos + length + 10;
            }
        }
        is.close();
    }

    public int getTagSize() {
        return tagSize;
    }

    private String getTagText(String tag) {
        byte[] data = (byte[]) tags.get(tag);
        //查询帧体的编码方式
        String encoding = encoding(data[0]);
        try {
            return new String(data, 1, data.length - 1, encoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String encoding(byte data) {
        String encoding = null;
        switch (data) {
            case 0:
                encoding = "ISO-8859-1";
                break;
            case 1:
                encoding = "UTF-16";
                break;
            case 2:
                encoding = "UTF-16BE";
                break;
            case 3:
                encoding = "UTF-8";
                break;
            default:
                encoding = "ISO-8859-1";
        }
        return encoding;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("标题:" + getTagText("TIT2") + "\n");
        buffer.append("歌手:" + getTagText("TPE1") + "\n");
        buffer.append("专辑:" + getTagText("TALB") + "\n");
        return buffer.toString();
    }
}

