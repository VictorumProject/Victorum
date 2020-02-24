package me.victorum.victorum;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;

import me.victorum.claim.Faction;
import me.victorum.data.PlayerData;

public class KickHandler {
	private final Victorum pl;
	private final HashMap<UUID, Set<UUID>> kickWishes;

	public KickHandler(Victorum pl) {
		this.pl = pl;
		kickWishes = pl.getSqlManager().getAllKickWishes();
	}

	public void wishKick(UUID kicker, UUID target) {
		getOutgoingKicks(kicker).add(target);
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().createKickWish(kicker, target);
		});
	}

	public Set<UUID> getOutgoingKicks(UUID kicker) {
		kickWishes.putIfAbsent(kicker, new HashSet<>());
		return kickWishes.get(kicker);
	}

	public Set<UUID> getIncomingKicks(UUID target) {
		Set<UUID> values = new HashSet<>();
		for (Entry<UUID, Set<UUID>> entries : kickWishes.entrySet()) {
			for (UUID id : entries.getValue()) {
				if (id.equals(target))
					values.add(entries.getKey());
			}
		}
		return values;
	}

	/**
	 * Returns the number of kicks from this factions members to its own members.
	 */
	public HashMap<UUID, Integer> getOutgoingKicks(int factionID) {
		HashMap<UUID, Integer> kicksForFaction = new HashMap<>(5);
		for (Entry<UUID, Set<UUID>> e : kickWishes.entrySet()) {
			PlayerData inviterData = pl.getPlayerDataHandler().getPlayerData(e.getKey());
			if (inviterData.getFactionID() == factionID) {
				Set<UUID> kicks = e.getValue();
				for (UUID uuid : kicks) {
					int oldKickNumber = kicksForFaction.getOrDefault(uuid, 0);
					kicksForFaction.put(uuid, oldKickNumber + 1);
				}
			}
		}
		return kicksForFaction;
	}

	public void removeKickWish(UUID kicker, UUID target) {
		getOutgoingKicks(kicker).remove(target);
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().removeKickWish(kicker, target);
		});
	}

	public void removeKicksFromFaction(Faction faction, UUID target) {
		for (UUID kicker : faction.getPlayers()) {
			if (hasKickWishFor(kicker, target))
				removeKickWish(kicker, target);
		}
	}

	public boolean hasKickWishFor(UUID kicker, UUID target) {
		return getOutgoingKicks(kicker).contains(target);
	}

	public void removeOutgoingKicks(UUID kicker) {
		kickWishes.remove(kicker);
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().removeOutgoingKicks(kicker);
		});
	}
}
