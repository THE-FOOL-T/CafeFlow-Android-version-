package com.example.cafeflow;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderListFragment extends Fragment implements OrderDisplayAdapter.OnOrderAdminControlsListener {

    private static final String ARG_SHOW_ALL_ORDERS = "showAllOrders";

    private OrderDisplayAdapter orderAdapter;
    private List<OrderModel> orderList = new ArrayList<>();
    private FirebaseFirestore db;
    private boolean showAllOrders;

    public static OrderListFragment newInstance(boolean showAllOrders) {
        OrderListFragment fragment = new OrderListFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_SHOW_ALL_ORDERS, showAllOrders);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            showAllOrders = getArguments().getBoolean(ARG_SHOW_ALL_ORDERS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_generic_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.generic_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        db = FirebaseFirestore.getInstance();

        orderAdapter = new OrderDisplayAdapter(orderList, showAllOrders, this);
        recyclerView.setAdapter(orderAdapter);

        fetchOrders();

        return view;
    }

    private void fetchOrders() {
        Query query;
        if (showAllOrders) {
            query = db.collection("orders").orderBy("timestamp", Query.Direction.DESCENDING);
        } else {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String uid = currentUser.getUid();
                query = db.collection("orders").whereEqualTo("customerId", uid)
                          .orderBy("timestamp", Query.Direction.DESCENDING);
            } else {
                return;
            }
        }

        query.addSnapshotListener((value, error) -> {
            if (error != null) return;
            if (value == null) return;
            orderList.clear();
            for (QueryDocumentSnapshot doc : value) {
                orderList.add(doc.toObject(OrderModel.class));
            }
            orderAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onConfirmOrderClick(OrderModel order) {
        if (order.getStatus() == OrderStatus.PENDING) {
            db.collection("orders").document(order.getId()).update("status", OrderStatus.CONFIRMED);
        } else {
            Toast.makeText(getContext(), "Order already confirmed or completed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGenerateBillClick(OrderModel order) {
        if (order.getStatus() == OrderStatus.COMPLETED) {
            Toast.makeText(getContext(), "Bill already generated", Toast.LENGTH_SHORT).show();
        } else {
            db.collection("orders").document(order.getId()).update("status", OrderStatus.COMPLETED);
            showBillDialog(order);
        }
    }

    private void showBillDialog(OrderModel order) {
        StringBuilder billDetails = new StringBuilder();
        billDetails.append("Order ID: ").append(order.getId()).append("\n");
        billDetails.append("Customer: ").append(order.getCustomerName()).append("\n\n");
        for (OrderItem item : order.getItems()) {
            billDetails.append(item.getItemName()).append(" x").append(item.getQuantity())
                       .append(" - $").append(String.format(Locale.US, "%.2f", item.getPrice() * item.getQuantity()))
                       .append("\n");
        }
        billDetails.append("\nSubtotal: ").append(String.format(Locale.US, "$%.2f", order.getTotalAmount()));
        double tax = order.getTotalAmount() * 0.05;
        billDetails.append("\nTax (5%): ").append(String.format(Locale.US, "$%.2f", tax));
        billDetails.append("\nTotal: ").append(String.format(Locale.US, "$%.2f", order.getTotalAmount() + tax));

        new AlertDialog.Builder(getContext())
                .setTitle("Receipt")
                .setMessage(billDetails.toString())
                .setPositiveButton("OK", null)
                .show();
    }
}