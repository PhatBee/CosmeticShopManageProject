package vn.phatbee.cosmesticshopapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.activity.ReviewActivity;
import vn.phatbee.cosmesticshopapp.model.OrderLine;
import vn.phatbee.cosmesticshopapp.model.ProductFeedback;

public class ReviewItemAdapter extends RecyclerView.Adapter<ReviewItemAdapter.ReviewItemViewHolder> {

    private Context context;
    private List<OrderLine> orderLines;
    private int orderId;
    private List<ProductFeedback> feedbackList;

    public ReviewItemAdapter(Context context, List<OrderLine> orderLines, int orderId) {
        this.context = context;
        this.orderLines = orderLines;
        this.orderId = orderId;
        this.feedbackList = new ArrayList<>();
    }

    public void setFeedbackList(List<ProductFeedback> feedbackList) {
        this.feedbackList = feedbackList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review_product, parent, false);
        return new ReviewItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewItemViewHolder holder, int position) {
        OrderLine orderLine = orderLines.get(position);
        Map<String, Object> productSnapshot = orderLine.getProductSnapshot();

        if (productSnapshot != null) {
            holder.tvProductName.setText(productSnapshot.get("productName") != null ? productSnapshot.get("productName").toString() : "N/A");

            String imageUrl = productSnapshot.get("image") != null ? productSnapshot.get("image").toString() : "";
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.ivProductImage);

            // Check if feedback exists for this product
            ProductFeedback existingFeedback = findFeedbackForProduct(orderLine.getProductId());
            if (existingFeedback != null) {
                holder.btnReview.setText("Chỉnh sửa đánh giá");
                holder.btnReview.setOnClickListener(v -> {
                    Intent intent = new Intent(context, ReviewActivity.class);
                    intent.putExtra("productId", orderLine.getProductId());
                    intent.putExtra("orderId", orderId);
                    intent.putExtra("productSnapshot", new HashMap<>(productSnapshot));
                    intent.putExtra("existingFeedback", existingFeedback);
                    context.startActivity(intent);
                });
            } else {
                holder.btnReview.setText("Đánh giá");
                holder.btnReview.setOnClickListener(v -> {
                    Intent intent = new Intent(context, ReviewActivity.class);
                    intent.putExtra("productId", orderLine.getProductId());
                    intent.putExtra("orderId", orderId);
                    intent.putExtra("productSnapshot", new HashMap<>(productSnapshot));
                    context.startActivity(intent);
                });
            }
        }
    }

    private ProductFeedback findFeedbackForProduct(Long productId) {
        if (productId == null) return null;
        for (ProductFeedback feedback : feedbackList) {
            if (productId.equals(feedback.getProductId())) {
                return feedback;
            }
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return orderLines.size();
    }

    static class ReviewItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        Button btnReview;

        public ReviewItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            btnReview = itemView.findViewById(R.id.btnReview);
        }
    }
}