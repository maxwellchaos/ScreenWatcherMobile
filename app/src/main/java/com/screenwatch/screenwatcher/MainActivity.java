package com.screenwatch.screenwatcher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.screenwatch.screenwatcher.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    public SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = getSharedPreferences("serverSettings",MODE_PRIVATE);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph())
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        ServiceStart();
    }
    public void ServiceStart()
    {
        //Попытка остановить службу
        ServiceStop();

        //Это запуск службы слежения
        Intent serviceIntent = new Intent(this, ScreenWatcherMobileService.class);
        serviceIntent.putExtra("ipAddress", getIpAddress());

        settings = this.getSharedPreferences(DesktopIdList.clientSettings,MODE_PRIVATE);
        String data = settings.getString(DesktopIdList.idsList,null);
        serviceIntent.putExtra("idsList", data);

        ContextCompat.startForegroundService(this,serviceIntent);
    }
    public void ServiceStop()
    {
        try {
            Intent serviceIntent = new Intent(this, ScreenWatcherMobileService.class);
            stopService(serviceIntent);
            Toast.makeText(this, "Слежение остановлено", Toast.LENGTH_SHORT).show();
        }
        catch(Exception e)
        {

        }

    }
    public void saveIpAddress(String serverIpAddress)
    {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("serverIpAddress",serverIpAddress);
        editor.apply();
    }
    public String getIpAddress()
    {
        return settings.getString("serverIpAddress",null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

/*        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
    public void onSettings(MenuItem item)
    {
        //переход к настройкам
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.Settings);
        //Исправляю меню
        //item.setEnabled(false);

    }
    public void onUpdate(MenuItem item)
    {
        //переход к настройкам
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.DesktopList);
    }
    public void onDesktopAdd(MenuItem item)
    {
        //переход к настройкам
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.DesktopAdd);
    }
}