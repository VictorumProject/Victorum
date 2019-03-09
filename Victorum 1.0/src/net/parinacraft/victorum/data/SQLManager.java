package net.parinacraft.victorum.data;

import java.sql.Connection;
import java.sql.Date;
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
import net.parinacraft.victorum.claim.FactionRole;

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

	public int createDatabases() {
		int defaultFactionID = 1;

		checkConnection();
		try (Statement stmt = conn.createStatement(Statement.RETURN_GENERATED_KEYS, ResultSet.CONCUR_READ_ONLY)) {
			stmt.addBatch(
					"CREATE TABLE IF NOT EXISTS PlayerData (UUID VARCHAR(36) PRIMARY KEY NOT NULL, FactionID int NOT NULL, FactionRole TINYINT NOT NULL DEFAULT 3)");
			stmt.addBatch(
					"CREATE TABLE IF NOT EXISTS Claim (ChunkX int(5), ChunkZ int(5), FactionID int NOT NULL, ExpirationDate Date NOT NULL, PRIMARY KEY(ChunkX, ChunkZ))");
			stmt.addBatch(
					"CREATE TABLE IF NOT EXISTS Faction (FactionID int PRIMARY KEY NOT NULL AUTO_INCREMENT, Short VARCHAR("
							+ Opt.MAX_FACTION_NAME_SHORT + ") UNIQUE NOT NULL, Name VARCHAR("
							+ Opt.MAX_FACTION_NAME_LONG
							+ ") NOT NULL, Founder VARCHAR(36) NOT NULL, Value BIGINT, LeaderThreshold BIGINT) AUTO_INCREMENT = "
							+ defaultFactionID + "");
			stmt.addBatch(
					"CREATE TABLE IF NOT EXISTS Invite (InviteID INT PRIMARY KEY AUTO_INCREMENT, Inviter varchar(36), Invited VARCHAR(36), FactionID INT, FOREIGN KEY (FactionID) REFERENCES Faction(FactionID) ON DELETE CASCADE)");
			// Create default faction
			stmt.addBatch(
					"INSERT IGNORE INTO Faction (Short, Name, Founder, Value) VALUES ('VICT', 'Victorum', 'c2b2ae69-8010-4610-a8ad-4a95de884efb', 0)");
			stmt.executeBatch();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultFactionID;
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
					UUID founder = UUID.fromString(rs.getString("Founder"));
					long value = rs.getLong("Value");
					long leaderThreshold = rs.getLong("LeaderThreshold");
					val.put(id, new Faction(pl, id, shortName, longName, founder, value, leaderThreshold));
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
					FactionRole role = FactionRole.valueOf(rs.getInt("FactionRole"));
					val.put(id, new PlayerData(pl, id, facID, role));
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
					Date expiration = rs.getDate("ExpirationDate");
					val.put(claimID, new Claim(pl, chunkX, chunkZ, facID, expiration.getTime()));
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
		try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO PlayerData (UUID, FactionID) VALUES (?, 0)")) {
			stmt.setString(1, uuid.toString());
			stmt.execute();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public Faction createFaction(String name, UUID founder) {
		checkConnection();
		try (PreparedStatement stmt = conn.prepareStatement(
				"INSERT INTO Faction (Short, Name, Founder, Value, LeaderThreshold) VALUES (?, ?, ?, 0, ?)",
				Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, name);
			stmt.setString(2, name);
			stmt.setString(3, founder.toString());
			stmt.setLong(4, Opt.DEFAULT_LEADER_THRESHOLD);
			stmt.execute();

			try (ResultSet keys = stmt.getGeneratedKeys()) {
				if (keys.next())
					return new Faction(pl, keys.getInt(1), name, name, founder, 0, Opt.DEFAULT_LEADER_THRESHOLD);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return null;
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
		try (PreparedStatement stmt = conn.prepareStatement(
				"INSERT INTO Claim (ChunkX, ChunkZ, FactionID, ExpirationDate) VALUES (?, ?, ?, ?)")) {
			stmt.setInt(1, claim.getChunkX());
			stmt.setInt(2, claim.getChunkZ());
			stmt.setInt(3, claim.getFactionID());

			stmt.setDate(4, new java.sql.Date(claim.getExpirationDate()));
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

	public void setValue(int factionID, long value) {
		checkConnection();
		try (PreparedStatement stmt = conn.prepareStatement("UPDATE Faction SET Value = ? WHERE FactionID = ?")) {
			stmt.setLong(1, value);
			stmt.setInt(2, factionID);
			stmt.execute();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public void setFactionRole(UUID UUID, FactionRole role) {
		checkConnection();
		try (PreparedStatement stmt = conn.prepareStatement("UPDATE PlayerData SET FactionRole = ? WHERE UUID = ?")) {
			stmt.setInt(1, role.getValue());
			stmt.setString(2, UUID.toString());
			stmt.execute();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

}