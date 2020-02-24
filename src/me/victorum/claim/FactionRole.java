package me.victorum.claim;

public enum FactionRole {
	LEADER(2), MEMBER(3);

	private final int val;

	private FactionRole(int val) {
		this.val = val;
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
