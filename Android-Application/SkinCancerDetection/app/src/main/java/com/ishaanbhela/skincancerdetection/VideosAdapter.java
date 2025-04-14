package com.ishaanbhela.skincancerdetection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideoViewHolder> {

    List<VideoModel> vids;
    OnVideoItemClickListener listener;

    public VideosAdapter(List<VideoModel> v, OnVideoItemClickListener listener){
        this.vids = v;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_view, parent, false);
        return new VideosAdapter.VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoModel video = vids.get(position);
        Glide.with(holder.img.getContext()).load(video.getImgURL()).into(holder.img);
    }

    @Override
    public int getItemCount() {
        return vids.size();
    }

    public interface OnVideoItemClickListener {
        void onVideoItemClick(int position);
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder{
        ImageView img;
        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.articleImage);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onVideoItemClick(position);
                    }
                }
            });
        }
    }
}
