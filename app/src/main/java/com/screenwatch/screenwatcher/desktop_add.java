package com.screenwatch.screenwatcher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.screenwatch.screenwatcher.databinding.FragmentDesktopAddBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link desktop_add#newInstance} factory method to
 * create an instance of this fragment.
 */
public class desktop_add extends Fragment {

    private FragmentDesktopAddBinding binding;
    private EditText edit;

    public desktop_add() {
        // Required empty public constructor
    }


    public static desktop_add newInstance(String param1, String param2) {
        desktop_add fragment = new desktop_add();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDesktopAddBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        edit = view.findViewById(R.id.editServerIpAdress);
        //edit.setText(((MainActivity)getActivity()).getIpAddress());

        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //((MainActivity)getActivity()).saveIpAdress(edit.getText().toString());
                DesktopIdList idList = new DesktopIdList(getActivity());
                idList.addDesktopId(edit.getText().toString());
                ((MainActivity)getActivity()).ServiceStart();

                NavHostFragment.findNavController(desktop_add.this)
                        .navigate(R.id.action_DesktopAdd_to_DesktopList);
            }
        });

    }
}