package org.MMPlayer.MMPlayer.notification;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;

public class SetVoice {

    public void setMyRingtone(Context context, String path) {
        File sdfile = new File(path);
        ContentValues values = new ContentValues();

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile
                .getAbsolutePath());
        Cursor cursor = context.getContentResolver().query(uri, null,
                MediaStore.MediaColumns.DATA + "=?", new String[] { path },
                null);
        if (cursor.moveToFirst() && cursor.getCount() > 0) {
            String _id = cursor.getString(0);
            values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
            values.put(MediaStore.Audio.Media.IS_ALARM, false);
            values.put(MediaStore.Audio.Media.IS_MUSIC, false);
            context.getContentResolver().update(uri, values,
                    MediaStore.MediaColumns.DATA + "=?", new String[] { path });
            Uri newUri = ContentUris.withAppendedId(uri, Long.valueOf(_id));
            RingtoneManager.setActualDefaultRingtoneUri(context,
                    RingtoneManager.TYPE_RINGTONE, newUri);
            Toast.makeText(context, "设置来电铃声成功！", Toast.LENGTH_SHORT).show();
        }
    }

    // 设置--提示音的具体实现方法
    public void setMyNotification(Context context, String path) {

        File sdfile = new File(path);
        ContentValues values = new ContentValues();
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile
                .getAbsolutePath());
        Cursor cursor = context.getContentResolver().query(uri, null,
                MediaStore.MediaColumns.DATA + "=?", new String[] { path },
                null);
        if (cursor.moveToFirst() && cursor.getCount() > 0) {
            String _id = cursor.getString(0);
            values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
            values.put(MediaStore.Audio.Media.IS_ALARM, false);
            values.put(MediaStore.Audio.Media.IS_MUSIC, false);

            context.getContentResolver().update(uri, values,
                    MediaStore.MediaColumns.DATA + "=?", new String[] { path });
            Uri newUri = ContentUris.withAppendedId(uri, Long.valueOf(_id));
            RingtoneManager.setActualDefaultRingtoneUri(context,
                    RingtoneManager.TYPE_NOTIFICATION, newUri);
            Toast.makeText(context, "设置通知铃声成功！", Toast.LENGTH_SHORT).show();
        }
    }

    // 设置--闹铃音的具体实现方法
    public void setMyAlarm(Context context, String path) {
        File sdfile = new File(path);
        ContentValues values = new ContentValues();
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile
                .getAbsolutePath());
        Cursor cursor = context.getContentResolver().query(uri, null,
                MediaStore.MediaColumns.DATA + "=?", new String[] { path },
                null);
        if (cursor.moveToFirst() && cursor.getCount() > 0) {
            String _id = cursor.getString(0);
            values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
            values.put(MediaStore.Audio.Media.IS_ALARM, true);
            values.put(MediaStore.Audio.Media.IS_MUSIC, false);
            context.getContentResolver().update(uri, values,
                    MediaStore.MediaColumns.DATA + "=?", new String[] { path });
            Uri newUri = ContentUris.withAppendedId(uri, Long.valueOf(_id));
            RingtoneManager.setActualDefaultRingtoneUri(context,
                    RingtoneManager.TYPE_ALARM, newUri);
            Toast.makeText(context, "设置闹钟铃声成功！", Toast.LENGTH_SHORT).show();
        }
    }
}
