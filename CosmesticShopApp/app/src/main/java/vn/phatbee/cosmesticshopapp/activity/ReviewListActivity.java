package vn.phatbee.cosmesticshopapp.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.adapter.ReviewItemAdapter;
import vn.phatbee.cosmesticshopapp.model.OrderLine;
import vn.phatbee.cosmesticshopapp.model.ProductFeedback;
import vn.phatbee.cosmesticshopapp.retrofit.ApiService;
import vn.phatbee.cosmesticshopapp.retrofit.RetrofitClient;

public class ReviewListActivity extends AppCompatActivity {

    private RecyclerView rvReviewItems;
    private ReviewItemAdapter reviewItemAdapter;
    private List<OrderLine> orderLines;
    private ImageView ivBack;
    private ApiService apiService;
    private int orderId;
    private boolean isFetching = false; // Prevent duplicate API calls

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_list);

        // Initialize views
        rvReviewItems = findViewById(R.id.rvReviewItems);
        ivBack = findViewById(R.id.ivBack);
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Get data from Intent
        orderLines = (ArrayList<OrderLine>) getIntent().getSerializableExtra("orderLines");
        orderId = getIntent().getIntExtra("orderId", -1);
        if (orderLines == null || orderLines.isEmpty() || orderId == -1) {
            Toast.makeText(this, "Không thể tải danh sách sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup RecyclerView
        reviewItemAdapter = new ReviewItemAdapter(this, orderLines, orderId);
        rvReviewItems.setLayoutManager(new LinearLayoutManager(this));
        rvReviewItems.setAdapter(reviewItemAdapter);

        // Fetch existing feedback for the order
        fetchProductFeedback();

        // Handle back button
        ivBack.setOnClickListener(v -> finish());


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when activity is revisited
        fetchProductFeedback();
    }

    private void fetchProductFeedback() {
        isFetching = true;
        Call<List<ProductFeedback>> call = apiService.getFeedbackByOrderId(orderId);
        call.enqueue(new Callback<List<ProductFeedback>>() {
            @Override
            public void onResponse(Call<List<ProductFeedback>> call, Response<List<ProductFeedback>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ProductFeedback> feedbackList = response.body();
                    reviewItemAdapter.setFeedbackList(feedbackList);
                } else {
                    Log.e("ReviewList", "Failed to fetch feedback: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<ProductFeedback>> call, Throwable t) {
                isFetching = false;
                Log.e("ReviewList", "Error fetching feedback: " + t.getMessage());
            }
        });
    }
}