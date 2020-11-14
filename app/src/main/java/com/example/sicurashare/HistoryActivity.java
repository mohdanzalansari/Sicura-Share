package com.example.sicurashare;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    Button clrAllButton;
    ListView historyData;
    ArrayAdapter arrayAdapter;
    ArrayList<String> list;

    DatabaseHelper mdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        clrAllButton=findViewById(R.id.clr_btn);
        historyData=findViewById(R.id.hist_data);

        mdb= new DatabaseHelper(this);
        list = new ArrayList<String>();
        Cursor cursor=mdb.showData();

        if(cursor.getCount()==0)
        {
            Toast.makeText(HistoryActivity.this, "No Data", Toast.LENGTH_LONG).show();
        }
        else
        {
            while(cursor.moveToNext())
            {
                list.add(cursor.getString(1)+"\n"+cursor.getString(2)+"\n");

            }
            arrayAdapter =new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
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

        Toast.makeText(HistoryActivity.this,"All History Deleted",Toast.LENGTH_LONG).show();
        list.clear();

        arrayAdapter =new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
        historyData.setAdapter(arrayAdapter);
    }
}
