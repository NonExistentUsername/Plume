module com.unknown.plumedesktop {
    requires MaterialFX;
    requires javafx.controls;
    requires javafx.fxml;

    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    opens com.unknown.plumedesktop to javafx.fxml;
    exports com.unknown.plumedesktop;
}