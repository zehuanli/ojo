package it.danieleverducci.ojo.ui.videoplayer.vlc;

import android.net.Uri;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IVLCVout;

import it.danieleverducci.ojo.entities.Camera;
import it.danieleverducci.ojo.ui.SurveillanceFragment;
import it.danieleverducci.ojo.ui.videoplayer.BaseCameraView;

/**
 * Contains all entities (views and java entities) related to a camera stream viewer
 */
public class VlcCameraView extends BaseCameraView {
    public MediaPlayer mediaPlayer;
    private boolean isHD;
    private String url;
    public IVLCVout ivlcVout;
    public LibVLC libvlc;

    public VlcCameraView(FragmentActivity context, Camera camera) {
        super(context, camera);
        surfaceView = new SurfaceView(context);
        this.libvlc = VlcConfig.getInstance().getLibVlc(context);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setKeepScreenOn(true);
        // Create media player
        mediaPlayer = new MediaPlayer(libvlc);

        // Set up video output
        ivlcVout = mediaPlayer.getVLCVout();
        ivlcVout.setVideoView(surfaceView);
        ivlcVout.attachViews();

        // Load media and start playing
        isHD = false;
        url = camera.getRtspUrl();

        // Register for view resize events
        final ViewTreeObserver observer = surfaceView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(() -> {
            // Set rendering size
            ivlcVout.setWindowSize(surfaceView.getWidth(), surfaceView.getHeight());
        });
    }

    /**
     * Starts the playback.
     */
    @Override
    public void startPlayback() {
        if (! ivlcVout.areViewsAttached()) {
            ivlcVout.setVideoView(surfaceView);
            ivlcVout.attachViews();
        }

        Media m = new Media(libvlc, Uri.parse(url));
        m.setHWDecoderEnabled(true, false);
        mediaPlayer.setMedia(m);

        mediaPlayer.play();
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public void resume() {
        mediaPlayer.play();
    }

    @Override
    public void stop() {
        mediaPlayer.stop();
        ivlcVout.detachViews();
    }

    /**
     * Destroys the object and frees the memory
     */
    @Override
    public void destroy() {
        if (libvlc == null) {
            Log.e(SurveillanceFragment.TAG, this.toString() + " already destroyed");
            return;
        }

        mediaPlayer.stop();
        ivlcVout.detachViews();
        libvlc.release();
        libvlc = null;
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override
    public void fullScreen(@Nullable FullEvent fullEvent) {
        surfaceView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (fullEvent != null) {
                    fullEvent.fullOrNot(VlcCameraView.this);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void toggleResolution() {
        String hdUrl = camera.getRtspHDUrl();
        if (hdUrl != null && ! hdUrl.isEmpty()) {
            isHD = ! isHD;
            if (isHD) {
                url = hdUrl;
            } else {
                url = camera.getRtspUrl();
            }
            mediaPlayer.stop();
            Media m = new Media(libvlc, Uri.parse(url));
            m.setHWDecoderEnabled(true, false);
            mediaPlayer.setMedia(m);
            mediaPlayer.play();
        }
    }

    @NonNull
    @Override
    public String toString() {
        return url;
    }
}
