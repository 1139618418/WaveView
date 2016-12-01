package me.happy.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import me.happy.demo.view.wave.WaveView3;

public class WaveActivity extends AppCompatActivity {

    private ImageView imageView;
    private WaveView3 waveView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wave);
        imageView = (ImageView) findViewById(R.id.image);
        waveView3 = (WaveView3) findViewById(R.id.wave_view);

        final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-2,-2);
        lp.gravity = Gravity.BOTTOM|Gravity.CENTER;
        waveView3.setOnWaveAnimationListener(new WaveView3.OnWaveAnimationListener() {
            @Override
            public void OnWaveAnimation(float y) {
                lp.setMargins(0,0,0,(int)y+2);
                imageView.setLayoutParams(lp);
            }
        });
    }
}
