package com.example.cafeflow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class MenuManagementFragment extends Fragment implements MenuAdapter.OnAdminControlsListener {

    private EditText itemNameField, itemPriceField, itemStockField;
    private RecyclerView menuManagementRecyclerView;
    private FirebaseFirestore db;

    private ArrayList<MenuItem> menuItems = new ArrayList<>();
    private MenuAdapter menuAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu_management, container, false);

        itemNameField = view.findViewById(R.id.itemNameField);
        itemPriceField = view.findViewById(R.id.itemPriceField);
        itemStockField = view.findViewById(R.id.itemStockField);
        menuManagementRecyclerView = view.findViewById(R.id.menuManagementRecyclerView);
        Button addItemButton = view.findViewById(R.id.addItemButton);

        db = FirebaseFirestore.getInstance();
        menuManagementRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        menuAdapter = new MenuAdapter(menuItems, this, true);
        menuManagementRecyclerView.setAdapter(menuAdapter);

        addItemButton.setOnClickListener(v -> {
            String name = itemNameField.getText().toString();
            String priceStr = itemPriceField.getText().toString();
            String stockStr = itemStockField.getText().toString();

            if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String id = db.collection("menu_items").document().getId();
            MenuItem item = new MenuItem(id, name, Double.parseDouble(priceStr), Integer.parseInt(stockStr), "", "");

            db.collection("menu_items").document(id).set(item)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Item Added!", Toast.LENGTH_SHORT).show();
                        itemNameField.setText("");
                        itemPriceField.setText("");
                        itemStockField.setText("");
                    });
        });

        fetchMenuItems();

        return view;
    }

    private void fetchMenuItems() {
        db.collection("menu_items").addSnapshotListener((value, error) -> {
            if (error != null) return;
            if (value == null) return;
            menuItems.clear();
            for (QueryDocumentSnapshot doc : value) {
                menuItems.add(doc.toObject(MenuItem.class));
            }
            menuAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onIncreaseStockClick(MenuItem item) {
        db.collection("menu_items").document(item.getId()).update("stockQuantity", item.getStockQuantity() + 1);
    }

    @Override
    public void onDecreaseStockClick(MenuItem item) {
        if (item.getStockQuantity() > 0) {
            db.collection("menu_items").document(item.getId()).update("stockQuantity", item.getStockQuantity() - 1);
        }
    }

    @Override
    public void onRemoveItemClick(MenuItem item) {
        db.collection("menu_items").document(item.getId()).delete();
    }

    @Override
    public void onAddToCartClick(MenuItem item) {

    }
}