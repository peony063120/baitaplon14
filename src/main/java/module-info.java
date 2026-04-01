module com.auction.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.slf4j;

    opens com.auction.client to javafx.fxml;
    opens com.auction.client.controller to javafx.fxml;
    opens com.auction.client.model to com.fasterxml.jackson.databind;

    exports com.auction.client;
    exports com.auction.client.controller;
}