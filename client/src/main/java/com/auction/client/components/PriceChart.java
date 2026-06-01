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
    private double priceScale = 1.0;

    public PriceChart() {
        super(new NumberAxis(), new NumberAxis());
        xAxis = (NumberAxis) getXAxis();
        yAxis = (NumberAxis) getYAxis();

        xAxis.setLabel("Bid Count");
        yAxis.setLabel("Price (USD)");
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override
            public String toString(Number object) {
                double value = object.doubleValue() * priceScale;
                if (priceScale >= 1_000_000) {
                    return String.format("%,.1fM", value / 1_000_000.0);
                }
                if (priceScale >= 1_000) {
                    return String.format("%,.0fK", value / 1_000.0);
                }
                return String.format("%,.0f", value);
            }
        });

        setTitle("Price History");
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
        configureScale(Math.max(price, yAxis.getUpperBound() * priceScale));
        pointCount++;
        dataList.add(new XYChart.Data<>((double) pointCount, price / priceScale));

        double chartValue = price / priceScale;
        if (chartValue > yAxis.getUpperBound()) {
            yAxis.setUpperBound(chartValue * 1.15);
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
            configureScale(1_000);
            addEmptyData();
            return;
        }

        double maxPrice = history.stream().mapToDouble(BidTransaction::getAmount).max().orElse(1_000);
        double minPrice = history.stream().mapToDouble(BidTransaction::getAmount).min().orElse(0);
        configureScale(maxPrice);

        for (BidTransaction bid : history) {
            pointCount++;
            dataList.add(new XYChart.Data<>((double) pointCount, bid.getAmount() / priceScale));
        }

        double maxChartValue = maxPrice / priceScale;
        double minChartValue = minPrice / priceScale;
        yAxis.setUpperBound(Math.max(maxChartValue * 1.05, minChartValue + 1));
        if (maxPrice - minPrice < Math.max(maxPrice * 0.05, 1)) {
            yAxis.setLowerBound(Math.max(minChartValue * 0.995, 0));
        } else {
            yAxis.setLowerBound(0);
        }
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(Math.max(pointCount + 1, 2));
    }

    private void configureScale(double maxPrice) {
        if (maxPrice >= 1_000_000) {
            priceScale = 1_000_000.0;
            yAxis.setLabel("Price (M USD)");
        } else if (maxPrice >= 1_000) {
            priceScale = 1_000.0;
            yAxis.setLabel("Price (K USD)");
        } else {
            priceScale = 1.0;
            yAxis.setLabel("Price (USD)");
        }
    }

    private void addEmptyData() {
        dataList.add(new XYChart.Data<>(0, 0));
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(1);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(2);
    }

    public void addNewBid(BidTransaction bid, int index) {
        configureScale(Math.max(bid.getAmount(), yAxis.getUpperBound() * priceScale));
        pointCount++;
        dataList.add(new XYChart.Data<>((double) pointCount, bid.getAmount() / priceScale));

        double chartValue = bid.getAmount() / priceScale;
        if (chartValue > yAxis.getUpperBound()) {
            yAxis.setUpperBound(chartValue * 1.15);
        }
    }

    public void clear() {
        dataList.clear();
        pointCount = 0;
        addEmptyData();
    }
}
