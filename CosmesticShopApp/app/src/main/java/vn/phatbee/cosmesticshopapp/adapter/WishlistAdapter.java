package vn.phatbee.cosmesticshopapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.model.Wishlist;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {

    private List<Wishlist> wishlistItems;
    private Context context;
    private final OnWishlistActionListener listener;

    public interface OnWishlistActionListener {
        void onRemove(Wishlist wishlist);
        void onAddToCart(Wishlist wishlist);
        void onItemClick(Wishlist wishlist);
    }

    public WishlistAdapter(List<Wishlist> wishlistItems, OnWishlistActionListener listener) {
        this.wishlistItems = wishlistItems;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.row_item_wishlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Wishlist wishlist = wishlistItems.get(position);
        holder.textProductName.setText(wishlist.getProduct().getProductName());
        holder.textProductPrice.setText(String.format("%,.0f VND", wishlist.getProduct().getPrice()));

        if (wishlist.getProduct().getImage() != null && !wishlist.getProduct().getImage().isEmpty()) {
            Glide.with(context).load(wishlist.getProduct().getImage()).placeholder(R.drawable.ic_launcher_background).into(holder.imageProduct);
        } else {
            holder.imageProduct.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.imageHeart.setOnClickListener(v -> listener.onRemove(wishlist));
        holder.ivAddToCart.setOnClickListener(v -> listener.onAddToCart(wishlist));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(wishlist));
    }

    @Override
    public int getItemCount() {
        return wishlistItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct, imageHeart, ivAddToCart;
        TextView textProductName, textProductPrice;

        public ViewHolder(View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.image_product);
            imageHeart = itemView.findViewById(R.id.image_heart);
            ivAddToCart = itemView.findViewById(R.id.iv_add_to_cart);
            textProductName = itemView.findViewById(R.id.text_product_name);
            textProductPrice = itemView.findViewById(R.id.text_product_price);
        }
    }
}