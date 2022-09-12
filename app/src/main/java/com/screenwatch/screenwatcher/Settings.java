package com.screenwatch.screenwatcher;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.screenwatch.screenwatcher.databinding.SettingsBinding;

public class Settings extends Fragment {

    private SettingsBinding binding;
    private EditText edit;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = SettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        edit = view.findViewById(R.id.editServerIpAdress);
        edit.setText(((MainActivity)getActivity()).getIpAddress());

        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).saveIpAdress(edit.getText().toString());

                NavHostFragment.findNavController(Settings.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });
        //запустить сервис слежения
        //Сообщение о запуске службы будет показано самой службой
        binding.buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent serviceIntent = new Intent(((MainActivity)getActivity()), ScreenWatcherMobileService.class);
                serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
                ContextCompat.startForegroundService(((MainActivity)getActivity()),serviceIntent);
            }
        });

        //остновка службы
        binding.buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent serviceIntent = new Intent(((MainActivity)getActivity()), ScreenWatcherMobileService.class);
                getActivity().stopService(serviceIntent);
                Toast.makeText(getContext(), "Слежение остановлено", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onStartService(View v)
    {

    }

    public void onStopService(View v)
    {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}