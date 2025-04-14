package com.ishaanbhela.skincancerdetection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class prevFormItemAdapter extends RecyclerView.Adapter<prevFormItemAdapter.formViewHolder> {

    private List<prevFormModel> formItemList;
    private OnItemClickListener listener;


    public prevFormItemAdapter(List<prevFormModel> formItemList, OnItemClickListener listener) {
        this.formItemList = formItemList;
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    @NonNull
    @Override
    public formViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.prev_form_view, parent, false);
        return new formViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull formViewHolder holder, int position) {
        prevFormModel formItem = formItemList.get(position);
        holder.textViewItem.setText(formItem.getText());
        holder.imageViewItem.setImageResource(R.drawable.file_icon);
    }


    @Override
    public int getItemCount() {
        return formItemList.size();
    }


    public class formViewHolder extends RecyclerView.ViewHolder{

        public ImageView imageViewItem;
        public TextView textViewItem;
        public LinearLayout layout;

        public formViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewItem = itemView.findViewById(R.id.imageViewItem);
            textViewItem = itemView.findViewById(R.id.textViewItem);
            layout = itemView.findViewById(R.id.prevFormLayout);

            itemView.setOnClickListener(v -> {
                System.out.println("RecyclerView Item Clicked");
                if (listener != null) {
                    int position = getAdapterPosition();
                    System.out.println(position);
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }
}
