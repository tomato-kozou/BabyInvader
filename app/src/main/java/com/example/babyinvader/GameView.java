package com.example.babyinvader;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;



public class GameView extends FrameLayout {
    private List<ImageView> enemies = new ArrayList<>(); // 敵を管理するリスト
    private DroidCharacter droidChar;
    private int screenWidth, screenHeight;
    private boolean isGameOver = false; // ゲームオーバー状態を管理
    private Handler handler = new Handler();
    private long startTime;
    private TextView scoreText;
    private List<ImageView> bullets = new ArrayList<>();
    private GameListener gameListener;

    // コンストラクタ
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    // ゲーム開始時に呼ぶ初期化
    public void initGame(ImageView droidImage, int screenWidth, int screenHeight, TextView scoreText) {
        // ログに出力
        L.d("IN");

        this.droidChar = new DroidCharacter(droidImage);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.scoreText = scoreText; // MainActivityクラスから受け取る
        this.startTime = System.currentTimeMillis(); //時間を記録

        // 敵を呼び出す
        this.startEnemySpawing();
        // ログに出力
        L.d("OUT");
    }

    public void update(float sensorX) {
        // ログに出力
        L.d("IN");

        if (isGameOver) {
            return;
        }

        // タイマー更新
        long currentTime = System.currentTimeMillis();
        float survivalTime = (currentTime - startTime) / 1000.0f;
        scoreText.setText(String.format("%.2fs", survivalTime));

        // ドロイド君の移動
        if (droidChar != null) {
            droidChar.move(sensorX, screenWidth);
        }

        // 敵の移動と当たり判定
        for (int i = 0; i < enemies.size(); i++ ) {
            ImageView enemy = enemies.get(i);
            float enemyY = enemy.getTranslationY() + 10; // 敵を下に10px進める
            enemy.setTranslationY(enemyY);

            // 当たり判定（ドロイド君と敵の距離が近いか）
            if (checkCollision(droidChar.getImageView(), enemy)) {
                // Game Over処理はMainActivityに通知する設計にするのが理想?今回はToastのみ
                isGameOver = true;

                // 司令塔に通知
                if (gameListener != null) {
                    gameListener.onGameOver(survivalTime);
                }
                break;
            }

            // 画面外に出た敵を削除してメモリを節約
            if (enemy.getTranslationY() > screenHeight) {
                this.removeView(enemy);
                enemies.remove(i);
                i--;
            }
        }

        // 弾の移動処理
        for (int i = 0; i < bullets.size(); i++) {
            ImageView b = bullets.get(i);
            b.setTranslationY(b.getTranslationY() - 20);

            // 画面外で消去
            if (b.getY() < -50) {
                removeView(b);
                bullets.remove(i);
                i--;
            }
        }

        // ログに出力
        L.d("OUT");

    }

    public void spawnEnemy() {
        // ログに出力
        L.d("IN");

        ImageView enemy = new ImageView(getContext());
        enemy.setImageResource(R.drawable.enemy);

        // 敵のサイズ設定（100px相当）
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100, 100);

        // 出現位置を横方向でランダムにする
        params.leftMargin = (int) (Math.random() * (screenWidth - 100));
        params.topMargin = -100; // 画面のすぐ外（上）に配置

        this.addView(enemy, params);
        enemies.add(enemy);

        // ログに出力
        L.d("OUT");
    }

    // 当たり判定のメソッド
    private boolean checkCollision(View v1, View v2) {
        // ログに出力
        L.d("IN");

        int[] loc1 = new int[2];
        v1.getLocationOnScreen(loc1);
        int[] loc2 = new int[2];
        v2.getLocationOnScreen(loc2);

        // 簡易的な円判定（中心点同士の距離で判定）
        float d1x = loc1[0] + v1.getWidth() / 2f;
        float d1y = loc1[1] + v1.getHeight() / 2f;
        float d2x = loc2[0] + v2.getWidth() / 2f;
        float d2y = loc2[1] + v2.getHeight() / 2f;
        double distance = Math.sqrt(Math.pow(d1x - d2x, 2) + Math.pow(d1y - d2y, 2));
        // ログに出力
        L.d("OUT");

        return distance < (v1.getWidth() / 2f + v2.getWidth() / 2f) * 0.8; // 少し甘めに判定
    }

    // 敵を出現させる
    private void startEnemySpawing() {
        // ログに出力
        L.d("IN");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isGameOver) {
                    spawnEnemy();
                    // 2秒ごとに自分自身を呼び出す（ループ）
                    handler.postDelayed(this, 2000);
                }
            }
        }, 2000);
        // ログに出力
        L.d("OUT");
    }

    public void shoot() {
        // ログに出力
        L.d("IN");

        ImageView bullet = new ImageView(getContext());
        bullet.setImageResource(R.drawable.bullet);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(50, 50);

        // ドロイド君の位置から発射
        params.leftMargin = (int) (droidChar.getImageView().getX() + 140);
        params.topMargin = (int) droidChar.getImageView().getY();

        this.addView(bullet, params);
        bullets.add(bullet);

        // ログに出力
        L.d("OUT");
    }

    // MainActivityクラスにゲームオーバーを伝えるインタフェース
    public interface GameListener {
        void onGameOver(float score);
    }

    public void setGameListener(GameListener listener) {
        this.gameListener = listener;
    }

}
