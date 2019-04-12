package net.parinacraft.victorum.claim;

import org.bukkit.ChatColor;

public enum Relation {
	NEUTRAL(ChatColor.YELLOW), ALLY(ChatColor.DARK_PURPLE), ENEMY(ChatColor.RED), OWN(ChatColor.GREEN);

	private final ChatColor color;

	private Relation(ChatColor color) {
		this.color = color;
	}

	public ChatColor getColor() {
		return color;
	}

	@Override
	public String toString() {
		return color.toString();
	}
}
