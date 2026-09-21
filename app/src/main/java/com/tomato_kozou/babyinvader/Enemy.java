package com.tomato_kozou.babyinvader;

import android.media.Image;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.tomato_kozou.babyinvader.GameObject;

public class Enemy extends GameObject {
    private float speed;
    private int hp = 1;
    private boolean isBoss = false;
    private int screenWidth; // 画面の幅
    private int screenHeight; // 画面の高さ
    private int direction = 1; // 移動方向（1: 右、-1: 左）

    public Enemy(ImageView imageView) {
        super(imageView);
        this.y = -100; // 初期位置を設定
        this.speed = 0; // 初期速度を設定
    }

    public float getEnemyPositionY() {
        return this.y;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    public void move () {
        FrameLayout.LayoutParams params =
                (FrameLayout.LayoutParams) this.imageView.getLayoutParams(); // ImageViewのサイズを取得

        if(this.isBoss) {
//            // 左右ゆらゆら移動
//            this.x += (float) (Math.sin(System.currentTimeMillis() / 500.0f) * 10);
            // 横往復
            int bossSpeed = 10;
            this.x += (float) (bossSpeed * this.direction);

            // 画面端で反転
            float maxRight = screenWidth - imageView.getWidth();

            if (this.x < 0) {
                this.x = 0; // 左端で止める
                this.direction = 1; // 右向き
            } else if (this.x > maxRight) {
                this.x = maxRight; // 右端で止める
                this.direction = -1; // 左向き
            }
            params.leftMargin = (int) this.x;

//            // 画面内に収める
//            float limitX = (screenWidth / 2f) - (this.imageView.getWidth() / 2f);
//            if (this.x < -limitX) {
//                this.x = -limitX;
//            } else if (this.x > limitX) {
//                this.x = limitX;
//            }
//
//            this.imageView.setTranslationX(this.x);

//            this.y += speed;
////            float limitY = screenHeight * 0.3f;
//            if (this.y > screenHeight * 0.3f) {
//                speed = -Math.abs(speed); // マイナスにして上に登らせる
//            }
//            if (this.y < -200) {
//                this.y = -200; // 画面外上部で停止
//                speed = Math.abs(speed); // マイナスにして下に登らせる
//            }
//            this.imageView.setTranslationY(this.y);
//            params.topMargin = (int) this.y;
        } else {
            this.y += speed;
//            this.imageView.setTranslationY(this.y);
            params.topMargin = (int) this.y;
        }
        imageView.setLayoutParams(params); // viewを再配置

    }

    public boolean isBoss() {
        return this.isBoss;
    }

    public void setBoss(boolean isBoss) {
        this.isBoss = isBoss;
    }

    public int getHp() {
        return this.hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void takeDamage(int damage) {
        this.hp -= damage;
        if (this.hp <= 0) {
            this.hp = 0;
        }
    }

    public boolean isDead() {
        return this.hp <= 0;
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }
}
