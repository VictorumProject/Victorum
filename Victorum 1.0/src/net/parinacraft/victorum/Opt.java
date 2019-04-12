package net.parinacraft.victorum;

import java.util.HashSet;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class Opt {

	public static int DEFAULT_FACTION_ID;
	public static long DEFAULT_LEADER_THRESHOLD = 0;
	public static World GAME_WORLD;
	public static int MAX_FACTION_NAME_SHORT;
	public static int MAX_FACTION_NAME_LONG;
	public static double MINIMUM_PERSONAL_MONEY;
	public static HashSet<Character> ALLOWED_FACTION_NAME_CHARS;

	public static void load(Victorum pl) {
		FileConfiguration conf = pl.getConfig();
		GAME_WORLD = pl.getServer().getWorld("world");
		MAX_FACTION_NAME_SHORT = 4;
		MAX_FACTION_NAME_LONG = 50;
		DEFAULT_LEADER_THRESHOLD = 15000;
		MINIMUM_PERSONAL_MONEY = 5000;
		DEFAULT_FACTION_ID = conf.getInt("default-faction-id");

		ALLOWED_FACTION_NAME_CHARS = new HashSet<>();
		String values = pl.getConfig().getString("allowed-faction-name-letters");
		for (int i = 0; i < values.length(); i++) {
			ALLOWED_FACTION_NAME_CHARS.add(values.charAt(i));
		}
	}
}
