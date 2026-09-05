package com.tomato_kozou.babyinvader;

import android.widget.ImageView;

public class DroidCharacter {
    private ImageView imageView;
    private float droidX; // 左右の移動

    public DroidCharacter(ImageView imageView) {
        this.imageView = imageView;
        this.droidX = 0; // 初期位置を0(中央)にリセット
    }

    public void move(float sensorX, int screenWidth) {
        // ログに出力
        L.d("IN");
        // 前後の移動は不可能にする（sensorYを無視する）
        droidX -= sensorX * 10; // 傾きの感度を5に設定

        // ログに出力
        L.d("sensorX: " + sensorX);
        L.d("screenWidth: " + screenWidth);
        L.d("X軸移動: " + droidX);

        if (imageView.getWidth() == 0) {
            return;
        }

        // 画面からはみ出さないように制限
        float halfWidth = imageView.getWidth() / 2f;
        float limitX = (screenWidth / 2f -halfWidth);

        if (droidX < -limitX) {
            droidX = -limitX;
        } else if (droidX > limitX) {
            droidX = limitX;
        }

        imageView.setTranslationX(droidX);

        // ログに出力
        L.d("X軸移動: " + droidX);
        L.d("OUT");
    }

    // 当たり判定のために外部からImageViewを参照するためのメソッド
    public ImageView getImageView() {
        return this.imageView;
    }

    // 現在の座標を取得するメソッド
    public float getDroidX() {
        return droidX;
    }
}
