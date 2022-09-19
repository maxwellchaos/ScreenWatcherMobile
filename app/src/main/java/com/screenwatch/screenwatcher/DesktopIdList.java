package com.screenwatch.screenwatcher;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DesktopIdList implements DesktopIdRepositopy
{
    private List<String> idList;
    private SharedPreferences settings;
    private Context _context;

    public static String clientSettings = "clientSettings";
    public static String idsList = "IdsList";


    public DesktopIdList(Context context) {
        _context = context;

        //Загрузить данные из настроек

        settings = _context.getSharedPreferences(clientSettings,MODE_PRIVATE);

        String data = settings.getString(idsList,null);
        IdListFromString(data);
    }

    public DesktopIdList(String data)
    {
        IdListFromString(data);
    }

    public void IdListFromString(String idLists)
    {
        if(idLists == null) {
            idList = new ArrayList<String>();
        }
        else {
            //Разобрать данные в список
            Gson gson = new Gson();
            Type listOfComputerIds = new TypeToken<ArrayList<String>>() {
            }.getType();
            idList = gson.fromJson(idLists, listOfComputerIds);
        }
    }


    @Override
    public void addDesktopId(String desktopId) {
        if(!contains(desktopId))
        {
            if(idList != null)
            {
                idList.add(desktopId);
            }
        }
        saveIdList();
    }

    @Override
    public void removeDesktopId(String desktopId) {
        if(contains(desktopId))
        {
            idList.remove(desktopId);
        }
        saveIdList();
    }

    @Override
    public boolean contains(String desktopId) {
        if(idList != null)
        {
            return idList.contains(desktopId);
        }
        return false;
    }

    //Сохранить данные из списка в строку, а ее в preferences
    private void saveIdList()
    {
        Gson gson = new Gson();
        String result = gson.toJson(idList);

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(idsList,result);
        editor.apply();
    }
}
