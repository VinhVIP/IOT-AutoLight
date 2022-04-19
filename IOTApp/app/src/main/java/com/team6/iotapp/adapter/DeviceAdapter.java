package com.team6.iotapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.team6.iotapp.MainActivity;
import com.team6.iotapp.databinding.ItemDeviceBinding;
import com.team6.iotapp.model.DeviceInfoModel;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private Context context;
    private List<DeviceInfoModel> deviceLists;

    public DeviceAdapter(Context context, List<DeviceInfoModel> deviceLists) {
        this.context = context;
        this.deviceLists = deviceLists;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemDeviceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindData(deviceLists.get(position));
    }

    @Override
    public int getItemCount() {
        return deviceLists == null ? 0 : deviceLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ItemDeviceBinding binding;

        public ViewHolder(ItemDeviceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindData(DeviceInfoModel device) {
            binding.tvDeviceName.setText(device.getDeviceName());
            binding.tvDeviceAddress.setText(device.getDeviceAddress());

            binding.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(context, MainActivity.class);
                // Send device details to the MainActivity
                intent.putExtra("deviceName", device.getDeviceName());
                intent.putExtra("deviceAddress", device.getDeviceAddress());
                // Call MainActivity
                context.startActivity(intent);
            });
        }
    }
}
