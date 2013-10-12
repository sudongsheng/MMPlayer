package org.MMPlayer.MMPlayer.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import org.MMPlayer.MMPlayer.R;


/**
 * Created with IntelliJ IDEA.
 * User: panlei
 * Date: 13-10-4
 * Time: 下午11:38
 * To change this template use File | Settings | File Templates.
 */
public class ActivityWelcome extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        // 闪屏的核心代码
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ActivityWelcome.this,ActivityMusicMenu.class);  //从启动动画ui跳转到主ui
                startActivity(intent);
                ActivityWelcome.this.finish();    // 结束启动动画界面
            }
        }, 2000);    //启动动画持续2秒钟
    }
}
