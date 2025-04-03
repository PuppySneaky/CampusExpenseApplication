package com.example.campusexpensemanagerse06304;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class EventActivity extends AppCompatActivity {
    Button btnClickMe, btnOpen, btnSubmit; // property in class - oop java
    EditText editText; // property
    TextView tvText; // property
    CheckBox cbCondition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_demo);
        // find element in layout by ID
        btnClickMe = findViewById(R.id.btnClickMe);
        editText = findViewById(R.id.edtInput);
        btnOpen = findViewById(R.id.btnOpen);
        tvText = findViewById(R.id.tvTextHidden);
        cbCondition = findViewById(R.id.cbAgree);
        btnSubmit = findViewById(R.id.btnSubmit);

        editText.setEnabled(false); // block input
        btnClickMe.setEnabled(false); // block button
        btnSubmit.setEnabled(false);

        cbCondition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    btnSubmit.setEnabled(true);
                } else {
                    btnSubmit.setEnabled(false);
                }
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //tvText.setText(String.valueOf(count));
                tvText.setText(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // gan cho su kien
        btnClickMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // lay noi dung nguoi nguoi dung nhap
                String data = editText.getText().toString().trim();
                Toast.makeText(EventActivity.this,data, Toast.LENGTH_SHORT).show();
            }
        });
        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setEnabled(true);
                btnClickMe.setEnabled(true);
            }
        });
    }
}
