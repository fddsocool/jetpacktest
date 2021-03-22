package com.frx.jetpro.ui.publish;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.frx.jetpro.R;
import com.frx.libnavannotation.ActivityDestination;

@ActivityDestination(pageUrl = "main/tabs/publish", needLogin = true)
public class PublishActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
    }
}