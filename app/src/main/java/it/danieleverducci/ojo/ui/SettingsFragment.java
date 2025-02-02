package it.danieleverducci.ojo.ui;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import it.danieleverducci.ojo.R;
import it.danieleverducci.ojo.SharedPreferencesManager;
import it.danieleverducci.ojo.databinding.FragmentSettingsItemListBinding;
import it.danieleverducci.ojo.db.AppDatabase;
import it.danieleverducci.ojo.db.Camera;
import it.danieleverducci.ojo.ui.adapters.SettingsRecyclerViewAdapter;
import it.danieleverducci.ojo.utils.ItemMoveCallback;

/**
 * A fragment representing a list of Items.
 */
public class SettingsFragment extends Fragment {

    private FragmentSettingsItemListBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsItemListBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Setup toolbar
        binding.settingsToolbar.getOverflowIcon().setTint(Color.WHITE);
        binding.settingsToolbar.inflateMenu(R.menu.settings_menu);
        MenuItem rotMenuItem = binding.settingsToolbar.getMenu().findItem(R.id.menuitem_allow_rotation);
        rotMenuItem.setTitle(((MainActivity)getActivity()).getRotationEnabledSetting() ? R.string.menuitem_deny_rotation : R.string.menuitem_allow_rotation);

        // Register for item click
        binding.settingsToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menuitem_add_camera:
                        ((MainActivity)getActivity()).navigateToFragment(R.id.action_settingsToCameraUrl);
                        return true;
                    case R.id.menuitem_allow_rotation:
                        ((MainActivity)getActivity()).toggleRotationEnabledSetting();
                        SharedPreferencesManager.saveRotationEnabled(getContext(), ((MainActivity)getActivity()).getRotationEnabledSetting());
                        item.setTitle(((MainActivity)getActivity()).getRotationEnabledSetting() ? R.string.menuitem_deny_rotation : R.string.menuitem_allow_rotation);
                        return true;
                    case R.id.menuitem_info:
                        ((MainActivity)getActivity()).navigateToFragment(R.id.action_SettingsToInfoFragment);
                        return true;
                }
                return false;
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        // Load cameras
        List<Camera> cams = AppDatabase.getInstance(getContext()).cameraDAO().getAll();

        // Set the adapter
        RecyclerView recyclerView = binding.list;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        SettingsRecyclerViewAdapter adapter = new SettingsRecyclerViewAdapter(cams);
        ItemTouchHelper.Callback callback =
                new ItemMoveCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
        adapter.setOnDragListener(touchHelper::startDrag);
        recyclerView.setAdapter(adapter);
        // Onclick listener
        adapter.setOnClickListener(new SettingsRecyclerViewAdapter.OnClickListener() {
            @Override
            public void onItemClick(int pos) {
                Bundle b = new Bundle();
                b.putInt(StreamUrlFragment.ARG_CAMERA, pos);
                ((MainActivity)getActivity()).navigateToFragment(R.id.action_settingsToCameraUrl, b);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        // Save cameras
        List<Camera> cams = ((SettingsRecyclerViewAdapter)binding.list.getAdapter()).getItems();
        AppDatabase.getInstance(getContext()).cameraDAO().insertAll(cams.toArray(new Camera[0]));
    }
}