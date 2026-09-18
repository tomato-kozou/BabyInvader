package com.tomato_kozou.babyinvader;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;



public class GameView extends FrameLayout {
    private List<Enemy> enemies = new ArrayList<>(); // 敵を管理するリスト
    private DroidCharacter droidChar;
    private int screenWidth, screenHeight;
    private boolean isGameOver = false; // ゲームオーバー状態を管理
    private Handler handler = new Handler();
    private long startTime;
    private TextView scoreText;
    private List<ImageView> bullets = new ArrayList<>();
    private GameListener gameListener;
    private long pausedTime;
    private boolean isPaused = false; // 停止判定フラグ
    private float centerX;
    private float centerY;
    private int enemyCountPerSpawm = 1; // 一度に出現する敵の数
    private long gameStartTime; // ゲーム全体の開始時間

    // コンストラクタ
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    // ゲーム開始時に呼ぶ初期化
    public void initGame(ImageView droidImage, int screenWidth, int screenHeight, TextView scoreText) {
        // ログに出力
        L.d("IN");

        this.droidChar = new DroidCharacter(droidImage, screenWidth);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.scoreText = scoreText; // MainActivityクラスから受け取る
        this.startTime = System.currentTimeMillis(); //時間を記録
        this.pausedTime = 0; // 時間のリセット
        this.gameStartTime = System.currentTimeMillis(); // 全体の開始時間

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
            // ドロイド君クラスへセンサーを登録
            droidChar.setSensorX(sensorX);
            droidChar.move();
        }

        // 敵と弾の当たり判定
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);

            for (int j = 0; j < bullets.size(); j++) {
                ImageView bullet = bullets.get(j);

                if (checkCollision(bullet, enemy.imageView)) {
                    // 当たった敵を削除
                    this.removeView(enemy.imageView);
                    enemies.remove(i);
                    this.removeView(bullet);
                    bullets.remove(j);
                    return;
                }
            }

        }

        // 敵の移動と当たり判定
        for (int i = 0; i < enemies.size(); i++ ) {
            Enemy enemy = enemies.get(i); // enemyオブジェクトを取り出す

            enemy.setSpeed(10);
            enemy.move();

            // 当たり判定（ドロイド君と敵の距離が近いか）
            if (checkCollision(droidChar.getImageView(), enemy.imageView)) {
                // Game Over処理はMainActivityに通知する設計にするのが理想?今回はToastのみ
                isGameOver = true;

                // 司令塔に通知
                if (gameListener != null) {
                    gameListener.onGameOver(survivalTime);
                }
                break;
            }

            // 画面外に出た敵を削除してメモリを節約
            if (enemy.getEnemyPositionY() > screenHeight) {
                isGameOver = true;

                // 司令塔に通知
                if (gameListener != null) {
                    gameListener.onGameOver(survivalTime);
                }
                break;
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

        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(R.drawable.enemy);
        Enemy enemy = new Enemy(imageView);

        // 敵のサイズ設定（100px相当）
        int enemySize = 100;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(enemySize, enemySize);

        // 画面端の敵を倒せるようにドロイド君の半分の幅を追加
        int padding = (int) (droidChar.imageView.getWidth() * 0.5f);

        // 敵を出現させる範囲を計算
        // 0(左端) + padding から、screenWidth - padding(右端) まで
        int range = screenWidth - (padding * 2);

        // 出現位置を横方向でランダムにする
        params.leftMargin = padding + (int) (Math.random() * range);
        params.topMargin = -enemySize; // 画面のすぐ外（上）に配置

        this.addView(imageView, params);
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

        // 1秒から3秒のランダムに設定
        int randomDelay = 1000 + (int)(Math.random() * 2000);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isGameOver) {
                    // 時間経過に応じて敵の数を増やす
                    long elapsedSeconds = (System.currentTimeMillis() - gameStartTime) / 1000;
                    L.d("elapsedSeconds: " + elapsedSeconds);
                    if (elapsedSeconds >= 60) {
                        enemyCountPerSpawm = 3;
                    } else if(elapsedSeconds >= 30) {
                        enemyCountPerSpawm = 2;
                    } else {
                        enemyCountPerSpawm = 1;
                    }

                    for(int i = 0; i < enemyCountPerSpawm; i++) {
                        spawnEnemy();
                    }

                    // 次の敵もランダムな秒数で予約する
                    int nextDelay = 100 + (int)(Math.random() * 2000);
                    handler.postDelayed(this, nextDelay);
                }
            }
        }, randomDelay);
        // ログに出力
        L.d("OUT");
    }

    public void shoot() {
        // ログに出力
        L.d("IN");

        ImageView droidView = droidChar.imageView;
        ImageView bulletImageView = new ImageView(getContext());

        this.centerX = droidView.getX()
                + (droidView.getWidth() / 2f) - 25f; // 25fは弾の幅の半分
        this.centerY = droidView.getY(); // ドロイド君の上部から発射

        bulletImageView.setImageResource(R.drawable.bullet);
        Bullet bullet = new Bullet(bulletImageView, centerX, centerY);

        this.addView(bullet.imageView);
        bullets.add(bulletImageView);

        // ログに出力
        L.d("OUT");
    }

    // MainActivityクラスにゲームオーバーを伝えるインタフェース
    public interface GameListener {
        void onGameOver(float score);
        void onRestartRequested();
    }

    public void setGameListener(GameListener listener) {
        this.gameListener = listener;
    }

    public void restartGame() {
        // ログに出力
        L.d("IN");

        isGameOver = false;
        startTime = System.currentTimeMillis(); // 時間のリセット
        this.gameStartTime = System.currentTimeMillis(); // 全体の開始時間リセット

        // 敵を全部消す
        for (Enemy enemy : enemies) {
            this.removeView(enemy.imageView);
        }
        enemies.clear();

        // センサーの再開をMainActivityクラスへ通知
        if (gameListener != null) {
            gameListener.onRestartRequested();
        }

        this.startEnemySpawing();
        Toast.makeText(getContext(), "RESTART!", Toast.LENGTH_SHORT).show();

        // ログに出力
        L.d("OUT");

    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public void stopTimer() {
        isPaused = true;
        handler.removeCallbacksAndMessages(null);
        this.pausedTime = System.currentTimeMillis();
    }

    public void setStartTime() {
        if (!isPaused) {
            return;
        }

        long pauseDuration = System.currentTimeMillis() - this.pausedTime;
        this.startTime += pauseDuration;
        isPaused = false;

        this.startEnemySpawing();

    }

    public void showGameOverDialog(float score, float bestScore) {
        new AlertDialog.Builder(getContext())
                .setTitle("Game Over")
                .setMessage("今回のスコア" + score + "\nベストスコア"
                        + bestScore + "\n再開しますか？")
                .setPositiveButton("はい", (dialog, which) -> {
                    restartGame();
                })
//                .setNegativeButton("いいえ",((dialog, which) -> {
//                  //  ゲームを終了してホーム画面に戻る？ホーム画面を実装する場合はここを使う
//                });
                .setCancelable(false)
                .show();
    }

}
