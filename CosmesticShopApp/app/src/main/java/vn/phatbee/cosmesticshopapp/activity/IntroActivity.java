package vn.phatbee.cosmesticshopapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.databinding.ActivityIntroBinding;


public class IntroActivity extends AppCompatActivity {

    Button btnStart;
    private ActivityIntroBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        setContentView(R.layout.activity_intro);
        btnStart = (Button) findViewById(R.id.btnIntro);

        btnStart.setOnClickListener(view -> {
            Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}