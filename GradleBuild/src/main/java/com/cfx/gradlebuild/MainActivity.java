package com.cfx.gradlebuild;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        drawBadge();
    }

    private void drawBadge() {
        View view = new View(this);
        view.setBackgroundColor(Color.GREEN);
        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        decorView.addView(view, 200, 200);
    }
}