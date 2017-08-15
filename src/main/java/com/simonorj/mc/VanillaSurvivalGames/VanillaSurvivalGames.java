package com.simonorj.mc.vanillasurvivalgames;

import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

// Hitting-tagging based games
public class VanillaSurvivalGames extends JavaPlugin {
	private HashSet<AbstractTag> tags = new HashSet<>();
	private static final TextComponent HELP = new TextComponent();
	
	public VanillaSurvivalGames() {
		String[][] help = {
				{"/game help","Show this menu"},
				{"/game list","List all existing games"},
				{"/game join <player>","Join a game the player is in"},
				{"/game create <game type>","Create a new game"},
				{"-- While in game:"},
				{"/game invite <player...>","Invite a player to the game"},
				{"/game it","Volunteer to be the tagger or seeker"},
				{"/game status","Show the status of the game"},
				{"/game reset","Reset the game"},
				{"/game leave","Leave the game"},
		};
		
		TextComponent h = new TextComponent("Minigame Menu");
		h.setBold(true);
		h.setColor(ChatColor.GOLD);
		
		HELP.setColor(ChatColor.GRAY);
		HELP.addExtra("----- ");
		HELP.addExtra(h);
		HELP.addExtra(" -----\n");

		for (String[] s : help) {
			if (s.length == 1) {
				HELP.addExtra(s[0]);
				HELP.addExtra("\n");
				continue;
			}
			TextComponent a = new TextComponent(s[0]);
			a.setColor(ChatColor.YELLOW);
			a.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, s[0] + " "));
			
			HELP.addExtra("- ");
			HELP.addExtra(a);
			HELP.addExtra(" - ");
			HELP.addExtra(s[1]);
			HELP.addExtra("\n");
		}
		
		HELP.addExtra("--");
	}
	
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
				sender.spigot().sendMessage(HELP);
				return true;
			} else {
				game.sendStatus((Player)sender);
				return true;
			}
		}
		
		if (args[0].equalsIgnoreCase("help")) {
			sender.spigot().sendMessage(HELP);
			return true;
		}
		
		// Lists all the games (so to view it even when in a game)
		if (args[0].equalsIgnoreCase("list")) {
			listTags(sender);
			return true;
		}
		
		if (args[0].equalsIgnoreCase("status")) {
			if (game == null) {
				sender.sendMessage(tagMsg(Messages.NOT_IN_GAME.msg));
				return true;
			}
			game.sendStatus((Player)sender);
		}
		
		// No Console Zone
		if (!(sender instanceof Player)) {
			sender.sendMessage(tagMsg(Messages.NO_CONSOLE.msg));
			return true;
		}
		Player p = (Player)sender;
		
		// Reset a game to initial state (must be in a game)
		if (args[0].equalsIgnoreCase("reset")) {
			if (game == null) {
				sender.sendMessage(tagMsg(Messages.NOT_IN_GAME.msg));
				return true;
			}
			
			game.reset();
			return true;
		}
		
		// Create a game
		if (args[0].equalsIgnoreCase("create")) {
			if (game != null) {
				sender.sendMessage(tagMsg(Messages.ALREADY_IN_GAME.msg));
				return true;
			}
			
			if (args.length == 1) {
				sender.sendMessage(tagMsg("Available gamemodes: tag\n"//, freezetag, hideandseek, and hideandseek2\n"
						+ " Usage: /game create <tag>"));//|freezetag|hideandseek|hideandseek2>"));
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
			
			TextComponent tc= tagTC();
			tc.addExtra("You successfully created a new game.  Invite people!");
			tc.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/game invite "));
			
			sender.spigot().sendMessage();
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
			
			TextComponent t = tagTC(),
					a = new TextComponent(p.getName());
			t.addExtra("You're invited to ");
			t.addExtra(a);
			t.addExtra("'s game of " + game.getGameName() + ". Click to join!");
			t.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/game join " + p.getName()));
			
			for (int i = 1; i < args.length; i++) {
				Player inv = getServer().getPlayer(args[i]);
				if (inv == null) {
					sender.sendMessage(args[i] + " is not a valid player");
				} else {
					inv.spigot().sendMessage(t);
				}
			}
			return true;
		}
		
		// Joining a game
		if (args[0].equalsIgnoreCase("join")) {
			if (game != null) {
				sender.sendMessage(tagMsg(Messages.ALREADY_IN_GAME.msg));
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
			
			sender.sendMessage(tagMsg(toJoin.getName() + " is not playing any game."));
			return true;
		}
		
		// Being it or the seeker
		if (args[0].equalsIgnoreCase("it")) {
			if (game == null) {
				sender.sendMessage(tagMsg(Messages.NOT_IN_GAME.msg));
				return true;
			}
			
			game.volunteer(p);
			return true;
		}
		
		// Leaving a game
		if (args[0].equalsIgnoreCase("leave")) {
			if (game == null) {
				sender.sendMessage(tagMsg(Messages.NOT_IN_GAME.msg));
				return true;
			}
			
			game.removePlayer(p);
			return true;
		}
		
		sender.sendMessage(tagMsg("Usage: /game [help|list|join|create|invite|it|status|reset|leave]"));
		return true;
	}
	
	private void listTags(CommandSender sender) {
		if (tags.isEmpty()) {
			sender.sendMessage(tagMsg("There are no games running."));
			return;
		}
		TextComponent t = tagTC();
		t.addExtra("Click a game below to join it:\n");

		for (AbstractTag at : tags) {
			TextComponent a = new TextComponent(at.getGameName()),
					b = new TextComponent("- ");
			
			a.setColor(ChatColor.YELLOW);
			b.addExtra(a);
			b.addExtra(": ");
			
			Iterator<Player> i = at.setListPlayers().iterator();
			Player p = i.next();
			
			b.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/game join " + p.getName()));
			b.addExtra(p.getName());
			
			
			while (i.hasNext()) {
				b.addExtra(", ");
				b.addExtra(i.next().getName());
			}
			t.addExtra(b);
			t.addExtra("\n");
		}
		sender.spigot().sendMessage(t);
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
		ALREADY_IN_GAME("You're already in a game!"),
		NO_CONSOLE("You must be a player.");
		
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
