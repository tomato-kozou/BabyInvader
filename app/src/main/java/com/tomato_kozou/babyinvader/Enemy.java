package com.tomato_kozou.babyinvader;

import android.media.Image;
import android.widget.ImageView;

public class Enemy {
    public ImageView imageView;
    public float y;

    public Enemy(ImageView imageView) {
        this.imageView = imageView;
        this.y = -100; // 初期位置を設定
    }

    public void move (float speed) {
        this.y += speed;
        this.imageView.setTranslationY(this.y);
    }
}
