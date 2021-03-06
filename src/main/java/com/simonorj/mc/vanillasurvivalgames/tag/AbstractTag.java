package com.simonorj.mc.vanillasurvivalgames.tag;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.simonorj.mc.vanillasurvivalgames.VanillaSurvivalGames;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public abstract class AbstractTag {
	private final String TAG;
	private final HashSet<Player> playing = new HashSet<>();
	private final TagListener listener = new TagListener();
	private final FrzListener frzListener;
	private final Scoreboard scoreboard;
	private final Objective obj;
	private final Team team1, team2, team3;
	
	private BukkitRunnable timerTask;
	private int time;
	protected final VanillaSurvivalGames plugin;
	protected abstract String[] getScoreboard();

	public abstract String getGameName();
	
	public AbstractTag(VanillaSurvivalGames plugin, String tag, boolean freezeListener) {
		// Initial Variables
		this.plugin = plugin;
		this.TAG = tag;
		
		plugin.getServer().getPluginManager().registerEvents(listener, plugin);
		if (freezeListener) {
			frzListener = new FrzListener();
			plugin.getServer().getPluginManager().registerEvents(frzListener, plugin);
		} else {
			frzListener = null;
		}
		
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
			obj.getScore(ChatColor.BOLD.toString()).setScore(4);
			obj.getScore(ChatColor.BOLD + getScoreboard()[3] + ":").setScore(3);
			team3 = scoreboard.registerNewTeam(getScoreboard()[3]);
			team3.setPrefix(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH);
			// Team 3 Member score: 2
		} else {
			team3 = team1;
		}
	}
	
	public final void reset() {
		gameReseting();
		if (timerTask != null)
			timerTask.cancel();
		
		broadcast(tagHeading("Game was reset."));
	}
	
	public final boolean addPlayer(Player p) {
		if (playing.contains(p))
			return false;
		
		p.setScoreboard(scoreboard);
		playing.add(p);
		p.sendMessage(tagHeading("You joined the " + getGameName() + "."));
		broadcastAction(actionHeading(p.getName() + " joined the game"));
		added(p);
		return true;
	}
	
	public final void removePlayer(Player p) {
		// True means he was attacker/attacked; "false" means currently in-game
		p.sendMessage(tagHeading("You left the " + getGameName()+ "."));
		playing.remove(p);
		scoreboard.getEntryTeam(p.getName()).removeEntry(p.getName());
		scoreboard.resetScores(p.getName());
		p.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
		
		broadcastAction(actionHeading(p.getName() + " left the game"));
		removed(p); // Fire player leaving
		
    	if (playing.isEmpty()) {
    		// Kill this game.
			HandlerList.unregisterAll(listener);
			if (frzListener != null) HandlerList.unregisterAll(frzListener);
			plugin.removeGame(this);
    	}
	}
	
	public final HashSet<Player> setListPlayers() { return playing; }
	public final String getTime() {
		if (time <= 60)
			return time + "s";
		
		int m = time/60;
		int s = time%60;
		
		return (m<10 ? "0" : "") + m + ":" + (s<10 ? "0" : "") + time%60;
	}
	
	protected final void scoreTaggingPlayer(Player p) {
		team1.addEntry(p.getName());
		obj.getScore(p.getName()).setScore(8);
	}
	
	protected final void scoreFleeingPlayer(Player p) {
		team2.addEntry(p.getName());
		obj.getScore(p.getName()).setScore(5);
	}
	
	protected final void scoreOutPlayer(Player p) {
		team3.addEntry(p.getName());
		obj.getScore(p.getName()).setScore((team1 == team3) ? 8 : 2);
	}
	
	protected final BukkitRunnable getTimerTask() {
		return timerTask;
	}
	
	protected final BukkitRunnable newTimerTask(String text, int from, boolean increment) {
		if (timerTask != null) {
			timerTask.cancel();
		}
		// Setup initial scoreboard message
		time = from;
		String s = text + ": " + (increment ? ChatColor.GREEN : ChatColor.RED),
				l = s + time + "s";
		obj.getScore(ChatColor.RESET.toString()).setScore(1);
		obj.getScore(l).setScore(0);
		
		timerTask = new BukkitRunnable() {
			private String last = l;
			private String head = s;
			
			@Override
			public void run() {
				if (time == 0 && !increment) {
					cancel();
					return;
				}
				scoreboard.resetScores(last);
				
				if (time <= 60) {
					last = head + (increment ? ++time : --time) + "s";
				} else {
					if (increment) time++;
					else time--;
					
					int m = time/60;
					int s = time%60;
					
					last = head + (m<10 ? "0" : "") + m + ":" + (s<10 ? "0" : "") + time%60;
				}
				
				obj.getScore(last).setScore(0);
			}
			
			@Override
			public void cancel() {
				super.cancel();
				timerTask = null;
				scoreboard.resetScores(last);
				scoreboard.resetScores(ChatColor.RESET.toString());
				timerDone();
			}
		};
		
		timerTask.runTaskTimer(plugin, 20L, 20L);
		
		return timerTask;
	}
	
	protected final void frzAddPlayer(Player p) {
		frzListener.add(p);
	}
	
	protected final void frzRemovePlayer(Player p) {
		frzListener.remove(p);
	}
	
	protected final TextComponent actionHeading(String msg) {
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
	
	protected final void actionBar(Player p, String msg) {
		p.spigot().sendMessage(ChatMessageType.ACTION_BAR, actionHeading(msg));
	}
	
	protected final void broadcastAction(TextComponent msg) {
		for (Player p : playing) {
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, msg);
		}
	}
	
	protected final void broadcast(TextComponent msg) {
		for (Player p : playing) {
			p.spigot().sendMessage(msg);
		}
	}
	
	protected final void broadcast(String msg) {
		for (Player p : playing) {
			p.sendMessage(msg);
		}
	}
	
	protected final String tagHeading(String msg) {
		return ChatColor.YELLOW.toString() + ChatColor.BOLD + "[" + ChatColor.YELLOW + TAG + ChatColor.BOLD + "] " + ChatColor.GRAY + msg;
	}
	
	protected final TextComponent tagTCHeading() {
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
	
	protected final TextComponent tagTCHeading(String msg) {
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
	
	private class FrzListener implements Listener {
		private HashMap<Player, Location> plist = new HashMap<>();
		
		final void add(Player p) {
			Location l = p.getLocation();
			plist.put(p, new Location(p.getWorld(), l.getX(), l.getY(), l.getZ()));
		}
		
		final void remove(Player p) {
			plist.remove(p);
		}
		
	    @EventHandler (priority=EventPriority.MONITOR)
	    public final void keepStuck(PlayerMoveEvent e) {
	    	Location l = plist.get(e.getPlayer());
	    	if (l == null)
	    		return;
	    	
	    	Location t = e.getTo();
	    	
	    	if (Math.abs(l.getX() - t.getX()) > 1.5d || Math.abs(l.getZ() - t.getZ()) > 1.5d) {
		    	e.getPlayer().teleport(l);
		    	actionBar(e.getPlayer(), "You're not allowed to move now!"); // TODO: TEST
	    	}
	    }
	}
	
    protected void timerDone() {}
    abstract protected void added(Player joiner);
    abstract protected void hit(Player hitter, Player victim);
    abstract protected void removed(Player quitter);
    abstract protected void gameReseting();
    abstract public void volunteer(Player volunteer);
	abstract public void sendStatus(Player p);
}
