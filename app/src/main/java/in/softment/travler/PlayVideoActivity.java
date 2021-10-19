package in.softment.travler;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class PlayVideoActivity extends AppCompatActivity {

    VideoView video;
    MediaController ctlr;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);

        video = (VideoView)findViewById(R.id.video);

        String video_url = getIntent().getStringExtra("videourl");
        pd = new ProgressDialog(this);
        pd.setMessage("Buffering video please wait...");
        pd.show();

        Uri uri = Uri.parse(video_url);
        video.setVideoURI(uri);
        video.start();
        ctlr = new MediaController(this);
        ctlr.setMediaPlayer(video);
        video.setMediaController(ctlr);
        video.requestFocus();

        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //close the progress dialog when buffering is done
                pd.dismiss();
            }
        });
    }
}