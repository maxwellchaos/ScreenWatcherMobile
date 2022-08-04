package com.screenwatch.screenwatcher;

import android.graphics.Color;
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

import java.util.List;

public class DesktopList extends Fragment {

    private DesktopListBinding binding;

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
        //Удалить все из него
        ListLayout.removeAllViews();
        try {
            List<Desktop> dataItems = Desktop.Parse(Desktop.getContent("http://62.109.29.127:80/api/ToMobile/"));
            Log.e("err", String.valueOf(dataItems.size()));
            for (Desktop item:dataItems) {
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
                switch (item.getStatus())
                {
                    case 0:
                        statusItem.setText("Нет соединения ");
                        statusItem.setBackgroundColor(Color.MAGENTA);
                        break;
                    case 1:
                        statusItem.setText("Работает");
                        statusItem.setBackgroundColor(Color.GREEN);
                        break;
                    case 2:
                        statusItem.setText("Остановлен");
                        statusItem.setBackgroundColor(Color.RED);
                        break;
                    case 3:
                        statusItem.setText("Заблокирован");
                        break;
                    case 4:
                        statusItem.setText("Ожидат данные");
                        break;
                }
                statusItem.append(item.getLastChangeStatusDate().toString());

                ListLayout.addView(statusItem);
            }
        }
        catch (Exception ex)
        {
            TextView statusItem = new TextView(ListLayout.getContext());
            statusItem.setText("Ошибка загрузки данных:"+ex.getMessage());
            ListLayout.addView(statusItem,0);
        }


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}