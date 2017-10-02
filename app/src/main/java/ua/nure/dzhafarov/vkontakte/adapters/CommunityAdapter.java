package ua.nure.dzhafarov.vkontakte.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;
import ua.nure.dzhafarov.vkontakte.R;
import ua.nure.dzhafarov.vkontakte.models.Community;
import ua.nure.dzhafarov.vkontakte.utils.OnUserClickListener;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.CommunityHolder> {
    
    class CommunityHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        
        private Community community;

        private CircleImageView photoCommunity;
        private TextView name;
        private TextView type;

        CommunityHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            photoCommunity = (CircleImageView) itemView.findViewById(R.id.community_image);
            name = (TextView) itemView.findViewById(R.id.community_name);
            type = (TextView) itemView.findViewById(R.id.community_type);
        }

        void bindCommunity(final Community com) {
            community = com;

            name.setText(community.getName());
            type.setText(community.getType());
            
            Picasso.with(context)
                    .load(community.getPhotoUrl())
                    .into(photoCommunity);
        }


        @Override
        public void onClick(View v) {
            listener.onUserClicked(community, v);
        }
    }
    
    private List<Community> communities;
    private OnUserClickListener<Community> listener;
    private Context context;

    public CommunityAdapter(List<Community> communities, Context context, OnUserClickListener<Community> listener) {
        this.communities = communities;
        this.listener = listener;
        this.context = context;
    }

    @Override
    public CommunityHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.community_item_layout, parent, false);

        return new CommunityHolder(view);
    }

    @Override
    public void onBindViewHolder(CommunityHolder holder, int position) {
        holder.bindCommunity(communities.get(position));
    }

    @Override
    public int getItemCount() {
        return communities.size();
    }
}
