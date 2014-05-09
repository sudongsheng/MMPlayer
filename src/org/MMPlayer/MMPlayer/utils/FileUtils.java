package org.MMPlayer.MMPlayer.utils;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import org.MMPlayer.MMPlayer.model.Mp3Info;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    private String SDCardRoot;

    public FileUtils() {
        SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    }

    public File createFileInSDCard(String fileName, String dir) throws IOException {
        File file = new File(SDCardRoot + dir + File.separator + fileName);
        Log.i("TAG",file.toString());
        file.createNewFile();
        return file;
    }

    public File createSDDir(String dir) {
        File dirFile = new File(SDCardRoot + dir + File.separator);
        dirFile.mkdirs();
        return dirFile;
    }

    public boolean isFileExist(String fileName, String path) {
        File file = new File(SDCardRoot + path + File.separator + fileName);
        return file.exists();
    }

    public File write2SDFromInput(String path, String fileName,InputStream input) {
        File file = null;
        OutputStream output = null;
        try {
//            createSDDir(path);
            file = new File(path + File.separator+fileName);
            Log.i("TAG",file.toString());
            file.createNewFile();
            output = new FileOutputStream(file);
            byte buffer[] = new byte[4 * 1024];
            int temp;
            while ((temp = input.read(buffer)) != -1) {
                output.write(buffer, 0, temp);
            }
            output.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public long getFileSizes(String path) throws Exception {
        long s = 0;
        File f = new File(path);
        if (f.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(f);
            s = fis.available();
        }
        return s;
    }

    public List<Mp3Info> getMp3InfosByDirs(String path) {
        ArrayList cutArrayList = new ArrayList();
        File f = new File(SDCardRoot + path);
        if (f.isDirectory()) {
            File[] fileArray = f.listFiles();
            if (null != fileArray && 0 != fileArray.length) {
                for (int i = 0; i < fileArray.length; i++) {
                    if (fileArray[i].toString().endsWith(".mp3")) {
                        Mp3Info cutMp3Info = new Mp3Info();
                        cutMp3Info.setId(Integer.parseInt(fileArray[i].toString().substring(fileArray[i].toString().lastIndexOf("- 剪切 - ") + 7, fileArray[i].toString().lastIndexOf(".mp3"))));
                        cutMp3Info.setMp3Name(fileArray[i].toString().substring(fileArray[i].toString().indexOf("音乐剪切/") + 5, fileArray[i].toString().indexOf(".mp3")));
                        cutMp3Info.setTitle(fileArray[i].toString().substring(fileArray[i].toString().indexOf("音乐剪切/") + 5, fileArray[i].toString().indexOf(".mp3")));
                        cutMp3Info.setMp3Path(fileArray[i].toString());
                        cutMp3Info.setSinger("<unknown>");
                        try {
                            cutMp3Info.setMp3Duration(Long.parseLong(fileArray[i].toString().substring(fileArray[i].toString().lastIndexOf("- 剪切 - ") + 7, fileArray[i].toString().lastIndexOf(".mp3"))));
                        } catch (Exception e) {
                            cutMp3Info.setMp3Duration(Long.parseLong("1000000"));
                        }
                        cutArrayList.add(cutMp3Info);
                    }
                }
            }
        }
        return cutArrayList;
    }

    public List<Mp3Info> getMp3InfosBySystem(Context context) {
        ArrayList localArrayList = new ArrayList();
        Cursor localCursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.TITLE,
                }, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        try {
            if (localCursor.moveToFirst()) {
                do {
                    if (localCursor.getString(2).endsWith(".mp3") || localCursor.getString(2).endsWith(".wma") || localCursor.getString(2).endsWith(".wav") || localCursor.getString(2).endsWith(".aac") || localCursor.getString(2).endsWith(".amr")) {
                        Mp3Info localMp3Info = new Mp3Info();
                        localMp3Info.setId(localCursor.getInt(0));
                        localMp3Info.setMp3Path(localCursor.getString(1));
                        localMp3Info.setMp3Name(localCursor.getString(2).substring(0, localCursor.getString(2).indexOf(".")));
                        localMp3Info.setMp3Duration(localCursor.getLong(3));
                        localMp3Info.setSinger(localCursor.getString(4));
                        localMp3Info.setAlbum(localCursor.getString(5));
                        localMp3Info.setTitle(localCursor.getString(6));
                        try {
                            localMp3Info.setMp3Size(new FormateSize()
                                    .formateSize(getFileSizes(localCursor
                                            .getString(1))));
                        } catch (Exception e) {
                            localMp3Info.setMp3Size("0");
                        }
                        localMp3Info.setLrcName(localMp3Info.getMp3Name() + ".lrc");
                        localArrayList.add(localMp3Info);
                    }
                } while (localCursor.moveToNext());
            }
            return localArrayList;
        } catch (NullPointerException e) {
            return null;
        }
    }
}
