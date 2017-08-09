package com.simonorj.mc.VanillaSurvivalGames;

import org.bukkit.entity.Player;

public class Tag extends AbstractTag {

	Tag(VanillaSurvivalGames plugin) {
		super(plugin);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void playerJoin(Player joiner) {
		broadcast(joiner.getName() + " joined the game of tag");
	}

	@Override
	protected void playerHit(Player hitter, Player victim) {
		if (hitter == getMark()) {
			setMark(victim);
			broadcast(victim.getName() + " is now it");
		}
	}

	@Override
	protected void playerLeft(Player quitter) {
		broadcast(quitter.getName() + " left the game of tag");
		if (quitter == getMark()) {
			broadcast("The tagger has left.  Type \"/tag it\" to volunteer to be it!");
		}
	}

}
