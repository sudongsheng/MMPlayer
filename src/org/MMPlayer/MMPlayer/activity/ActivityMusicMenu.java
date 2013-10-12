package org.MMPlayer.MMPlayer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.*;
import android.widget.*;
import org.MMPlayer.MMPlayer.R;
import org.MMPlayer.MMPlayer.model.Mp3Info;
import org.MMPlayer.MMPlayer.notification.SetVoice;
import org.MMPlayer.MMPlayer.service.ServicePlaying;
import org.MMPlayer.MMPlayer.utils.AppConstant;
import org.MMPlayer.MMPlayer.utils.FileUtils;
import org.MMPlayer.MMPlayer.utils.FormatTime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ActivityMusicMenu extends Activity implements RadioGroup.OnCheckedChangeListener {

    private ViewPager mViewPager;
    private View view_local, view_favorite, view_cut;
    private ListView listView_local, listView_favorite, listView_cut;
    private TextView songName;
    private ImageView music_album;
    private Button playButton;
    private ImageButton exit, favoriteMusic, localMusic, cutMusic, toFavorite;
    private int flag = AppConstant.LOCAL_MUSIC;
    private int position;
    private List<Mp3Info> mp3Infos_all, mp3Infos_favorite, mp3Infos_cut;
    private SharedPreferences preferences;
    private MyAdapter localAdapter, favoriteAdapter, cutAdapter;

    private Receiver receiver = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_menu);
        mViewPager = (ViewPager) findViewById(R.id.mViewPager);
        initView();
        mViewPager.setAdapter(new MyPagerAdapter(view_local, view_favorite, view_cut));
        mViewPager.setOnPageChangeListener(new MyPagerOnPageChangeListener());
    }

    private void initView() {
        mp3Infos_all = (ArrayList) new FileUtils().getMp3InfosBySystem(this);
        mp3Infos_cut = (ArrayList) new FileUtils().getMp3InfosByDirs("MM播放器/音乐剪切/");
        //  Log.i("TAG",mp3Infos_all.toString());
        view_local = getLayoutInflater().inflate(R.layout.view_local_music, null);
        listView_local = (ListView) view_local.findViewById(R.id.local_listView);
        exit = (ImageButton) view_local.findViewById(R.id.exit);
        favoriteMusic = (ImageButton) view_local.findViewById(R.id.favoriteMusic);
        favoriteMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(1);
            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ActivityMusicMenu.this).setIcon(R.drawable.exit).setTitle("关闭").setMessage("退出悦我音乐？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(ActivityMusicMenu.this, ServicePlaying.class);
                        stopService(intent);
                        Intent intent1 = new Intent(Intent.ACTION_MAIN);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent1.addCategory(Intent.CATEGORY_HOME);
                        startActivity(intent1);
                    }
                }).setNegativeButton("取消", null).show();
            }
        });
        if (mp3Infos_all != null) {
            localAdapter = new MyAdapter(this, mp3Infos_all);
            listView_local.setAdapter(localAdapter);
        }
        view_favorite = getLayoutInflater().inflate(R.layout.view_favorite_music, null);
        localMusic = (ImageButton) view_favorite.findViewById(R.id.localMusic);
        cutMusic = (ImageButton) view_favorite.findViewById(R.id.cutMusic);
        localMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(0);
            }
        });
        cutMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(2);
            }
        });

        view_cut = getLayoutInflater().inflate(R.layout.view_cut_music, null);
        toFavorite = (ImageButton) view_cut.findViewById(R.id.toFavorite);
        toFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(1);
            }
        });
        listView_cut = (ListView) view_cut.findViewById(R.id.cut_listView);
        cutAdapter = new MyAdapter(this, mp3Infos_cut);
        listView_cut.setAdapter(cutAdapter);
        setFooterView();
        receiver = new Receiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstant.UPDATE_ACTION);
        registerReceiver(receiver, filter);
    }

    private void setFooterView() {
        songName = (TextView) findViewById(R.id.songName);
        music_album = (ImageView) findViewById(R.id.music_album);
        playButton = (Button) findViewById(R.id.playMusic);
        final String name = getIntent().getStringExtra("mp3Info");
        if (name == null) {
            try {
                songName.setText(mp3Infos_all.get(0).getMp3Name());
            } catch (Exception e) {
                songName.setText("找不到音乐");
            }
        } else {
            songName.setText(name);
            if (ActivityPlaying.isPlaying) {
                playButton.setBackgroundResource(R.drawable.pause2);
            } else
                playButton.setBackgroundResource(R.drawable.play2);
        }
        music_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name != null)
                    onBackPressed();
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name == null) {

                } else {
                    if (ActivityPlaying.isPlaying == false) {
                        ActivityPlaying.isPlaying = true;
                        playService(AppConstant.MEDIA_PAUSE);
                        playButton.setBackgroundResource(R.drawable.pause2);
                    } else {
                        ActivityPlaying.isPlaying = false;
                        playService(AppConstant.MEDIA_PAUSE);
                        playButton.setBackgroundResource(R.drawable.play2);
                    }
                }
            }
        });
    }

    private void playService(int i) {
        Intent intent = new Intent(ActivityMusicMenu.this, ServicePlaying.class);
        intent.putExtra("MSG", i);
        startService(intent);
    }

    private class MyAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private List<Mp3Info> mp3Infos;

        public MyAdapter(Context context, List<Mp3Info> mp3Infos) {
            preferences = PreferenceManager.getDefaultSharedPreferences(context);
            this.inflater = LayoutInflater.from(context);
            this.mp3Infos = mp3Infos;
        }

        @Override
        public int getCount() {
            return mp3Infos.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            view = inflater.inflate(R.layout.view_listadapter, null);
            TextView textView = (TextView) view.findViewById(R.id.music_name);
            final ImageButton favorite = (ImageButton) view.findViewById(R.id.favorite_local);
            final ImageButton item_menu = (ImageButton) view.findViewById(R.id.item_menu);
            textView.setText((i + 1) + "." + mp3Infos.get(i).getMp3Name());
            if (preferences.getBoolean("favorite_" + mp3Infos.get(i).getId(), false)) {
                favorite.setImageResource(R.drawable.favorite);
            } else {
                favorite.setImageResource(R.drawable.unfavorite);
            }
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        view.setBackgroundColor(Color.parseColor("#F0F8FA"));
                    }
                    return false;
                }
            });
            item_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    position = i;
                    createMenu(mp3Infos, i);
                }
            });
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ActivityMusicMenu.this, ActivityPlaying.class);
                    intent.putExtra("position", i);
                    intent.putParcelableArrayListExtra("mp3Infos", (ArrayList) mp3Infos);
                    startActivity(intent);
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    position = i;
                    createMenu(mp3Infos, i);
                    return false;
                }
            });
            favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (flag == AppConstant.LOCAL_MUSIC) {
                        if (!preferences.getBoolean("favorite_" + mp3Infos.get(i).getId(), false)) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("favorite_" + mp3Infos.get(i).getId(), true);
                            editor.commit();
                            favorite.setImageResource(R.drawable.favorite);
                        } else {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("favorite_" + mp3Infos.get(i).getId(), false);
                            favorite.setImageResource(R.drawable.unfavorite);
                            editor.commit();
                        }
                    } else if (flag == AppConstant.FAVORITE_MUSIC) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("favorite_" + mp3Infos.get(i).getId(), false);
                        editor.commit();
                        mp3Infos_favorite.remove(i);
                        favoriteAdapter.notifyDataSetChanged();
                        localAdapter.notifyDataSetInvalidated();
                    } else if (flag == AppConstant.CUT_MUSIC) {
                        if (!preferences.getBoolean("favorite_" + mp3Infos.get(i).getId(), false)) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("favorite_" + mp3Infos.get(i).getId(), true);
                            editor.commit();
                            favorite.setImageResource(R.drawable.favorite);
                        } else {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("favorite_" + mp3Infos.get(i).getId(), false);
                            favorite.setImageResource(R.drawable.unfavorite);
                            editor.commit();
                        }
                    }
                }
            });
            return view;
        }
    }

    private void createMenu(final List<Mp3Info> mp3Infos, int i) {
        String[] items = {"播放音乐", "设为铃声", "歌曲信息", "删除歌曲"};
        new AlertDialog.Builder(ActivityMusicMenu.this).setIcon(R.drawable.message_icon)
                .setTitle(mp3Infos.get(i).getMp3Name()).setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch (which) {
                    case 0:
                        Intent intent = new Intent(ActivityMusicMenu.this, ActivityPlaying.class);
                        intent.putExtra("position", position);
                        intent.putParcelableArrayListExtra("mp3Infos", (ArrayList) mp3Infos);
                        startActivity(intent);
                        break;
                    case 1:
                        String[] items = {"设置为来电铃声", "设置为通知铃声", "设置为闹钟铃声"};
                        new AlertDialog.Builder(ActivityMusicMenu.this)
                                .setIcon(R.drawable.ringtone)
                                .setTitle("设为铃声")
                                .setItems(items, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SetVoice setting = new SetVoice();
                                        switch (which) {
                                            case 0:
                                                setting.setMyRingtone(getApplicationContext(),
                                                        mp3Infos.get(position).getMp3Path());
                                                break;
                                            case 1:
                                                setting.setMyNotification(
                                                        getApplicationContext(),
                                                        mp3Infos.get(position).getMp3Path());
                                                break;
                                            case 2:
                                                setting.setMyAlarm(getApplicationContext(),
                                                        mp3Infos.get(position).getMp3Path());
                                                break;
                                        }
                                    }
                                })
                                .setNegativeButton("取消",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog,
                                                                int which) {
                                            }
                                        }).create().show();
                        break;
                    case 2:
                        new AlertDialog.Builder(ActivityMusicMenu.this)
                                .setTitle("歌曲信息")
                                .setMessage(
                                        "歌名：" + mp3Infos.get(position).getTitle()
                                                + "\n歌手："
                                                + mp3Infos.get(position).getSinger()
                                                + "\n专辑："
                                                + mp3Infos.get(position).getAlbum()
                                                + "\n大小:"
                                                + mp3Infos.get(position).getMp3Size()
                                                + "\n时长："
                                                + new FormatTime().formatTime(mp3Infos.get(
                                                position).getMp3Duration()) + "\n路径："
                                                + mp3Infos.get(position).getMp3Path() + "\n")
                                .create().show();
                        break;
                    case 3:
                        File file = new File(mp3Infos.get(position).getMp3Path());
                        file.delete();
                        ActivityMusicMenu.this.getContentResolver().delete(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                MediaStore.Audio.Media._ID + "=" + mp3Infos.get(position).getId(), null);
                        if (flag == AppConstant.CUT_MUSIC) {
                            mp3Infos.remove(position);
                            cutAdapter.notifyDataSetChanged();
                        }
                        break;
                    default:
                        break;
                }
            }
        }).setPositiveButton("取消", null).create().show();

    }

    private class MyPagerAdapter extends PagerAdapter {
        private View view1;
        private View view2;
        private View view3;

        public MyPagerAdapter(View view1, View view2, View view3) {
            this.view1 = view1;
            this.view2 = view2;
            this.view3 = view3;
        }

        @Override
        public void destroyItem(View v, int position, Object obj) {
            if (position == 0)
                ((ViewPager) v).removeView(view1);
            else if (position == 1)
                ((ViewPager) v).removeView(view2);
            else if (position == 2) {
                ((ViewPager) v).removeView(view3);
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (position == 0) {
                ((ViewPager) container).addView(view1);
                return view1;
            } else if (position == 1) {
                ((ViewPager) container).addView(view2);
                return view2;
            } else {
                ((ViewPager) container).addView(view3);
                return view3;
            }
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {

    }

    private class MyPagerOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int position) {
            if (position == 0) {
                flag = AppConstant.LOCAL_MUSIC;
            } else if (position == 1) {
                if (mp3Infos_all != null) {
                    flag = AppConstant.FAVORITE_MUSIC;
                    mp3Infos_favorite = new ArrayList<Mp3Info>();
                    for (int i = 0; i < mp3Infos_all.size(); i++) {
                        if (preferences.getBoolean("favorite_" + mp3Infos_all.get(i).getId(), false))
                            mp3Infos_favorite.add(mp3Infos_all.get(i));
                    }
                    for (int i = 0; i < mp3Infos_cut.size(); i++) {
                        if (preferences.getBoolean("favorite_" + mp3Infos_cut.get(i).getId(), false))
                            mp3Infos_favorite.add(mp3Infos_cut.get(i));
                    }
                    listView_favorite = (ListView) view_favorite.findViewById(R.id.favorite_listView);
                    favoriteAdapter = new MyAdapter(ActivityMusicMenu.this, mp3Infos_favorite);
                    listView_favorite.setAdapter(favoriteAdapter);
                }
            } else if (position == 2) {
                flag = AppConstant.CUT_MUSIC;
            }
        }
    }

    class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(AppConstant.UPDATE_ACTION)) {
                // 获取Intent中的current消息，current代表当前正在播放的歌曲
                songName.setText(intent.getStringExtra("name"));
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addCategory(Intent.CATEGORY_HOME);
            startActivity(i);
        }
        return super.onKeyUp(keyCode, event);
    }
}
