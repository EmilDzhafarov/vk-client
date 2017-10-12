package ua.nure.dzhafarov.vkontakte.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    
    private List<Photo> photos;
    private PhotoAdapter photoAdapter;
    private VKManager vkManager;
    private User owner;
    
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_photos_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        photos = new ArrayList<>();
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_photos);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPhotos(owner);
            }
        });
        swipeRefreshLayout.setColorSchemeColors(getActivity().getColor(R.color.colorPrimary));
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        photoAdapter = new PhotoAdapter(photos, getActivity(), new OnUserClickListener<Photo>() {
            @Override
            public void onUserClicked(Photo result, View view) {
                // skip this yet
            }
        });
        
        recyclerView.setAdapter(photoAdapter);
        vkManager = VKManager.getInstance();
        
        User user = (User) getActivity().getIntent().getSerializableExtra(REQUEST_USER_PROFILE);
        
        if (user == null) {
            owner = vkManager.getCurrentUser();
        } else {
            owner = user;
        }
        
        getActivity().setTitle(String.format(Locale.ROOT, "%s's photos", owner.getFirstName()));
        loadPhotos(owner);
        loadPhotoAlbums(owner);
    }
    
    private void loadPhotos(User owner) {
        swipeRefreshLayout.setRefreshing(true);
        vkManager.loadPhotos(owner.getId(), "q", new OperationListener<List<Photo>>() {
            @Override
            public void onSuccess(final List<Photo> object) {
                loadPhotosInUI(object);
            }

            @Override
            public void onFailure(String message) {
                showErrorInUI(message);
            }
        });
    }
    
    private void loadPhotoAlbums(User owner) {
        vkManager.loadPhotoAlbums(owner.getId(), new OperationListener<List<PhotoAlbum>>() {
            @Override
            public void onSuccess(List<PhotoAlbum> object) {
            }

            @Override
            public void onFailure(String message) {

            }
        });
    }


    private void loadPhotosInUI(final List<Photo> phs) {
        Activity activity = getActivity();

        if (activity != null) {
            activity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            photos.clear();
                            photos.addAll(phs);
                            photoAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
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
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }
    }
}
