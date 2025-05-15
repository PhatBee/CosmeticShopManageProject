package vn.phatbee.cosmesticshopapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.activity.EditAddressActivity;
import vn.phatbee.cosmesticshopapp.manager.UserSessionManager;
import vn.phatbee.cosmesticshopapp.model.Address;
import vn.phatbee.cosmesticshopapp.retrofit.ApiService;
import vn.phatbee.cosmesticshopapp.retrofit.RetrofitClient;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private List<Address> addresses;
    private Context context;
    private ApiService apiService;
    private UserSessionManager sessionManager;
    private ActivityResultLauncher<Intent> editAddressLauncher;
    private OnAddressSelectedListener selectListener;
    private int selectedPosition = -1;

    public interface OnAddressSelectedListener {
        void onAddressSelected(Address address);
    }

    public AddressAdapter(Context context, List<Address> addresses, ActivityResultLauncher<Intent> editAddressLauncher) {
        this.context = context;
        this.addresses = addresses;
        this.apiService = RetrofitClient.getClient().create(ApiService.class);
        this.sessionManager = new UserSessionManager(context);
        this.editAddressLauncher = editAddressLauncher;
    }

    public void setOnAddressSelectedListener(OnAddressSelectedListener listener) {
        this.selectListener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addresses.get(position);

        holder.tvName.setText(address.getReceiverName());
        holder.tvPhone.setText(address.getReceiverPhone());
        holder.tvAddress.setText(String.format("%s, %s, %s, %s",
                address.getAddress(),
                address.getWard() != null ? address.getWard() : "",
                address.getDistrict() != null ? address.getDistrict() : "",
                address.getProvince() != null ? address.getProvince() : ""));
        holder.tvDefault.setVisibility(address.isDefaultAddress() ? View.VISIBLE : View.GONE);

        // Làm nổi bật item được chọn
        holder.itemView.setBackgroundResource(selectedPosition == position ? R.drawable.selected_background : 0);

        // Xử lý click trên toàn bộ item để chọn địa chỉ
        holder.itemView.setOnClickListener(v -> {
            if (selectListener != null) {
                selectedPosition = holder.getAdapterPosition();
                notifyDataSetChanged();
                selectListener.onAddressSelected(address);
            }
        });

        holder.ivEditAddress.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditAddressActivity.class);
            intent.putExtra("address", address);
            editAddressLauncher.launch(intent);
        });

        holder.ivDeleteAddress.setOnClickListener(v -> {
            Long userId = sessionManager.getUserDetails().getUserId();
            if (userId == null || userId == 0) {
                Toast.makeText(context, "Please log in to delete address", Toast.LENGTH_SHORT).show();
                return;
            }

            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }

            new android.app.AlertDialog.Builder(context)
                    .setTitle("Delete Address")
                    .setMessage("Do you want to delete this address?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        Address addressToDelete = addresses.get(currentPosition);
                        Call<Void> call = apiService.deleteAddress(addressToDelete.getAddressId());
                        call.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    addresses.remove(currentPosition);
                                    notifyItemRemoved(currentPosition);
                                    notifyItemRangeChanged(currentPosition, addresses.size());
                                    Toast.makeText(context, "Delete Successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(context, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    public void updateAddresses(List<Address> newAddresses) {
        this.addresses.clear();
        this.addresses.addAll(newAddresses);
        selectedPosition = -1; // Reset selected position when updating list
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvAddress, tvDefault;
        ImageView ivEditAddress, ivDeleteAddress;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDefault = itemView.findViewById(R.id.tvDefault);
            ivEditAddress = itemView.findViewById(R.id.ivEditAddress);
            ivDeleteAddress = itemView.findViewById(R.id.ivDeleteAddress);
        }
    }
}