package com.example.sicurashare.Fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sicurashare.DatabaseHelper;
import com.example.sicurashare.R;

import java.util.ArrayList;

public class HistoryFragment  extends Fragment {

    Button clrAllButton;
    ListView historyData;
    ArrayAdapter arrayAdapter;
    ArrayList<String> list;

    DatabaseHelper mdb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        clrAllButton=view.findViewById(R.id.clr_btn);
        historyData=view.findViewById(R.id.hist_data);

        mdb= new DatabaseHelper(view.getContext());
        list = new ArrayList<String>();
        Cursor cursor=mdb.showData();

        if(cursor.getCount()==0)
        {
            Toast.makeText(view.getContext(), "No Data", Toast.LENGTH_LONG).show();
        }
        else
        {
            while(cursor.moveToNext())
            {
                list.add(cursor.getString(1)+"\n"+cursor.getString(2)+"\n");

            }
            arrayAdapter =new ArrayAdapter<String>(view.getContext(),android.R.layout.simple_list_item_1,list);
            historyData.setAdapter(arrayAdapter);

        }





        clrAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Delete();

            }
        });


    }

    public void Delete()
    {
        mdb.deleteALL();

        Toast.makeText(getView().getContext(),"All History Deleted",Toast.LENGTH_LONG).show();
        list.clear();

        arrayAdapter =new ArrayAdapter<String>(getView().getContext(),android.R.layout.simple_list_item_1,list);
        historyData.setAdapter(arrayAdapter);
    }


    }


