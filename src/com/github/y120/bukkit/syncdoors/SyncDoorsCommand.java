package com.github.y120.bukkit.syncdoors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.y120.bukkit.Common.Utils;

import static com.github.y120.bukkit.Common.*;
import static com.github.y120.bukkit.syncdoors.SyncDoorsConfig.Config;

public class SyncDoorsCommand implements CommandExecutor {

	private SyncDoors plugin;
	private SyncDoorsListener listener;
	public SyncDoorsCommand(SyncDoors pl, SyncDoorsListener li) {
		plugin = pl;
		listener = li;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!cmd.getName().equalsIgnoreCase("syncdoors"))
			return false;
		
		if (args.length == 0) {
			sendHelpText(sender);
			return true;
		} else if (args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("c")) {
			if (!Utils.enforcePerm(sender, "syncdoors.check"))
				return true;
			
			if (!plugin.getConfig().getBoolean("global")) {
				sender.sendMessage("SyncDoors is " + Constants.Strings.DISABLED + ".");
				return true;
			}
			sender.sendMessage("SyncDoors is " + Constants.Strings.ENABLED + ".");
			
			sender.sendMessage("The following options are enabled:");
			String s = "";
			for (String opt : Config.OPTIONS)
				if (Config.options.get(opt))
					s += ", " + opt;
			sender.sendMessage(CC.Y + "  " + s.substring(2));

			if (Config.options.get("redstone")) {
				s = "";
				sender.sendMessage("The following redstone components are checked:");
				
				List<String> st = new ArrayList<String>();
				for (Material mat : Config.redstone)
					st.add(Utils.materialName(mat));
				Collections.sort(st);
				
				for (String t : st)
					s += ", " + t;
				sender.sendMessage(CC.Y + "  " + s.substring(2));
			}
			
			return true;
		} else if (args[0].equalsIgnoreCase("toggle") || args[0].equalsIgnoreCase("t")) {
			if (!Utils.enforcePerm(sender, "syncdoors.toggle"))
				return true;
			
			if (args.length > 1) {
				for (String opt : Config.OPTIONS)
					if (args[1].equalsIgnoreCase(opt) || args[1].equalsIgnoreCase(opt.substring(0, 1))) {
						Config.options.put(opt, !Config.options.get(opt));
						Config.save();
						if (opt.equals("redstone") || opt.equals("player")) // update listeners
							listener.register();
						sender.sendMessage(opt + " is now " + Utils.strEnabled(Config.options.get(opt)) + ".");
						return true;
					}
				if (args[1].equalsIgnoreCase("component") || args[1].equalsIgnoreCase("c")) {
					if (!Utils.enforcePlayer(sender))
						return true;
					Player p = (Player) sender;
					Block b = p.getTargetBlock(SyncDoorsConfig.transparent, 30);
					if (b == null) {
						sender.sendMessage(CC.R + "No block in sight or block too far!");
						return true;
					}
					Material mat = b.getType();
					Debug.log(mat.toString());
					if (!SyncDoorsConfig.validRedstone.contains(mat)) {
						sender.sendMessage(CC.R + "That does not appear to be a redstone block!");
						return true;
					}
					if (Config.redstone.contains(mat))
						Config.redstone.remove(mat);
					else
						Config.redstone.add(mat);
					sender.sendMessage(Utils.materialName(mat) + " is now " + Utils.strEnabled(Config.redstone.contains(mat)) + ".");
					return true;
				}
				sender.sendMessage("Unknown option " + args[1] + ". Type /syncdoors help for usage.");
				return true;
			}
			Config.enabled = !Config.enabled;
			Config.save();
			listener.register();
			sender.sendMessage("SyncDoors is now " + Utils.strEnabled(Config.enabled) + ".");
			return true;
		} else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r")) {
			if (!Utils.enforcePerm(sender, "syncdoors.reload"))
				return true;
			Config.load();
			listener.register();
			sender.sendMessage("SyncDoors configuration reloaded.");
			
			if (sender.hasPermission("syncdoors.check"))
				plugin.getServer().dispatchCommand(sender, "syncdoors c");
			return true;
		} else if (args[0].equals("debug") && sender.hasPermission("syncdoors.reload")) {
			Debug.setEnabled(args.length > 1 && (args[1].equals("on") || args[1].equals("true") || args[1].equals("1")));
			sender.sendMessage("Debugging output is " + Utils.strEnabled(Debug.isEnabled()));
			return true;
		} else {
			sendHelpText(sender);
			return true;
		}
	}
	
	private void sendHelpText(CommandSender sender) {
		String temp = "";
		boolean c = sender.hasPermission("syncdoors.check"),
				t = sender.hasPermission("syncdoors.toggle"),
				r = sender.hasPermission("syncdoors.reload"),
				f = true;
		final String SEP = ChatColor.GRAY + "|", A = ChatColor.AQUA.toString(), G = ChatColor.GREEN.toString(),
				Y = ChatColor.YELLOW.toString(), R = ChatColor.RED.toString(), SA = SEP + A, SG = SEP + G;
		
		if (c || t || r) {
			sender.sendMessage(R + "SyncDoors Usage:");
			
			// build usage line
			temp += R + "  /syncdoors " + A + "<";
			if (c)
				f = "" == (temp += "c");
			if (t)
				f = "" == (temp += (f ? "" : SA) + "t" + G + " [r" + SG + "p" + SG + "i" + SG + "w" + SG + "c]");
			if (r)
				f = "" == (temp += (f ? "" : SA) + "r");
			temp += ">";
			sender.sendMessage(temp);
			
			if (c)
				sender.sendMessage(A + "    c" + SA + "check" + Y + ": Checks plugin configuration.");
			if (t) {
				sender.sendMessage(A + "    t" + SA + "toggle" + Y + ": Turn the entire plugin on or off.");
				sender.sendMessage(G + "      r" + SG + "redstone" + Y + ": Toggle redstone-opened doors.");
				sender.sendMessage(G + "      p" + SG + "player" + Y + ": Toggle player-opened doors.");
				sender.sendMessage(G + "      i" + SG + "iron" + Y + ": Toggle iron double doors.");
				sender.sendMessage(G + "      w" + SG + "wooden" + Y + ": Toggle wooden double doors.");
				sender.sendMessage(G + "      c" + SG + "component" + Y + ": Toggle the targeted component.");
			}
			if (r)
				sender.sendMessage(A + "    r" + SA + "reload" + Y + ": Reloads plugin configuration.");
		} else // no permissions? output version and whether plugin is enabled.
			sender.sendMessage("SyncDoors v" + plugin.getDescription().getVersion() + " is " + Utils.strEnabled(plugin.getConfig().getBoolean("global")) + ".");
	}
}
