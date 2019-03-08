package net.parinacraft.victorum;

import java.util.HashSet;

import org.bukkit.World;

public class Opt {

	public static World GAME_WORLD;
	public static int MAX_FACTION_NAME_SHORT;
	public static int MAX_FACTION_NAME_LONG;
	public static HashSet<Character> ALLOWED_FACTION_NAME_CHARS;

	public static void load(Victorum pl) {
		GAME_WORLD = pl.getServer().getWorld("world");
		MAX_FACTION_NAME_SHORT = 4;
		MAX_FACTION_NAME_LONG = 50;

		ALLOWED_FACTION_NAME_CHARS = new HashSet<>();
		String values = pl.getConfig().getString("allowed-faction-name-letters");
		for (int i = 0; i < values.length(); i++) {
			ALLOWED_FACTION_NAME_CHARS.add(values.charAt(i));
		}
	}
}
