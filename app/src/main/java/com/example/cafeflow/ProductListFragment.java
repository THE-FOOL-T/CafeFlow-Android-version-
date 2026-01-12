package com.example.cafeflow;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ProductListFragment extends Fragment implements MenuAdapter.OnAdminControlsListener {

    private static final String ARG_CATEGORY = "category";

    private RecyclerView menuRecyclerView;
    private MenuAdapter menuAdapter;
    private List<MenuItem> allMenuItems = new ArrayList<>();
    private FirebaseFirestore db;
    private CartViewModel cartViewModel;

    public static ProductListFragment newInstance(String category) {
        ProductListFragment fragment = new ProductListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        TextView greetingTextView = view.findViewById(R.id.greetingTextView);
        EditText searchBar = view.findViewById(R.id.searchBar);
        menuRecyclerView = view.findViewById(R.id.menuRecyclerView);
        FloatingActionButton viewCartButton = view.findViewById(R.id.viewCartButton);

        menuRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        menuAdapter = new MenuAdapter(new ArrayList<>(), this, false);
        menuRecyclerView.setAdapter(menuAdapter);

        if (mAuth.getCurrentUser() != null) {
            db.collection("users").document(mAuth.getCurrentUser().getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        greetingTextView.setText("Good Morning, " + user.getName());
                    }
                }
            });
        }

        fetchMenuItems();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        viewCartButton.setOnClickListener(v -> {
            new CartDialogFragment().show(getParentFragmentManager(), "CartDialog");
        });

        return view;
    }

    private void fetchMenuItems() {
        Query query = db.collection("menu_items");

        if (getArguments() != null && getArguments().getString(ARG_CATEGORY) != null) {
            query = query.whereEqualTo("category", getArguments().getString(ARG_CATEGORY));
        }

        query.addSnapshotListener((value, error) -> {
            if (error != null) return;
            if (value == null) return;
            allMenuItems.clear();
            for (QueryDocumentSnapshot doc : value) {
                allMenuItems.add(doc.toObject(MenuItem.class));
            }
            // update the adapter with the full list
            menuAdapter = new MenuAdapter(allMenuItems, this, false);
            menuRecyclerView.setAdapter(menuAdapter);
        });
    }

    private void filter(String text) {
        List<MenuItem> filteredList = new ArrayList<>();
        for (MenuItem item : allMenuItems) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        // Create a new adapter with the filtered list and set it
        menuAdapter = new MenuAdapter(filteredList, this, false);
        menuRecyclerView.setAdapter(menuAdapter);
    }

    @Override
    public void onIncreaseStockClick(MenuItem item) { /* Not used */ }

    @Override
    public void onDecreaseStockClick(MenuItem item) { /* Not used */ }

    @Override
    public void onRemoveItemClick(MenuItem item) { /* Not used */ }

    @Override
    public void onAddToCartClick(MenuItem item) {
        cartViewModel.addToCart(item);
    }
}