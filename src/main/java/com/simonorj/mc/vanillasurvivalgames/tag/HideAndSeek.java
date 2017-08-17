package com.simonorj.mc.vanillasurvivalgames.tag;

import java.util.HashSet;

import org.bukkit.entity.Player;

import com.simonorj.mc.vanillasurvivalgames.VanillaSurvivalGames;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class HideAndSeek extends AbstractTag {
	private final HashSet<Player> hider = new HashSet<>();
	private static final String[] SCOREBOARD = {"  Hide And Seek  ","Seeker","Hidden","Found"};
	private static final String GAME_NAME = "Hide and Seek";
	private Player seeker = null;
	private Player nextSeeker = null;
	private boolean hidingTime = false;

	public HideAndSeek(VanillaSurvivalGames plugin) {
		super(plugin, "Hide&Seek", true);
		
	}

	@Override
	protected String[] getScoreboard() {
		return SCOREBOARD;
	}

	@Override
	public String getGameName() {
		return GAME_NAME;
	}

	@Override
	protected void added(Player joiner) {
		scoreOutPlayer(joiner);
	}

	@Override
	protected void hit(Player hitter, Player victim) {
		if (!hidingTime && hitter == seeker && hider.contains(victim)) {
			hider.remove(victim);
			scoreOutPlayer(victim);
			
			if (nextSeeker == null)
				nextSeeker = victim;
			
			if (hider.isEmpty()) {
				getTimerTask().cancel();
				broadcast(tagHeading("Everyone's found! Time taken: " + getTime()));
				// TODO: Setup so next seeker can choose when to start countdown and hunting.
				reset();
			}
		}
	}

	@Override
	protected void removed(Player quitter) {
		if (quitter == seeker) {
			seeker = null;
			hidingTime = false;
			TextComponent t = tagTCHeading("The seeker has left.  Click "),
					a = new TextComponent("here");
			t.setColor(ChatColor.GRAY);
			a.setColor(ChatColor.WHITE);
			t.addExtra(a);
			t.addExtra(" to volunteer to be the new seeker!");
			t.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vanillasurvivalgames:game it"));
			
			getTimerTask().cancel();
			broadcast(t);
		}
	}

	@Override
	protected void gameReseting() {
		seeker = nextSeeker = null;
		for (Player p : setListPlayers()) {
			scoreOutPlayer(p);
		}
		hidingTime = false;
		getTimerTask().cancel();
	}

	@Override
	public void volunteer(Player volunteer) {
		if (seeker != null) {
			volunteer.sendMessage(tagHeading("A game is already ongoing.  Please complete the game, or reset the game first."));
			return;
		}
		
		seeker = volunteer;
		
		for (Player p : setListPlayers()) {
			if (p == volunteer) {
				scoreTaggingPlayer(p);
				continue;
			}
			scoreFleeingPlayer(p);
		}
		
		broadcast(tagHeading(volunteer.getName() + " volunteered to be a seeker!"));
		
		hidingTime = true;
		newTimerTask("Seeking in", 20, false);
		frzAddPlayer(seeker);
	}

	@Override
	public void sendStatus(Player p) {
		p.sendMessage(tagHeading(seeker.getName() + " is currently it!"));

	}

	@Override
	protected void timerDone() {
		if (!hidingTime)
			return;
		
		frzRemovePlayer(seeker);
		hidingTime = false;
		broadcastAction(actionHeading("Seeker is now seeking!"));
		newTimerTask("Time Elspsed", 0, true);
	}
}
