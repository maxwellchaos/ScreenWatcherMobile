package com.screenwatch.screenwatcher;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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
        //Button buttonStart = getActivity().findViewById(R.id.buttonStart);

        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).saveIpAdress(edit.getText().toString());

                NavHostFragment.findNavController(Settings.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });
        binding.buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("out","onStartService finished");
                Intent serviceIntent = new Intent(((MainActivity)getActivity()), ScreenWatcherMobileService.class);
                serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
                ContextCompat.startForegroundService(((MainActivity)getActivity()),serviceIntent);
                Log.e("out","onStartService finished");
            }
        });
        binding.buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent serviceIntent = new Intent(((MainActivity)getActivity()), ScreenWatcherMobileService.class);
                getActivity().stopService(serviceIntent);
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