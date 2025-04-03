package com.example.campusexpensemanagerse06304;

import static android.Manifest.permission.CALL_PHONE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class DemoIntentActivity extends AppCompatActivity {
    EditText edtPhone;
    Button btnPhone;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_intent);
        edtPhone = findViewById(R.id.edtPhoneNumber);
        btnPhone = findViewById(R.id.btnCallPhone);

        btnPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = edtPhone.getText().toString().trim();
                if (TextUtils.isEmpty(phone)){
                    Toast.makeText(DemoIntentActivity.this, "Phone number is not empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intentCall = new Intent(Intent.ACTION_CALL);
                intentCall.setData(Uri.parse("tel:"+phone));
                if (ContextCompat.checkSelfPermission(getApplicationContext(),CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
                    startActivity(intentCall);
                } else {
                    requestPermissions(new String[] {CALL_PHONE}, 1);
                }
            }
        });
    }
}
