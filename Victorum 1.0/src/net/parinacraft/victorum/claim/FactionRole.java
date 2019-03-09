package net.parinacraft.victorum.claim;

public enum FactionRole {
	FOUNDER(1), LEADER(2), MEMBER(3);

	private final int val;

	private FactionRole(int val) {
		this.val = 0;
	}

	public int getValue() {
		return val;
	}

	public static FactionRole valueOf(int val) {
		for (FactionRole role : values()) {
			if (role.getValue() == val)
				return role;
		}
		throw new IllegalArgumentException("" + val);
	}
}
