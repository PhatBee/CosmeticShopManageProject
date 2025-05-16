package vn.phatbee.cosmesticshopapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.activity.OrderDetailActivity;
import vn.phatbee.cosmesticshopapp.model.Order;
import vn.phatbee.cosmesticshopapp.model.OrderLine;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orders;

    public OrderAdapter(Context context, List<Order> orders) {
        this.context = context;
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);

        // Order ID
        holder.tvOrderId.setText("Mã đơn hàng: #" + order.getOrderId());

        // Order Date
        if (order.getOrderDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            holder.tvOrderDate.setText(order.getOrderDate().format(formatter));
        } else {
            holder.tvOrderDate.setText("N/A");
        }

        // Product Info
        if (order.getOrderLines() != null && !order.getOrderLines().isEmpty()) {
            // First Product
            OrderLine firstOrderLineRequest = order.getOrderLines().get(0);
            Map<String, Object> productSnapshot1 = firstOrderLineRequest.getProductSnapshot();
            if (productSnapshot1 != null) {
                holder.firstProductLayout.setVisibility(View.VISIBLE);
                holder.tvProductName1.setText(productSnapshot1.get("productName").toString());
                holder.tvQuantity1.setText("Số lượng: " + firstOrderLineRequest.getQuantity());
                String imageUrl1 = productSnapshot1.get("image") != null ? productSnapshot1.get("image").toString() : "";
                Glide.with(context)
                        .load(imageUrl1)
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(holder.ivProductImage1);
            } else {
                holder.firstProductLayout.setVisibility(View.GONE);
            }

            // Second Product (if exists)
            if (order.getOrderLines().size() > 1) {
                OrderLine secondOrderLineRequest = order.getOrderLines().get(1);
                Map<String, Object> productSnapshot2 = secondOrderLineRequest.getProductSnapshot();
                if (productSnapshot2 != null) {
                    holder.secondProductLayout.setVisibility(View.VISIBLE);
                    holder.tvProductName2.setText(productSnapshot2.get("productName").toString());
                    holder.tvQuantity2.setText("Số lượng: " + secondOrderLineRequest.getQuantity());
                    String imageUrl2 = productSnapshot2.get("image") != null ? productSnapshot2.get("image").toString() : "";
                    Glide.with(context)
                            .load(imageUrl2)
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(holder.ivProductImage2);
                } else {
                    holder.secondProductLayout.setVisibility(View.GONE);
                }
            } else {
                holder.secondProductLayout.setVisibility(View.GONE);
            }

            // Show expand button if more than 2 products
            if (order.getOrderLines().size() > 2) {
                holder.ivExpand.setVisibility(View.VISIBLE);
                holder.ivExpand.setOnClickListener(v -> {
                    toggleExpand(holder, order.getOrderLines());
                });
            } else {
                holder.ivExpand.setVisibility(View.GONE);
                holder.expandableProductContainer.setVisibility(View.GONE);
            }
        } else {
            holder.firstProductLayout.setVisibility(View.GONE);
            holder.secondProductLayout.setVisibility(View.GONE);
            holder.ivExpand.setVisibility(View.GONE);
            holder.expandableProductContainer.setVisibility(View.GONE);
        }

        // Total
        holder.tvTotal.setText("Tổng: " + String.format("%,.0f VND", order.getTotal()));

        // Status
        String statusText;
        int statusColor;
        switch (order.getOrderStatus()) {
            case "PENDING":
                statusText = "Chờ xác nhận";
                statusColor = context.getResources().getColor(android.R.color.holo_orange_light);
                break;
            case "PROCESSING":
                statusText = "Đang xử lý";
                statusColor = context.getResources().getColor(android.R.color.holo_blue_light);
                break;
            case "SHIPPING":
                statusText = "Đang giao hàng";
                statusColor = context.getResources().getColor(android.R.color.holo_blue_light);
                break;
            case "DELIVERED":
                statusText = "Đã giao hàng";
                statusColor = context.getResources().getColor(android.R.color.holo_green_light);
                break;
            case "CANCELLED":
                statusText = "Đã hủy";
                statusColor = context.getResources().getColor(android.R.color.holo_red_light);
                break;
            default:
                statusText = "Không xác định";
                statusColor = context.getResources().getColor(android.R.color.darker_gray);
        }
        holder.tvStatus.setText(statusText);
        holder.tvStatus.setTextColor(statusColor);

        // Xử lý sự kiện nhấn nút "Xem chi tiết"
        holder.btnViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("order", order);
            context.startActivity(intent);
        });
    }

    private void toggleExpand(OrderViewHolder holder, List<OrderLine> orderLineRequests) {
        if (holder.expandableProductContainer.getVisibility() == View.GONE) {
            holder.expandableProductContainer.removeAllViews();
            for (int i = 2; i < orderLineRequests.size(); i++) {
                OrderLine orderLineRequest = orderLineRequests.get(i);
                Map<String, Object> productSnapshot = orderLineRequest.getProductSnapshot();
                if (productSnapshot != null) {
                    View productView = LayoutInflater.from(context).inflate(R.layout.row_order_product, holder.expandableProductContainer, false);
                    ImageView ivProductImage = productView.findViewById(R.id.ivProductImage);
                    TextView tvProductName = productView.findViewById(R.id.tvProductName);
                    TextView tvQuantity = productView.findViewById(R.id.tvQuantity);

                    tvProductName.setText(productSnapshot.get("productName").toString());
                    tvQuantity.setText("Số lượng: " + orderLineRequest.getQuantity());
                    String imageUrl = productSnapshot.get("image") != null ? productSnapshot.get("image").toString() : "";
                    Glide.with(context)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(ivProductImage);

                    holder.expandableProductContainer.addView(productView);
                }
            }
            holder.expandableProductContainer.setVisibility(View.VISIBLE);
            holder.ivExpand.setImageResource(R.drawable.ic_expand_less);
        } else {
            holder.expandableProductContainer.setVisibility(View.GONE);
            holder.ivExpand.setImageResource(R.drawable.ic_expand_more);
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void updateOrders(List<Order> newOrders) {
        this.orders.clear();
        this.orders.addAll(newOrders);
        notifyDataSetChanged();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvTotal, tvStatus;
        LinearLayout firstProductLayout, secondProductLayout, expandableProductContainer;
        ImageView ivProductImage1, ivProductImage2, ivExpand;
        TextView tvProductName1, tvQuantity1, tvProductName2, tvQuantity2;
        Button btnViewDetails; // Thêm nút Xem chi tiết

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvStatus = itemView.findViewById(R.id.tvStatus);

            firstProductLayout = itemView.findViewById(R.id.firstProductLayout);
            ivProductImage1 = itemView.findViewById(R.id.ivProductImage1);
            tvProductName1 = itemView.findViewById(R.id.tvProductName1);
            tvQuantity1 = itemView.findViewById(R.id.tvQuantity1);

            secondProductLayout = itemView.findViewById(R.id.secondProductLayout);
            ivProductImage2 = itemView.findViewById(R.id.ivProductImage2);
            tvProductName2 = itemView.findViewById(R.id.tvProductName2);
            tvQuantity2 = itemView.findViewById(R.id.tvQuantity2);

            expandableProductContainer = itemView.findViewById(R.id.expandableProductContainer);
            ivExpand = itemView.findViewById(R.id.ivExpand);

            btnViewDetails = itemView.findViewById(R.id.btnViewDetails); // Khởi tạo nút
        }
    }
}