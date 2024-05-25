package it.danieleverducci.ojo.ui;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import it.danieleverducci.ojo.R;
import it.danieleverducci.ojo.SharedPreferencesManager;
import it.danieleverducci.ojo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private NavController navController;
    private boolean rotationEnabledSetting;
    private OnBackButtonPressedListener onBackButtonPressedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rotationEnabledSetting = SharedPreferencesManager.loadRotationEnabled(this);
        this.setRequestedOrientation(this.rotationEnabledSetting ? ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Show FAB only on first fragment
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.HomeFragment)
                binding.fab.show();
            else
                binding.fab.hide();
        });

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToFragment(R.id.action_homeToSettings);
            }
        });

        binding.fab2.hide();

        setDragListener(binding.fab);
        setDragListener(binding.fab2);
    }

    public void setOnBackButtonPressedListener(OnBackButtonPressedListener onBackButtonPressedListener) {
        this.onBackButtonPressedListener = onBackButtonPressedListener;
    }

    @Override
    public void onBackPressed() {
        if (this.onBackButtonPressedListener != null && this.onBackButtonPressedListener.onBackPressed())
            return;
        super.onBackPressed();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void navigateToFragment(int actionId) {
        navigateToFragment(actionId, null);
    }

    public void navigateToFragment(int actionId, Bundle bundle) {
        if (navController == null) {
            Log.e(TAG, "Not initialized");
            return;
        }

        try {
            if (bundle != null)
                navController.navigate(actionId, bundle);
            else
                navController.navigate(actionId);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unable to navigate to fragment: " + e.getMessage());
        }
    }

    public boolean getRotationEnabledSetting() {
        return this.rotationEnabledSetting;
    }

    public void toggleRotationEnabledSetting() {
        this.rotationEnabledSetting = !this.rotationEnabledSetting;
        this.setRequestedOrientation(this.rotationEnabledSetting ? ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setDragListener(FloatingActionButton fab) {
        fab.setOnTouchListener(new View.OnTouchListener() {
            private float initialTouchX;
            private float initialTouchY;
            private boolean isDragging;
            private final int CLICK_DRAG_TOLERANCE = 10;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isDragging = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float deltaX = Math.abs(event.getRawX() - initialTouchX);
                        float deltaY = Math.abs(event.getRawY() - initialTouchY);
                        if (deltaX > CLICK_DRAG_TOLERANCE || deltaY > CLICK_DRAG_TOLERANCE) {
                            isDragging = true;
                            // Handle dragging the view
                            v.setX(event.getRawX() - v.getWidth());
                            v.setY(event.getRawY() - v.getHeight() / 2);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!isDragging) {
                            // It was a click, pass the click event to the listener
                            v.performClick();
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });
    }
}