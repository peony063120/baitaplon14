package com.auction.client.model;

import javafx.scene.chart.XYChart;

public class PriceChartModel {

    private XYChart.Series<Number, Number> series;

    public PriceChartModel() {
        this.series = new XYChart.Series<>();
        this.series.setName("Bid Price");
    }

    /**
     * Thêm một điểm dữ liệu giá theo thời gian vào biểu đồ
     *
     * @param time  thời gian (milliseconds hoặc timestamp)
     * @param price giá đấu tại thời điểm đó
     */
    public void addPoint(long time, double price) {
        XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(time, price);
        series.getData().add(dataPoint);
    }

    /**
     * Lấy series dữ liệu để hiển thị trên biểu đồ
     */
    public XYChart.Series<Number, Number> getSeries() {
        return series;
    }

    /**
     * Xóa toàn bộ dữ liệu trên biểu đồ
     */
    public void clear() {
        series.getData().clear();
    }

    public void setSeries(XYChart.Series<Number, Number> series) {
        this.series = series;
    }

    @Override
    public String toString() {
        return "PriceChartModel{" +
                "dataPoints=" + series.getData().size() +
                '}';
    }
}