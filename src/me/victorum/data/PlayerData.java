package me.victorum.data;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.victorum.claim.Faction;
import me.victorum.claim.FactionRole;
import me.victorum.victorum.Opt;
import me.victorum.victorum.Victorum;

public class PlayerData {
	private final Victorum pl;

	private final UUID UUID;
	private int factionID;
	// private FactionRole role;
	private long balance;
	private String lastSeenName;
	private Extras extras;

	public PlayerData(Victorum pl, UUID id, int factionID, FactionRole role, long balance, String lastSeenName,
			Extras extras) {
		this.pl = pl;
		this.UUID = id;
		this.factionID = factionID;
		if (factionID == 0)
			throw new NullPointerException("Faction ID can't be 0");
		// this.role = role;
		this.balance = balance;
		this.lastSeenName = lastSeenName;
		this.extras = extras;
	}

	/**
	 * Sets new default values for other parameters.
	 */
	public PlayerData(Victorum pl, UUID id, String lastSeenName) {
		this(pl, id, Opt.DEFAULT_FACTION_ID, FactionRole.MEMBER, 0, lastSeenName, new Extras(pl, id, false, false,
				false));
	}

	public int getFactionID() {
		return factionID;
	}

	public Faction getFaction() {
		return pl.getFactionHandler().getFaction(factionID);
	}

	public UUID getUUID() {
		return UUID;
	}

	public FactionRole getRole() {
		if (balance >= getFaction().getLeaderThreshold())
			return FactionRole.LEADER;
		else
			return FactionRole.MEMBER;
	}

	public void setFactionID(int id) {
		if (this.factionID != id)
			pl.getInviteHandler().removeOutgoingInvites(UUID);

		this.factionID = id;
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().setFactionID(this.UUID, id);
		});
	}

	public void setRole(FactionRole role) {
		// this.role = role;
		// Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
		// pl.getSqlManager().setFactionRole(UUID, role);
		// });
	}

	public long getBalance() {
		return balance;
	}

	public long getBalanceForFaction() {
		return (long) Math.max(0, balance - Opt.MINIMUM_PERSONAL_MONEY);
	}

	public void setBalance(long balance) {
		this.balance = balance;
		updateBalanceAsync();
	}

	public void addBalance(long amount) {
		this.balance += amount;
		updateBalanceAsync();
	}

	public void subtractBalance(long amount) {
		this.balance -= amount;
		updateBalanceAsync();
	}

	private void updateBalanceAsync() {
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().setBalance(UUID, balance);
		});
	}

	public String getLastSeenName() {
		return lastSeenName;
	}

	/**
	 * @return null if player isn't online.
	 */
	public Player getPlayer() {
		return Bukkit.getPlayer(this.UUID);
	}

	public Extras getExtras() {
		return extras;
	}

}
