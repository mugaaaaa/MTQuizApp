/**
 * 定义应用程序的主模块
 */
module io.github.mugaaaaa {
    // 依赖JavaFX的核心模块
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // 依赖Java的数据库连接(JDBC)模块
    requires java.sql;

    // 依赖SQLite的JDBC驱动模块 (通常是自动模块)
    requires org.xerial.sqlitejdbc;


    // 开放包给其他模块

    // 必须将包含FXML控制器类的包开放给javafx.fxml模块
    // 这样FXMLLoader才能通过反射注入@FXML字段和调用方法
    opens io.github.mugaaaaa.controller to javafx.fxml;

    // 将包含主程序入口的包开放给JavaFX
    opens io.github.mugaaaaa to javafx.fxml, javafx.graphics;


    // 导出包

    // 导出主包，以便JavaFX可以启动应用程序
    exports io.github.mugaaaaa;
    exports io.github.mugaaaaa.controller;
    exports io.github.mugaaaaa.model;
}