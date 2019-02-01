package net.parinacraft.victorum;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import net.parinacraft.victorum.commands.ClaimCommand;
import net.parinacraft.victorum.events.ChatListener;
import net.parinacraft.victorum.events.ChestOpenListener;
import net.parinacraft.victorum.events.ClaimInvClickCanceller;
import net.parinacraft.victorum.events.ConnectionListener;

public class Victorum extends JavaPlugin {
	@Override
	public void onEnable() {
		saveDefaultConfig();
		getCommand("claim").setExecutor(new ClaimCommand());

		Bukkit.getPluginManager().registerEvents(new ConnectionListener(), this);
		Bukkit.getPluginManager().registerEvents(new ChestOpenListener(), this);
		Bukkit.getPluginManager().registerEvents(new ClaimInvClickCanceller(), this);
		Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
	}

	@Override
	public void onDisable() {

	}

	public static Victorum getPlugin() {
		return (Victorum) Bukkit.getPluginManager().getPlugin("Victorum");
	}
}
