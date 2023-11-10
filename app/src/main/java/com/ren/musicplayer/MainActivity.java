package com.ren.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import java.text.Normalizer;

public class MainActivity extends AppCompatActivity {

    private Button[] buttons;
    private ImageButton[] controlButtons;
    private MediaPlayer mediaPlayer;
    private int currentPosition = 0;
    private Button currentButton;
    private SeekBar seekBar;
    private Handler handler = new Handler();
    private Runnable updateSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttons = new Button[]{
                findViewById(R.id.btn1),
                findViewById(R.id.btn2),
                findViewById(R.id.btn3),
                findViewById(R.id.btn4),
                findViewById(R.id.btn5),
                findViewById(R.id.btn6),
                findViewById(R.id.btn7),
                findViewById(R.id.btn8),
                findViewById(R.id.btn9),
                findViewById(R.id.btn10),
                findViewById(R.id.btn11),
                findViewById(R.id.btn12),
                findViewById(R.id.btn13),
                findViewById(R.id.btn14),
                findViewById(R.id.btn15)
        };

        controlButtons = new ImageButton[]{
                findViewById(R.id.ib1), // Back
                findViewById(R.id.ib3), // Pause
                findViewById(R.id.ib4), // Play
                findViewById(R.id.ib5), // Stop
                findViewById(R.id.ib6), // Repeat
                findViewById(R.id.ib2)  //Next
        };

        mediaPlayer = new MediaPlayer();

        seekBar = findViewById(R.id.seekBar2);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(updateSeekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.postDelayed(updateSeekBar, 1000);
            }
        });

        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    handler.postDelayed(this, 1000);
                }
            }
        };

        for (Button button : buttons) {
            button.setOnClickListener(this::handleButtonClick);
        }

        for (ImageButton button : controlButtons) {
            button.setOnClickListener(this::handleButtonClick);
        }
    }

    private void handleButtonClick(View view) {
        if (view instanceof Button) {
            handleSongButtonClick((Button) view);
        } else if (view instanceof ImageButton) {
            handleControlButtonClick((ImageButton) view);
        }
    }

    private void handleSongButtonClick(Button button) {
        playSong(button);
    }

    private void handleControlButtonClick(ImageButton button) {
        int buttonId = button.getId();

        if (buttonId == R.id.ib1) { // Back
            playPreviousSong();
        } else if (buttonId == R.id.ib3) { // Pause
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        } else if (buttonId == R.id.ib4) { // Play
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        } else if (buttonId == R.id.ib5) { // Stop
            stopPlayback();
        } else if (buttonId == R.id.ib6) { // Repeat
            mediaPlayer.setLooping(!mediaPlayer.isLooping());
        } else if (buttonId == R.id.ib2) { // Next
            playNextSong();
        }
    }

    private void playPreviousSong() {
        if (currentButton != null) {
            currentButton.setTextColor(Color.BLACK);
        }

        currentPosition = (currentPosition - 1 + buttons.length) % buttons.length;

        if (mediaPlayer != null) {
            stopPlayback();
            playSong(buttons[currentPosition]);
        }
    }

    private void playNextSong() {
        if (currentButton != null) {
            currentButton.setTextColor(Color.BLACK);
        }

        currentPosition = (currentPosition + 1) % buttons.length;
        highlightButton(buttons[currentPosition]);

        if (mediaPlayer != null) {
            stopPlayback();
            playSong(buttons[currentPosition]);
        }

        if (currentPosition == 0) {
            currentPosition = 0;
        }
    }



    private void playSong(Button button) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            stopPlayback();
        }

        String fullName = button.getText().toString().trim().toLowerCase();
        String[] parts = fullName.split("-");

        if (parts.length >= 2) {
            String nameSong = parts[0].trim().toLowerCase().replace(" ", "");

            nameSong = Normalizer.normalize(nameSong, Normalizer.Form.NFD)
                    .replaceAll("[^\\p{ASCII}]", "");

            int resId = getResources().getIdentifier(nameSong, "raw", getPackageName());
            if (resId != 0) {
                try {
                    mediaPlayer = MediaPlayer.create(this, resId);

                    mediaPlayer.setOnCompletionListener(mp -> {
                        stopPlayback();
                        playNextSong();
                    });

                    mediaPlayer.start();

                    seekBar.setMax(mediaPlayer.getDuration());
                    handler.postDelayed(updateSeekBar, 1000);

                    highlightButton(button);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error playing song", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Resource not found for: " + nameSong, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            seekBar.setProgress(0);
            if (currentButton != null) {
                currentButton.setTextColor(Color.BLACK);
            }
        }
    }

    private void highlightButton(Button button) {
        if (currentButton != null) {
            currentButton.setTextColor(Color.BLACK);
        }
        button.setTextColor(Color.RED);
        currentButton = button;
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        handler.removeCallbacks(updateSeekBar);
        super.onDestroy();
    }
}
