package com.ishaanbhela.skincancerdetection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;

import java.util.List;

public class ArticlesAdapter extends RecyclerView.Adapter<ArticlesAdapter.ArticleViewHolder> {

    private List<ArticlesModel> articles;
    private ArticlesAdapter.OnArticleItemClickListener listener;

    public ArticlesAdapter(List<ArticlesModel> a, OnArticleItemClickListener listener){
        this.articles = a;
        this.listener = listener;
    }


    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_view, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        ArticlesModel arts = articles.get(position);
        Glide.with(holder.img.getContext()).load(arts.getImgURL()).into(holder.img);
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public interface OnArticleItemClickListener {
        void onArticleItemClick(int position);
    }

    public class ArticleViewHolder extends RecyclerView.ViewHolder{
        ImageView img;
        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.articleImage);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onArticleItemClick(position);
                    }
                }
            });
        }
    }
}
