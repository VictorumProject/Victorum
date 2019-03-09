package net.parinacraft.victorum.data;

import net.parinacraft.victorum.claim.Relation;

public class RelationHandler {
	public Relation getRelation(int facID, int otherFacID) {
		if (facID == otherFacID)
			return Relation.OWN;
		return Relation.NEUTRAL;
	}

}
