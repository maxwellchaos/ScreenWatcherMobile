package com.screenwatch.screenwatcher;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Desktop extends Object {

    //Перерчисление статусов
    public enum Statuses {
        NotConnected,// = 0
        Stoped,// = 1
        Running,// = 2
        Blocked,// = 3
        Unblocked // = 4
    }

    /// <summary>
    /// Псевдоним компьютера, введенный пользователем в веб-интерфейсе
    /// </summary>
    private String alias;

    /// <summary>
    /// Название компьютера, указанное в десктопной программе
    /// </summary>
    private String computerName;

    /// <summary>
    /// идентификатор компьютера в системе
    /// этот идентификатор генерит десктопная программа
    /// </summary>
    private String computerId;


    /// <summary>
    /// Запущено ли сейчас видео на компьютере.
    /// </summary>
    private int status;


    /// <summary>
    /// Дата последнего изменения статуса
    /// </summary>
    private Date lastChangeStatusDate;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    public String getComputerId() {
        return computerId;
    }

    public void setComputerId(String computerId) {
        this.computerId = computerId;
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getLastChangeStatusDate() {
        return lastChangeStatusDate;
    }

    public void setLastChangeStatusDate(Date lastChangeStatusDate) {
        this.lastChangeStatusDate = lastChangeStatusDate;
    }

    //Перегргка метода equals чтобы объекты были равны только по их идентификаторам
    @Override
    public boolean equals(Object obj){
        if(obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        if (((Desktop) obj).computerId.equals(this.computerId))
            return true;
        return false;

    }
    public static List<Desktop> Parse(String content)
    {
        List<Desktop> dataItems;
        FileLog.d("method start with content:"+content);
        try {
            Gson gson = new Gson();
            Type listOfComputers = new TypeToken<ArrayList<Desktop>>() {
            }.getType();
            dataItems = gson.fromJson(content, listOfComputers);
            FileLog.d("method finish, Returned list count:"+String.valueOf( dataItems.size()));
            return dataItems;
        }catch (Exception ex) {
            FileLog.d("method finish with exception:"+ex.getMessage());
        }
        return null;
    }

    //Взять данные с сервера по http
    @NonNull
    public static String getContent(String path) throws IOException {
        FileLog.d("method start with path:" + path);
        BufferedReader reader = null;
        InputStream stream = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(path);
            connection = (HttpURLConnection) url.openConnection();
            // Установить метод запроса
            connection.setRequestMethod("GET");
            // Установить время ожидания соединения (мс)
            connection.setConnectTimeout(5000);
            // Установить время ожидания чтения (мс)
            connection.setReadTimeout(5000);
            //connection.connect();
            stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder buf = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                buf.append(line).append("\n");
            }
            FileLog.d("method finish, Returned data:" + (buf.toString()));
            return (buf.toString());
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (stream != null) {
                stream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
            FileLog.d("method finish with exception");
        }
    }

}