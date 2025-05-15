package vn.phatbee.cosmesticshopapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.model.CartItem;
import vn.phatbee.cosmesticshopapp.model.Product;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartItemViewHolder> {

    private Context context;
    private List<CartItem> cartItems;
    private List<Boolean> itemSelections;
    private CartItemListener listener;

    public interface CartItemListener {
        void onQuantityChanged(int position, CartItem item, long newQuantity);
        void onItemCheckedChanged(int position, boolean isChecked);
        void onItemRemoved(int position, CartItem item);
    }

    public CartAdapter(Context context, List<CartItem> cartItems, CartItemListener listener) {
        this.context = context;
        this.cartItems = cartItems != null ? cartItems : new ArrayList<>();
        this.listener = listener;
        this.itemSelections = new ArrayList<>();
        for (int i = 0; i < this.cartItems.size(); i++) {
            itemSelections.add(false);
        }
    }

    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        Product product = item.getProduct();

        // Set product name
        holder.tvProductName.setText(product != null ? product.getProductName() : "Unknown Product");

        // Set prices
        double currentPrice = product != null ? product.getPrice() : 0.0;
        holder.tvCurrentPrice.setText(String.format("%,.0f đ", currentPrice));

        // Set quantity
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        // Set product image
        if (product != null && product.getImage() != null && !product.getImage().isEmpty()) {
            Glide.with(context)
                    .load(product.getImage())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.ivProductImage);
        } else {
            holder.ivProductImage.setImageResource(R.drawable.ic_launcher_background);
        }

        // Set checkbox state
        holder.checkboxItem.setChecked(position < itemSelections.size() ? itemSelections.get(position) : false);

        // Setup click listeners
        holder.checkboxItem.setOnCheckedChangeListener(null);
        holder.checkboxItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (position < itemSelections.size()) {
                itemSelections.set(position, isChecked);
                if (listener != null) {
                    listener.onItemCheckedChanged(position, isChecked);
                }
            }
        });

        holder.btnDecrease.setOnClickListener(v -> {
            long newQuantity = item.getQuantity() - 1;
            if (newQuantity >= 1) {
                item.setQuantity(newQuantity);
                holder.tvQuantity.setText(String.valueOf(newQuantity));
                if (listener != null) {
                    listener.onQuantityChanged(position, item, newQuantity);
                }
            }
        });

        holder.btnIncrease.setOnClickListener(v -> {
            long newQuantity = item.getQuantity() + 1;
            item.setQuantity(newQuantity);
            holder.tvQuantity.setText(String.valueOf(newQuantity));
            if (listener != null) {
                listener.onQuantityChanged(position, item, newQuantity);
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemRemoved(position, item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void updateCartItems(List<CartItem> newCartItems) {
        this.cartItems = newCartItems != null ? newCartItems : new ArrayList<>();
        List<Boolean> newSelections = new ArrayList<>();
        for (int i = 0; i < this.cartItems.size(); i++) {
            newSelections.add(i < itemSelections.size() ? itemSelections.get(i) : false);
        }
        this.itemSelections = newSelections;
        notifyDataSetChanged();
    }

    public void selectAll(boolean isSelected) {
        for (int i = 0; i < itemSelections.size(); i++) {
            itemSelections.set(i, isSelected);
        }
        notifyDataSetChanged();
    }

    public List<CartItem> getSelectedItems() {
        List<CartItem> selectedItems = new ArrayList<>();
        for (int i = 0; i < cartItems.size() && i < itemSelections.size(); i++) {
            if (itemSelections.get(i)) {
                selectedItems.add(cartItems.get(i));
            }
        }
        return selectedItems;
    }

    public boolean areAllItemsSelected() {
        if (itemSelections.isEmpty()) return false;
        for (Boolean isSelected : itemSelections) {
            if (!isSelected) {
                return false;
            }
        }
        return true;
    }

    static class CartItemViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkboxItem;
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvCurrentPrice;
        Button btnDecrease;
        TextView tvQuantity;
        Button btnIncrease;
        Button btnRemove;

        public CartItemViewHolder(@NonNull View itemView) {
            super(itemView);
            checkboxItem = itemView.findViewById(R.id.checkboxItem);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvCurrentPrice = itemView.findViewById(R.id.tvCurrentPrice);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}