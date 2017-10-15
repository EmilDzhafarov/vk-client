package ua.nure.dzhafarov.vkontakte.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import ua.nure.dzhafarov.vkontakte.R;
import ua.nure.dzhafarov.vkontakte.models.Photo;

public class FragmentPhoto extends Fragment {
    
    public static final String REQUEST_PHOTO = "request_photo";
    
    public static FragmentPhoto newInstance(Photo photo) {
        Bundle args = new Bundle();
        args.putSerializable(REQUEST_PHOTO, photo);
        
        FragmentPhoto fragmentPhoto = new FragmentPhoto();
        fragmentPhoto.setArguments(args);
        return fragmentPhoto;
    }
    
    private Photo photo;
    private ImageView photoImageView;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.photo = (Photo) getArguments().getSerializable(REQUEST_PHOTO);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        photoImageView = (ImageView) view.findViewById(R.id.fragment_photo_image_view);
        progressBar = (ProgressBar) view.findViewById(R.id.fragment_photo_progress_bar);
        
        loadPhoto();
    }
    
    private void  loadPhoto() {
        final Activity activity = getActivity();
        
        if (activity != null) {
            activity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            loadPhotoInUI(activity);
                        }
                    }
            );
        }
    }
    
    private void loadPhotoInUI(Context context) {
        progressBar.setVisibility(View.VISIBLE);
        Picasso.with(context)
                .load(photo.getPhotoURL())
                .fit()
                .centerInside()
                .into(photoImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
