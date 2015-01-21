package com.github.y120.bukkit.syncdoors;

import java.io.IOException;

import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsLite;

import com.github.y120.bukkit.Common;

import static com.github.y120.bukkit.syncdoors.SyncDoorsConfig.Config;

public class SyncDoors extends JavaPlugin
{	
	private SyncDoorsListener listener = null;
	private SyncDoorsCommand command = null;
	
	@Override
    public void onEnable() {
		Common.init(this);
		
		Config.init(this);
		Config.load();
		
		listener = new SyncDoorsListener(this);
		listener.register();
		
		command = new SyncDoorsCommand(this, listener);
		getCommand("syncdoors").setExecutor(command);
		
		try {
			new MetricsLite(this).start();
		} catch (IOException e) {
			getLogger().warning(e.getMessage());
		}
    }
	
	@Override
	public void onDisable() {
		Config.save();
	}
}