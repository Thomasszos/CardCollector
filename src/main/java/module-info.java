module org.example.cardcollectorproject {
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

    // Combined requirements
    requires java.desktop;
    requires java.smartcardio;

    // Combined opens/exports
    opens org.example.cardcollectorproject to javafx.fxml, com.google.gson;
    opens org.example.cardcollectorproject.controllers to javafx.fxml, com.google.gson;
    exports org.example.cardcollectorproject;
    exports org.example.cardcollectorproject.controllers;
}

