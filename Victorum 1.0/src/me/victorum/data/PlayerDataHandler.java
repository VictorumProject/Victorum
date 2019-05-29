package me.victorum.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import me.victorum.victorum.Victorum;

public class PlayerDataHandler {
	private final HashMap<UUID, PlayerData> playerData;
	private final Victorum pl;

	public PlayerDataHandler(Victorum pl) {
		this.pl = pl;
		this.playerData = pl.getSqlManager().loadPlayerData();
	}

	public PlayerData getPlayerData(UUID id) {
		return playerData.get(id);
	}

	/**
	 * Not the most accurate method. It's recommended to use the
	 * {@link #getPlayerData(UUID)} since data is structured by UUID which is not
	 * changeable.
	 */
	public PlayerData getPlayerData(String lastSeenName) {
		for (PlayerData pd : playerData.values()) {
			if (pd.getLastSeenName().equalsIgnoreCase(lastSeenName))
				return pd;
		}
		return null;
	}

	public void checkForExistingData(OfflinePlayer pd) {
		if (!playerData.containsKey(pd.getUniqueId())) {
			PlayerData defaultPlayerData = new PlayerData(pl, pd.getUniqueId(), pd.getName());
			playerData.put(pd.getUniqueId(), defaultPlayerData);

			// Update database
			Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
				pl.getSqlManager().createPlayerData(pd.getUniqueId());
			});
		}
	}

	public Set<PlayerData> getAllData() {
		return new HashSet<>(playerData.values());
	}

	/**
	 * @return null if player has never logged on or wasn't found.
	 */
	public UUID getUUIDWithName(String name) {
		for (PlayerData pd : playerData.values()) {
			System.out.println(pd.getLastSeenName());
			if (pd.getLastSeenName().equalsIgnoreCase(name))
				return pd.getUUID();
		}
		return null;
	}
}
