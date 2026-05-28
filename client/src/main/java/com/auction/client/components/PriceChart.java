package com.auction.client.components;

import java.util.List;

import com.auction.common.entity.BidTransaction;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class PriceChart extends LineChart<Number, Number> {
    private final NumberAxis xAxis;
    private final NumberAxis yAxis;
    private final ObservableList<XYChart.Data<Number, Number>> dataList;
    private int pointCount = 0;

    public PriceChart() {
        super(new NumberAxis(), new NumberAxis());
        xAxis = (NumberAxis) getXAxis();
        yAxis = (NumberAxis) getYAxis();

        xAxis.setLabel("Lan dat gia");
        yAxis.setLabel("Gia (trieu VND)");
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override
            public String toString(Number object) {
                return String.format("%,.0f Tr", object.doubleValue());
            }
        });

        setTitle("Bieu do dau gia");
        setLegendVisible(false);
        setCreateSymbols(true);
        setAnimated(false);
        getStyleClass().add("chart-panel");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        dataList = FXCollections.observableArrayList();
        series.setData(dataList);
        getData().add(series);
    }

    public void addPricePoint(long timestamp, double price) {
        double priceInMillions = price / 1_000_000.0;
        pointCount++;
        dataList.add(new XYChart.Data<>((double) pointCount, priceInMillions));

        double currentMax = yAxis.getUpperBound();
        if (priceInMillions > currentMax) {
            yAxis.setUpperBound(priceInMillions * 1.1);
        }

        if (pointCount > 20) {
            xAxis.setLowerBound(pointCount - 20);
            xAxis.setUpperBound(pointCount);
        }
    }

    public void updateWithBidHistory(List<BidTransaction> history) {
        dataList.clear();
        pointCount = 0;

        if (history == null || history.isEmpty()) {
            addEmptyData();
            return;
        }

        for (BidTransaction bid : history) {
            double priceInMillions = bid.getAmount() / 1_000_000.0;
            pointCount++;
            dataList.add(new XYChart.Data<>((double) pointCount, priceInMillions));
        }

        double maxPrice = history.stream().mapToDouble(BidTransaction::getAmount).max().orElse(100_000_000);
        yAxis.setUpperBound(Math.ceil(maxPrice / 1_000_000) * 1.1);
        yAxis.setLowerBound(0);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(pointCount + 2);
    }

    private void addEmptyData() {
        dataList.add(new XYChart.Data<>(0, 0));
    }

    public void addNewBid(BidTransaction bid, int index) {
        double priceInMillions = bid.getAmount() / 1_000_000.0;
        pointCount++;
        dataList.add(new XYChart.Data<>((double) pointCount, priceInMillions));

        double currentMax = yAxis.getUpperBound();
        if (priceInMillions > currentMax) {
            yAxis.setUpperBound(priceInMillions * 1.1);
        }
    }

    public void clear() {
        dataList.clear();
        pointCount = 0;
        addEmptyData();
    }
}
