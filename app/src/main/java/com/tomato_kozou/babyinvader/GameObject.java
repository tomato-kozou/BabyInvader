package com.tomato_kozou.babyinvader;

import android.widget.ImageView;

public abstract class GameObject {
    protected ImageView imageView;
    protected float x, y;

    public GameObject(ImageView imageView) {
        this.imageView = imageView;
    }

    public abstract void move();
}
