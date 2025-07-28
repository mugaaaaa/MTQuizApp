package io.github.mugaaaaa.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcUtil {

    // "jdbc:sqlite:" 是固定的协议前缀。
    private static final String DB_URL = "jdbc:sqlite:data.db";

    // 单例型对象
    private JdbcUtil() {}

    /**
     * 获取一个数据库连接
     * @return Connection 对象
     * @throws SQLException 连接失败
     */
    public static Connection getConnection() throws SQLException {
        // 对于SQLite, Class.forName("org.sqlite.JDBC") 是可选的，但写上更规范
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("找不到SQLite JDBC驱动！请检查pom.xml依赖。");
            e.printStackTrace();
        }
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * 安全地关闭数据库资源
     * @param conn 数据库连接 (可以为null)
     * @param stmt Statement 或 PreparedStatement (可以为null)
     * @param rs ResultSet (可以为null)
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        // 从里到外依次关闭资源：ResultSet -> Statement -> Connection
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}