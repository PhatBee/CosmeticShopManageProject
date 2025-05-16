package vn.phatbee.cosmesticshopapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.activity.OrderProductDetailsActivity;
import vn.phatbee.cosmesticshopapp.model.OrderLine;

public class OrderLineAdapter extends RecyclerView.Adapter<OrderLineAdapter.OrderLineViewHolder> {

    private Context context;
    private List<OrderLine> orderLine;

    public OrderLineAdapter(Context context, List<OrderLine> orderLine) {
        this.context = context;
        this.orderLine = orderLine;
    }

    @NonNull
    @Override
    public OrderLineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_product_detail, parent, false);
        return new OrderLineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderLineViewHolder holder, int position) {
        OrderLine orderLineRequest = orderLine.get(position);
        Map<String, Object> productSnapshot = orderLineRequest.getProductSnapshot();

        if (productSnapshot != null) {
            holder.tvProductName.setText(productSnapshot.get("productName").toString());
            holder.tvQuantity.setText("Số lượng: " + orderLineRequest.getQuantity());

            if (productSnapshot.containsKey("price")) {
                double price = Double.parseDouble(productSnapshot.get("price").toString());
                double totalPrice = price * orderLineRequest.getQuantity();
                holder.tvPrice.setText(String.format("%,.0f VND", totalPrice));
            } else {
                holder.tvPrice.setText("N/A");
            }

            // Hiển thị hình ảnh
            String imageUrl = productSnapshot.get("image") != null ? productSnapshot.get("image").toString() : "";
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.ivProductImage);
        }

        // Handle click event to view product details
        holder.itemContainer.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderProductDetailsActivity.class);
            intent.putExtra("productSnapshot", new HashMap<>(productSnapshot)); // Pass snapshot as HashMap
            intent.putExtra("productId", orderLineRequest.getProductId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderLine.size();
    }

    static class OrderLineViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvQuantity, tvPrice; // Thêm tvPrice
        LinearLayout itemContainer; // Container for click event

        public OrderLineViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice); // Khởi tạo tvPrice
            itemContainer = itemView.findViewById(R.id.itemContainer);

        }
    }
}