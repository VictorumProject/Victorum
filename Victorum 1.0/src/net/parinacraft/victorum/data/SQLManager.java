package net.parinacraft.victorum.data;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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

	public SQLManager(Victorum pl) {
		this.pl = pl;
	}

	private void checkConnection() {
		if (conn == null) {
			FileConfiguration conf = Victorum.getPlugin().getConfig();
			String pw = conf.getString("mysql.password");
			String user = conf.getString("mysql.user");
			String server = conf.getString("mysql.server");
			String DB_NAME = conf.getString("mysql.database");

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
		try (Statement stmt = conn.createStatement(Statement.RETURN_GENERATED_KEYS, ResultSet.CONCUR_READ_ONLY)) {
			stmt.addBatch(
					"CREATE TABLE IF NOT EXISTS PlayerData (UUID VARCHAR(36) NOT NULL, LastSeenName VARCHAR(16) NOT NULL, FactionID int NOT NULL, FactionRole TINYINT NOT NULL DEFAULT 3, Balance BIGINT NOT NULL DEFAULT 0, PRIMARY KEY(UUID))");
			stmt.addBatch(
					"CREATE TABLE IF NOT EXISTS Faction (FactionID INT NOT NULL AUTO_INCREMENT, Short VARCHAR(4) UNIQUE NOT NULL, Name VARCHAR(50) NOT NULL, Founder VARCHAR(36) NOT NULL, Value BIGINT NOT NULL DEFAULT 0, LeaderThreshold BIGINT NOT NULL, Home INT DEFAULT NULL, PRIMARY KEY (FactionID))");
			stmt.addBatch(
					"CREATE TABLE IF NOT EXISTS Home (FactionID INT PRIMARY KEY NOT NULL, X FLOAT NOT  NULL, Y FLOAT NOT NULL, Z FLOAT NOT NULL, Yaw FLOAT, Pitch FLOAT, FOREIGN KEY (FactionID) REFERENCES Faction(FactionID) ON DELETE CASCADE)");
			stmt.addBatch(
					"CREATE TABLE IF NOT EXISTS Claim (ChunkX int(5), ChunkZ int(5), FactionID int NOT NULL, ExpirationDate Date NOT NULL, PRIMARY KEY(ChunkX, ChunkZ), FOREIGN KEY (FactionID) REFERENCES Faction(FactionID))");

			stmt.addBatch(
					"CREATE TABLE IF NOT EXISTS Invite (Inviter VARCHAR(36) NOT NULL, Invited VARCHAR(36) NOT NULL, PRIMARY KEY (Inviter, Invited))");
			// Create default faction
			stmt.addBatch(
					"INSERT IGNORE INTO Faction (Short, Name, Founder, Value) VALUES ('VICT', 'Victorum', 'c2b2ae69-8010-4610-a8ad-4a95de884efb', 0)");
			stmt.executeBatch();
			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) {
					Opt.DEFAULT_FACTION_ID = rs.getInt(1);
					pl.getConfig().set("default-faction-id", Opt.DEFAULT_FACTION_ID);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public HashMap<Integer, Faction> loadFactions() {
		checkConnection();
		HashMap<Integer, Faction> val = new HashMap<>();
		try (Statement stmt = conn.createStatement()) {
			try (ResultSet rs = stmt.executeQuery(
					"SELECT F.FactionID, F.Short, F.Name, F.Founder, F.Value, F.LeaderThreshold, H.X, H.Y, H.Z, H.Yaw, H.Pitch FROM Faction F LEFT JOIN Home H ON H.FactionID = F.FactionID")) {
				while (rs.next()) {
					int id = rs.getInt("FactionID");
					String shortName = rs.getString("Short");
					String longName = rs.getString("Name");
					UUID founder = UUID.fromString(rs.getString("Founder"));
					long value = rs.getLong("Value");
					long leaderThreshold = rs.getLong("LeaderThreshold");
					Location home = new Location(Opt.GAME_WORLD, rs.getFloat("X"), rs.getFloat("Y"), rs.getFloat("Z"),
							rs.getFloat("Yaw"), rs.getFloat("Pitch"));
					val.put(id, new Faction(pl, id, shortName, longName, founder, value, leaderThreshold, home));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (val.size() == 0)
			throw new NullPointerException();
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
					long balance = rs.getLong("Balance");
					String lastSeenName = rs.getString("LastSeenName");
					val.put(id, new PlayerData(pl, id, facID, role, balance, lastSeenName));
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
		try (PreparedStatement stmt = conn
				.prepareStatement("INSERT INTO PlayerData (UUID, FactionID, LastSeenName) VALUES (?, ?, ?)")) {
			stmt.setString(1, uuid.toString());
			stmt.setInt(2, Opt.DEFAULT_FACTION_ID);
			stmt.setString(3, Bukkit.getOfflinePlayer(uuid).getName());
			stmt.execute();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public Faction createFaction(String name, UUID founder) {
		checkConnection();
		try (PreparedStatement stmt = conn.prepareStatement(
				"INSERT INTO Faction (Short, Name, Founder, LeaderThreshold) VALUES (?, ?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, name);
			stmt.setString(2, name);
			stmt.setString(3, founder.toString());
			stmt.setLong(4, Opt.DEFAULT_LEADER_THRESHOLD);
			stmt.execute();

			try (ResultSet keys = stmt.getGeneratedKeys()) {
				if (keys.next())
					return new Faction(pl, keys.getInt(1), name, name, founder, 0, Opt.DEFAULT_LEADER_THRESHOLD, null);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	public void setShortName(int facID, String shortName) {
		checkConnection();
		try (PreparedStatement stmt = conn.prepareStatement("UPDATE Faction SET Short = ? WHERE FactionID = ?")) {
			stmt.setString(1, shortName);
			stmt.setInt(2, facID);
			stmt.execute();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public void setLongName(int id, String longName) {
		checkConnection();
		try (PreparedStatement stmt = conn.prepareStatement("UPDATE Faction SET Name = ? WHERE FactionID = ?")) {
			stmt.setString(1, longName);
			stmt.setInt(2, id);
			stmt.execute();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public void setHome(Faction faction, Location loc) {
		checkConnection();
		if (faction.getHome() != null) {
			try (PreparedStatement stmt = conn.prepareStatement(
					"UPDATE Home SET X = ?, Y = ?, Z = ?, Yaw = ?, Pitch = ? WHERE FactionID = ?",
					Statement.RETURN_GENERATED_KEYS)) {
				stmt.setFloat(1, (float) loc.getX());
				stmt.setFloat(2, (float) loc.getY());
				stmt.setFloat(3, (float) loc.getZ());
				stmt.setFloat(4, loc.getYaw());
				stmt.setFloat(5, loc.getPitch());
				stmt.setInt(6, faction.getID());
				stmt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try (PreparedStatement stmt = conn.prepareStatement(
					"INSERT INTO Home (FactionID, X, Y, Z, Yaw, Pitch) VALUES (?, ?, ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS)) {
				stmt.setFloat(1, faction.getID());
				stmt.setFloat(2, (float) loc.getX());
				stmt.setFloat(3, (float) loc.getY());
				stmt.setFloat(4, (float) loc.getZ());
				stmt.setFloat(5, loc.getYaw());
				stmt.setFloat(6, loc.getPitch());
				stmt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
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

	public void setBalance(UUID UUID, long balance) {
		checkConnection();
		try (PreparedStatement stmt = conn.prepareStatement("UPDATE PlayerData SET Balance = ? WHERE UUID = ?")) {
			stmt.setLong(1, balance);
			stmt.setString(2, UUID.toString());
			stmt.execute();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public HashMap<UUID, Set<UUID>> getAllInvites() {
		HashMap<UUID, Set<UUID>> invites = new HashMap<>();
		try (Statement stmt = conn.createStatement()) {
			try (ResultSet rs = stmt.executeQuery("SELECT Inviter, Invited FROM Invite")) {
				while (rs.next()) {
					UUID inviter = UUID.fromString(rs.getString("Inviter"));
					UUID invited = UUID.fromString(rs.getString("Invited"));

					invites.putIfAbsent(inviter, new HashSet<>());
					Set<UUID> invitesForInviter = invites.get(inviter);
					invitesForInviter.add(invited);
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return invites;
	}

	public void setLastSeenName(UUID uniqueId, String name) {
		checkConnection();
		try (PreparedStatement stmt = conn.prepareStatement("UPDATE PlayerData SET LastSeenName = ? WHERE UUID = ?")) {
			stmt.setString(1, name);
			stmt.setString(2, uniqueId.toString());
			stmt.execute();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public void createInvite(UUID inviter, UUID invited) {
		checkConnection();
		try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO Invite (Inviter, Invited) VALUES (?, ?)")) {
			stmt.setString(1, inviter.toString());
			stmt.setString(2, invited.toString());
			stmt.execute();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public static void main(String[] args) {
		while (true) {
			List<Integer> ls = new ArrayList<>();
			ls.add(new Integer(5325));
		}
	}
}