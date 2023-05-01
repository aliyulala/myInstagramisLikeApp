package com.example.myapp1;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp1.databinding.RecylerRowBinding;

import java.util.ArrayList;

public class PageAdaptor extends RecyclerView.Adapter<PageAdaptor.PageHolder> {


    ArrayList<Page> pageArrayList;
    public PageAdaptor(ArrayList<Page> pageArrayList){
        this.pageArrayList = pageArrayList;
    }

    @NonNull
    @Override
    public PageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecylerRowBinding recylerRowBinding = RecylerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new PageHolder(recylerRowBinding);

    }

    @Override
    public void onBindViewHolder(@NonNull PageHolder holder, int position) {
        holder.binding.recyclerViewTextView.setText(pageArrayList.get(position).name);
        holder.itemView.setOnContextClickListener(new View.OnContextClickListener() {
            @Override
            public boolean onContextClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(),detailsActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("pictureId",pageArrayList.get(position).id);
                holder.itemView.getContext().startActivity(intent);
                return false;
            }
        });
    }



    @Override
    public int getItemCount() {
        return pageArrayList.size();
    }

    public class PageHolder extends RecyclerView.ViewHolder{

        private RecylerRowBinding binding;

        public PageHolder(RecylerRowBinding binding){
            super(binding.getRoot());
            this.binding=binding;
        }
    }

}
