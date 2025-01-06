package stallitium.DailyLoginBonus;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.time.LocalDate;
import java.util.Arrays;

public class DbDailyLB {
    private Connection connection;
    private final String path;

    public DbDailyLB(JavaPlugin plugin) {
        this.path = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "\\" + getClass().getPackageName() + ".db";
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            // データベース接続の初期化
            this.connection = DriverManager.getConnection(this.path);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS dlb ('key' TEXT, 'value' DATE)");
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Error initializing database: " + Arrays.toString(e.getStackTrace()));
        }
    }

    private Connection getConnection() throws SQLException {
        if (this.connection == null || this.connection.isClosed()) {
            this.connection = DriverManager.getConnection(this.path);
        }
        return this.connection;
    }

    public boolean check(String k) {
        String key = k.toLowerCase();
        String selectQuery = "SELECT value FROM dlb WHERE key=?";
        String insertQuery = "INSERT INTO dlb (key, value) VALUES (?, ?)";

        try {
            Connection connection = getConnection(); // ここで接続を取得
            Bukkit.getLogger().info("Connection obtained in check method");
            try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
                selectStmt.setString(1, key);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        Date byDB = rs.getDate("value");
                        if (Date.valueOf(LocalDate.now()).equals(byDB)) {
                            return true;
                        }
                    }
                }
                try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                    insertStmt.setString(1, key);
                    insertStmt.setDate(2, Date.valueOf(LocalDate.now()));
                    insertStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Error in check method: " + Arrays.toString(e.getStackTrace()));
            return false;
        }
        return false;
    }

    public void reset() {
        try {
            Connection connection = getConnection(); // ここで接続を取得
            Bukkit.getLogger().info("Connection obtained in reset method");
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP TABLE dlb");
                stmt.execute("CREATE TABLE IF NOT EXISTS dlb ('key' TEXT, 'value' DATE)");
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Error in reset method: " + Arrays.toString(e.getStackTrace()));
        }
    }

    public void closeConnection() {
        if (this.connection != null) {
            try {
                this.connection.close();
                Bukkit.getLogger().info("Connection closed");
            } catch (SQLException e) {
                Bukkit.getLogger().warning("Failed to close connection: " + Arrays.toString(e.getStackTrace()));
            }
        }
    }
}
