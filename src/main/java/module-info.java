module com.roadmap.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.sql;
    requires java.naming;

    opens com.roadmap.app to javafx.fxml;
    opens com.roadmap.app.model to org.hibernate.orm.core, javafx.base;
    opens com.roadmap.app.ui.views to javafx.fxml;
    opens com.roadmap.app.ui.components to javafx.fxml;

    exports com.roadmap.app;
    exports com.roadmap.app.model;
    exports com.roadmap.app.ui.views;
    exports com.roadmap.app.ui.components;
}
