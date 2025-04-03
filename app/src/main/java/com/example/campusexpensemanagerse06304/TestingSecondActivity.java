package com.example.campusexpensemanagerse06304;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class TestingSecondActivity extends AppCompatActivity {
    Button btnGotoFirst;
    TextView tvData;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing_second);
        btnGotoFirst = findViewById(R.id.btnGotoFist);
        tvData = findViewById(R.id.tvData);
        // get data from TestingActivity
        Intent getDataIntent = getIntent();
        Bundle getDataBundle = getDataIntent.getExtras();
        if (getDataBundle != null){
            String data = getDataBundle.getString("DATA_TEST","");
            tvData.setText(data);
        }
        btnGotoFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestingSecondActivity.this, TestingActivity.class);
                startActivity(intent);
            }
        });
    }
}
