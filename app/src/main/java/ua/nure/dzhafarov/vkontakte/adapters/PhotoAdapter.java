package ua.nure.dzhafarov.vkontakte.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;
import ua.nure.dzhafarov.vkontakte.R;
import ua.nure.dzhafarov.vkontakte.models.Photo;
import ua.nure.dzhafarov.vkontakte.utils.OnUserClickListener;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoHolder>{

    class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        
        private Photo photo;
        private ImageView photoImageView;

        PhotoHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            photoImageView = (ImageView) itemView.findViewById(R.id.photo_image_view_item);
        }

        void bindPhoto(final Photo photo) {
            this.photo = photo;
            
            Picasso.with(context).load(photo.getPhotoLowResolution()).into(photoImageView);
        }

        @Override
        public void onClick(View v) {
            listener.onUserClicked(photo, v);
        }
    }

    private List<Photo> photos;
    private OnUserClickListener<Photo> listener;
    private Context context;
    
    public PhotoAdapter(List<Photo> photos, Context context, OnUserClickListener<Photo> listener) {
        this.photos = photos;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.photo_item_layout, parent, false);
        
        return new PhotoHolder(view);
    }

    @Override
    public void onBindViewHolder(PhotoHolder holder, int position) {
        holder.bindPhoto(photos.get(position));
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }
}
