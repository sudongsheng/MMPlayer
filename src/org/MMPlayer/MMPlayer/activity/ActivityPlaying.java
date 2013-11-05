package org.MMPlayer.MMPlayer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.umeng.analytics.MobclickAgent;
import org.MMPlayer.MMPlayer.R;
import org.MMPlayer.MMPlayer.cut.ID3V1;
import org.MMPlayer.MMPlayer.cut.ID3V2;
import org.MMPlayer.MMPlayer.cut.MP3File;
import org.MMPlayer.MMPlayer.model.Mp3Info;
import org.MMPlayer.MMPlayer.service.ServicePlaying;
import org.MMPlayer.MMPlayer.utils.AppConstant;
import org.MMPlayer.MMPlayer.utils.FormatTime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: helloworld
 * Date: 13-9-18
 * Time: 上午1:10
 * To change this template use File | Settings | File Templates.
 */
public class ActivityPlaying extends Activity {
    private ImageButton beforeButton;
    private ImageButton pauseandplayButton;
    private ImageButton nextButton;
    private ImageButton listButton;
    private ImageButton favorite;
    private ImageButton circle;
    private ImageButton cut;
    private ImageButton cut_start;
    private SeekBar seekBar;
    private TextView currentTime;
    private TextView finalTime;
    private TextView musicName;

    private int position;
    private int repeatState;
    private boolean isFavorite;
    private SharedPreferences preferences;

    public static List<Mp3Info> mp3Infos;
    public static boolean isPlaying = true;
    public static int musicNum;

    private Handler handler = new Handler();
    private UpdateTimeCallback updateTimeCallback = null;
    private long offset = 0;
    private long begin = 0;
    private long nextTimeMill = 0;
    private long currentTimeMill = 0;
    private long pauseTimeMills = 0;

    private boolean cutFlag = false;
    private boolean cutStartFlag = true;
    private long start_cutTime = 0;
    private long end_cutTime = 0;

    private final static int BEFORE = 1;
    private final static int NEXT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        position = getIntent().getIntExtra("position", 0);
        savePosition();
        mp3Infos = (ArrayList) getIntent().getParcelableArrayListExtra("mp3Infos");
        musicNum = mp3Infos.size();
        initView();
    }

    private void savePosition() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("position", position);
        editor.commit();
    }

    private void initView() {
        findViewID();
        setListener();
        setView();
        setDynamicView();
        prepareLrc();
        begin = System.currentTimeMillis();
        handler.post(updateTimeCallback);
        playService(AppConstant.MEDIA_PLAY);
        PlayerReceiver playerReceiver = new PlayerReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstant.UPDATE_ACTION);
        registerReceiver(playerReceiver, filter);
    }

    private void findViewID() {
        beforeButton = (ImageButton) findViewById(R.id.beforesong);
        pauseandplayButton = (ImageButton) findViewById(R.id.pauseandplay);
        nextButton = (ImageButton) findViewById(R.id.nextsong);
        listButton = (ImageButton) findViewById(R.id.list);
        favorite = (ImageButton) findViewById(R.id.favorite);
        circle = (ImageButton) findViewById(R.id.circle);
        cut = (ImageButton) findViewById(R.id.cut);
        cut_start = (ImageButton) findViewById(R.id.cut_start);
        seekBar = (SeekBar) findViewById(R.id.seekBarId);
        finalTime = (TextView) findViewById(R.id.final_time);
        currentTime = (TextView) findViewById(R.id.current_time);
        musicName = (TextView) findViewById(R.id.musicName);
    }

    private void setView() {
        repeatState = preferences.getInt("repeatState", AppConstant.AllRepeat);
        switch (repeatState) {
            case AppConstant.AllRepeat:
                circle.setBackgroundResource(R.drawable.circle_all);
                break;
            case AppConstant.SingleRepeat:
                circle.setBackgroundResource(R.drawable.circle_single);
                break;
            case AppConstant.RandomRepeat:
                circle.setBackgroundResource(R.drawable.circle_random);
                break;
        }
    }

    private void setDynamicView() {
        finalTime.setText(new FormatTime().formatTime(mp3Infos.get(position).getMp3Duration()));
        musicName.setText(mp3Infos.get(position).getMp3Name());
        isFavorite = preferences.getBoolean("favorite_" + mp3Infos.get(position).getId(), false);
        if (isFavorite)
            favorite.setBackgroundResource(R.drawable.favorite_white);
        else
            favorite.setBackgroundResource(R.drawable.unfavorite_white);
    }

    private void setListener() {
        pauseandplayButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (isPlaying) {
                        view.setBackgroundResource(R.drawable.play_pressed);
                    } else {
                        view.setBackgroundResource(R.drawable.pause_pressed);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (isPlaying) {
                        view.setBackgroundResource(R.drawable.play);
                    } else {
                        view.setBackgroundResource(R.drawable.pause);
                    }
                }
                return false;
            }
        });
        pauseandplayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playService(AppConstant.MEDIA_PAUSE);
                if (isPlaying) {
                    handler.removeCallbacks(updateTimeCallback);
                    pauseTimeMills = System.currentTimeMillis();
                } else {
                    begin = System.currentTimeMillis() - pauseTimeMills + begin;
                    handler.post(updateTimeCallback);
                }
                isPlaying = isPlaying ? false : true;
            }
        });
        beforeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMusicPosition(BEFORE);
                savePosition();
                setDynamicView();
                pauseandplayButton.setBackgroundResource(R.drawable.pause);
                isPlaying = true;
                if (isPlaying)
                    playService(AppConstant.MEDIA_PLAY);
                else
                    playService(AppConstant.MEDIA_NEXT);
                updateTimeCallback = new UpdateTimeCallback(0);
                begin = System.currentTimeMillis();
                handler.post(updateTimeCallback);
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMusicPosition(NEXT);
                savePosition();
                setDynamicView();
                pauseandplayButton.setBackgroundResource(R.drawable.pause);
                isPlaying = true;
                if (isPlaying)
                    playService(AppConstant.MEDIA_PLAY);
                else
                    playService(AppConstant.MEDIA_NEXT);
                updateTimeCallback = new UpdateTimeCallback(0);
                begin = System.currentTimeMillis();
                handler.post(updateTimeCallback);
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser == true) {
                    progress = (int) (progress * mp3Infos.get(position).getMp3Duration()) / 100;
                    playService(AppConstant.MEDIA_SEEKTO, progress);
                    updateTimeCallback = new UpdateTimeCallback(progress);
                    handler.post(updateTimeCallback);
                    pauseandplayButton.setBackgroundResource(R.drawable.pause);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isPlaying = false;
                playService(AppConstant.MEDIA_PAUSE);
                handler.removeCallbacks(updateTimeCallback);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isPlaying = true;
                playService(AppConstant.MEDIA_PAUSE);
                begin = System.currentTimeMillis();
            }
        });
        circle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = preferences.edit();
                if (repeatState == AppConstant.AllRepeat) {
                    circle.setBackgroundResource(R.drawable.circle_single);
                    repeatState = AppConstant.SingleRepeat;
                    editor.putInt("repeatState", repeatState);
                } else if (repeatState == AppConstant.SingleRepeat) {
                    circle.setBackgroundResource(R.drawable.circle_random);
                    repeatState = AppConstant.RandomRepeat;
                    editor.putInt("repeatState", repeatState);
                } else if (repeatState == AppConstant.RandomRepeat) {
                    circle.setBackgroundResource(R.drawable.circle_all);
                    repeatState = AppConstant.AllRepeat;
                    editor.putInt("repeatState", repeatState);
                }
                editor.commit();
            }
        });
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = preferences.edit();
                if (isFavorite) {
                    editor.putBoolean("favorite_" + mp3Infos.get(position).getId(), false);
                    isFavorite = false;
                    favorite.setBackgroundResource(R.drawable.unfavorite_white);
                } else {
                    editor.putBoolean("favorite_" + mp3Infos.get(position).getId(), true);
                    isFavorite = true;
                    favorite.setBackgroundResource(R.drawable.favorite_white);
                }
                editor.commit();
            }
        });
        musicName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String items[] = new String[mp3Infos.size()];
                for (int i = 0; i < mp3Infos.size(); i++)
                    items[i] = mp3Infos.get(i).getMp3Name();
                new AlertDialog.Builder(ActivityPlaying.this)
                        .setTitle("选择音乐")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putInt("position", which);
                                editor.commit();
                                position = which;
                                setDynamicView();
                                playService(AppConstant.MEDIA_PLAY);
                                updateTimeCallback = new UpdateTimeCallback(0);
                                begin = System.currentTimeMillis();
                                handler.post(updateTimeCallback);
                            }
                        }).show();
            }
        });
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityPlaying.this, ActivityMusicMenu.class);
                intent.putExtra("mp3Info", mp3Infos.get(position).getMp3Name());
                startActivity(intent);
            }
        });
        cut.setOnClickListener(new CutButtonListener());
    }

    private void prepareLrc() {
        /*try {
            InputStream inputStream = new FileInputStream(mp3Info.getMp3Path()
                    .substring(0, mp3Info.getMp3Path().lastIndexOf("."))
                    + ".lrc");
            LrcProcessor lrcProcessor = new LrcProcessor();
            queues = lrcProcessor.process(inputStream);

            begin = 0;
            currentTimeMill = 0;
            nextTimeMill = 0;
        } catch (FileNotFoundException e) {
            // e.printStackTrace();
            updateTimeCallback = new UpdateTimeCallback();
        }*/
        updateTimeCallback = new UpdateTimeCallback();
    }

    private void getMusicPosition(int flag) {
        if (repeatState == AppConstant.RandomRepeat) {
            position = (int) ((musicNum - 1) * Math.random());
        } else {
            if (flag == BEFORE) {
                if (position == 0) { // 如果已经时第一首则播放最后一首
                    position = musicNum - 1;
                } else { // 否则播放上一首
                    position -= 1;
                }
            } else if (flag == NEXT) {
                if (position == musicNum - 1) { // 如果已经时第一首则播放最后一首
                    position = 0;
                } else { // 否则播放上一首
                    position += 1;
                }
            }
        }
    }

    private void playService(int i) {
        Intent intent = new Intent(ActivityPlaying.this, ServicePlaying.class);
        intent.putExtra("position", position);
        intent.putParcelableArrayListExtra("mp3Infos", (ArrayList) mp3Infos);
        intent.putExtra("MSG", i);
        startService(intent);
    }

    private void playService(int i, int progress) {
        Intent intent = new Intent(ActivityPlaying.this, ServicePlaying.class);
        intent.putExtra("MSG", i);
        intent.putExtra("progress", progress);
        startService(intent);
    }

    class CutButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (cutFlag)
                setViewVisible();
            else {
                setViewInVisible();
                cut_start.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (cutStartFlag) {
                            cut_start.setBackgroundResource(R.drawable.stop_cut);
                            cutStartFlag = false;
                            start_cutTime = offset;
                            Toast.makeText(ActivityPlaying.this, "开始剪切", Toast.LENGTH_SHORT).show();
                        } else {
                            end_cutTime = offset;
                            if (isPlaying) {
                                playService(AppConstant.MEDIA_PAUSE);
                                handler.removeCallbacks(updateTimeCallback);
                                pauseTimeMills = System.currentTimeMillis();
                            }
                            isPlaying = false;
                            new AlertDialog.Builder(ActivityPlaying.this)
                                    .setTitle("确定保存").setMessage("您是否保存剪切歌曲？").setIcon(R.drawable.cut)
                                    .setPositiveButton("确定",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    playService(AppConstant.MEDIA_PAUSE);
                                                    begin = System.currentTimeMillis() - pauseTimeMills + begin;
                                                    handler.post(updateTimeCallback);
                                                    isPlaying = true;
                                                    cutFlag = false;
                                                    setViewVisible();
                                                    Log.i("TAG", start_cutTime + "##$$%" + end_cutTime + "");
                                                    File f = new File(mp3Infos.get(position).getMp3Path());
                                                    ID3V1 id3v1 = new ID3V1(f);
                                                    try {
                                                        id3v1.initialize();
                                                        Log.i("TAG", id3v1.toString());
                                                    } catch (Exception e) {
                                                    }
                                                    ID3V2 id3v2 = new ID3V2(f);
                                                    try {
                                                        id3v2.initialize();
                                                        Log.i("TAG", id3v2.toString());
                                                    } catch (Exception e) {
                                                    }
                                                    if (start_cutTime < end_cutTime) {
                                                        try {
                                                            MP3File file = new MP3File(mp3Infos.get(position).getMp3Path(), mp3Infos.get(position).getMp3Name(), start_cutTime, end_cutTime);
                                                            long need = file.time2offset(start_cutTime / 1000) - file.time2offset(end_cutTime / 1000) + 128 + file.getFrameOffset();
                                                            File fi = Environment.getExternalStorageDirectory();
                                                            StatFs fs = new StatFs(fi.getPath());
                                                            int c = fs.getAvailableBlocks();
                                                            int s = fs.getBlockSize();
                                                            int avaliable = c * s;
                                                            Log.i("TAG", "need:" + need + "c:" + c + "  s:" + s + "  avaliable:" + avaliable);
                                                            if (avaliable >= need) {
                                                                file.cut(start_cutTime, end_cutTime);
                                                                Toast.makeText(ActivityPlaying.this, "MP3剪切成功", Toast.LENGTH_SHORT).show();
                                                            } else
                                                                Toast.makeText(ActivityPlaying.this, "内存空间不足", Toast.LENGTH_SHORT).show();
                                                        } catch (Exception e) {
                                                            Toast.makeText(ActivityPlaying.this, "暂不支持此歌曲的裁剪", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }
                                            })
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            playService(AppConstant.MEDIA_PAUSE);
                                            begin = System.currentTimeMillis() - pauseTimeMills + begin;
                                            handler.post(updateTimeCallback);
                                            isPlaying = true;
                                            cutStartFlag = true;
                                            setViewInVisible();
                                        }
                                    }).create().show();
                        }
                    }
                });
            }
        }
    }

    private void setViewInVisible() {
        cutFlag = true;
        if (cut_start.isClickable()) {
            cut_start.setClickable(true);
        }
        cut_start.setVisibility(View.VISIBLE);
        cut_start.setBackgroundResource(R.drawable.start_cut);
        beforeButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        circle.setVisibility(View.GONE);
        beforeButton.setClickable(false);
        nextButton.setClickable(false);
        listButton.setClickable(false);
    }

    private void setViewVisible() {
        cutFlag = false;
        cutStartFlag = true;
        beforeButton.setClickable(true);
        nextButton.setClickable(true);
        circle.setClickable(true);
        listButton.setClickable(true);
        beforeButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
        circle.setVisibility(View.VISIBLE);
        cut_start.setVisibility(View.GONE);
    }

    class UpdateTimeCallback implements Runnable {
        ArrayList times = null;
        ArrayList messages = null;
        int seekTo = 0;
        int ss;

        public UpdateTimeCallback() {
        }

        public UpdateTimeCallback(int seekTo) {
            this.seekTo = seekTo;
        }

        public UpdateTimeCallback(ArrayList<ArrayList> queues) {
            // 从ArrayList当中取出相应的对象对象
            times = queues.get(0);
            messages = queues.get(1);
            ss = times.size();
        }

        @Override
        public void run() {
            // 计算偏移量，也就是说从开始播放Mp3到现在为止，共消耗了多少时间
            offset = System.currentTimeMillis() - begin + seekTo;
            if (offset <= mp3Infos.get(position).getMp3Duration()) {
                // 更新进度条
                int bar = (int) Math.floor(offset * 100 / mp3Infos.get(position).getMp3Duration());
                seekBar.setProgress(bar);
                currentTime.setText(new FormatTime().formatTime(offset));
                currentTimeMill = currentTimeMill + 10;
            }
            /*if (currentTimeMill == 0) {
                nextTimeMill = (Long) times.get(i);
                message = (String) messages.get(i);
                i++;
                lrcTextView.setText(message);
            }

            if (offset >= nextTimeMill) {
                lrcTextView.setText(message);
                if (i < ss) {
                    message = (String) messages.get(i);
                    nextTimeMill = (Long) times.get(i);
                    i++;
                }
            }*/
            handler.postDelayed(updateTimeCallback, 10);
        }
    }

    class PlayerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(AppConstant.UPDATE_ACTION)) {
                // 获取Intent中的current消息，current代表当前正在播放的歌曲
                position = intent.getIntExtra("position", 0);
                setDynamicView();
                handler.removeCallbacks(updateTimeCallback);
                updateTimeCallback = new UpdateTimeCallback(0);
                begin = System.currentTimeMillis();
                handler.post(updateTimeCallback);
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(ActivityPlaying.this, ActivityMusicMenu.class);
            intent.putExtra("mp3Info", mp3Infos.get(position).getMp3Name());
            startActivity(intent);
        }
        return false;
    }
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
