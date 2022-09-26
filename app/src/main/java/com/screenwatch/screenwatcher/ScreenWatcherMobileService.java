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
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Date;
import java.util.List;

public class ScreenWatcherMobileService extends Service {
    public ScreenWatcherMobileService() {
        }

    //Звук изменения состояния десктопа
    private MediaPlayer notificationSound;

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    private String IpAdress;// = "http://62.109.29.127:80/api/ToMobile/";
    private List<Desktop> desktopList;
    private DesktopIdList desktopIdsList;
    // private Notification notification;
    private  Intent ForedgroundServiceIntent;
    String alarmChannelId = "mySoundChannel";
    String startStopChannelId = "serviceChannel";
    String alarmChannelName = "Предупреждения об отключении десктопов";
    String startStopChannelNmae = "Уведомления о запуске/остановке службы слежения";
    Thread thread = null;
    @Override
    public void onCreate()
    {
        notificationSound = MediaPlayer.create(this,R.raw.uvedomlenie_0_db);

        //Log.e("err", "onCreate");
        super.onCreate();

    }

    //Старт службы
    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        ForedgroundServiceIntent = intent;
        IpAdress = intent.getStringExtra("ipAddress");
        String IdsList = intent.getStringExtra("idsList");


        desktopIdsList = new DesktopIdList(IdsList);
        FileLog.d("IpAddress: "+ IpAdress);
        if(IdsList == null) {
            FileLog.d("idsList is null");
        }
        else {
            FileLog.d("idsList:"+ IdsList);
        }
        Toast.makeText((this), "Слежение запущено", Toast.LENGTH_SHORT).show();
        //Создать уведомление
        Notification notification = CreateNotification("", true);

        //Поместить службу на передний план
        startForeground(1, notification);
        //do heavy work on a background thread
        //stopSelf();
        LoopingGetData();
        //при релизе нужно возвращать  START_STICKY
        return START_STICKY;
    }

    private Notification CreateNotification(String NotificationText, boolean isStartStop)
    {
        FileLog.d("method start");
        try {
            String input = ForedgroundServiceIntent.getStringExtra("inputExtra");
            Intent notificationIntent = new Intent(this, MainActivity.class);
            //PendingIntent pendingIntent = PendingIntent.getService(this,
            //        0,notificationIntent,
            //         PendingIntent.FLAG_CANCEL_CURRENT);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);
            String ChannelId;
            if (isStartStop) {
                ChannelId = getChannelId(startStopChannelId, startStopChannelNmae, "mainGroup", "Все уведомления");
            } else {
                ChannelId = getChannelId(alarmChannelId, alarmChannelName, "mainGroup", "Все уведомления");
            }
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

            FileLog.d("method finish");
            return new NotificationCompat.Builder(this, ChannelId)
                    .setContentTitle("LedBell работает")
                    .setContentText(NotificationText)
                    .setSound(null)
                    .setSmallIcon(R.drawable.small_watcher)
                    .setContentIntent(pendingIntent)
                    .build();
        }
        catch (Exception ex)
        {
            FileLog.d("method finish with exception: "+ex.getMessage());
            return null;
        }

    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onDestroy()
    {
        FileLog.d("method start");
        if(thread!=null)
        {
            thread.interrupt();
        }
    }


    //запуск фоновой задачи на получение данных с сервиса
    private void LoopingGetData()
    {
        FileLog.d("method start");
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                FileLog.d("method start");
                boolean previousError = true;
                while (true) {
                    FileLog.d("Запуск следующей проверки");

                    try {
                        //подождать
                        Thread.sleep(5000);//5 секунд

                        List<Desktop> newDesktopList;
                        FileLog.d("getting Data from server "+ IpAdress);
                        //взять данные с сервиса
                        String result = Desktop.getContent("http://" + IpAdress + ":80/api/ToMobile/");
                        newDesktopList = Desktop.Parse(result);
                        FileLog.d("Data from server: "+ result);
                        //проверить на изменение данных
                        if (newDesktopList == null) {
                            throw new Exception("Не удалось получить данные с сервера.");

                        }
                        if (ScreenWatcherMobileService.this.desktopList == null) {
                            ScreenWatcherMobileService.this.desktopList = newDesktopList;
                            FileLog.d( "Старый список компьютеров отсутствует");
                        }
                        for (Desktop desktop : newDesktopList) {
                            //Если компа нет в списке слежения
                            if (!desktopIdsList.contains(desktop.getComputerId())) {
                                continue;
                            } else {
                                FileLog.d("обработан desktop: "+ desktop.getComputerId() + " " + desktop.getComputerName());
                                FileLog.d("status: "+ String.valueOf(desktop.getStatus()));

                            }
                            int index = ScreenWatcherMobileService.this.desktopList.indexOf(desktop);
                            if (index == -1) {
                                //Подключен новый десктоп
                                FileLog.d( "Подключен новый десктоп");
                                continue;
                            }

                            Desktop oldDesktop = ScreenWatcherMobileService.this.desktopList.get(index);
                            //Если статус изменился
                            if (oldDesktop.getStatus() != desktop.getStatus()) {
                                //Сообщать только если статус изменился особым образом
                                // if(oldDesktop.getStatus() != desktop.getStatus())
                                {
                                    String notificationText = "Компьютер \"" + desktop.getComputerName() + "\" изменил свой статус на ";
                                    if (desktop.getStatus() == 0) {
                                        notificationText += "\"Нет подключения\"";
                                    }
                                    if (desktop.getStatus() == 1) {
                                        notificationText += "\"Остановлен\"";
                                    }
                                    if (desktop.getStatus() == 2) {
                                        notificationText += "\"Работает\"";
                                    }
                                    if (desktop.getStatus() == 3) {
                                        notificationText += "\"Заблокирован\"";
                                    }
                                    if (desktop.getStatus() == 4) {
                                        notificationText += "\"Разблокирован\"";
                                    }
                                    notificationSound.start();
                                    Notification notification = CreateNotification(notificationText, false);
                                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                    mNotificationManager.notify(2, notification);
                                    FileLog.d("notification:"+ notificationText);
                                }
                            }
                        }
                        ScreenWatcherMobileService.this.desktopList = newDesktopList;
                        previousError = false;
                    } catch (InterruptedException e) {
                        //Произошла внешняя остановка сервиса
                        FileLog.d("service interrupted");
                        return;
                    } catch (Exception ex) {
                        //прочие ошибки
                        if (!previousError) {
                            notificationSound.start();
                            Notification notification = CreateNotification("Ошибка получения данных с сервера. Проверьте интернет соединение.", false);
                            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            mNotificationManager.notify(3, notification);
                            previousError = true;
                            FileLog.d("Ошибка получения данных с сервера: " + ex.getMessage());
                        }
                    }
                }

            }
        });
        thread.start();
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
               // Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                AudioAttributes.Builder audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM);
                //notificationChannel.setSound(soundUri, audioAttributes.build());
                notificationChannel.setSound(null,null);
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

}