package com.example.babyinvader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.babyinvader.R;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private GameView gameView;
    private MediaPlayer bgmPlayer, sePlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // GameViewとキャラの紐づけ
        gameView = findViewById(R.id.game_view);
        ImageView droidImage = findViewById(R.id.droid_image);
        TextView scoreText = findViewById(R.id.score_text);

        // ボタンの紐づけ
        Button shootButton = findViewById(R.id.btn_shoot);
        shootButton.setOnClickListener(v -> {
            // 司令塔であるMainActivityからGameViewへ「撃て！」と命令を出す
            gameView.shoot();
        });

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        gameView.initGame(droidImage, screenWidth, screenHeight, scoreText);

        // ゲームオーバーの通知を受け取る
        gameView.setGameListener(score -> {
            triggerGameOver(score);
        });

        // BGMの準備
        bgmPlayer = MediaPlayer.create(this, R.raw.bgm);
        bgmPlayer.setLooping(true);

        // 効果音の設定
        sePlayer = MediaPlayer.create(this, R.raw.explosion);

        // センサーマネージャの取得
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // 加速度センサーのして
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }

    // onResumeで登録
    @Override
    protected void onResume() {
        super.onResume();
        // ★ライフサイクル確認用のログ
        Log.d("LIFECYCLE", "onResume: センサーを開始しました（画面が最前面に出ました）");

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        // 画面に戻ったらBGMを再開
        if (bgmPlayer != null) {
            bgmPlayer.start();
        }
    }

    // onPauseで解除
    @Override
    protected void onPause() {
        super.onPause();
        // ★ライフサイクル確認用のログ
        Log.d("LIFECYCLE", "onPause: センサーを停止しました（画面が隠れました）");

        sensorManager.unregisterListener(this);

        // 画面が隠れたらBGMを一時停止（バッテリーとメモリの節約）
        if (bgmPlayer != null && bgmPlayer.isPlaying()) {
            bgmPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // リソースの開放
        if (bgmPlayer != null) {
            bgmPlayer.release();
            bgmPlayer = null;
        }
        if (sePlayer != null) {
            sePlayer.release();
            sePlayer = null;
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onSensorChanged(SensorEvent event) {
        // 司令塔の仕事：センサーの値をそのままGameViewに流す
        gameView.update(event.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 今回は使わないが、インターフェースの実装上必要
    }

    // ゲームオーバー処理
    private void triggerGameOver(float score) {
        // ログに出力
        L.d("IN");

        // ハイスコアの保存と読込
        this.saveHighScore(score);
        float bestScore = this.getBestScore();

        Toast.makeText(this, "GAME OVER! Score: "
                + score + "s" + "\nBEST SCORE: " + bestScore + "s", Toast.LENGTH_LONG).show();

        // ゲームオーバー音を再生
        if(sePlayer != null) {
            sePlayer.start();
        }
        // BGMを停止
        if(bgmPlayer != null) {
            bgmPlayer.stop();
        }

        // センサーを停止
        sensorManager.unregisterListener(this);

        // ログに出力
        L.d("OUT");
    }

    // スコアの保存
    private void saveHighScore(float score) {
        // ログに出力
        L.d("IN");

        SharedPreferences prefs = getSharedPreferences("BabyInvader", MODE_PRIVATE);
        float bestScore = prefs.getFloat("BEST_SCORE", 0f);

        if (score > bestScore) {
            prefs.edit().putFloat("BEST_SCORE", score).apply();
        }

        // ログに出力
        L.d("OUT");
    }

    private float getBestScore() {
        // ログに出力
        L.d("IN");

        // ログに出力
        L.d("OUT");

        return getSharedPreferences("GamePrefs", MODE_PRIVATE).getFloat("BEST_SCORE", 0f);
    }

}