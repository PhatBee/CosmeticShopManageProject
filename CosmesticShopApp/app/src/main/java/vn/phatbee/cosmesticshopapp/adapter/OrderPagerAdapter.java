package vn.phatbee.cosmesticshopapp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

import vn.phatbee.cosmesticshopapp.fragment.OrderTabFragment;
import vn.phatbee.cosmesticshopapp.model.Order;

public class OrderPagerAdapter extends FragmentStateAdapter {

    private final List<OrderTabFragment> fragments = new ArrayList<>();

    public OrderPagerAdapter(@NonNull FragmentActivity fragment) {
        super(fragment);
        // Initialize fragments for each tab
        fragments.add(OrderTabFragment.newInstance(new ArrayList<>()));
        fragments.add(OrderTabFragment.newInstance(new ArrayList<>()));
        fragments.add(OrderTabFragment.newInstance(new ArrayList<>()));
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return 3; // Active, Completed, Cancelled
    }

    public void updateFragments(List<Order> active, List<Order> completed, List<Order> cancelled) {
        fragments.get(0).updateOrders(active);
        fragments.get(1).updateOrders(completed);
        fragments.get(2).updateOrders(cancelled);
    }
}