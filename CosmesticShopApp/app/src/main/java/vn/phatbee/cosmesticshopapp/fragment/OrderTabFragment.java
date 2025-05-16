package vn.phatbee.cosmesticshopapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.adapter.OrderAdapter;
import vn.phatbee.cosmesticshopapp.model.Order;

public class OrderTabFragment extends Fragment {

    private static final String ARG_ORDERS = "orders";

    private List<Order> orders = new ArrayList<>();
    private OrderAdapter orderAdapter;
    private RecyclerView rvOrders;
    private TextView tvEmpty;

    public OrderTabFragment() {
        // Required empty public constructor
    }

    public static OrderTabFragment newInstance(List<Order> orders) {
        OrderTabFragment fragment = new OrderTabFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ORDERS, new ArrayList<>(orders));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orders = (List<Order>) getArguments().getSerializable(ARG_ORDERS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_tab, container, false);

        rvOrders = view.findViewById(R.id.rvOrders);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        orderAdapter = new OrderAdapter(getContext(), orders);
        rvOrders.setAdapter(orderAdapter);

        updateEmptyView();

        return view;
    }

    private void updateEmptyView() {
        if (orders == null || orders.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
        }
    }

    public void updateOrders(List<Order> newOrders) {
        this.orders = new ArrayList<>(newOrders); // Always update the orders list
        if (orderAdapter != null) {
            orderAdapter.updateOrders(newOrders);
            updateEmptyView();
        }
    }
}