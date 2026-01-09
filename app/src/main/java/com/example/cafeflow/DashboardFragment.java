package com.example.cafeflow;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Calendar;

public class DashboardFragment extends Fragment {

    private TextView salesTodayLabel, ordersTodayLabel, pendingOrdersLabel, lowStockLabel;
    private BarChart salesChart;
    private ListView recentActivityList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        salesTodayLabel = view.findViewById(R.id.salesTodayLabel);
        ordersTodayLabel = view.findViewById(R.id.ordersTodayLabel);
        pendingOrdersLabel = view.findViewById(R.id.pendingOrdersLabel);
        lowStockLabel = view.findViewById(R.id.lowStockLabel);
        salesChart = view.findViewById(R.id.salesChart);
        recentActivityList = view.findViewById(R.id.recentActivityList);
        db = FirebaseFirestore.getInstance();

        setupChart();
        fetchDashboardData();

        return view;
    }

    private void setupChart() {
        salesChart.getDescription().setEnabled(false);
        salesChart.setDrawGridBackground(false);
        salesChart.setDrawBarShadow(false);
        salesChart.getAxisLeft().setDrawGridLines(false);
        salesChart.getAxisRight().setEnabled(false);

        XAxis xAxis = salesChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
    }

    private void fetchDashboardData() {
        db.collection("orders").addSnapshotListener((value, error) -> {
            if (error != null) return;
            if (value == null) return;

            double salesToday = 0;
            int totalOrders = 0;
            int pendingOrders = 0;
            List<String> recentActivities = new ArrayList<>();
            Map<String, Float> dailySales = new HashMap<>();

            for (QueryDocumentSnapshot doc : value) {
                OrderModel order = doc.toObject(OrderModel.class);
                totalOrders++;

                if (order.getStatus() == OrderStatus.PENDING) {
                    pendingOrders++;
                } else if (order.getStatus() == OrderStatus.COMPLETED) {
                    Date orderDate = order.getTimestamp();
                    if (isToday(orderDate)) {
                        salesToday += order.getTotalAmount();
                    }
                    recentActivities.add(String.format(Locale.US, "Sale: $%.2f to %s", order.getTotalAmount(), order.getCustomerName()));

                    if(orderDate != null) {
                         String dayKey = new SimpleDateFormat("MM-dd", Locale.US).format(orderDate);
                         float currentSales = dailySales.getOrDefault(dayKey, 0f);
                         dailySales.put(dayKey, currentSales + (float) order.getTotalAmount());
                    }
                }
            }

            salesTodayLabel.setText(String.format(Locale.US, "Today's Sales: $%.2f", salesToday));
            ordersTodayLabel.setText(String.format(Locale.US, "Total Orders: %d", totalOrders));
            pendingOrdersLabel.setText(String.format(Locale.US, "Pending Orders: %d", pendingOrders));

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, recentActivities);
            recentActivityList.setAdapter(adapter);

            updateSalesChart(dailySales);
        });

        db.collection("menu_items").whereLessThan("stockQuantity", 10).addSnapshotListener((value, error) -> {
            if (error != null) return;
            if (value != null) {
                lowStockLabel.setText(String.format(Locale.US, "Low Stock: %d", value.size()));
            }
        });
    }

    private void updateSalesChart(Map<String, Float> dailySales) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>(dailySales.keySet());
        Collections.sort(labels);

        for (int i = 0; i < labels.size(); i++) {
            entries.add(new BarEntry(i, dailySales.get(labels.get(i))));
        }

        salesChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        BarDataSet dataSet = new BarDataSet(entries, "Daily Sales");
        dataSet.setColor(Color.parseColor("#4E342E"));
        BarData barData = new BarData(dataSet);
        salesChart.setData(barData);
        salesChart.invalidate();
    }

    private boolean isToday(Date date) {
        if (date == null) return false;
        Calendar today = Calendar.getInstance();
        Calendar specifiedDate = Calendar.getInstance();
        specifiedDate.setTime(date);
        return today.get(Calendar.DAY_OF_YEAR) == specifiedDate.get(Calendar.DAY_OF_YEAR) &&
               today.get(Calendar.YEAR) == specifiedDate.get(Calendar.YEAR);
    }
}