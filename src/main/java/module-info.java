module org.example.cardcollectorproject {

    requires com.fasterxml.jackson.databind;
    requires com.azure.cosmos;

    exports org.example.cardcollectorproject.models to com.fasterxml.jackson.databind;

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires com.google.gson;
    requires java.net.http;

    requires java.desktop;
    requires java.smartcardio;
    requires spring.data.commons;
    requires javafx.media;

    opens org.example.cardcollectorproject to javafx.fxml, com.google.gson;
    opens org.example.cardcollectorproject.controllers to javafx.fxml, com.google.gson;
    opens org.example.cardcollectorproject.models to com.google.gson; // ← this line is required

    exports org.example.cardcollectorproject;
    exports org.example.cardcollectorproject.controllers;
}


