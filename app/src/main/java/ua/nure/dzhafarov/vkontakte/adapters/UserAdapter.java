package ua.nure.dzhafarov.vkontakte.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import de.hdodenhof.circleimageview.CircleImageView;
import ua.nure.dzhafarov.vkontakte.R;
import ua.nure.dzhafarov.vkontakte.models.User;
import ua.nure.dzhafarov.vkontakte.utils.OnUserClickListener;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.FriendHolder> {
    
    class FriendHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        
        private User user;
        
        private CircleImageView photoProfile;
        private TextView fullName;
        private TextView lastSeen;
        private TextView optionDots;

        FriendHolder(View itemView) {
            super(itemView);
            
            itemView.setOnClickListener(this);
            photoProfile = (CircleImageView) itemView.findViewById(R.id.profile_image);
            fullName = (TextView) itemView.findViewById(R.id.friend_full_name);
            lastSeen = (TextView) itemView.findViewById(R.id.friend_last_seen);
            optionDots = (TextView) itemView.findViewById(R.id.option_dots);
            optionDots.setOnClickListener(this);
        }

        void bindFriend(final User fr) {
            user = fr;
            
            fullName.setText(String.format(Locale.US, "%s %s", user.getFirstName(), user.getLastName()));
            
            if (user.isOnline()) {
                lastSeen.setText("online");
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm", Locale.ROOT);

                if (user.getLastSeen() != 0) {
                    lastSeen.setText(context.getString(R.string.last_seen, sdf.format(user.getLastSeen() * 1000)));
                } else {
                    lastSeen.setText("");
                }   
            }

            Picasso.with(context)
                    .load(user.getPhotoURL())
                    .into(photoProfile);
        }
        
        
        @Override
        public void onClick(View v) {
            listener.onUserClicked(user, v);
        }
    }

    private List<User> users;
    private OnUserClickListener<User> listener;
    private Context context;
    
    public UserAdapter(List<User> users, Context context, OnUserClickListener<User> listener) {
        this.users = users;
        this.listener = listener;
        this.context = context;
    }

    @Override
    public FriendHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.friend_item_layout, parent, false);
        
        return new FriendHolder(view);
    }

    @Override
    public void onBindViewHolder(FriendHolder holder, int position) {
        holder.bindFriend(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}