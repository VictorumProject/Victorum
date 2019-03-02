package net.parinacraft.victorum;

import org.bukkit.World;

public class Opt {

	public static World GAME_WORLD;
	public static int MAX_FACTION_NAME_SHORT;
	public static int MAX_FACTION_NAME_LONG;

	public static void init(Victorum pl) {
		GAME_WORLD = pl.getServer().getWorld("world");
		MAX_FACTION_NAME_SHORT = 4;
		MAX_FACTION_NAME_LONG = 50;
	}
}
