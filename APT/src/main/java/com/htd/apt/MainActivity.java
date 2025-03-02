package com.htd.apt;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-03-01 17:20
 **/
public class MainActivity extends AppCompatActivity {
    // @BindView(R.id.textView)
    TextView mTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }
}
