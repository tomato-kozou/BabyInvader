package com.tomato_kozou.babyinvader;

import android.widget.ImageView;
import com.tomato_kozou.babyinvader.GameObject;

public class DroidCharacter extends GameObject {
    private float droidX; // 左右の移動
    private int screenWidth; // 画面の幅
    private float sensorX; // センサーの値


    public DroidCharacter(ImageView imageView, int screenWidth) {
        super(imageView);
        this.droidX = 0; // 初期位置を0(中央)にリセット
        this.screenWidth = screenWidth; // 画面の幅をセット
    }

    public void setSensorX(float sensorX) {
        this.sensorX = sensorX;
    }

    @Override
    public void move() {
        // ログに出力
        L.d("IN");

        // 前後の移動は不可能にする（sensorYを無視する）
        this.droidX -= this.sensorX * 10; // 傾きの感度を設定

        // ログに出力
        L.d("sensorX: " + this.sensorX);
        L.d("screenWidth: " + this.screenWidth);
        L.d("X軸移動(修正前): " + this.droidX);
        L.d("画像のサイズ: " + this.imageView.getWidth());

        if (this.imageView.getWidth() == 0) {
            return;
        }

        // 画面からはみ出さないように制限
        float halfWidth = this.imageView.getWidth() / 2f;
        float limitX = (this.screenWidth / 2f -halfWidth);

        if (this.droidX < -limitX) {
            this.droidX = -limitX;
        } else if (this.droidX > limitX) {
            this.droidX = limitX;
        }

        this.imageView.setTranslationX(this.droidX);

        // ログに出力
        L.d("X軸移動: " + this.droidX);
        L.d("OUT");
    }

    // 当たり判定のために外部からImageViewを参照するためのメソッド
    public ImageView getImageView() {
        return this.imageView;
    }

    // 現在の座標を取得するメソッド
    public float getDroidX() {
        return this.droidX;
    }
}
