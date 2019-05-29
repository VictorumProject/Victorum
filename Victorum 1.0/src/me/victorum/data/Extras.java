package me.victorum.data;

/**
 * Class for abilities that would be otherwise used with permission nodes.
 * Includes /f fly and silktouchspawners.
 *
 */
public class Extras {
	private boolean canFly, nearCommand;

	public Extras(boolean canFly, boolean nearCommand, boolean canMineSpawners) {
		this.canFly = canFly;
		this.nearCommand = nearCommand;
	}

	public boolean canFly() {
		return canFly;
	}

	public boolean canUseNearCommand() {
		return nearCommand;
	}
}
