package com.hku.lesinventory;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class ThumbnailItemAdapter extends RecyclerView.Adapter<ThumbnailItemAdapter.ViewHolder>{
    private int[] itemIds;
    private String[] names;
    private String[] descriptions;
    //private String[] quantities;
    private Bitmap[] images;
    private Listener listener;


    interface Listener {
        void onClick(int itemId);
    }

    public void setListener(Listener listener) { this.listener = listener; }

    public ThumbnailItemAdapter(int[] itemIds, String[] names, String[] descriptions, Bitmap[] images) {
        this.itemIds = itemIds;
        this.names = names;
        this.descriptions = descriptions;
        //this.quantities = quantites;
        this.images = images;
    }

    @Override
    public ThumbnailItemAdapter.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_thumbnail_item, parent, false);
        return new ViewHolder(cv);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        CardView cardView = holder.cardView;
        ImageView imageView = cardView.findViewById(R.id.info_image);
        imageView.setImageBitmap(images[position]);
        imageView.setContentDescription(names[position]);
        TextView nameText = cardView.findViewById(R.id.name_text);
        nameText.setText(names[position]);
        TextView descriptionText = cardView.findViewById(R.id.description_text);
        descriptionText.setText(descriptions[position]);
        //TextView quantityText = cardView.findViewById(R.id.quantity_text);
        //quantityText.setText(quantities[position]);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(itemIds[position]);
                }
            }
        });
    }

    @Override
    public int getItemCount() { return names.length; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;

        public ViewHolder(CardView v) {
            super(v);
            cardView = v;
        }
    }
}