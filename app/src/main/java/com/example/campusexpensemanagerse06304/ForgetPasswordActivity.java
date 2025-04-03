package com.example.campusexpensemanagerse06304;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campusexpensemanagerse06304.database.UserDb;

public class ForgetPasswordActivity extends AppCompatActivity {
    Button btnConfirm, btnCancel;
    EditText edtAccount,edtEmail;
    UserDb userDb;
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        userDb = new UserDb(ForgetPasswordActivity.this);
        btnConfirm =findViewById(R.id.btnConfirmAccount);
        edtAccount =findViewById(R.id.edtAccount);
        edtEmail =findViewById(R.id.edtEmail);
        btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgetPasswordActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = edtAccount.getText().toString().trim();
                if(TextUtils.isEmpty(account)){
                    edtAccount.setError("Account can not empty");
                    return;
                }
                String email = edtEmail.getText().toString().trim();
                if (TextUtils.isEmpty(email)){
                    edtEmail.setError("Email can not empty");
                    return;

                }
                boolean checking = userDb.checkExistsUsername(account,email);
                if(checking){
                    Intent intent = new Intent(ForgetPasswordActivity.this,UpdatePasswordActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("ACCOUNT_FORGET_PW",account);
                    bundle.putString("EMAIL_FORGET_PW",email);
                    intent.putExtras(bundle);
                    startActivity(intent);

                }else {
                    Toast.makeText(ForgetPasswordActivity.this, "Account Invalid", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
