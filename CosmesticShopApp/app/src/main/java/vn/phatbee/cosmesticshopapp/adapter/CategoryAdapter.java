package vn.phatbee.cosmesticshopapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.model.Category;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<Category> categoryList;
    private Context context;
    private OnCategoryClickListener listener;

    // Interface for click handling
    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    // Constructor
    public CategoryAdapter(Context context, OnCategoryClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.categoryList = new ArrayList<>();
    }

    // Update data method
    public void setCategories(List<Category> categories) {
        this.categoryList = categories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.bind(category, listener);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private ImageView categoryImage;
        private TextView categoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryImage = itemView.findViewById(R.id.categoryImage);
            categoryName = itemView.findViewById(R.id.categoryName);
        }

        public void bind(Category category, OnCategoryClickListener listener) {
            categoryName.setText(category.getCategoryName());

            // Load image using Glide
            if (category.getImageUrl() != null && !category.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(category.getImageUrl())
                        .placeholder(R.drawable.cat_1)
                        .centerCrop()
                        .into(categoryImage);
            } else {
                // Set default image if no image URL is available
                categoryImage.setImageResource(R.drawable.cat_1);
            }

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category);
                }
            });
        }
    }
}
