package me.victorum.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;

import me.victorum.claim.Faction;
import me.victorum.victorum.Victorum;

public class InviteHandler {
	private final HashMap<UUID, Set<UUID>> invites;
	private final Victorum pl;

	public InviteHandler(Victorum pl) {
		this.pl = pl;
		this.invites = pl.getSqlManager().getAllInvites();
	}

	public Set<UUID> getOutgoingInvites(UUID inviter) {
		invites.putIfAbsent(inviter, new HashSet<>());
		return invites.get(inviter);
	}

	public void createInvite(UUID inviter, UUID invited) {
		getOutgoingInvites(inviter).add(invited);
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().createInvite(inviter, invited);
		});
	}

	public Set<UUID> getIncomingInvites(UUID invited) {
		Set<UUID> values = new HashSet<>();
		for (Entry<UUID, Set<UUID>> entries : invites.entrySet()) {
			for (UUID id : entries.getValue()) {
				if (id.equals(invited))
					values.add(entries.getKey());
			}
		}
		return values;
	}

	public HashMap<UUID, Integer> getOutgoingInvites(int factionID) {
		HashMap<UUID, Integer> invitesForFaction = new HashMap<>(5);
		for (Entry<UUID, Set<UUID>> e : invites.entrySet()) {
			PlayerData inviterData = pl.getPlayerDataHandler().getPlayerData(e.getKey());
			if (inviterData.getFactionID() == factionID) {
				Set<UUID> invites = e.getValue();
				for (UUID uuid : invites) {
					int old = invitesForFaction.getOrDefault(uuid, 0);
					invitesForFaction.put(uuid, old + 1);
				}
			}
		}
		return invitesForFaction;
	}

	public void removeInvite(UUID inviter, UUID invited) {
		getOutgoingInvites(inviter).remove(invited);
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().removeInvite(inviter, invited);
		});
	}

	public void removeInvitesTo(Faction faction, UUID invited) {
		for (UUID inviter : faction.getPlayers()) {
			if (hasInvited(inviter, invited))
				removeInvite(inviter, invited);
		}
	}

	public boolean hasInvited(UUID inviter, UUID invited) {
		return getOutgoingInvites(inviter).contains(invited);
	}

	public void removeOutgoingInvites(UUID inviter) {
		this.invites.remove(inviter);
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().removeOutgoingInvites(inviter);
		});
	}
}
