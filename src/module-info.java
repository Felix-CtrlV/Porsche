module Porsche {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;
    requires javafx.web;
    requires javafx.swing;

    requires java.sql;
    requires java.desktop;
    requires java.scripting;

    requires mysql.connector.j;
    requires de.jensd.fx.glyphs.fontawesome;
    requires org.slf4j;
    requires com.zaxxer.hikari;
    requires jakarta.mail;
//    requires Porsche;

    opens MainUI to javafx.fxml;
    opens Controllers to javafx.fxml;

    exports MainUI;
    exports Controllers;
}
