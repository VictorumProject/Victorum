package net.parinacraft.victorum;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import net.parinacraft.victorum.claim.Faction;
import net.parinacraft.victorum.commands.BalanceCommand;
import net.parinacraft.victorum.commands.BaltopCommand;
import net.parinacraft.victorum.commands.ClaimCommand;
import net.parinacraft.victorum.data.ClaimHandler;
import net.parinacraft.victorum.data.FactionHandler;
import net.parinacraft.victorum.data.InviteHandler;
import net.parinacraft.victorum.data.PlayerDataHandler;
import net.parinacraft.victorum.data.RelationHandler;
import net.parinacraft.victorum.data.SQLManager;
import net.parinacraft.victorum.events.ChatListener;
import net.parinacraft.victorum.events.ChestOpenListener;
import net.parinacraft.victorum.events.ClaimBuildProtection;
import net.parinacraft.victorum.events.ClaimInvClickCanceller;
import net.parinacraft.victorum.events.ConnectionListener;
import net.parinacraft.victorum.events.MovementListener;
import net.parinacraft.victorum.test.GrassForMoney;

public class Victorum extends JavaPlugin {
	private PlayerDataHandler playerDataHandler;
	private RelationHandler relationHandler;
	private FactionHandler factionHandler;
	private EconomyHandler economyHandler;
	private InviteHandler inviteHandler;
	private ClaimHandler claimHandler;
	private SQLManager sqlManager;

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
		this.claimHandler = new ClaimHandler(this);
		this.inviteHandler = new InviteHandler(this);
		this.factionHandler = new FactionHandler(this);
		this.relationHandler = new RelationHandler(this);
		this.playerDataHandler = new PlayerDataHandler(this);

		List<Faction> removing = new ArrayList<>();
		for (Faction fac : factionHandler.getAllFactions()) {
			if (fac.getPlayers().size() == 0)
				removing.add(fac);
		}
		removing.forEach((Faction fac) -> {
			factionHandler.delete(fac.getID());
			System.out.println("Removed faction " + fac.getLongName() + " because they had no members.");
		});

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

}
