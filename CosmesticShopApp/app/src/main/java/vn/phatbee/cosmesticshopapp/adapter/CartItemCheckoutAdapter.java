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

import java.util.List;
import java.util.Set;

import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.model.CartItem;

public class CartItemCheckoutAdapter extends RecyclerView.Adapter<CartItemCheckoutAdapter.ViewHolder> {

    private Context context;
    private List<CartItem> cartItems;

    public CartItemCheckoutAdapter(Context context, List<CartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    public void updateCartItems(Set<CartItem> newCartItems) {
        this.cartItems.clear();
        this.cartItems.addAll(newCartItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_item_checkout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        holder.tvProductName.setText(cartItem.getProduct().getProductName());
        holder.tvProductDetails.setText(String.format("VND %.2f", cartItem.getProduct().getPrice()));
        holder.tvQuantity.setText("Quantity: " + cartItem.getQuantity());
        Glide.with(context)
                        .load(cartItem.getProduct().getImage())
                .placeholder(R.drawable.ic_launcher_background)
                                .into(holder.ivProductImage);

        holder.ivDelete.setOnClickListener(v -> {
            // Remove item from cart
            // Implement delete logic if needed
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage, ivDelete;
        TextView tvProductName, tvProductDetails, tvQuantity;

        ViewHolder(View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductDetails = itemView.findViewById(R.id.tv_product_details);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
        }
    }
}
