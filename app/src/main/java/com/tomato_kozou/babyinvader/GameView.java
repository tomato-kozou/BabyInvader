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
    private List<Bullet> bullets = new ArrayList<>();
    private GameListener gameListener;
    private long pausedTime;
    private boolean isPaused = false; // 停止判定フラグ
    private float centerX;
    private float centerY;
    private int enemyCountPerSpawm = 1; // 一度に出現する敵の数
    private long gameStartTime; // ゲーム全体の開始時間
    private boolean isBossSpawned = false;
    private Enemy boss;
    private Bullet enemyBullet;

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
//        // ログに出力
//        L.d("IN");

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

        if (System.currentTimeMillis() - startTime > 90000 && !isBossSpawned) {
//        if (System.currentTimeMillis() - startTime > 3000 && !isBossSpawned) { // test
            this.spawnBoss();
            this.isBossSpawned = true;
        }

        // 敵と弾の当たり判定
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            enemy.setSpeed(10);
            enemy.move();

            for (int j = 0; j < bullets.size(); j++) {
                Bullet bullet = bullets.get(j);

                if (checkCollision(bullet.imageView, enemy.imageView)) {
                    // ボス判定
                    if (enemy.isBoss()) {
                        enemy.takeDamage(1);
                        if (enemy.isDead()) {
                            this.removeView(enemy.imageView);
                            enemies.remove(i);
                            // ゲームクリア、スコアタイム表示(MainActivityクラスにゲームクリアを伝える)
                            if (gameListener != null) {
                                float finalScore = (System.currentTimeMillis() - gameStartTime) / 1000.0f;
                                this.isBossSpawned = false;
                                this.isGameOver = true;
                                gameListener.onGameClear(finalScore);
                            }
                            return;
                        }
                    } else {
                        // 当たった敵を削除
                        this.removeView(enemy.imageView);
                        enemies.remove(i);
                        i--;
                    }

                    this.removeView(bullet.imageView);
                    bullets.remove(j);
                    j--;
                }
            }
        }

        // 敵の移動と当たり判定
        for (int i = 0; i < enemies.size(); i++ ) {
            Enemy enemy = enemies.get(i); // enemyオブジェクトを取り出す

            // 当たり判定（ドロイド君と敵の距離が近いか）
            if (checkCollision(droidChar.getImageView(), enemy.imageView)) {
                // Game Over処理はMainActivityに通知する設計にするのが理想?今回はToastのみ
                isGameOver = true;

                // 司令塔に通知
                if (gameListener != null) {
                    this.isBossSpawned = false;
                    gameListener.onGameOver(survivalTime);
                }
            }

            // 画面外に出た敵を削除してメモリを節約
            if (enemy.getEnemyPositionY() > screenHeight) {
                isGameOver = true;
                removeView(enemy.imageView);
                enemies.remove(i);

                // 司令塔に通知
                if (gameListener != null) {
                    this.isBossSpawned = false;
                    gameListener.onGameOver(survivalTime);
                }
                break;
            }
        }

        // 弾の移動処理
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            b.move();

            if (b.getEnemyBullet()) {
                if (checkCollision(b.imageView, droidChar.getImageView())) {
                    isGameOver = true;

                    // 司令塔に通知
                    if (gameListener != null) {
                        this.isBossSpawned = false;
                        gameListener.onGameOver(survivalTime);

                        // 弾を消去
                        removeView(b.imageView);
                        bullets.remove(i);
                        break;
                    }
                }
            }

            // 画面外で消去
            if (b.imageView.getY() < -50 || b.imageView.getX() > screenWidth + 50) {
                removeView(b.imageView);
                bullets.remove(i);
                i--;
            }
        }

//        // ログに出力
//        L.d("OUT");

    }

    public void spawnEnemy() {
//        // ログに出力
//        L.d("IN");

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

//        // ログに出力
//        L.d("OUT");
    }

    // 当たり判定のメソッド
    private boolean checkCollision(View v1, View v2) {
//        // ログに出力
//        L.d("IN");

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
//        // ログに出力
//        L.d("OUT");

        return distance < (v1.getWidth() / 2f + v2.getWidth() / 2f) * 0.8; // 少し甘めに判定
    }

    // 敵を出現させる
    private void startEnemySpawing() {
//        // ログに出力
//        L.d("IN");

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
//        // ログに出力
//        L.d("OUT");
    }

    public void shoot() {
//        // ログに出力
//        L.d("IN");

        ImageView droidView = droidChar.imageView;
        ImageView bulletImageView = new ImageView(getContext());

        this.centerX = droidView.getX()
                + (droidView.getWidth() / 2f) - 25f; // 25fは弾の幅の半分
        this.centerY = droidView.getY(); // ドロイド君の上部から発射

        bulletImageView.setImageResource(R.drawable.bullet);
        Bullet bullet = new Bullet(bulletImageView, centerX, centerY);

        // 弾を判別
        bullet.setEnemyBullet(false);

        this.addView(bullet.imageView);
        bullets.add(bullet);

//        // ログに出力
//        L.d("OUT");
    }

    // MainActivityクラスにゲームオーバーを伝えるインタフェース
    public interface GameListener {
        void onGameOver(float score);
        void onRestartRequested();
        void onGameClear(float score);
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

        // 弾をすべて削除
        for (Bullet bullet : bullets) {
            this.removeView(bullet.imageView);
        }
        bullets.clear();

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
//                .setMessage("今回のスコア" + score + "\nベストスコア"
//                        + bestScore + "\n再開しますか？")
                .setMessage("再開しますか？")
                .setPositiveButton("はい", (dialog, which) -> {
                    restartGame();
                })
//                .setNegativeButton("いいえ",((dialog, which) -> {
//                  //  ゲームを終了してホーム画面に戻る？ホーム画面を実装する場合はここを使う
//                });
                .setCancelable(false)
                .show();
    }

    public void spawnBoss() {
        L.d("IN");
        ImageView bossImageView = new ImageView(getContext());
        bossImageView.setImageResource(R.drawable.boss);

        this.boss = new Enemy(bossImageView);
        boss.setBoss(true); // ボス属性を付与
        boss.setHp(100);
//        boss.setHp(10); // test

        // 敵のサイズ設定（100px相当）
        int enemySize = 200;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(enemySize, enemySize);

        params.leftMargin = 0;
        params.topMargin = enemySize;

        // 画面サイズをEnemyクラスに渡す
        this.boss.setScreenWidth(screenWidth);
        this.boss.setScreenHeight(screenHeight);

        this.addView(bossImageView, params);

        // ボスの初期位置を設定
        boss.x = 0; // 中央
        boss.y = 100; // 画面外上部

        // 初回描画時に位置を反映
        bossImageView.setTranslationX(boss.x);
        bossImageView.setTranslationY(boss.y);

        enemies.add(boss);

        this.startEnemyShooting(boss);
        L.d("OUT");
    }

    private void startEnemyShooting(Enemy boss) {
        L.d("IN");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (enemies.contains(boss)) {
                    // 今のボスの座標を再取得する
                    float currentBossX = boss.imageView.getTranslationX();
                    float currentBossY = boss.imageView.getTranslationY();

                    // 敵の弾を生成
                    ImageView enemyBulletImageView = new ImageView(getContext());
                    enemyBulletImageView.setImageResource(R.drawable.enemy_bullet);

                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(50, 50);
                    params.leftMargin = (int) (currentBossX + 75);
                    params.topMargin = (int) (currentBossY + 200);

                    // 弾の移動処理
                    Bullet enemyBullet = new Bullet(enemyBulletImageView, boss.x, boss.y);
                    // 敵の弾フラグをON
                    enemyBullet.setEnemyBullet(true);

                    // ボス画像の中心位置を計算
                    float bossCenterX = boss.x + boss.imageView.getWidth() / 2f;
                    float bossCenterY = boss.y + boss.imageView.getHeight() / 2f;

                    // 弾の画像の中心位置を補正
                    float bulletStartX = bossCenterX - (enemyBulletImageView.getWidth() / 2f);
                    float bulletStartY = bossCenterY - (enemyBulletImageView.getHeight() / 2f);

                    enemyBulletImageView.setTranslationX(bulletStartX);
                    enemyBulletImageView.setTranslationY(bulletStartY);

                    addView(enemyBulletImageView, params);
                    bullets.add(enemyBullet);

                    // 次の射撃予約
                    handler.postDelayed(this, 3000);

                }
            }
        }, 3000);
        L.d("OUT");
    }

    public void showGameClearDialog(float score, float bestScore) {
        new AlertDialog.Builder(getContext())
                .setTitle("GAME CLEAR")
                .setMessage("ゲームクリア!\nSCORE : " + score + "s\n"
                        + "BEST SCORE : " + bestScore + "s\n再開しますか？")
                .setPositiveButton("OK", (dialog, which) -> {
                    restartGame(); // リスタート処理を呼び出し
                })
                .setCancelable(false)
                .show();
    }

}
