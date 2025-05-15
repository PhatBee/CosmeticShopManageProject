package vn.phatbee.cosmesticshopapp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.manager.UserSessionManager;
import vn.phatbee.cosmesticshopapp.model.Product;
import vn.phatbee.cosmesticshopapp.model.Wishlist;
import vn.phatbee.cosmesticshopapp.retrofit.ApiService;
import vn.phatbee.cosmesticshopapp.retrofit.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

public class ProductRecentAdapter extends RecyclerView.Adapter<ProductRecentAdapter.ProductViewHolder> {
    private static final String TAG = "ProductRecentAdapter";
    private Context context;
    private List<Product> products;
    private OnProductRecentClickListener listener;
    private UserSessionManager sessionManager;

    public interface OnProductRecentClickListener {
        void onProductRecentClick(Product product);
    }

    public ProductRecentAdapter(Context context, OnProductRecentClickListener listener) {
        this.context = context;
        this.products = new ArrayList<>();
        this.listener = listener;
    }

    public void setProducts(List<Product> products) {
        this.products.clear();
        this.products.addAll(products);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewholder_recommend, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvProductPrice;
        ImageView ivWishlist;
        boolean isInWishlist;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvPrice);
            ivWishlist = itemView.findViewById(R.id.ivWishlist);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductRecentClick(products.get(getAdapterPosition()));
                }
            });
        }

        void bind(Product product) {
            // Handle product name
            tvProductName.setText(product.getProductName() != null ? product.getProductName() : "Unknown Product");

            // Handle product price
            Double price = product.getPrice();
            if (price != null) {
                tvProductPrice.setText(String.format("%.2f VNĐ", price));
            } else {
                tvProductPrice.setText("Price Unavailable");
            }

            // Handle product image
            Glide.with(context)
                    .load(product.getImage())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(ivProductImage);
        }

        private void checkWishlistStatus(Product product) {
            Long userId = sessionManager.getUserDetails().getUserId();
            if (userId == null || userId == 0) {
                ivWishlist.setImageResource(R.drawable.ic_heart);
                isInWishlist = false;
                return;
            }

            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            Call<Boolean> call = apiService.isProductInWishlist(userId, product.getProductId());
            call.enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        isInWishlist = response.body();
                        ivWishlist.setImageResource(isInWishlist ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    Log.e(TAG, "Error checking wishlist status: " + t.getMessage());
                }
            });
        }

        private void toggleWishlist(Product product) {
            Long userId = sessionManager.getUserDetails().getUserId();
            if (userId == null || userId == 0) {
                Toast.makeText(context, "Please log in to manage wishlist", Toast.LENGTH_SHORT).show();
                return;
            }

            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            if (isInWishlist) {
                Call<Void> call = apiService.removeFromWishlist(userId, product.getProductId());
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            isInWishlist = false;
                            ivWishlist.setImageResource(R.drawable.ic_heart);
                            Toast.makeText(context, "Removed from wishlist", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to remove from wishlist", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "Error removing from wishlist: " + t.getMessage());
                        Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Call<Wishlist> call = apiService.addToWishlist(userId, product.getProductId());
                call.enqueue(new Callback<Wishlist>() {
                    @Override
                    public void onResponse(Call<Wishlist> call, Response<Wishlist> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            isInWishlist = true;
                            ivWishlist.setImageResource(R.drawable.ic_heart_filled);
                            Toast.makeText(context, "Added to wishlist", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to add to wishlist", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Wishlist> call, Throwable t) {
                        Log.e(TAG, "Error adding to wishlist: " + t.getMessage());
                        Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}
