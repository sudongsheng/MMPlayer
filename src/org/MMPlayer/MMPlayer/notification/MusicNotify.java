package org.MMPlayer.MMPlayer.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import org.MMPlayer.MMPlayer.R;
import org.MMPlayer.MMPlayer.activity.ActivityMusicMenu;
import org.MMPlayer.MMPlayer.activity.ActivityPlaying;

/**
 * Created by sudongsheng on 2014/5/1 0001.
 */
public class MusicNotify {
    private Context context;
    private String musicName;
    private String singer;

    public MusicNotify(Context context,String musicName,String singer) {
        this.context = context;
        this.musicName=musicName;
        this.singer=singer;
    }

    public void sendNotification() {
//        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        Intent intent = new Intent(context, ActivityMusicMenu.class);
//        PendingIntent contentIntent = PendingIntent.getActivity(context, 100, intent, 0);
//        Notification.Builder builder = new Notification.Builder(context)
//                .setContentIntent(contentIntent).setSmallIcon(R.drawable.play)
//                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.music_album))
//                .setWhen(System.currentTimeMillis()).setTicker("正在播放"+musicName)
//                .setAutoCancel(true).setContentTitle(musicName).setContentText(singer);
//        Notification notification = builder.build();// API 16添加创建notification的方法
//        manager.notify(110, notification);
        //得到通知管理器
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //构建通知
        Notification notification = new Notification(R.drawable.play, "正在播放"+musicName,System.currentTimeMillis());

        //设置通知的点击事件
        Intent intent = new Intent(context, ActivityMusicMenu.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 100,intent, 0);
        notification.setLatestEventInfo(context, musicName, singer, contentIntent);

        notification.flags = Notification.FLAG_AUTO_CANCEL;// 点击通知之后自动消失

        //发送通知
        nm.notify(100, notification);
    }
}
