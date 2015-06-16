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
 * A simple sample for use alarmManager.(�x��/�����p�ɾ�)
 * @20150611,
 * onReceive����������...
 * �ggoogle�ӧۤ��� , �N AndroidManifest�̪� <receiver></receiver>���q�h�� <application></application>�̨ç�W ".MyReceiver" -> ".AlarmManagerService$MyReceiver"�N������recevier�F...(why? �]��PendingIntent?
 * �M��N���F...."BroadcastReceiver: can't instantiate class; no empty constructor"...
 * �ggoogle�� , �N public class MyReceiver �אּ public static class MyReceiver , �N�n�F , ���G�O�]���b <application>�̪����Y.
 *    "You need to declare your inner class as static. Otherwise, an inner class is associated with an instance of your outer class."
 */
public class AlarmManagerService extends Service
{
    protected MyReceiver pReceiver;//BroadcastReceiver
    protected String TAG = "" ;
    protected boolean isTestAlarm = false;//���լO�_�@��? //20150616@ it is work!
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
        //�b���U�s������:
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.dygame.broadcast");//��BroadcastReceiver���waction�A�Ϥ��Ω󱵦��Paction���s��
        intentFilter.addAction("com.dygame.unknown") ;
        intentFilter.addAction("com.dygame.alarmmanager") ;
        pReceiver = new MyReceiver();
        registerReceiver(pReceiver, intentFilter);
        //�]�x��
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
        //���P
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
                Toast.makeText(context, "�ӤU�Z�F...", Toast.LENGTH_LONG).show();
                Log.i("MyCrashHandler", "broadcast incoming=" + action);
            }
            Log.i("MyCrashHandler", "broadcast incomingII="+action);
        }
    }
    //�]�x�� 5sec
    public void setAlarmAfter5Second(Context cpntext)
    {
        //�]�w�@�Ӥ���᪺�ɶ�
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 5);
        long setMills = calendar.getTimeInMillis();
        //�s��
        Intent intent = new Intent(this, MyReceiver.class);
        intent.setAction("com.dygame.unknown");
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        //�t�ΪA��
        AlarmManager mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5 * 1000, mPendingIntent);
        //�o�e���Ƽs���A�H��C�j1��������@���C
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, setMills, 1000, mPendingIntent);
    }
    //�]�x�� 18:30 everyday
    public void setAlarmEveryDay(Context context)
    {
        //�b��Ѫ�18:30�I���Ұʾx���o�e�s��
        TimeZone GreenwichMeanTime=TimeZone.getTimeZone("GMT+08:00");//��L�ªv�зǮɶ�
        Calendar mSetCalendar = Calendar.getInstance(GreenwichMeanTime);
        mSetCalendar.setTimeInMillis(System.currentTimeMillis());
        mSetCalendar.set(Calendar.HOUR_OF_DAY, 18);
        mSetCalendar.set(Calendar.MINUTE, 30);
        mSetCalendar.set(Calendar.SECOND, 10);
        mSetCalendar.set(Calendar.MILLISECOND, 0);
        long setMills = mSetCalendar.getTimeInMillis();
        //�s�� ; //PendingIntent.getService(context, 0, intent, 0);
        Intent intent = new Intent(context, MyReceiver.class);
        intent.setAction("com.dygame.alarmmanager");
        intent.putExtra(TAG, "POI~");
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        //�t�ΪA��
        AlarmManager mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //�o�e�@���ʼs���A����ίv���A�U�]�|����
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, setMills, mPendingIntent);
        //�o�e���Ƽs���A�Ĥ@���b18:30����A�H��C�j10�����q���@���C
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
