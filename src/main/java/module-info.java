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
    requires java.net.http;  // Added for HTTP Client API
    requires com.google.gson; // Added for Gson

    // Allow reflection into your package for both FXML and Gson if needed
    opens org.example.cardcollectorproject to javafx.fxml, com.google.gson;
    exports org.example.cardcollectorproject;
}
