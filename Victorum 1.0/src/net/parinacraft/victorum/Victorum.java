package net.parinacraft.victorum;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import net.parinacraft.victorum.commands.ClaimCommand;
import net.parinacraft.victorum.data.ClaimHandler;
import net.parinacraft.victorum.data.FactionHandler;
import net.parinacraft.victorum.data.PlayerDataHandler;
import net.parinacraft.victorum.data.SQLManager;
import net.parinacraft.victorum.events.ChatListener;
import net.parinacraft.victorum.events.ChestOpenListener;
import net.parinacraft.victorum.events.ClaimInvClickCanceller;
import net.parinacraft.victorum.events.ConnectionListener;

public class Victorum extends JavaPlugin {
	private FactionHandler factionHandler;
	private PlayerDataHandler playerDataHandler;
	private ClaimHandler claimHandler;

	@Override
	public void onEnable() {
		saveDefaultConfig();
		getCommand("claim").setExecutor(new ClaimCommand(this));

		Bukkit.getPluginManager().registerEvents(new ConnectionListener(this), this);
		Bukkit.getPluginManager().registerEvents(new ChestOpenListener(this), this);
		Bukkit.getPluginManager().registerEvents(new ClaimInvClickCanceller(this), this);
		Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);

		// Create databases
		SQLManager.createDatabases();

		// Load all data from database to memory
		this.factionHandler = new FactionHandler(this);
		this.playerDataHandler = new PlayerDataHandler(this);
		this.claimHandler = new ClaimHandler(this);
	}

	@Override
	public void onDisable() {

	}

	public static Victorum getPlugin() {
		return (Victorum) Bukkit.getPluginManager().getPlugin("Victorum");
	}

	public FactionHandler getFactionHandler() {
		return this.factionHandler;
	}

	public ClaimHandler getClaimHandler() {
		return claimHandler;
	}

	public PlayerDataHandler getPlayerDataHandler() {
		return playerDataHandler;
	}
}
