package com.tomato_kozou.babyinvader;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class Bullet extends GameObject {
    private float x;
    private float y;


    public Bullet (ImageView imageView, float startX, float startY) {
        super(imageView);
        L.d("IN");
        this.imageView.setImageResource(R.drawable.bullet);
        this.x = startX;
        this.y = startY;

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(50, 50);
        this.imageView.setLayoutParams(params);
        this.imageView.setTranslationX(this.x);
        this.imageView.setTranslationY(this.y);
        L.d("OUT");
    }

    @Override
    public void move() {
        L.d("IN");
        this.y -= 10;
        this.imageView.setTranslationY(y);
        L.d("OUT");
    }
}
