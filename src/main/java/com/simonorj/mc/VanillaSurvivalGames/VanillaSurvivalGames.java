package com.simonorj.mc.VanillaSurvivalGames;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

// Hitting-tagging based games
public class VanillaSurvivalGames extends JavaPlugin {
	private AbstractTag tag;
	
	@Override
	public void onEnable() {
		tag = new Tag(this);
	}
	
	@Override
	public void onDisable() {
		tag.reset();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("tag")) {
			if (args.length == 0) {
				return false;
			}
			
			if (!(sender instanceof Player)) {
				sender.sendMessage("Only players can use this.");
				return true;
			}
			
			Player p = (Player)sender;
			
			if (args[0].equalsIgnoreCase("join")) {
				tag
				.addPlayer(p);
			}
			else if (args[0].equalsIgnoreCase("it")) {
				tag.setMark(p);
				tag
				.broadcast(p.getName() + " volunteered to be it");
				tag
				.tag(p);
			}
			else if (args[0].equalsIgnoreCase("leave")) {
				tag
				.removePlayer(p);
			}
			else if (args[0].equalsIgnoreCase("reset")) {
				tag.reset();
				tag = new Tag(this);
			}
			return true;
		}
		return false;
	}
	
}
