package com.example.catmap;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView search_results;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        search_results = (ListView) findViewById(R.id.search_results);

        ArrayList<String> arrayResults = new ArrayList<>();
        arrayResults.addAll(Arrays.asList(getResources().getStringArray(R.array.results)));

        adapter = new ArrayAdapter<String>(
                MainActivity.this,
                android.R.layout.simple_list_item_1,
                arrayResults
        );

        search_results.setAdapter(adapter);
    } // end function
} // end MainActivity
