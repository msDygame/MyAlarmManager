package com.dygame.myalarmmanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * A simple sample for use alarmManager.(鬧鐘/全局計時器)
 * @20150611,
 * onReceive完全收不到...
 * 經google照抄之後 , 將 AndroidManifest裡的 <receiver></receiver>片段搬到 <application></application>裡並改名 ".MyReceiver" -> ".AlarmManagerService$MyReceiver"就收的到recevier了...(why? 因為PendingIntent?
 * 然後就當掉了...."BroadcastReceiver: can't instantiate class; no empty constructor"...
 * 經google後 , 將 public class MyReceiver 改為 public static class MyReceiver , 就好了 , 似乎是因為在 <application>裡的關係.
 *    "You need to declare your inner class as static. Otherwise, an inner class is associated with an instance of your outer class."
 */
public class AlarmManagerService extends Service
{
    protected MyReceiver pReceiver;//BroadcastReceiver
    protected String TAG = "" ;
    protected boolean isTestAlarm = false;//測試是否作用? //20150616@ it is work!
    public AlarmManagerService()
    {
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null ;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //Uncaught Exception Handler(Crash Exception)
        MyCrashHandler pCrashHandler = MyCrashHandler.getInstance();
        pCrashHandler.init(getApplicationContext());
        TAG = pCrashHandler.getTag() ;
        //在註冊廣播接收:
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.dygame.broadcast");//為BroadcastReceiver指定action，使之用於接收同action的廣播
        intentFilter.addAction("com.dygame.unknown") ;
        intentFilter.addAction("com.dygame.alarmmanager") ;
        pReceiver = new MyReceiver();
        registerReceiver(pReceiver, intentFilter);
        //設鬧鐘
        if (isTestAlarm == true)
            setAlarmAfter5Second(this) ;
        else
            setAlarmEveryDay(this) ;
        //
        return Service.START_STICKY ;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        stopSelf() ;
        //註銷
        if (pReceiver!=null)
        {
            unregisterReceiver(pReceiver);
            pReceiver=null;
        }
    }

    public static class MyReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if ("com.dygame.broadcast".equals(action))
            {
                Log.i("MyCrashHandler", "broadcast incoming=" + action);
            }
            if ("com.dygame.unknown".equals(action))
            {
                Log.i("MyCrashHandler", "broadcast incoming=" + action);
            }
            if ("com.dygame.alarmmanager".equals(action))
            {
                Toast.makeText(context, "該下班了...", Toast.LENGTH_LONG).show();
                Log.i("MyCrashHandler", "broadcast incoming=" + action);
            }
            Log.i("MyCrashHandler", "broadcast incomingII="+action);
        }
    }
    //設鬧鐘 5sec
    public void setAlarmAfter5Second(Context cpntext)
    {
        //設定一個五秒後的時間
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 5);
        long setMills = calendar.getTimeInMillis();
        //廣播
        Intent intent = new Intent(this, MyReceiver.class);
        intent.setAction("com.dygame.unknown");
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        //系統服務
        AlarmManager mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5 * 1000, mPendingIntent);
        //發送重複廣播，以後每隔1秒鐘執行一次。
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, setMills, 1000, mPendingIntent);
    }
    //設鬧鐘 18:30 everyday
    public void setAlarmEveryDay(Context context)
    {
        //在當天的18:30點鐘啟動鬧鐘發送廣播
        TimeZone GreenwichMeanTime=TimeZone.getTimeZone("GMT+08:00");//格林威治標準時間
        Calendar mSetCalendar = Calendar.getInstance(GreenwichMeanTime);
        mSetCalendar.setTimeInMillis(System.currentTimeMillis());
        mSetCalendar.set(Calendar.HOUR_OF_DAY, 18);
        mSetCalendar.set(Calendar.MINUTE, 30);
        mSetCalendar.set(Calendar.SECOND, 10);
        mSetCalendar.set(Calendar.MILLISECOND, 0);
        long setMills = mSetCalendar.getTimeInMillis();
        //廣播 ; //PendingIntent.getService(context, 0, intent, 0);
        Intent intent = new Intent(context, MyReceiver.class);
        intent.setAction("com.dygame.alarmmanager");
        intent.putExtra(TAG, "POI~");
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        //系統服務
        AlarmManager mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //發送一次性廣播，手機睡眠狀態下也會執行
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, setMills, mPendingIntent);
        //發送重複廣播，第一次在18:30執行，以後每隔10秒鐘通知一次。
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, setMills, 10000, mPendingIntent);
        //DEBUG
        String time = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(setMills) ;
        Log.i(TAG, "AlarmManager set secs=" + time);
        String time2 = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(System.currentTimeMillis()) ;
        Log.i(TAG, "AlarmManager now is=" + time2);
    }
    //set alarm
    public void SetAlarm(Context context)
    {
            AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, MyReceiver.class);
            intent.setAction("com.dygame.alarmmanager");
            intent.putExtra("MyCrashHandler", "POI~");
            PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent , 0);
            am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 1, sender); // Millisec * Second * Minute
    }
    //cancel alarm
    public void cancelAlarm(Context context)
    {
            Intent intent = new Intent(context, MyReceiver.class);
            PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.cancel(sender);
    }
}
