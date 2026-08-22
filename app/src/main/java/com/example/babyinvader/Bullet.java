package com.example.babyinvader;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class Bullet {
    public ImageView imageView;
    public float x, y;

    public Bullet(Context context, float startX, float startY) {
        imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.bullet);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(50, 50);
        params.leftMargin = (int) startX;
        params.topMargin = (int) startY;
        imageView.setLayoutParams(params);
    }
}
