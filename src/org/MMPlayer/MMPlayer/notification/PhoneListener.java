package org.MMPlayer.MMPlayer.notification;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import org.MMPlayer.MMPlayer.activity.ActivityPlaying;
import org.MMPlayer.MMPlayer.service.ServicePlaying;
import org.MMPlayer.MMPlayer.utils.AppConstant;

/**
 * Created by sudongsheng on 2014/4/24 0024.
 */
public class PhoneListener extends PhoneStateListener{
    public Context context;
    public static Boolean flag=false;

    public PhoneListener(Context context){
        this.context=context;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        //注意，方法必须写在super方法后面，否则incomingNumber无法获取到值。
        switch(state){
            case TelephonyManager.CALL_STATE_IDLE:
                Log.i("TAG", "挂机");
                if(ActivityPlaying.isPlaying==false&&flag==true) {
                    flag=false;
                    ActivityPlaying.isPlaying =true;
                    Intent intent = new Intent(context, ServicePlaying.class);
                    intent.putExtra("MSG", AppConstant.MEDIA_PAUSE);
                    context.startService(intent);
                }
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                Log.i("TAG", "接听");
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                //输出来电号码
                Log.i("TAG", "响铃:来电号码"+incomingNumber);
                flag=true;
                if(ActivityPlaying.isPlaying==true) {
                    ActivityPlaying.isPlaying=false;
                    Intent intent = new Intent(context, ServicePlaying.class);
                    intent.putExtra("MSG", AppConstant.MEDIA_PAUSE);
                    context.startService(intent);
                }
                break;
        }
    }
}
