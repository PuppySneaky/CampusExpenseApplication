package com.example.campusexpensemanagerse06304;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class FunnyGameActivity extends AppCompatActivity {
    Button btnAnswer;
    RadioGroup radGrAnswer;
    RadioButton radCat, radTiger, radDog, radMonkey;
    int count = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_funny_game);
        btnAnswer = findViewById(R.id.btnAnswer);
        radGrAnswer = findViewById(R.id.radGpAnswer);
        radCat = findViewById(R.id.radCat);
        radDog = findViewById(R.id.radDog);
        radTiger = findViewById(R.id.radTiger);
        radMonkey = findViewById(R.id.radMonkey);

        // khi be bam nut tra loi
        btnAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // kiem tra cac dap an ma be da chon la gi ?
                int selectedId = radGrAnswer.getCheckedRadioButtonId();
                RadioButton radAnswer = (RadioButton) findViewById(selectedId);
                if (radAnswer == null){
                    Toast.makeText(FunnyGameActivity.this, "Vui lòng chọn đáp án", Toast.LENGTH_SHORT).show();
                    return;
                }
                String answer = radAnswer.getText().toString().trim().toLowerCase();

                // kiem tra dap an be chon dung hay sai?
                if (answer.equalsIgnoreCase("cat")){
                    Toast.makeText(FunnyGameActivity.this, "Đáp án chính xác, chúc mừng bé !", Toast.LENGTH_SHORT).show();
                } else {
                    count += 1;
                    if (count > 2){
                        Toast.makeText(FunnyGameActivity.this, "Be sai qua 2 lan roi !", Toast.LENGTH_SHORT).show();
                        btnAnswer.setEnabled(false);
                        return;
                    }
                    Toast.makeText(FunnyGameActivity.this, "Sai mất rồi, Bé hãy chọn lại !", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
