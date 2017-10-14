package ua.nure.dzhafarov.vkontakte.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;;
import android.widget.TextView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import ua.nure.dzhafarov.vkontakte.R;
import ua.nure.dzhafarov.vkontakte.models.PhotoAlbum;
import ua.nure.dzhafarov.vkontakte.utils.OnUserClickListener;

public class PhotoAlbumAdapter extends RecyclerView.Adapter<PhotoAlbumAdapter.PhotoAlbumHolder> {

    class PhotoAlbumHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private PhotoAlbum photoAlbum;
        
        private ImageView coverImageView;
        private TextView countPhotosTextView;
        private TextView titleTextView;
        private ProgressBar progressBar;

        PhotoAlbumHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            coverImageView = (ImageView) itemView.findViewById(R.id.image_view_cover_photo_album);
            countPhotosTextView = (TextView) itemView.findViewById(R.id.text_view_count_of_photo);
            titleTextView = (TextView) itemView.findViewById(R.id.text_view_title_photo_album);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
        }

        void bindPhotoAlbum(final PhotoAlbum photoAlbum) {
            this.photoAlbum = photoAlbum;

            countPhotosTextView.setText(String.valueOf(photoAlbum.getSize()));
            titleTextView.setText(photoAlbum.getTitle());

            progressBar.setVisibility(View.VISIBLE);
            Picasso.with(context)
                    .load(photoAlbum.getThumbSrc())
                    .centerCrop()
                    .fit()
                    .into(coverImageView, new Callback() {
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
    

        @Override
        public void onClick(View v) {
            listener.onUserClicked(photoAlbum, v);
        }

    }

    private List<PhotoAlbum> photoAlbums;
    private OnUserClickListener<PhotoAlbum> listener;
    private Context context;

    public PhotoAlbumAdapter(List<PhotoAlbum> photoAlbums, Context context, OnUserClickListener<PhotoAlbum> listener) {
        this.photoAlbums = photoAlbums;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public PhotoAlbumHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.photo_album_item_layout, parent, false);

        return new PhotoAlbumHolder(view);
    }

    @Override
    public void onBindViewHolder(PhotoAlbumHolder holder, int position) {
        holder.bindPhotoAlbum(photoAlbums.get(position));
    }

    @Override
    public int getItemCount() {
        return photoAlbums.size();
    }
}

