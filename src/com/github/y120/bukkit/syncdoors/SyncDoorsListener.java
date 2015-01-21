package com.github.y120.bukkit.syncdoors;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import static com.github.y120.bukkit.Common.*;
import static com.github.y120.bukkit.syncdoors.SyncDoorsConfig.Config;

public class SyncDoorsListener
{
	private SyncDoors plugin;
	SyncDoorsListener(SyncDoors instance) {
		plugin = instance;
	}
	
	public void register() {
		Debug.log("Unregistering listeners...");
		HandlerList.unregisterAll(plugin);
		
		if (!Config.enabled)
			return;
		if (Config.options.get("redstone")) {
			Debug.log("Registering redstone listener...");
			plugin.getServer().getPluginManager().registerEvents(new BlockPhysicsListener(), plugin);
		}
		if (Config.options.get("player")) {
			Debug.log("Registering player listener...");
			plugin.getServer().getPluginManager().registerEvents(new PlayerInteractListener(), plugin);
		}
	}
	
    private static final BlockFace[] FACES = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
    
    // a and b must be the lower blocks
    private boolean doorsAreConnected(Block a, Block b) {
    	if (a.getType() != b.getType() || a.getType() != Material.WOODEN_DOOR && a.getType() != Material.IRON_DOOR_BLOCK)
    		return false;
    	
    	if ((a.getRelative(BlockFace.UP).getData() & 0x1) == (b.getRelative(BlockFace.UP).getData() & 0x1))
    		return false;									// hinges must be different
    	return (a.getData() & 0x3) == (b.getData() & 0x3);	// sides must be the same
    }
    
    private class SyncDoorsTask extends BukkitRunnable {
    	Block door;
    	public SyncDoorsTask(Block b) {
    		door = b;
    	}
    	
		@Override
		public void run() {
	        Block other = null;
	        
	        if ((door.getData() & 0x8) == 0x8)
	        	door = door.getRelative(BlockFace.DOWN); // get the lower block if necessary 
	        
	        for (BlockFace bf : FACES) {
	        	Block b = door.getRelative(bf);
	        	if (doorsAreConnected(door, b))
	        		other = b;
	        }
	        if (other == null) 
	        	return;

	        if ((door.getData() != other.getData())) {
	        	Debug.log("Synchronising door @ " + door.getLocation().getX() + "," + door.getLocation().getY() + 
	        			"," + door.getLocation().getZ() + " with door @ " + other.getLocation().getX() + "," + 
	        			other.getLocation().getY() + "," + other.getLocation().getZ());
	            other.setData((byte)(other.getData() ^ 0x4)); // flip the open/closed bit
	            other.getState().update(true);
	        }
		}
    }
    
    private void doStuff(Block door) {
    	if (door.getType() == Material.WOODEN_DOOR     && Config.options.get("wooden") ||
    		door.getType() == Material.IRON_DOOR_BLOCK && Config.options.get("iron"))
    		new SyncDoorsTask(door).runTaskLater(plugin, 0);
    }
    
    private class PlayerInteractListener implements Listener {
	    @EventHandler(priority=EventPriority.HIGHEST)
	    public void onPlayerInteract(PlayerInteractEvent e) {
	        if (e.isCancelled())
	            return;
	        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
	        	return;
	        doStuff(e.getClickedBlock());
	    }
    }
    
    private class BlockPhysicsListener implements Listener {
	    @EventHandler(priority=EventPriority.HIGHEST)
	    public void onBlockPhysics(BlockPhysicsEvent e) {
	    	if (e.isCancelled())
	    		return;
	    	if (e.getBlock().getType() != Material.WOODEN_DOOR && e.getBlock().getType() != Material.IRON_DOOR_BLOCK)
	    		return;
	    	Debug.log(e.getChangedType().toString());
	    	/*for (Material mat : Config.redstone)
	    		if (mat == e.getChangedType()) {
	    			doStuff(e.getBlock());
	    			return;
	    		}*/
	    	if (Config.redstone.contains(e.getChangedType()))
	    		doStuff(e.getBlock());
	    }
    }
}