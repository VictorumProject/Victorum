package net.parinacraft.victorum.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import net.parinacraft.victorum.Opt;
import net.parinacraft.victorum.Victorum;
import net.parinacraft.victorum.claim.Claim;
import net.parinacraft.victorum.claim.Faction;

/**
 * Handles SQL connecting and executing of queries
 */
public class SQLManager {
	private final Victorum pl;

	private Connection conn;
	private String DB_NAME;

	public SQLManager(Victorum pl) {
		this.pl = pl;
	}

	private void checkConnection() {
		if (conn == null) {
			FileConfiguration conf = Victorum.getPlugin().getConfig();
			String pw = conf.getString("mysql.password");
			String user = conf.getString("mysql.user");
			String server = conf.getString("mysql.server");
			DB_NAME = conf.getString("mysql.database");

			System.out.println("Connecting to " + server + "/" + DB_NAME + " as user " + user);
			try {
				conn = DriverManager.getConnection("jdbc:mysql://" + server + "/" + DB_NAME, user, pw);
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

	public void createDatabases() {
		checkConnection();
		try {
			Statement stmt = conn.createStatement();
			stmt.addBatch("CREATE TABLE IF NOT EXISTS PlayerData (UUID varchar(36), FactionID int, UNIQUE (UUID))");
			stmt.addBatch("CREATE TABLE IF NOT EXISTS Claim (ChunkX int(5), ChunkZ int(5), FactionID int)");
			stmt.addBatch("CREATE TABLE IF NOT EXISTS Faction (FactionID int, Short varchar("
					+ Opt.MAX_FACTION_NAME_SHORT + "), Name varchar(" + Opt.MAX_FACTION_NAME_LONG
					+ "), Value int(30), BoardPosition int, UNIQUE (FactionID, Short))");
			// Create default faction
			stmt.addBatch(
					"INSERT IGNORE INTO Faction (FactionID, Short, Name, Value, BoardPosition) VALUES (0, 'VICT', 'Victorum', 0, 0)");
			stmt.executeBatch();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public HashMap<Integer, Faction> loadFactions() {
		checkConnection();
		HashMap<Integer, Faction> val = new HashMap<>();
		try (Statement stmt = conn.createStatement()) {
			try (ResultSet rs = stmt.executeQuery("SELECT * FROM Faction")) {
				while (rs.next()) {
					int id = rs.getInt("FactionID");
					String shortName = rs.getString("Short");
					String longName = rs.getString("Name");
					long value = rs.getLong("Value");
					int boardPosition = rs.getInt("BoardPosition");
					val.put(rs.getInt("FactionID"), new Faction(id, shortName, longName, value, boardPosition));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return val;
	}

	public HashMap<UUID, PlayerData> loadPlayerData() {
		checkConnection();
		HashMap<UUID, PlayerData> val = new HashMap<>();
		try (Statement stmt = conn.createStatement()) {
			try (ResultSet rs = stmt.executeQuery("SELECT * FROM PlayerData")) {
				while (rs.next()) {
					UUID id = UUID.fromString(rs.getString("UUID"));
					int facID = rs.getInt("FactionID");
					val.put(id, new PlayerData(pl, id, facID));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return val;
	}

	public HashMap<Integer, Claim> loadClaims() {
		checkConnection();
		HashMap<Integer, Claim> val = new HashMap<>();
		try (Statement stmt = conn.createStatement()) {
			try (ResultSet rs = stmt.executeQuery("SELECT * FROM Claim")) {
				while (rs.next()) {
					int facID = rs.getInt("FactionID");
					int chunkX = rs.getInt("ChunkX");
					int chunkZ = rs.getInt("ChunkZ");
					int claimID = chunkX << 16 | (chunkZ & 0xFFFF);
					val.put(claimID, new Claim(pl, chunkX, chunkZ, facID));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return val;
	}

	public void createPlayerData(UUID uuid) {
		checkConnection();
		// Make sure there is data
		try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO PlayerData VALUES (?, 0)")) {
			stmt.setString(1, uuid.toString());
			stmt.execute();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public void createFaction(Faction fac) {
		checkConnection();
		try (PreparedStatement stmt = conn.prepareStatement(
				"INSERT INTO Faction (FactionID, Short, Name, Value, BoardPosition) VALUES (?, ?, ?, 0, 0)")) {
			stmt.setInt(1, fac.getID());
			stmt.setString(2, fac.getShortName());
			stmt.setString(3, fac.getLongName());
			stmt.execute();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public void removeFaction(int id) {
		checkConnection();
		try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Faction WHERE FactionID = ?")) {
			stmt.setInt(1, id);
			stmt.execute();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public void createClaim(Claim claim) {
		checkConnection();
		try (PreparedStatement stmt = conn
				.prepareStatement("INSERT INTO Claim (ChunkX, ChunkZ, FactionID) VALUES (?, ?, ?)")) {
			stmt.setInt(1, claim.getChunkX());
			stmt.setInt(2, claim.getChunkZ());
			stmt.setInt(3, claim.getFactionID());
			stmt.execute();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public void removeClaim(int chunkX, int chunkZ) {
		checkConnection();
		try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Claim WHERE ChunkX = ? AND ChunkZ = ?")) {
			stmt.setInt(1, chunkX);
			stmt.setInt(2, chunkZ);
			stmt.execute();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public void setFactionID(UUID uuid, int id) {
		checkConnection();
		try (PreparedStatement stmt = conn.prepareStatement("UPDATE PlayerData SET FactionID = ? WHERE UUID = ?")) {
			stmt.setInt(1, id);
			stmt.setString(2, uuid.toString());
			stmt.execute();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public void unclaimAll(int facID) {
		checkConnection();
		try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Claim WHERE FactionID = ?")) {
			stmt.setInt(1, facID);
			stmt.execute();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

}