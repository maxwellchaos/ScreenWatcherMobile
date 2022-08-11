package com.screenwatch.screenwatcher;

public interface DesktopIdRepositopy {

        //Добавить в список
        void addDesktopId(String desktopId);
        //удалить из списка
        void removeDesktopId(String desktopId);
        //проверить, есть ли в списке
        boolean contains(String desktopId);
}

