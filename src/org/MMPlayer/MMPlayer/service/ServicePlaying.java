package org.MMPlayer.MMPlayer.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import org.MMPlayer.MMPlayer.activity.ActivityMusicMenu;
import org.MMPlayer.MMPlayer.activity.ActivityPlaying;
import org.MMPlayer.MMPlayer.model.Mp3Info;
import org.MMPlayer.MMPlayer.utils.AppConstant;

import java.util.ArrayList;
import java.util.List;

public class ServicePlaying extends Service {
    private int position;
    private Mp3Info mp3Info;
    private MediaPlayer mediaPlayer;
    private int repeatState;
    private SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer arg0) {
                repeatState = preferences.getInt("repeatState", AppConstant.AllRepeat);
                if (repeatState == AppConstant.AllRepeat) {
                    if (position == ActivityPlaying.musicNum - 1) { // 如果已经时第一首则播放最后一首
                        position = 0;
                    } else { // 否则播放下一首
                        position += 1;
                    }
                    Mp3Info mp3Info = ActivityPlaying.mp3Infos.get(position);
                    play(mp3Info);
                } else if (repeatState == AppConstant.SingleRepeat) {
                    mediaPlayer.start();
                } else if (repeatState == AppConstant.RandomRepeat) {
                    position = (int) ((ActivityPlaying.musicNum - 1) * Math.random());
                    Mp3Info mp3Info = ActivityPlaying.mp3Infos.get(position);
                    play(mp3Info);
                }
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("position", position);
                editor.commit();
                Intent sendIntent = new Intent(AppConstant.UPDATE_ACTION);
                sendIntent.putExtra("position", position);
                sendIntent.putExtra("name", ActivityPlaying.mp3Infos.get(position).getMp3Name());
                // 发送广播，将被Activity组件中的BroadcastReceiver接收到
                sendBroadcast(sendIntent);
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        position = preferences.getInt("position", 0);
        //   Log.i("TAG","position:"+position);
        mp3Info = ActivityPlaying.mp3Infos.get(position);
        if (mp3Info != null) {
            int MSG = intent.getIntExtra("MSG", 0);
            switch (MSG) {
                case AppConstant.MEDIA_PLAY:
                    play(mp3Info);
                    break;
                case AppConstant.MEDIA_PAUSE:
                    pause();
                    break;
                case AppConstant.MEDIA_NEXT:
                    play(mp3Info);
                    mediaPlayer.pause();
                    break;
                case AppConstant.MEDIA_SEEKTO:
                    int progress = intent.getIntExtra("progress", 0);
                    mediaPlayer.seekTo(progress);
                    break;
                default:
                    break;
            }
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        mediaPlayer.release();
    }

    private void play(Mp3Info mp3Info) {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(mp3Info.getMp3Path());
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(new PreparedListener());// 注册一个监听器
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pause() {
        if (ActivityPlaying.isPlaying) {
            mediaPlayer.start();
        } else {
            mediaPlayer.pause();
        }
    }

    private final class PreparedListener implements OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mediaPlayer.start(); // 开始播放
        }
    }
}
