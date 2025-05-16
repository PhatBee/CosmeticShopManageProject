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

import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.model.CartItem;
import vn.phatbee.cosmesticshopapp.model.Product;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder> {

    private Context context;
    private List<CartItem> cartItems;
    private OnItemRemoveListener removeListener;

    public interface OnItemRemoveListener {
        void onItemRemoved(int position, CartItem item);
    }

    public CheckoutAdapter(Context context, List<CartItem> cartItems, OnItemRemoveListener removeListener) {
        this.context = context;
        this.cartItems = cartItems;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_item_checkout, parent, false);
        return new CheckoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        Product product = cartItem.getProduct();

        // Set product name
        holder.tvProductName.setText(product.getProductName());

        // Set product image
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            Glide.with(context)
                    .load(product.getImage())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.ivProductImage);
        } else {
            holder.ivProductImage.setImageResource(R.drawable.ic_launcher_background);
        }

        // Set product details (volume and price per unit)
        String details = product.getVolume() + " - $" + String.format("%.2f", product.getPrice()) + " per unit";
        holder.tvProductDetails.setText(details);

        // Set quantity
        holder.tvQuantity.setText("Quantity: " + cartItem.getQuantity());

        // Set remove button listener
        holder.ivDelete.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onItemRemoved(position, cartItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    public void updateCartItems(List<CartItem> newCartItems) {
        this.cartItems = newCartItems;
        notifyDataSetChanged();
    }

    static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvProductDetails;
        TextView tvQuantity;
        ImageView ivDelete;

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductDetails = itemView.findViewById(R.id.tv_product_details);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}