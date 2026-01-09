package com.example.cafeflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderDisplayAdapter extends RecyclerView.Adapter<OrderDisplayAdapter.OrderViewHolder> {

    private List<OrderModel> orders;
    private boolean isAdminMode;
    private OnOrderAdminControlsListener adminListener;

    public interface OnOrderAdminControlsListener {
        void onConfirmOrderClick(OrderModel order);
        void onGenerateBillClick(OrderModel order);
    }

    public OrderDisplayAdapter(List<OrderModel> orders, boolean isAdminMode, OnOrderAdminControlsListener listener) {
        this.orders = orders;
        this.isAdminMode = isAdminMode;
        this.adminListener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.unified_order_row, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderModel order = orders.get(position);

        holder.orderId.setText("Order ID: #" + order.getId());
        holder.orderStatus.setText("Status: " + order.getStatus().toString());
        holder.orderTotal.setText(String.format(Locale.US, "Total: $%.2f", order.getTotalAmount()));
        holder.orderDate.setText("Date: " + new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(order.getTimestamp()));

        StringBuilder itemsStr = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            itemsStr.append(item.getItemName()).append(" x").append(item.getQuantity()).append(", ");
        }
        holder.orderItems.setText("Items: " + itemsStr.toString());

        if (isAdminMode) {
            holder.customerName.setText("Customer: " + order.getCustomerName());
            holder.customerName.setVisibility(View.VISIBLE);
            holder.adminControls.setVisibility(View.VISIBLE);

            holder.confirmOrderButton.setOnClickListener(v -> adminListener.onConfirmOrderClick(order));
            holder.generateBillButton.setOnClickListener(v -> adminListener.onGenerateBillClick(order));
        } else {
            holder.customerName.setVisibility(View.GONE);
            holder.adminControls.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, customerName, orderDate, orderItems, orderStatus, orderTotal;
        LinearLayout adminControls;
        Button confirmOrderButton, generateBillButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.orderIdTextView);
            customerName = itemView.findViewById(R.id.customerNameTextView);
            orderDate = itemView.findViewById(R.id.orderDateTextView);
            orderItems = itemView.findViewById(R.id.orderItemsTextView);
            orderStatus = itemView.findViewById(R.id.orderStatusTextView);
            orderTotal = itemView.findViewById(R.id.orderTotalTextView);
            adminControls = itemView.findViewById(R.id.orderAdminControls);
            confirmOrderButton = itemView.findViewById(R.id.confirmOrderButton);
            generateBillButton = itemView.findViewById(R.id.generateBillButton);
        }
    }
}