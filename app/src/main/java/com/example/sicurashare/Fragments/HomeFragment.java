package com.example.sicurashare.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sicurashare.LobbyActivity;
import com.example.sicurashare.R;
import com.example.sicurashare.login_activity;
import com.google.firebase.auth.FirebaseAuth;

public class HomeFragment extends Fragment {

    Button sendBtn, receiveBtn, logout, historyButton;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sendBtn = view.findViewById(R.id.sendBtnBox);
        receiveBtn =  view.findViewById(R.id.receiveBtnBox);
        logout = view.findViewById(R.id.logout_button);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(view.getContext(), "Logged Out Successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getView().getContext(), login_activity.class));
                getActivity().finish();

            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getView().getContext(), LobbyActivity.class);
                intent.putExtra("user", "sender");
                startActivity(intent);
            }
        });

        receiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getView().getContext(), LobbyActivity.class);
                intent.putExtra("user", "receiver");
                startActivity(intent);
            }
        });




    }
}
