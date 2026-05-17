package com.auction.client.components;

import com.auction.common.entity.BidTransaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PriceChart extends LineChart<Number, Number> {
    private final NumberAxis xAxis;
    private final NumberAxis yAxis;
    private final XYChart.Series<Number, Number> series;
    private final ObservableList<XYChart.Data<Number, Number>> dataList;
    private long startTime = System.currentTimeMillis();

    public PriceChart() {
        super(new NumberAxis(), new NumberAxis());
        xAxis = (NumberAxis) getXAxis();
        yAxis = (NumberAxis) getYAxis();

        xAxis.setLabel("Thời gian (phút trước)");
        yAxis.setLabel("Giá (Triệu đồng)");
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override
            public String toString(Number object) {
                return String.format("%,.0f Tr", object.doubleValue());
            }
        });

        setTitle("📈 Biểu đồ giá đấu theo thời gian");
        setLegendVisible(false);
        setCreateSymbols(true);
        setAnimated(false);
        setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        series = new XYChart.Series<>();
        dataList = FXCollections.observableArrayList();
        series.setData(dataList);
        getData().add(series);
    }

    public void updateWithBidHistory(List<BidTransaction> history) {
        dataList.clear();
        if (history == null || history.isEmpty()) {
            addEmptyData();
            return;
        }

        LocalDateTime firstTime = history.get(0).getBidTime();
        for (int i = 0; i < history.size(); i++) {
            BidTransaction bid = history.get(i);
            long minutesAgo = java.time.Duration.between(bid.getBidTime(), firstTime).toMinutes();
            double priceInMillions = bid.getAmount() / 1_000_000.0;
            dataList.add(new XYChart.Data<>((double) minutesAgo, priceInMillions));
        }

        // Set axis ranges
        double maxPrice = history.stream().mapToDouble(BidTransaction::getAmount).max().orElse(100_000_000);
        yAxis.setUpperBound(Math.ceil(maxPrice / 1_000_000) * 1.1);
        yAxis.setLowerBound(0);
    }

    private void addEmptyData() {
        dataList.add(new XYChart.Data<>(0, 0));
    }

    public void addNewBid(BidTransaction bid, int index) {
        LocalDateTime firstTime = dataList.isEmpty() ? bid.getBidTime() : null;
        if (!dataList.isEmpty() && dataList.get(0).getXValue() != null) {
            // Recalculate from scratch for simplicity
            // Or just add new point
        }
        double priceInMillions = bid.getAmount() / 1_000_000.0;
        dataList.add(new XYChart.Data<>((double) dataList.size(), priceInMillions));

        // Update max value
        double currentMax = yAxis.getUpperBound();
        if (priceInMillions > currentMax) {
            yAxis.setUpperBound(priceInMillions * 1.1);
        }
    }

    public void clear() {
        dataList.clear();
        addEmptyData();
    }
}