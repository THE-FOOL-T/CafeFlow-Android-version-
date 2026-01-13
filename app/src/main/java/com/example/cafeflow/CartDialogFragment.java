package com.example.cafeflow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.List;
import java.util.Locale;

public class CartDialogFragment extends DialogFragment {

    private CartViewModel cartViewModel;

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int)(getResources().getDisplayMetrics().widthPixels * 0.95);
            int height = (int)(getResources().getDisplayMetrics().heightPixels * 0.80);
            getDialog().getWindow().setLayout(width, height);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_cart, container, false);

        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        RecyclerView cartRecyclerView = view.findViewById(R.id.cartRecyclerView);
        TextView subtotalTextView = view.findViewById(R.id.subtotalTextView);
        TextView taxTextView = view.findViewById(R.id.taxTextView);
        TextView totalTextView = view.findViewById(R.id.totalTextView);
        Button placeOrderButton = view.findViewById(R.id.placeOrderButton);

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), cartItems -> {
            CartAdapter cartAdapter = new CartAdapter(cartItems);
            cartRecyclerView.setAdapter(cartAdapter);
        });

        cartViewModel.getTotalAmount().observe(getViewLifecycleOwner(), total -> {
            if (total != null) {
                double tax = total * 0.05;
                subtotalTextView.setText(String.format(Locale.US, "Subtotal: $%.2f", total));
                taxTextView.setText(String.format(Locale.US, "Tax (5%%): $%.2f", tax));
                totalTextView.setText(String.format(Locale.US, "Total: $%.2f", total + tax));
            }
        });

        placeOrderButton.setOnClickListener(v -> placeOrder(db, mAuth));

        return view;
    }

    private void placeOrder(FirebaseFirestore db, FirebaseAuth mAuth) {
        List<OrderItem> cartItems = cartViewModel.getCartItems().getValue();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Double totalAmount = cartViewModel.getTotalAmount().getValue();

        if (cartItems == null || cartItems.isEmpty() || totalAmount == null) {
            Toast.makeText(getContext(), "Cart is empty or total is invalid.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentUser == null) {
            Toast.makeText(getContext(), "You must be logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String customerId = currentUser.getUid();
        db.collection("users").document(customerId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    WriteBatch batch = db.batch();

                    // Create order
                    String orderId = db.collection("orders").document().getId();
                    OrderModel order = new OrderModel(orderId, customerId, user.getName(), cartItems, totalAmount, OrderStatus.PENDING);
                    batch.set(db.collection("orders").document(orderId), order);

                    //  stock decreasing
                    for (OrderItem item : cartItems) {
                        DocumentReference menuItemRef = db.collection("menu_items").document(item.getItemId());
                        batch.update(menuItemRef, "stockQuantity", com.google.firebase.firestore.FieldValue.increment(-item.getQuantity()));
                    }

                    //commit batch
                    batch.commit().addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Order placed successfully!", Toast.LENGTH_SHORT).show();
                        cartViewModel.clearCart();
                        dismiss();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to place order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    // adapter for cart item
    private class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

        private List<OrderItem> cartItems;

        public CartAdapter(List<OrderItem> cartItems) {
            this.cartItems = cartItems;
        }

        @NonNull
        @Override
        public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item, parent, false);
            return new CartViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
            OrderItem item = cartItems.get(position);
            holder.itemName.setText(item.getItemName());
            holder.itemQuantity.setText(String.format(Locale.US, "x%d", item.getQuantity()));
            holder.itemPrice.setText(String.format(Locale.US, "$%.2f", item.getPrice() * item.getQuantity()));
            holder.removeItemButton.setOnClickListener(v -> cartViewModel.removeFromCart(item));
        }

        @Override
        public int getItemCount() {
            return cartItems.size();
        }

        class CartViewHolder extends RecyclerView.ViewHolder {
            TextView itemName, itemQuantity, itemPrice;
            Button removeItemButton;

            public CartViewHolder(@NonNull View itemView) {
                super(itemView);
                itemName = itemView.findViewById(R.id.cartItemNameTextView);
                itemQuantity = itemView.findViewById(R.id.cartItemQuantityTextView);
                itemPrice = itemView.findViewById(R.id.cartItemPriceTextView);
                removeItemButton = itemView.findViewById(R.id.cartItemRemoveButton);
            }
        }
    }
}