package com.simonorj.mc.VanillaSurvivalGames;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public abstract class AbstractTag {
	VanillaSurvivalGames plugin;
	private final HashMap<Player, Boolean> playing = new HashMap<>();
	private Player mark = null;
	private TagListener listener = new TagListener();
	
	AbstractTag(VanillaSurvivalGames plugin) {
		this.plugin = plugin;
	}
	
	final void reset() {
		playing.clear();
		mark = null;
		HandlerList.unregisterAll(listener);//TODO: unregister these events
		
	}
	
	final boolean addPlayer(Player p) {
		if (playing.containsKey(p))
			return false;
		
		if (playing.isEmpty())
			plugin.getServer().getPluginManager().registerEvents(listener, plugin);
		
		playing.put(p, false);
		playerJoin(p);
		return true;
	}
	
	final boolean removePlayer(Player p) {
		playerLeft(p);
		
		if (mark == p)
			mark = null;
    	if (playing.remove(p)) { // TODO: NullPointerException
    		return true;
    	}
    	
    	if (playing.isEmpty())
			HandlerList.unregisterAll(listener);//TODO: unregister these events
    	
		return false;
	}
	
	final void broadcast(String msg) {
		for (Player p : playing.keySet()) {
			p.sendMessage(msg);
		}
	}
	
	protected final Set<Player> listPlayer() { return playing.keySet(); }
	protected final void tag(Player p) { playing.put(p, true); }
	protected final Boolean isTagged(Player p) { return playing.get(p); }
	protected final void untag(Player p) { playing.put(p, false); }
	protected final void setMark(Player p) { mark = p; }
	protected final Player getMark() { return mark; }
	protected final void unmark() { mark = null; }
	
	private class TagListener implements Listener {
	    @EventHandler (priority=EventPriority.MONITOR)
	    public final void checkQuit(PlayerQuitEvent e) {
	    	removePlayer(e.getPlayer());
	    }
	    
	    @EventHandler (priority=EventPriority.HIGH)
	    public final void detectTag(EntityDamageByEntityEvent e) {
	    	// if damage is not done to player by player
	    	if (!(e.getDamager() instanceof Player && e.getEntity() instanceof Player))
	    		return;
	    	
	    	Player p = (Player)e.getDamager(),
	    			ph = (Player)e.getEntity();
	    	
	    	boolean pb = playing.containsKey(p),
	    			phb = playing.containsKey(ph);
	    	
	    	// Person who's it cannot damage anyone
	    	if (pb || phb) {
	    		e.setCancelled(true);
	    		if (pb && phb)
	    			playerHit(p, (Player)e.getEntity());
	    	}
	    }
	}
	
    abstract protected void playerJoin(Player joiner);
    abstract protected void playerHit(Player hitter, Player victim);
    abstract protected void playerLeft(Player quitter);
}
