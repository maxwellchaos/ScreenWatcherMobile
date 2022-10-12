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
    public static String idsList = "IdList";


    public DesktopIdList(Context context) {
        try {
            FileLog.d("ConstructorStart with context");
            _context = context;

            //Загрузить данные из настроек

            settings = _context.getSharedPreferences(clientSettings, MODE_PRIVATE);

            String data = settings.getString(idsList, null);
            IdListFromString(data);
        } catch (Exception ex) {
            FileLog.d("ConstructorFinish with Exception: "+ex.getMessage());
        }
        FileLog.d("ConstructorFinish successful");
    }

    public DesktopIdList(String data)
    {
        FileLog.d("ConstructorStart with data: "+data);
        IdListFromString(data);
        FileLog.d("ConstructorFinish");
    }

    public void IdListFromString(String idLists) {
        FileLog.d("methodStart");
        try {
            if (idLists == null) {
                idList = new ArrayList<String>();
            } else {
                //Разобрать данные в список
                Gson gson = new Gson();
                Type listOfComputerIds = new TypeToken<ArrayList<String>>() {
                }.getType();
                idList = gson.fromJson(idLists, listOfComputerIds);
            }
        } catch (Exception ex) {
            FileLog.d("MethodFinish with Exception: "+ex.getMessage());
        }
        FileLog.d("MethodFinish successful");
    }


    @Override
    public void addDesktopId(String desktopId) {
        FileLog.d("methodStart");
        if(!contains(desktopId))
        {
            if(idList != null)
            {
                idList.add(desktopId);
            }
        }
        saveIdList();
        FileLog.d("methodFinish");
    }

    @Override
    public void removeDesktopId(String desktopId) {
        FileLog.d("methodStart");
        if(contains(desktopId))
        {
            idList.remove(desktopId);
        }
        saveIdList();
        FileLog.d("methodFinish");
    }

    @Override
    public boolean contains(String desktopId) {
        FileLog.d("methodStart");
        if(idList != null)
        {
            FileLog.d("methodFinish");
            return idList.contains(desktopId);
        }
        FileLog.d("methodFinish");
        return false;

    }

    //Сохранить данные из списка в строку, а ее в preferences
    private void saveIdList()
    {
        FileLog.d("methodStart");
        try {
            Gson gson = new Gson();
            String result = gson.toJson(idList);

            SharedPreferences.Editor editor = settings.edit();
            editor.putString(idsList, result);
            editor.apply();
        } catch (Exception ex) {
            FileLog.d("MethodFinish with Exception: "+ex.getMessage());
        }
        FileLog.d("MethodFinish successful");
    }

    @Override
    public void clear() {
        idList.clear();
        saveIdList();
    }
}
