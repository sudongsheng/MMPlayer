package org.MMPlayer.MMPlayer.cut;

import android.os.Environment;
import android.util.Log;

import java.io.*;

public class MP3File {
    private File file;
    private long length;
    private ID3V2 id3v2;
    private Frames frames;
    public String fileName;
    public String songName;
    public long start_cutTime;
    public long end_cutTime;

    public MP3File(String path, String songName, long start_cutTime,
                   long end_cutTime) throws Exception {
        file = new File(path);
        this.songName = songName;
        this.start_cutTime = start_cutTime;
        this.end_cutTime = end_cutTime;
        if (file.exists()) {
            length = file.length();
            initialize();
        }
    }

    public long time2offset(long mediaTime) {
        if (frames != null)
            return frames.time2offset(mediaTime);
        return -1;
    }

    private void initialize() throws Exception {
        // 创建并初始化ID3V2标签
        if (id3v2 == null) {
            id3v2 = new ID3V2(file);
        }
        try {
            id3v2.initialize();
            // 创建Frames对象
            frames = new Frames(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPath() {
        if (file != null)
            return file.getPath();
        return null;
    }

    // 查询MP3帧的起始位置
    public int getFrameOffset() {
        if (id3v2 != null)
            return id3v2.getTagSize() + 10;
        return -1;
    }

    public long getLength() {
        return length;
    }

    /*
     * @param start 切割的开始位置，单位是毫秒
     * @param end   切割的终点位置，单位是毫秒
     * @return 新文件的路径
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public void cut(long start, long end) throws IOException {
        new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MM播放器/音乐剪切/").mkdirs();
        String path = file.getPath();
        fileName = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/MM播放器/音乐剪切/" + songName + " - 剪切 - " + ((int) end - (int) start) + ".mp3";
        //Log.i("TAG",fileName);
        FileInputStream fis = new FileInputStream(new File(path));
        // 新文件的输出流
        FileOutputStream fos = new FileOutputStream(new File(fileName));
        int tagSize = getFrameOffset();
        byte[] tag = new byte[tagSize];
        fis.read(tag);
        // 将ID3V2的内容写入到新文件的输出流中
        fos.write(tag);
        long off1 = frames.time2offset(start);
        long off2 = frames.time2offset(end);
        fis.skip(off1 - tagSize);
        byte[] buf = new byte[1024];
        int count = 0;
        int ch = -1;
        // 避免内存消耗过大，一块一块地读取输入流后再写入到输出流
        while ((count < off2 - off1) && (ch = fis.read(buf)) != -1) {
            fos.write(buf, 0, ch);
            count += ch;
        }
        byte[] id1 = new byte[128];
        fis.skip(length - off2 - 128);
        fis.read(id1);
        // 将ID3V1的内容写入到新文件的输出流
        fos.write(id1);
        fos.flush();
        fis.close();
        fos.close();
    }
}