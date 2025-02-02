package it.danieleverducci.ojo.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;

import it.danieleverducci.ojo.R;
import it.danieleverducci.ojo.databinding.FragmentAddStreamBinding;
import it.danieleverducci.ojo.db.AppDatabase;
import it.danieleverducci.ojo.db.Camera;

public class StreamUrlFragment extends Fragment {
    public static final String ARG_CAMERA = "arg_camera";

    private FragmentAddStreamBinding binding;
    private Integer selectedCamera = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentAddStreamBinding.inflate(inflater, container, false);

        // If passed an url, fill the details
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_CAMERA)) {
            this.selectedCamera = args.getInt(ARG_CAMERA);

            Camera c = AppDatabase.getInstance(getContext()).cameraDAO().getAll().get(this.selectedCamera);
            binding.streamName.setText(c.name);
            binding.streamName.setHint(requireContext().getString(R.string.stream_list_default_camera_name).replace("{camNo}", (this.selectedCamera+1)+""));
            binding.streamUrl.setText(c.rtspUrl);
            binding.streamHdUrl.setText(c.rtspHDUrl);
        }

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check the field is filled
                String url = binding.streamUrl.getText().toString();
                if (!(url.startsWith("rtsp://") || url.startsWith("http://") || url.startsWith("https://"))) {
                    Snackbar.make(view, R.string.add_stream_invalid_url, Snackbar.LENGTH_LONG)
                        .setAction(R.string.add_stream_invalid_url_dismiss, null).show();
                    return;
                }

                String hdUrl = binding.streamHdUrl.getText().toString();
                if (! hdUrl.isEmpty() && ! (hdUrl.startsWith("rtsp://") || hdUrl.startsWith("http://") || hdUrl.startsWith("https://"))) {
                    Snackbar.make(view, R.string.add_stream_invalid_hd_url, Snackbar.LENGTH_LONG)
                            .setAction(R.string.add_stream_invalid_url_dismiss, null).show();
                    return;
                }

                // Name can be empty
                String name = binding.streamName.getText().toString();

                if (StreamUrlFragment.this.selectedCamera != null) {
                    // Update camera
                    Camera c = AppDatabase.getInstance(getContext()).cameraDAO().getAll().get(StreamUrlFragment.this.selectedCamera);
                    c.name = name;
                    c.rtspUrl = url;
                    c.rtspHDUrl = hdUrl;
                } else {
                    // Add stream to list
                    AppDatabase.getInstance(getContext()).cameraDAO().insert(new Camera(name, url, hdUrl));
                }

                // Back to first fragment
                NavHostFragment.findNavController(StreamUrlFragment.this)
                        .popBackStack();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}