package net.parinacraft.victorum.claim;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.google.common.base.Preconditions;

import net.parinacraft.victorum.Opt;
import net.parinacraft.victorum.Victorum;
import net.parinacraft.victorum.data.PlayerData;

public class Faction {
	private final int factionID;
	private long value;
	private String shortName, longName;
	private final UUID founder;
	private long leaderThreshold;
	private Location home;

	private final Victorum pl;

	public Faction(Victorum pl, int factionID, String shortName, String longName, UUID founder, long value,
			long leaderThreshold, Location home) {
		this.pl = pl;
		this.factionID = factionID;
		this.shortName = Preconditions.checkNotNull(shortName);
		this.longName = Preconditions.checkNotNull(longName);
		this.founder = Preconditions.checkNotNull(founder);
		this.value = value;
		this.leaderThreshold = leaderThreshold;
		this.home = home;
	}

	public int getID() {
		return factionID;
	}

	public String getLongName() {
		return longName;
	}

	public String getShortName() {
		return shortName;
	}

	public UUID getFounder() {
		return founder;
	}

	public long getValue() {
		return value + getBalance();
	}

	public void setValue(long value) {
		this.value = value;
	}

	public Set<UUID> getPlayers() {
		Set<UUID> players = new HashSet<>();
		Set<PlayerData> data = pl.getPlayerDataHandler().getAllData();
		for (PlayerData pd : data) {
			if (pd.getFactionID() == this.factionID)
				players.add(pd.getUUID());
		}
		return players;
	}

	public long getLeaderThreshold() {
		return leaderThreshold;
	}

	public Location getHome() {
		return home;
	}

	public void setHome(Location home) {
		this.home = home;
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().setHome(this, home);
		});
	}

	public long getBalance() {
		long bal = 0;
		for (UUID id : this.getPlayers()) {
			PlayerData pd = pl.getPlayerDataHandler().getPlayerData(id);
			bal += pd.getBalanceForFaction();
		}
		return bal;
	}

	public void withdraw(long amount) {
		double amountPerBalance = (double) amount / (double) this.getBalance();
		for (UUID id : getPlayers()) {
			PlayerData pd = pl.getPlayerDataHandler().getPlayerData(id);
			long sub = (long) (pd.getBalanceForFaction() * amountPerBalance);
			pd.subtractBalance(sub);
		}
	}

	public boolean isDefaultFaction() {
		return this.factionID == Opt.DEFAULT_FACTION_ID;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().setShortName(this.getID(), shortName);
		});
	}

	public void setLongName(String longName) {
		this.longName = longName;
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().setLongName(this.getID(), longName);
		});
	}

	public Set<Claim> getClaims() {
		return pl.getClaimHandler().getAllClaims(this.factionID);
	}

}
