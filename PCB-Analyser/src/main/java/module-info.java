module com.example.pcbanalyser {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.pcbanalyser to javafx.fxml;
    exports com.example.pcbanalyser;
}