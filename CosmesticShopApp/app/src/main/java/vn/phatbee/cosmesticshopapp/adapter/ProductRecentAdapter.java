package vn.phatbee.cosmesticshopapp.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.activity.LoginActivity;
import vn.phatbee.cosmesticshopapp.activity.WishlistActivity;
import vn.phatbee.cosmesticshopapp.manager.UserSessionManager;
import vn.phatbee.cosmesticshopapp.model.Product;
import vn.phatbee.cosmesticshopapp.model.Wishlist;
import vn.phatbee.cosmesticshopapp.retrofit.ApiService;
import vn.phatbee.cosmesticshopapp.retrofit.RetrofitClient;

public class ProductRecentAdapter extends RecyclerView.Adapter<ProductRecentAdapter.ProductViewHolder> {
    private static final String TAG = "ProductRecentAdapter";
    private Context context;
    private List<Product> products;
    private OnProductRecentClickListener listener;
    private UserSessionManager sessionManager;

    public interface OnProductRecentClickListener {
        void onProductRecentClick(Product product);
    }

    public ProductRecentAdapter(Context context, OnProductRecentClickListener listener, UserSessionManager sessionManager) {
        this.context = context;
        this.products = new ArrayList<>();
        this.listener = listener;
        this.sessionManager = sessionManager;
    }

    public void setProducts(List<Product> products) {
        this.products.clear();
        this.products.addAll(products);
        notifyDataSetChanged();
    }

    public void refreshWishlistStatus() {
        notifyDataSetChanged(); // Làm mới toàn bộ danh sách để cập nhật trạng thái wishlist
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

            ivWishlist.setOnClickListener(v -> {
                Product product = products.get(getAdapterPosition());
                if (!checkLogin(product)) return;
                // Thêm animation
                ivWishlist.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100)
                        .withEndAction(() -> ivWishlist.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                        .start();
                toggleWishlist(product);
            });
        }

        void bind(Product product) {
            tvProductName.setText(product.getProductName() != null ? product.getProductName() : "Unknown Product");
            Double price = product.getPrice();
            if (price != null) {
                tvProductPrice.setText(String.format("%,.0f VND", price));
            } else {
                tvProductPrice.setText("Price Unavailable");
            }
            Glide.with(context)
                    .load(product.getImage())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(ivProductImage);

            if (sessionManager.isLoggedIn()) {
                ivWishlist.setEnabled(true);
                ivWishlist.setAlpha(1.0f);
                checkWishlistStatus(product);
            } else {
                ivWishlist.setImageResource(R.drawable.ic_heart);
                ivWishlist.setEnabled(false);
                ivWishlist.setAlpha(0.5f);
                isInWishlist = false;
            }
        }

        private boolean checkLogin(Product product) {
            if (!sessionManager.isLoggedIn()) {
                Toast.makeText(context, "Please log in to manage wishlist", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, LoginActivity.class);
                intent.putExtra("PRODUCT_ID", product.getProductId());
                context.startActivity(intent);
                return false;
            }
            return true;
        }

        private void checkWishlistStatus(Product product) {
            Long userId = sessionManager.getUserDetails().getUserId();
            if (userId == null || userId == 0) {
                isInWishlist = false;
                ivWishlist.setImageResource(R.drawable.ic_heart);
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
                    } else {
                        isInWishlist = false;
                        ivWishlist.setImageResource(R.drawable.ic_heart);
                        Log.e(TAG, "Failed to check wishlist status: HTTP " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    isInWishlist = false;
                    ivWishlist.setImageResource(R.drawable.ic_heart);
                    Log.e(TAG, "Error checking wishlist status: " + t.getMessage());
                }
            });
        }

        private void toggleWishlist(Product product) {
            Long userId = sessionManager.getUserDetails().getUserId();
            if (userId == null || userId == 0) {
                Log.e(TAG, "Invalid userId in toggleWishlist");
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
                            Snackbar.make(itemView, "Removed from wishlist", Snackbar.LENGTH_LONG)
                                    .setAction("Go to Wishlist", v -> {
                                        Intent intent = new Intent(context, WishlistActivity.class);
                                        context.startActivity(intent);
                                    })
                                    .show();
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
                            Snackbar.make(itemView, "Added to wishlist", Snackbar.LENGTH_LONG)
                                    .setAction("Go to Wishlist", v -> {
                                        Intent intent = new Intent(context, WishlistActivity.class);
                                        context.startActivity(intent);
                                    })
                                    .show();
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