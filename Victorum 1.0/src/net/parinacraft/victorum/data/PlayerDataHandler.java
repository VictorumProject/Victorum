package net.parinacraft.victorum.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;

import net.parinacraft.victorum.Victorum;
import net.parinacraft.victorum.claim.FactionRole;

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

	public void checkForExistingData(UUID UUID) {
		if (!playerData.containsKey(UUID)) {
			playerData.put(UUID, new PlayerData(pl, UUID, pl.getFactionHandler().getDefaultFactionID(),
					FactionRole.MEMBER));

			// Update database
			Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
				pl.getSqlManager().createPlayerData(UUID);
			});
		}
	}

	public Set<PlayerData> getAllData() {
		return new HashSet<>(playerData.values());
	}
}
