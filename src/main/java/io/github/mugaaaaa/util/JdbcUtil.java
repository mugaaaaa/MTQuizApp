package io.github.mugaaaaa.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;


/**
 * 主要处理数据库的复制, 连接, 关闭.
 */
public class JdbcUtil {
    private static final String APP_DATA_FOLDER_NAME = ".millitaryTheoryQuizApp";
    private static final String DB_FILE_NAME = "data.db";
    private static final String DB_URL;

    static {
        // 确定data.db在用户电脑上的存储路径
        String userHome = System.getProperty("user.Home");  // 获取用户主目录
        File appDataDir = new File(userHome, APP_DATA_FOLDER_NAME);
        File dbFile = new File(appDataDir, DB_FILE_NAME);

        setupDatabaseIfNotExist(dbFile);

        DB_URL = "jdbc:sqlite:" + dbFile.getAbsolutePath();
    }

    /**
     * 如果用户目录不存在数据库文件, 就从resources/io/github/mugaaaaa/data.db那里复制一份过去.
     * @param dbFile
     */
    private static void setupDatabaseIfNotExist(File dbFile) {
        if (dbFile.exists()) {
            return;
        }

        dbFile.getParentFile().mkdirs();

        try (InputStream is = JdbcUtil.class.getResourceAsStream("/" + DB_FILE_NAME)){
            if (is == null) {
                throw new IOException("找不到模板数据库: " + DB_FILE_NAME);
            }

            Files.copy(is, dbFile.toPath());
        } catch (IOException e) {
            System.err.println("数据库初始化失败");
            e.printStackTrace();
        }
    }

    /**
     * 获取数据库连接
     * @return 返回DB_URL.
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * 从里到外依次关闭: ResultSet->Statement->Connection.
     * @param conn
     * @param stmt
     * @param rs
     */
    private static void close(Connection conn, Statement stmt, ResultSet rs) {
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

    /**
     * 方便的重载方法, 接受一个Connection和Statement.
     * @param conn
     * @param stmt
     */
    public static void close(Connection conn, Statement stmt) {
        close(conn, stmt, null);
    }
    // 调用上面的close方法增加代码复用性
}