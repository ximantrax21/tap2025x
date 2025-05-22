module com.example.tap2025x {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens com.example.tap2025x to javafx.fxml;
    requires org.kordamp.bootstrapfx.core;
    exports com.example.tap2025x;
    requires mysql.connector.j;
    requires java.sql;
    opens com.example.tap2025x.modelos;
}