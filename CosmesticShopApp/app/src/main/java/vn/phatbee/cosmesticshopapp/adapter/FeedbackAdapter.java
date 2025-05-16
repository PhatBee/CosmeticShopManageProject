package vn.phatbee.cosmesticshopapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.model.ProductFeedback;
import vn.phatbee.cosmesticshopapp.model.User;
import vn.phatbee.cosmesticshopapp.retrofit.ApiService;
import vn.phatbee.cosmesticshopapp.retrofit.RetrofitClient;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.ViewHolder> {

    private Context context;
    private List<ProductFeedback> feedbackList;

    public FeedbackAdapter(Context context, List<ProductFeedback> feedbackList) {
        this.context = context;
        this.feedbackList = feedbackList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_feedback, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductFeedback feedback = feedbackList.get(position);

        holder.tvComment.setText(feedback.getComment() != null ? feedback.getComment() : "");
        holder.ratingBar.setRating(feedback.getRating() != null ? feedback.getRating().floatValue() : 0f);

        if (feedback.getImage() != null && !feedback.getImage().isEmpty()) {
            Glide.with(context)
                    .load(feedback.getImage())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.ivFeedbackImage);
        } else {
            holder.ivFeedbackImage.setVisibility(View.GONE);
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<User> call = apiService.getUser(feedback.getCustomerId());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    holder.tvReviewerName.setText(user.getFullName() != null ? user.getFullName() : "Anonymous");
                } else {
                    holder.tvReviewerName.setText("Anonymous");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                holder.tvReviewerName.setText("Anonymous");
            }
        });
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvReviewerName, tvComment;
        RatingBar ratingBar;
        ImageView ivFeedbackImage;

        ViewHolder(View itemView) {
            super(itemView);
            tvReviewerName = itemView.findViewById(R.id.tvReviewerName);
            tvComment = itemView.findViewById(R.id.tvComment);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            ivFeedbackImage = itemView.findViewById(R.id.ivFeedbackImage);
        }
    }
}