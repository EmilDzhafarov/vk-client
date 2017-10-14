package ua.nure.dzhafarov.vkontakte.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ua.nure.dzhafarov.vkontakte.R;
import ua.nure.dzhafarov.vkontakte.adapters.PhotoAdapter;
import ua.nure.dzhafarov.vkontakte.models.Photo;
import ua.nure.dzhafarov.vkontakte.models.PhotoAlbum;
import ua.nure.dzhafarov.vkontakte.models.User;
import ua.nure.dzhafarov.vkontakte.utils.OnUserClickListener;
import ua.nure.dzhafarov.vkontakte.utils.OperationListener;
import ua.nure.dzhafarov.vkontakte.utils.VKManager;

import static ua.nure.dzhafarov.vkontakte.activities.ActivityUserProfile.REQUEST_USER_PROFILE;

public class FragmentListPhotos extends Fragment {
    
    public static final String REQUEST_PHOTO_LIST = "request_photo_list";
    
    private List<Photo> photos;
    private PhotoAdapter photoAdapter;
    private VKManager vkManager;
    private User owner;
    private PhotoAlbum photoAlbum;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    public static FragmentListPhotos newInstance(User owner, PhotoAlbum photoAlbum) {
        Bundle args = new Bundle();
        args.putSerializable(REQUEST_USER_PROFILE, owner);
        args.putSerializable(REQUEST_PHOTO_LIST, photoAlbum);
        
        FragmentListPhotos result = new FragmentListPhotos();
        result.setArguments(args);
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        owner = (User) getArguments().getSerializable(REQUEST_USER_PROFILE);
        photoAlbum = (PhotoAlbum) getArguments().getSerializable(REQUEST_PHOTO_LIST);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_photos_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        photos = new ArrayList<>();
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_photos);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar); 
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        photoAdapter = new PhotoAdapter(photos, getActivity(), new OnUserClickListener<Photo>() {
            @Override
            public void onUserClicked(Photo result, View view) {
                // skip this yet
            }
        });

        recyclerView.setAdapter(photoAdapter);
        vkManager = VKManager.getInstance();
        
        getActivity().setTitle(String.format(Locale.ROOT, "%s", photoAlbum.getTitle()));
        loadPhotos(owner, "q");
    }

    private void loadPhotos(User owner, String size) {
        progressBar.setVisibility(View.VISIBLE);
        vkManager.loadPhotosFromAlbum(owner.getId(), size, photoAlbum.getId(), new OperationListener<List<Photo>>() {
            @Override
            public void onSuccess(List<Photo> object) {
                loadPhotosAlbumsInUI(object);
            }

            @Override
            public void onFailure(String message) {
                showErrorInUI(message);
            }
        });
    }

    private void loadPhotosAlbumsInUI(final List<Photo> phs) {
        Activity activity = getActivity();

        if (activity != null) {
            activity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            photos.clear();
                            photos.addAll(phs);
                            photoAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
            );
        }
    }

    private void showErrorInUI(final String message) {
        final Activity activity = getActivity();

        if (activity != null) {
            activity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }
    }
}
