package ua.nure.dzhafarov.vkontakte.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.List;

import ua.nure.dzhafarov.vkontakte.R;
import ua.nure.dzhafarov.vkontakte.fragments.FragmentPhoto;
import ua.nure.dzhafarov.vkontakte.models.Photo;

public class ActivityPhotoPager extends FragmentActivity {

    public static final String EXTRA_PHOTO_POSITION = "extra_photo_position";
    public static final String EXTRA_PHOTO_ID = "photo_id";
    public static final String EXTRA_LIST_PHOTOS = "list_photos";
    
    public static Intent newIntent(Context context, ArrayList<Photo> photos, Integer photoId) {
        Intent intent = new Intent(context, ActivityPhotoPager.class);
        intent.putExtra(EXTRA_PHOTO_ID, photoId);
        intent.putExtra(EXTRA_LIST_PHOTOS, photos);
        
        return intent;
    }
    
    private ViewPager viewPager;
    private List<Photo> photos;
    private int position;
    
    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_pager);

        getWindow().setStatusBarColor(ContextCompat.getColor(this ,R.color.dark_black));
        
        final Integer photoId = getIntent().getIntExtra(EXTRA_PHOTO_ID, 0);
        photos = (ArrayList<Photo>) getIntent().getSerializableExtra(EXTRA_LIST_PHOTOS);
        
        viewPager = (ViewPager) findViewById(R.id.activity_photo_pager_view_pager);

        FragmentManager fragmentManager = getSupportFragmentManager();
        viewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                ActivityPhotoPager.this.position = position;
                return FragmentPhoto.newInstance(photos.get(position));
            }

            @Override
            public int getCount() {
                return photos.size();
            }
        });
        
        for (int i = 0; i < photos.size(); i++) {
            if (photos.get(i).getId() == photoId) {
                viewPager.setCurrentItem(i);
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_PHOTO_POSITION, position);
        setResult(RESULT_OK, intent);
        finish();
    }
}
