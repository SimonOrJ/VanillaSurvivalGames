package com.simonorj.mc.VanillaSurvivalGames;

import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public abstract class AbstractTag {
	private final String TAG;
	private final HashSet<Player> playing = new HashSet<>();
	private final TagListener listener = new TagListener();
	private final Scoreboard scoreboard;
	private final Objective obj;
	private final Team team1, team2, team3;
	
	protected final VanillaSurvivalGames plugin;
	protected abstract String[] getScoreboard();

	abstract String getGameName();
	
	// Taking advantage of OOP
	
	AbstractTag(VanillaSurvivalGames plugin, String tag) {
		// Initial Variables
		this.plugin = plugin;
		this.TAG = tag;
		plugin.getServer().getPluginManager().registerEvents(listener, plugin);
		
		// Initiate Scoreboard
		this.scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
		obj = scoreboard.registerNewObjective("game", "dummy");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.setDisplayName(ChatColor.YELLOW.toString() + ChatColor.BOLD + getScoreboard()[0]);
		
		obj.getScore(ChatColor.BOLD + getScoreboard()[1] + ":").setScore(9);
		team1 = scoreboard.registerNewTeam(getScoreboard()[1]);
		team1.setPrefix(ChatColor.RED.toString());
		// Team 1 Member score: 8
		
		obj.getScore("").setScore(7);
		obj.getScore(ChatColor.BOLD + getScoreboard()[2] + ":").setScore(6);
		team2 = scoreboard.registerNewTeam(getScoreboard()[2]);
		team2.setPrefix(ChatColor.YELLOW.toString());
		// Team 2 Member score: 5
		
		if (getScoreboard().length >= 4) {
			obj.getScore("").setScore(4);
			obj.getScore(ChatColor.BOLD + getScoreboard()[3] + ":").setScore(3);
			team3 = scoreboard.registerNewTeam(getScoreboard()[3]);
			team3.setPrefix(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH);
			// Team 3 Member score: 2
		} else {
			team3 = team1;
		}
		
		
	}
	
	final void reset() {
		gameReseting();
		for (Player p : playing) {
			team2.addEntry(p.getName());
			p.sendMessage(tagHeading("Game was reset."));
		}
	}
	
	final boolean addPlayer(Player p) {
		if (playing.contains(p))
			return false;
		
		p.setScoreboard(scoreboard);
		playing.add(p);
		
		added(p);
		return true;
	}
	
	final void removePlayer(Player p) {
		// True means he was attacker/attacked; "false" means currently in-game
		p.sendMessage(tagHeading("You left the game."));
		playing.remove(p);
		scoreboard.getEntryTeam(p.getName()).removeEntry(p.getName());
		scoreboard.resetScores(p.getName());
		p.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
		
		removed(p); // Fire player leaving
		
    	if (playing.isEmpty()) {
    		// Kill this game.
			HandlerList.unregisterAll(listener);
			plugin.removeGame(this);
    	}
	}
	
	final void scoreTaggingPlayer(Player p) {
		team1.addEntry(p.getName());
		obj.getScore(p.getName()).setScore(8);
	}
	
	final void scoreFleeingPlayer(Player p) {
		team2.addEntry(p.getName());
		obj.getScore(p.getName()).setScore(5);
	}
	
	final void scoreOutPlayer(Player p) {
		team3.addEntry(p.getName());
		obj.getScore(p.getName()).setScore((team1 == team3) ? 8 : 2);
	}
	
	protected final HashSet<Player> setListPlayers() { return playing; }
	protected final boolean setHas(Player p) { return playing.contains(p); }
	
	final TextComponent actionHeading(String msg) {
		TextComponent t = new TextComponent(),
				a = new TextComponent(TAG);
		t.setColor(ChatColor.WHITE);
		a.setColor(ChatColor.YELLOW);
		a.setBold(true);
		t.addExtra(a);
		t.addExtra(": ");
		t.addExtra(msg);
		return t;
	}
	
	final void actionBar(Player p, String msg) {
		p.spigot().sendMessage(ChatMessageType.ACTION_BAR, actionHeading(msg));
	}
	
	final void broadcastAction(TextComponent msg) {
		for (Player p : playing) {
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, msg);
		}
	}
	
	final void broadcast(TextComponent msg) {
		for (Player p : playing) {
			p.spigot().sendMessage(msg);
		}
	}
	
	final String tagHeading(String msg) {
		return ChatColor.YELLOW.toString() + ChatColor.BOLD + "[" + ChatColor.YELLOW + TAG + ChatColor.BOLD + "] " + ChatColor.GRAY + msg;
	}
	
	final TextComponent tagTCHeading() {
		TextComponent t = new TextComponent(),
				a = new TextComponent("["),
				b = new TextComponent(TAG),
				c = new TextComponent("]");
		a.setBold(true);
		c.setBold(true);
		a.setColor(ChatColor.YELLOW);
		b.setColor(ChatColor.YELLOW);
		c.setColor(ChatColor.YELLOW);
		t.setColor(ChatColor.GRAY);
		t.addExtra(a);
		t.addExtra(b);
		t.addExtra(c);
		t.addExtra(" ");
		return t;
	}
	
	final TextComponent tagTCHeading(String msg) {
		TextComponent t = tagTCHeading();
		t.addExtra(msg);
		return t;
	}
	
	private class TagListener implements Listener {
	    @EventHandler (priority=EventPriority.MONITOR)
	    public final void checkQuit(PlayerQuitEvent e) {
	    	removePlayer(e.getPlayer());
	    }
	    
	    @EventHandler (priority=EventPriority.HIGH)
	    public final void detectTag(EntityDamageByEntityEvent e) {
	    	// if damage is not done by player to player
	    	if (!(e.getDamager() instanceof Player && e.getEntity() instanceof Player))
	    		return;
	    	
	    	Player p = (Player)e.getDamager(),
	    			ph = (Player)e.getEntity();
	    	
	    	boolean pb = playing.contains(p),
	    			phb = playing.contains(ph);
	    	
	    	// Person who's it cannot damage anyone
	    	if (pb || phb) {
	    		e.setCancelled(true);
	    		if (pb && phb)
	    			hit(p, ph);
	    	}
	    }
	}
	
    abstract protected void added(Player joiner);
    abstract protected void hit(Player hitter, Player victim);
    abstract protected void volunteer(Player volunteer);
    abstract protected void removed(Player quitter);
    abstract protected void gameReseting();
	abstract void sendStatus(Player p);
}
