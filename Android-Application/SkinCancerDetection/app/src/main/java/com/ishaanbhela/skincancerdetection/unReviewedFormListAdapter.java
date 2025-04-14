package com.ishaanbhela.skincancerdetection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class unReviewedFormListAdapter extends RecyclerView.Adapter<unReviewedFormListAdapter.UnReviewedFormViewHolder> {

    List<unRevievedFormListModel> unReviewedForms;
    onUnReviewedItemClicked listener;

    public unReviewedFormListAdapter(List<unRevievedFormListModel> unReviewedForms, onUnReviewedItemClicked listener){
        this.unReviewedForms = unReviewedForms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UnReviewedFormViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.doctor_form_list_view, parent, false);
        return new UnReviewedFormViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UnReviewedFormViewHolder holder, int position) {
        unRevievedFormListModel unRevieved = unReviewedForms.get(position);
        holder.Name.setText(unRevieved.getUID());
    }

    @Override
    public int getItemCount() {
        return unReviewedForms.size();
    }

    public interface onUnReviewedItemClicked{
        public void onClick(int position);
    }

    public class UnReviewedFormViewHolder extends RecyclerView.ViewHolder{
        TextView Name;
        public UnReviewedFormViewHolder(@NonNull View itemView) {
            super(itemView);
            Name = itemView.findViewById(R.id.UnReviewedName);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onClick(position);
                    }
                }
            });
        }
    }
}
