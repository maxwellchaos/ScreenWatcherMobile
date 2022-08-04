package com.screenwatch.screenwatcher;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.List;

public class ScreenWatcherMobileService extends Service {
    public ScreenWatcherMobileService() {
    }

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    private String IpAdress = "http://62.109.29.127:80/api/ToMobile/";
    private List<Desktop> desktopList;
    // private Notification notification;
    private  Intent ForedgroundServiceIntent;
    String alarmChannelId = "alarmChannel";
    String startStopChannelId = "serviceChannel";
    String alarmChannelName = "Предупреждения об отключении десктопов";
    String startStopChannelNmae = "Уведомления о запуске/остановке службы слежения";

    @Override
    public void onCreate()
    {
        //Log.e("err", "onCreate");
        super.onCreate();

    }

    //Старт службы
    @Override
    public int onStartCommand(Intent intent,int flags, int startId)
    {
        ForedgroundServiceIntent = intent;
        //Создать уведомление
        Notification notification = CreateNotification("",true);

        //Поместить службу на передний план
        startForeground(1, notification);
        //do heavy work on a background thread
        //stopSelf();
        LoopingGetData();
        //при релизе нужно возвращать  START_STICKY
        return START_NOT_STICKY;
    }

    private Notification CreateNotification(String NotificationText, boolean isStartStop)
    {
        String input = ForedgroundServiceIntent.getStringExtra("inputExtra");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        //PendingIntent pendingIntent = PendingIntent.getService(this,
        //        0,notificationIntent,
        //         PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        String ChannelId;
        if(isStartStop) {
            ChannelId = getChannelId(startStopChannelId, startStopChannelNmae, "mainGroup", "Все уведомления");
        }
        else
        {
            ChannelId = getChannelId(alarmChannelId, alarmChannelName, "mainGroup", "Все уведомления");
        }
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);


        return  new NotificationCompat.Builder(this,ChannelId)
                .setContentTitle("ScreenWatcher работает")
                .setContentText(NotificationText)
                .setSound(null)
                .setSmallIcon(R.drawable.small_watcher)
                .setContentIntent(pendingIntent)
                .build();
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    private String getChannelId(String channelId,String name, String groupId, String groupName)
    {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(nm != null)
        {
            List<NotificationChannel> channels = nm.getNotificationChannels();
            for(NotificationChannel channel: channels)
            {
                if(channel.getId().equals(channelId)){
                    return  channel.getId();
                }
            }
            String group = getGroupId(groupId,groupName);
            NotificationChannel notificationChannel = new NotificationChannel(channelId,name,
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableLights(true);
            //Звук
            if(channelId == alarmChannelId) {
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                AudioAttributes.Builder audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM);
                notificationChannel.setSound(soundUri, audioAttributes.build());
            }
            else
            {
                notificationChannel.setSound(null,null);
            }
            notificationChannel.setGroup(group);
            nm.createNotificationChannel(notificationChannel);
            return channelId;
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getGroupId(String groupId, String name)
    {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(nm != null)
        {
            List<NotificationChannelGroup> groups = nm.getNotificationChannelGroups();
            for(NotificationChannelGroup group: groups)
            {
                if(group.getId().equals(groupId)){
                    return  group.getId();
                }
            }
            nm.createNotificationChannelGroup(new NotificationChannelGroup(groupId,name));
            return  groupId;
        }
        return null;
    }

    //запуск фоновой задачи на получение данных с сервиса
    private void LoopingGetData()
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true)
                {

                    try {
                        //подождать
                        Thread.sleep(3000);//30 секунд

                        //проверить подключение к интернету


                        List<Desktop> newDesktopList;
                        //взять данные с сервиса
                        newDesktopList = Desktop.Parse( Desktop.getContent(IpAdress));
                        //проверить на изменение данных
                        if(newDesktopList == null)
                        {
                            throw  new Exception("Не удалось получить данные с сервера.");
                        }
                        if(ScreenWatcherMobileService.this.desktopList == null)
                        {
                            ScreenWatcherMobileService.this.desktopList = newDesktopList;
                            Log.d("MyService","Старый список компьютеров отсутствует");
                        }
                        for (Desktop desktop:newDesktopList) {
                            int index = ScreenWatcherMobileService.this.desktopList.indexOf(desktop);
                            if(index == -1)
                            {
                                //Подключен новый десктоп
                                Log.d("MyService","Подключен новый десктоп");
                                continue;

                            }

                            Desktop oldDesktop = ScreenWatcherMobileService.this.desktopList.get(index);
                            //Если статус изменился
                            if(oldDesktop.getStatus() != desktop.getStatus())
                            {
                                //Сообщать только если статус изменился особым образом
                                if(oldDesktop.getStatus() == 2 && desktop.getStatus() !=2)
                                {
                                    String notificationText = "Компьютер \""+desktop.getComputerName()+"\" изменил свой статус на ";
                                    if(desktop.getStatus() == 0)
                                    {
                                        notificationText += "\"Нет подключения\"";
                                    }
                                    if(desktop.getStatus() == 1)
                                    {
                                        notificationText += "\"Остановлен\"";
                                    }
                                    if(desktop.getStatus() == 3)
                                    {
                                        notificationText += "\"Заблокирован\"";
                                    }
                                    if(desktop.getStatus() == 4)
                                    {
                                        notificationText += "\"Разблокирован\"";
                                    }
                                    Notification notification = CreateNotification(notificationText,false);
                                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                    mNotificationManager.notify(2, notification);
                                }
                            }
                        }
                        ScreenWatcherMobileService.this.desktopList = newDesktopList;
                    }
                    catch (Exception ex)
                    {
                        Log.e("MyService","Ошибка получения данных с сервера: "+ ex.getMessage());
                    }
                }
            }
        });
        thread.start();
    }
}