package com.simonorj.mc.vanillasurvivalgames;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Tag extends AbstractTag {
	private static final String[] SCOREBOARD = {"       TAG       ","Tagger","Runners"};
	private static final String GAME_NAME = "Tag";
	private Player tagger = null, tagBack = null;
	
	String getGameName() {
		return GAME_NAME;
	}
	
	Tag(VanillaSurvivalGames plugin) {
		super(plugin,"Tag");
	}

	@Override
	protected String[] getScoreboard() {
		return SCOREBOARD;
	}

	@Override
	protected void added(Player joiner) {
		broadcastAction(actionHeading(joiner.getName() + " joined the game of tag"));
		scoreFleeingPlayer(joiner);
	}

	@Override
	protected void hit(Player hitter, Player victim) {
		if (hitter != tagger)
			return;
		
		// No tagbacks!
		if (tagBack == victim) {
			actionBar(hitter, "No tagbacks for 3 seconds!");
			return;
		}
		
		tagBack = hitter;
		scoreFleeingPlayer(tagger);
		tagger = victim;
		scoreTaggingPlayer(victim);
		
		new BukkitRunnable(){
			@Override
			public void run() {
				tagBack = null;
			}
		}.runTaskLater(plugin, 60L);
		
		broadcastAction(actionHeading(victim.getName() + " is now it"));
	}

	@Override
	protected void volunteer(Player volunteer) {
		if (tagger != null)
			scoreFleeingPlayer(tagger);
		scoreTaggingPlayer(volunteer);
		tagger = volunteer;
		broadcastAction(actionHeading(volunteer.getName() + " volunteered to be it"));
		volunteer.sendMessage(tagHeading("You Volunteered to be a tagger."));
	}

	@Override
	protected void removed(Player quitter) {
		broadcastAction(actionHeading(ChatColor.WHITE + quitter.getName() + ChatColor.GRAY + " left the game of tag"));
		if (quitter == tagger) {
			tagger = null;
			TextComponent t = tagTCHeading("The tagger has left.  Click "),
					a = new TextComponent("here");
			t.setColor(ChatColor.GRAY);
			a.setColor(ChatColor.WHITE);
			t.addExtra(a);
			t.addExtra(" to volunteer to be it!");
			t.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vanillasurvivalgames:game it"));
			
			broadcast(t);
		}
	}

	@Override
	void sendStatus(Player p) {
		if (tagger != null) {
			p.sendMessage(tagHeading(ChatColor.WHITE + tagger.getName() + ChatColor.GRAY + " is it!"));
		} else {
			TextComponent t = tagTCHeading("Nobody is it. Click "),
					a = new TextComponent("here");
			a.setColor(ChatColor.WHITE);
			t.addExtra(a);
			t.addExtra(" to volunteer to be it!");
			t.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vanillasurvivalgames:game it"));
			
			p.spigot().sendMessage(t);
		}
	}

	@Override
	protected void gameReseting() {
		for (Player p : setListPlayers())
			scoreFleeingPlayer(p);
		tagger = tagBack = null;
	}
}