package it.danieleverducci.ojo.ui;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import it.danieleverducci.ojo.R;
import it.danieleverducci.ojo.databinding.FragmentSurveillanceBinding;
import it.danieleverducci.ojo.db.AppDatabase;
import it.danieleverducci.ojo.db.Camera;
import it.danieleverducci.ojo.ui.videoplayer.BaseCameraView;
import it.danieleverducci.ojo.ui.videoplayer.vlc.VlcCameraView;
import it.danieleverducci.ojo.utils.DpiUtils;

/**
 * Some streams to test:
 * rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov
 * rtsp://demo:demo@ipvmdemo.dyndns.org:5541/onvif-media/media.amp?profile=profile_1_h264&sessiontimeout=60&streamtype=unicast
 */
public class SurveillanceFragment extends Fragment {
    public final static String TAG = "SurveillanceFragment";

    private FragmentSurveillanceBinding binding;
    private final List<BaseCameraView> cameraViews = new ArrayList<>();
    private boolean isFullScreen = false;
    private BaseCameraView fullScreenCameraView;
    private LinearLayout.LayoutParams cameraViewLayoutParams;
    private LinearLayout.LayoutParams rowLayoutParams;
    private com.google.android.material.floatingactionbutton.FloatingActionButton fab;
    private com.google.android.material.floatingactionbutton.FloatingActionButton fab2;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        int viewMargin = DpiUtils.DpToPixels(requireContext(), 2);
        cameraViewLayoutParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.0f
        );
        cameraViewLayoutParams.setMargins(viewMargin, viewMargin, viewMargin, viewMargin);

        rowLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );

        fab = getActivity().findViewById(R.id.fab);
        fab2 = getActivity().findViewById(R.id.fab2);

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFullScreen) {
                    fullScreenCameraView.toggleResolution();
                }
            }
        });

        binding = FragmentSurveillanceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Leanback mode (fullscreen)
        Window window = requireActivity().getWindow();
        if (window != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                final WindowInsetsController controller = window.getInsetsController();

                if (controller != null)
                    controller.hide(WindowInsets.Type.statusBars());
            } else {
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_IMMERSIVE
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            }
        }

        isFullScreen = false;
        addAllCameras();

        // Start playback for all streams
        for (BaseCameraView cv : cameraViews) {
            cv.startPlayback();
        }

        // Register for back pressed events
        ((MainActivity) requireActivity()).setOnBackButtonPressedListener(new OnBackButtonPressedListener() {
            @Override
            public boolean onBackPressed() {
                if (isFullScreen && cameraViews.size() > 1) {
                    isFullScreen = false;
                    showAllCameras();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        // Disable Leanback mode (fullscreen)
        Window window = requireActivity().getWindow();
        if (window != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                final WindowInsetsController controller = window.getInsetsController();

                if (controller != null)
                    controller.show(WindowInsets.Type.statusBars());
            } else {
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_VISIBLE);
            }
        }

        disposeAllCameras();
    }

    private void addAllCameras() {
        List<Camera> cc = AppDatabase.getInstance(getContext()).cameraDAO().getAll();

        int[] gridSize = calcGridDimensionsBasedOnNumberOfElements(cc.size());
        int camIdx = 0;
        for (int r = 0; r < gridSize[0]; r++) {
            // Create row and add to row container
            LinearLayout row = new LinearLayout(getContext());//几行
            binding.gridRowContainer.addView(row, rowLayoutParams);
            // Add camera viewers to the row
            for (int c = 0; c < gridSize[1]; c++) {
                if (camIdx < cc.size()) {
                    Camera cam = cc.get(camIdx);
                    BaseCameraView cv = addCameraView(cam, row);//几列
                    cv.startPlayback();
                    cv.fullScreen(new BaseCameraView.FullEvent() {
                        @Override
                        public void fullOrNot(BaseCameraView baseCameraView) {
                            // Toggle single/multi camera views
                            isFullScreen = !isFullScreen;
                            if (isFullScreen) {
                                hideAllCameraViewsButNot(baseCameraView);
                            } else {
                                showAllCameras();
                            }
                        }
                    });
                } else {
                    // Cameras are less than the maximum number of cells in grid: fill remaining cells with empty views
                    View ev = new View(getContext());
                    ev.setBackgroundColor(getResources().getColor(R.color.purple_700));
                    row.addView(ev, cameraViewLayoutParams);
                }
                camIdx++;
            }
        }
    }

    private void disposeAllCameras() {
        // Destroy players, libs etc
        for (BaseCameraView cv : cameraViews) {
            cv.destroy();
        }
        cameraViews.clear();
        // Remove views
        binding.gridRowContainer.removeAllViews();
    }

    public void hideAllCameraViewsButNot(BaseCameraView baseCameraView) {
        fab.hide();
        fab2.show();
        fullScreenCameraView = baseCameraView;
        View cameraView = baseCameraView.surfaceView;

        for (BaseCameraView cm : cameraViews) {//stop other VideoView
            if (cm != baseCameraView) {;
                cm.stop();
            }
        }

        for (int i = 0; i < binding.gridRowContainer.getChildCount(); i++) {
            LinearLayout row = (LinearLayout) binding.gridRowContainer.getChildAt(i);
            boolean emptyRow = true;
            for (int j = 0; j < row.getChildCount(); j++) {
                View cam = row.getChildAt(j);
                if (cameraView == cam) {
                    emptyRow = false;
                } else {
                    cam.setVisibility(View.GONE);
                }
            }
            if (emptyRow)
                row.setVisibility(View.GONE);
        }
    }

    public void showAllCameras() {
        fab.show();
        fab2.hide();
        for (int i = 0; i < binding.gridRowContainer.getChildCount(); i++) {
            LinearLayout row = (LinearLayout) binding.gridRowContainer.getChildAt(i);
            row.setVisibility(View.VISIBLE);
            for (int j = 0; j < row.getChildCount(); j++) {
                View cam = row.getChildAt(j);
                cam.setVisibility(View.VISIBLE);
            }
            for (BaseCameraView cameraView : cameraViews) {
                cameraView.startPlayback();
            }
        }
    }

    /**
     * 生成vlc版本的视频播放
     */
    private VlcCameraView genVlc(Camera camera, LinearLayout rowContainer) {
        VlcCameraView cv = new VlcCameraView(requireActivity(), camera);
        // Add to layout
        rowContainer.addView(cv.surfaceView, cameraViewLayoutParams);
        return cv;
    }

    private BaseCameraView addCameraView(Camera camera, LinearLayout rowContainer) {
        BaseCameraView cv;
        cv = genVlc(camera, rowContainer);
        cameraViews.add(cv);
        return cv;
    }

    /**
     * Returns the dimensions of the grid based on the number of elements.
     * Es: to display 3 elements is needed a 4-element grid, with 2 elements per side (a 2x2 grid)
     * Es: to display 6 elements is needed a 9-element grid, with 3 elements per side (a 2x3 grid)
     * Es: to display 7 elements is needed a 9-element grid, with 3 elements per side (a 3x3 grid)
     *
     * @param elements
     */
    private int[] calcGridDimensionsBasedOnNumberOfElements(int elements) {
        int rows = 1;
        int cols = 1;
        while (rows * cols < elements) {
            cols += 1;
            if (rows * cols >= elements) break;
            rows += 1;
        }
        return new int[]{rows, cols};
    }
}