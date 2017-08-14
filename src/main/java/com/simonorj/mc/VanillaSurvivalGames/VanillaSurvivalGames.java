package com.simonorj.mc.VanillaSurvivalGames;

import java.util.HashSet;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

// Hitting-tagging based games
public class VanillaSurvivalGames extends JavaPlugin {
	private HashSet<AbstractTag> tags = new HashSet<>();
	
	@Override
	public void onDisable() {
		for (AbstractTag t : tags)
			t.reset();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Check if player is in a game
		AbstractTag game = null;
		// NOTE: If variable `game` is not null, sender is definitely a player.
		if (sender instanceof Player) {
			for (AbstractTag t : tags) {
				if (t.setListPlayers().contains(sender)) {
					game = t;
				}
				break;
			}
		}
		
		if (args.length == 0) {
			if (game == null) {
				// send help
				return false;
			} else {
				game.sendStatus((Player)sender);
				return true;
			}
		}
		
		// Lists all the games (so to view it even when in a game)
		if (args[0].equalsIgnoreCase("list")) {
			listTags(sender);
			return true;
		}
		
		// Reset a game to initial state (must be in a game)
		if (args[0].equalsIgnoreCase("reset")) {
			if (game == null) {
				sender.sendMessage(tagMsg(Messages.NOT_IN_GAME.msg));
				// TODO: No Reset Reason
				return true;
			}
			
			game.reset();
			return true;
		}
		
		// No Console Zone
		if (!(sender instanceof Player)) {
			// TODO: No Console Message
			return true;
		}
		Player p = (Player)sender;
		
		// Create a game
		if (args[0].equalsIgnoreCase("create")) {
			if (game != null) {
				sender.sendMessage(tagMsg(Messages.ALREADY_IN_GAME.msg));
				return true;
			}
			
			if (args.length == 1) {
				// TODO: Send types of games
				sender.sendMessage(tagMsg("Available gamemodes: tag, freezetag, hideandseek, and hideandseek2\n"
						+ " Usage: /game create <tag|freezetag|hideandseek|hideandseek2>"));
				return true;
			}
			
			AbstractTag t;
			
			switch (args[1].toLowerCase()) {
			case "tag":
				t = new Tag(this);
				break;
			case "freeze":
			case "freezetag":
				//break;
			case "hideandseek":
			case "has":
			case "has1":
			case "hideandseek1":
				//break;
			case "has2":
			case "hideandseek2":
				sender.sendMessage(tagMsg("Coming Soon!"));
				return true;
				//break;
			default:
				sender.sendMessage(tagMsg("Invalid gamemode"));
				return true;
			}
			
			tags.add(t);
			sender.sendMessage(tagMsg("You successfully created a new game!"));
			t.addPlayer(p);
			
			return true;
		}
		
		/* TODO
		 * IDEA: Make it possible to make new tag rooms.
		 * Rooms will be based on who created the game and what kind of game it is.
		 *  - Tag - You become the new tagger.
		 *  - Freeze Tag - You freeze until unfrozen by teammate.  Last frozen person will be the new tagger next round. 
		 * Hide and Seek: First to be found will be the new seeker next round.
		 *  - 1 - You're out.
		 *  - 2 - You also become the seeker.
		 */
		
		// Inviting a player
		if (args[0].equalsIgnoreCase("invite")) {
			if (game == null) {
				sender.sendMessage(tagMsg(Messages.NOT_IN_GAME.toString()));
				return true;
			}
			
			if (args.length == 1) {
				sender.sendMessage(tagMsg("Please specify the player(s) to invite!"));
				return true;
			}
			
			for (int i = 1; i < args.length; i++) {
				Player inv = getServer().getPlayer(args[i]);
				if (inv == null) {
					sender.sendMessage(args[i] + " is not a valid player");
				} else {
					
					TextComponent t = tagTC(),
							a = new TextComponent(p.getName());
					t.addExtra("You're invited to ");
					t.addExtra(a);
					t.addExtra("'s game of " + game.getGameName() + "!");
					
					inv.spigot().sendMessage(t);
					// TODO: Specify the gamemode of the current game
				}
			}
			
		}
		
		// Joining a game
		if (args[0].equalsIgnoreCase("join")) {
			if (game != null) {
				sender.sendMessage(Messages.ALREADY_IN_GAME.toString());
				return true;
			}
			
			if (args.length == 1) {
				listTags(sender);
				return true;
			}
			
			// Join by a player in a game
			Player toJoin = getServer().getPlayer(args[1]);
			for (AbstractTag t : tags) {
				if (t.setListPlayers().contains(toJoin)) {
					t.addPlayer(p);
					return true;
				}
			}
			
			sender.sendMessage(toJoin.getName() + " is not playing any game.");
			return true;
		}
		
		// Being it or the seeker
		if (args[0].equalsIgnoreCase("it") || args[0].equalsIgnoreCase("seeker")) {
			if (game == null) {
				sender.sendMessage("You're not in game!");
				return true;
			}
			
			game.volunteer(p);
			return true;
		}
		
		// Leaving a game
		if (args[0].equalsIgnoreCase("leave")) {
			if (game == null) {
				sender.sendMessage("You're not in game!");
				return true;
			}
			
			game.removePlayer(p);
			return true;
		}
		return false;
	}
	
	private void listTags(CommandSender sender) {
		String lists = "";
		for (AbstractTag t : tags) {
			String list = "|Game: ";
			for (Player p : t.setListPlayers()) {
				list += p.getName() + " ";
			}
			list += " |";
			lists += list;
		}
		sender.sendMessage("Existing games: " + lists + "\nEnter the game using a username!");
	}
	
	String tagMsg(String msg) {
		return ChatColor.GRAY + "[" + ChatColor.YELLOW + "Game" + ChatColor.GRAY + "] " + ChatColor.RESET + msg;
	}
	
	TextComponent tagTC() {
		TextComponent ret = new TextComponent(),
				a = new TextComponent("["),
				b = new TextComponent("Game");
		a.setColor(ChatColor.GRAY);
		b.setColor(ChatColor.YELLOW);
		a.addExtra(b);
		a.addExtra("]");
		ret.addExtra(a);
		ret.addExtra(" ");
		return ret;
		
	}
	
	private enum Messages {
		NOT_IN_GAME("You're not in a game!"),
		ALREADY_IN_GAME("You're already in a game!");
		
		final String msg;
		private Messages(String s) { msg = s; }
		
		@Override
		public String toString() {
			return msg;
		}
	}
	
	void removeGame(AbstractTag game) {
		tags.remove(game);
	}
}
