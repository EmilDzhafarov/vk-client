package ua.nure.dzhafarov.vkontakte.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.Locale;
import de.hdodenhof.circleimageview.CircleImageView;
import ua.nure.dzhafarov.vkontakte.R;
import ua.nure.dzhafarov.vkontakte.activities.ActivityChat;
import ua.nure.dzhafarov.vkontakte.activities.BaseActivity;
import ua.nure.dzhafarov.vkontakte.models.User;

import static ua.nure.dzhafarov.vkontakte.activities.ActivityUserProfile.REQUEST_USER_PROFILE;

public class FragmentUserProfile extends Fragment implements View.OnClickListener {
    
    private User user;
    
    private LinearLayout linearLayoutDetails;
    private CircleImageView profileImage;
    private TextView lastSeen;
    private TextView fullName;
    private Button writeMessageButton;
    private Button showPhotosButton;
    private ProgressBar progressBar;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        user = (User) getActivity().getIntent().getSerializableExtra(REQUEST_USER_PROFILE); 
        new AsyncUserDownload().execute(user);
        
        linearLayoutDetails = (LinearLayout) view.findViewById(R.id.user_profile_linear_layout_details); 
        profileImage = (CircleImageView) view.findViewById(R.id.user_profile_image);
        lastSeen = (TextView) view.findViewById(R.id.user_profile_last_seen);
        fullName = (TextView) view.findViewById(R.id.user_full_name);
        writeMessageButton = (Button) view.findViewById(R.id.write_message_button);
        showPhotosButton = (Button) view.findViewById(R.id.show_photos_button); 
        progressBar = (ProgressBar) view.findViewById(R.id.user_profile_progress_bar);
        
        writeMessageButton.setOnClickListener(this);
        showPhotosButton.setOnClickListener(this);
    }
    
    private class AsyncUserDownload extends AsyncTask<User, Void, User> {

        @Override
        protected User doInBackground(User... params) {
            User user = params[0];

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // retrieving user from server code
            
            return user;
        }

        @Override
        protected void onPostExecute(User user) {

            fullName.setText(getString(R.string.user_chat_title, user.getFirstName(), user.getLastName()));

            if (user.isOnline()) {
                lastSeen.setText(getString(R.string.online));
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat(
                        getString(R.string.date_time_form_user_last_seen),
                        Locale.ROOT
                );

                if (user.getLastSeen() != 0) {
                    lastSeen.setText(getString(R.string.last_seen, sdf.format(user.getLastSeen()))
                    );
                } else {
                    lastSeen.setText("");
                }
            }
            
            getActivity().setTitle(getString(R.string.user_chat_title, user.getFirstName(), user.getLastName()));
            
            Picasso.with(FragmentUserProfile.this.getActivity())
                    .load(user.getPhotoURL())
                    .into(profileImage);
            
            progressBar.setVisibility(View.GONE);
            linearLayoutDetails.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.write_message_button) {
            Intent intent = new Intent(getActivity(), ActivityChat.class);
            intent.putExtra(REQUEST_USER_PROFILE, user);
            startActivity(intent);   
        } else if (v.getId() == R.id.show_photos_button) {
            FragmentListPhotoAlbums albums = FragmentListPhotoAlbums.newInstance(user);
            ((BaseActivity) getActivity()).addFragment(albums, true);
        }
    }
}
