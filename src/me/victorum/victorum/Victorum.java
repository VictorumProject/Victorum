package me.victorum.victorum;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import me.victorum.commands.BalanceCommand;
import me.victorum.commands.BaltopCommand;
import me.victorum.commands.ClaimCommand;
import me.victorum.data.ClaimHandler;
import me.victorum.data.FactionHandler;
import me.victorum.data.InviteHandler;
import me.victorum.data.PlayerDataHandler;
import me.victorum.data.RelationHandler;
import me.victorum.data.SQLManager;
import me.victorum.events.ChatListener;
import me.victorum.events.ChestOpenListener;
import me.victorum.events.ClaimBuildProtection;
import me.victorum.events.ClaimInvClickCanceller;
import me.victorum.events.ConnectionListener;
import me.victorum.events.MovementListener;
import me.victorum.temp.GrassForMoney;

public class Victorum extends JavaPlugin {
	private PlayerDataHandler playerDataHandler;
	private RelationHandler relationHandler;
	private FactionHandler factionHandler;
	private EconomyHandler economyHandler;
	private InviteHandler inviteHandler;
	private ClaimHandler claimHandler;
	private KickHandler kickHandler;
	private SQLManager sqlManager;

	// TODO: teamdamagestop

	@Override
	public void onEnable() {
		// Reload?
		saveDefaultConfig();

		// Prepare global options
		Opt.load(this);

		getCommand("claim").setExecutor(new ClaimCommand(this));
		getCommand("balance").setExecutor(new BalanceCommand(this));
		getCommand("baltop").setExecutor(new BaltopCommand(this));

		Bukkit.getPluginManager().registerEvents(new ConnectionListener(this), this);
		Bukkit.getPluginManager().registerEvents(new ChestOpenListener(this), this);
		Bukkit.getPluginManager().registerEvents(new ClaimInvClickCanceller(this), this);
		Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
		Bukkit.getPluginManager().registerEvents(new MovementListener(this), this);
		Bukkit.getPluginManager().registerEvents(new ClaimBuildProtection(this), this);

		// Tests
		Bukkit.getPluginManager().registerEvents(new GrassForMoney(this), this);

		// Create databases
		sqlManager = new SQLManager(this);
		sqlManager.createDatabases();

		// Load all data from database to memory
		getLogger().info("Importing data from MySQL...");
		long start = System.nanoTime();
		this.kickHandler = new KickHandler(this);
		this.claimHandler = new ClaimHandler(this);
		this.inviteHandler = new InviteHandler(this);
		this.economyHandler = new EconomyHandler(this);
		this.factionHandler = new FactionHandler(this);
		this.relationHandler = new RelationHandler(this);
		this.playerDataHandler = new PlayerDataHandler(this);

		// Log end time
		int timeMS = (int) ((System.nanoTime() - start) / 1E6);
		getLogger().info("Done (" + timeMS + "ms)");
	}

	@Override
	public void onDisable() {
	}

	public FactionHandler getFactionHandler() {
		return this.factionHandler;
	}

	public RelationHandler getRelationHandler() {
		return relationHandler;
	}

	public ClaimHandler getClaimHandler() {
		return claimHandler;
	}

	public PlayerDataHandler getPlayerDataHandler() {
		return playerDataHandler;
	}

	public SQLManager getSqlManager() {
		return sqlManager;
	}

	public InviteHandler getInviteHandler() {
		return inviteHandler;
	}

	public EconomyHandler getEconomyHandler() {
		return economyHandler;
	}

	public KickHandler getKickHandler() {
		return kickHandler;
	}

}
