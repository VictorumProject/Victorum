package me.victorum.data;

import java.util.UUID;

import org.bukkit.Bukkit;

import me.victorum.victorum.Victorum;

/**
 * Class for abilities that would be otherwise used with permission nodes.
 * Includes /f fly and silktouchspawners.
 */
public class Extras {
	private final UUID uuid;
	private final Victorum pl;
	private boolean canFly, nearCommand, canMineSpawners;

	public Extras(Victorum pl, UUID uuid, boolean canFly, boolean nearCommand, boolean canMineSpawners) {
		this.pl = pl;
		this.uuid = uuid;
		this.canFly = canFly;
		this.nearCommand = nearCommand;
		this.canMineSpawners = canMineSpawners;
	}

	public UUID getUUID() {
		return uuid;
	}

	public boolean canFly() {
		return canFly;
	}

	public boolean canUseNearCommand() {
		return nearCommand;
	}

	public boolean canMineSpawners() {
		return canMineSpawners;
	}

	public void setCanFly(boolean canFly) {
		this.canFly = canFly;
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().saveCanFly(uuid, nearCommand);
		});
	}

	public void setCanMineSpawners(boolean canMineSpawners) {
		this.canMineSpawners = canMineSpawners;
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().saveCanMineSpawners(uuid, nearCommand);
		});
	}

	public void setCanUseNearCommand(boolean nearCommand) {
		this.nearCommand = nearCommand;
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().saveCanUseNearCommand(uuid, nearCommand);
		});
	}

}
