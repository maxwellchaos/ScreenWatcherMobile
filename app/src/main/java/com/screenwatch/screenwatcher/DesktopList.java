package com.screenwatch.screenwatcher;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.screenwatch.screenwatcher.databinding.DesktopListBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DesktopList extends Fragment {

    private DesktopListBinding binding;

    //Это интерфейс - список компов на экране
    LinearLayout ListLayout;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = DesktopListBinding.inflate(inflater, container, false);

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //получить список со всеми десктопами
        ListLayout = (LinearLayout) view.findViewById(R.id.ListLayout);

        TextView textItem = new TextView(ListLayout.getContext());
        textItem.setText("Подключаюсь к серверу:"+((MainActivity)getActivity()).getIpAddress());
        ListLayout.addView(textItem);


        //Запуск задачи на получение данных от сервиса
        new  ProgressTask().execute();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    class ProgressTask extends AsyncTask<Void, Integer, Void> {
        String result;
        String connectionError = null;

        @Override
        protected Void doInBackground(Void... unused) {
            FileLog.d("Method start");
            try {
                //result = Desktop.getContent("http://62.109.29.127:80/api/ToMobile/");
                String ipAddress = ((MainActivity)getActivity()).getIpAddress();
                result = Desktop.getContent("http://" + ipAddress + ":80/api/ToMobile/");
                FileLog.d("From Server: "+ result);
            } catch (Exception ex) {
                FileLog.d("From Server Error:" + ex.getMessage());
                connectionError = ex.getMessage();
            }
            FileLog.d("Method finish");
            return (null);
        }

        @Override
        protected void onPostExecute(Void unused) {
            FileLog.d("method start");
            if (connectionError == null) {

                //удалить все компьютеры из списка
                ListLayout.removeAllViews();
                DesktopIdList idList = new DesktopIdList(getContext());
//                idList.clear();

                Calendar cal = Calendar.getInstance();
                TimeZone tz = cal.getTimeZone();

                /* debug: is it local time? */
                FileLog.d("Time zone: "+ tz.getDisplayName());

                /* date formatter in local timezone */
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                sdf.setTimeZone(tz);


                try {
                    //Разбираю полученные данные
                    List<Desktop> dataItems = Desktop.Parse(result);
                    for (Desktop item : dataItems) {
                        if(!idList.contains(item.getComputerId())) {
                            continue;
                        }
                        FileLog.d("getting Data: "+item.getComputerName());
                        //Название компа
                        TextView textItem = new TextView(ListLayout.getContext());
                        textItem.setText(item.getComputerName());
                        ListLayout.addView(textItem);
                        //ID компа
                        TextView idItem = new TextView(ListLayout.getContext());
                        idItem.setText(item.getComputerId());
                        ListLayout.addView(idItem);
                        //Статус компа
                        TextView statusItem = new TextView(ListLayout.getContext());
                        switch (item.getStatus()) {
                            case 0:
                                statusItem.setText("Нет соединения ");
                                statusItem.setBackgroundColor(Color.MAGENTA);
                                break;
                            case 1:
                                statusItem.setText("Остановлен");
                                statusItem.setBackgroundColor(Color.RED);
                                break;
                            case 2:
                                statusItem.setText("Работает");
                                statusItem.setBackgroundColor(Color.GREEN);
                                break;
                            case 3:
                                statusItem.setText("Заблокирован");
                                break;
                            case 4:
                                statusItem.setText("Ожидат данные");
                                break;
                        }
                        //преобразование в нужную временную зону
                        long timestamp = item.getLastChangeStatusDate().getTime();
                        String localTime = sdf.format(new Date(timestamp));

                        statusItem.append(localTime);
                        //statusItem.append(tz.getDisplayName());
                        ListLayout.addView(statusItem);
                    }
                } catch (Exception ex) {
                    TextView statusItem = new TextView(ListLayout.getContext());
                    statusItem.setText("Ошибка загрузки данных:" + ex.getMessage());
                    ListLayout.addView(statusItem, 0);
                    FileLog.d("Method finish with exception:"+ex.getMessage());
                }
            }
            else
            {
                //проблемы с интернетом

            }
        }
    }

}