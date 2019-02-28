package net.parinacraft.victorum.data;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;

import net.parinacraft.victorum.Victorum;

public class PlayerDataHandler {
	private final HashMap<UUID, PlayerData> playerData;
	private final Victorum pl;

	public PlayerDataHandler(Victorum pl) {
		this.pl = pl;
		this.playerData = SQLManager.loadPlayerData();
	}

	public PlayerData getPlayerData(UUID id) {
		return playerData.get(id);
	}

	public void checkForExistingData(UUID UUID) {
		if (!playerData.containsKey(UUID)) {
			playerData.put(UUID, new PlayerData(UUID, 0));

			// Update database
			Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
				SQLManager.createPlayerData(UUID);
			});
		}
	}
}
