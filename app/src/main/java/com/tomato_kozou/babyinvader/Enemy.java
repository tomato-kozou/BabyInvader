package com.tomato_kozou.babyinvader;

import android.media.Image;
import android.widget.ImageView;
import com.tomato_kozou.babyinvader.GameObject;

public class Enemy extends GameObject {
    private float y;

    private float speed;


    public Enemy(ImageView imageView) {
        super(imageView);
        this.y = -100; // 初期位置を設定
    }

    public float getEnemyPositionY() {
        return this.y;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    public void move () {
        this.y += speed;
        this.imageView.setTranslationY(this.y);
    }
}
