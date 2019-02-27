package net.parinacraft.victorum.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import net.parinacraft.victorum.Victorum;

/**
 * Handles SQL connecting and executing of queries
 */
public class SQLManager {

	private static Connection conn;

	private static void checkConnection() {
		if (conn == null) {
			FileConfiguration conf = Victorum.getPlugin().getConfig();
			String pw = conf.getString("mysql.password");
			String user = conf.getString("mysql.user");
			String server = conf.getString("mysql.server");
			String db = conf.getString("mysql.database");

			System.out.println("Connecting to " + server + "/" + db + " as user " + user);
			try {
				conn = DriverManager.getConnection("jdbc:mysql://" + server + "/" + db, user, pw);
			} catch (SQLException e) {
				e.printStackTrace();
				for (Player p : Bukkit.getOnlinePlayers()) {
					p.kickPlayer("§e§lVictorum\n§b      Palvelin uudelleenkäynnistyy teknisistä syistä.");
				}
				Bukkit.shutdown();
			}
		} else {
			try {
				if (!conn.isValid(0)) {
					conn.close();
					conn = null;
					checkConnection();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return;
		}
	}

	public static PreparedStatement prepare(String sql) throws SQLException {
		checkConnection();
		return conn.prepareStatement(sql);
	}

}