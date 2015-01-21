package com.github.y120.bukkit.syncdoors;

import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class SyncDoorsConfig {
	public static SyncDoorsConfig Config = new SyncDoorsConfig();
	private static final String configVersion = "1.0";

	public Map<String, Boolean> options = new HashMap<String, Boolean>();
	public boolean enabled;
	public String[] OPTIONS = {
			"redstone", "player", "wooden", "iron",
	};
	public Set<Material> redstone = new HashSet<Material>();
	
	public static final Set<Material> validRedstone;
	static {
		Set<Material> s = new HashSet<Material>();
		s.add(Material.DAYLIGHT_DETECTOR);
		s.add(Material.DIODE_BLOCK_OFF);
		s.add(Material.DIODE_BLOCK_ON);
		s.add(Material.GOLD_PLATE);
		s.add(Material.IRON_PLATE);
		s.add(Material.LEVER);
		s.add(Material.REDSTONE_BLOCK);
		s.add(Material.REDSTONE_COMPARATOR_OFF);
		s.add(Material.REDSTONE_COMPARATOR_ON);
		s.add(Material.REDSTONE_TORCH_OFF);
		s.add(Material.REDSTONE_TORCH_ON);
		s.add(Material.REDSTONE_WIRE);
		s.add(Material.STONE_BUTTON);
		s.add(Material.STONE_PLATE);
		s.add(Material.TRIPWIRE_HOOK);
		s.add(Material.WOOD_BUTTON);
		s.add(Material.WOOD_PLATE);
		validRedstone = Collections.unmodifiableSet(s);
	}
	/*public static final Set<Byte> transparent;
	static {
		Set<Byte> hs = new HashSet<Byte>();
		hs.add((byte) Material.TRIPWIRE.getId());
		transparent = Collections.unmodifiableSet(hs);
	}*/
	public static final HashSet<Byte> transparent;
	static {
		transparent = new HashSet<Byte>();
		transparent.add((byte) Material.AIR.getId());
		transparent.add((byte) Material.TRIPWIRE.getId());
		transparent.add((byte) Material.WOODEN_DOOR.getId());
		transparent.add((byte) Material.IRON_DOOR_BLOCK.getId());
	}
	
	private SyncDoors pl;
	private File cfgFile;
	private FileConfiguration cfg;
	
	private SyncDoorsConfig() {}
	
	public void init(SyncDoors plugin) {
		pl = plugin;
		pl.saveDefaultConfig();
		cfgFile = new File(pl.getDataFolder(), "config.yml");
	}
	
	public void load() {
		cfg = YamlConfiguration.loadConfiguration(cfgFile);
		
		enabled = cfg.getBoolean("global", true);
		
		options.clear();
		for (String opt : OPTIONS)
			options.put(opt, cfg.getBoolean("options." + opt, true));

		redstone.clear();
		List<String> redstoneComponents = cfg.getStringList("redstone-components");
		for (String s : redstoneComponents) {
			Material mat = Material.getMaterial(s);
			if (mat != null && validRedstone.contains(mat))
				redstone.add(mat);
			else
				pl.getLogger().warning("Invalid redstone component \"" + s + "\" in config");
		}
		
		if (redstoneComponents.isEmpty()) {
			pl.getLogger().info("There are no components checked.");
			pl.getLogger().info("Populating components list with all supported components...");
			pl.getLogger().info("(To disable redstone handling, toggle the redstone option off instead.)");
			redstone.addAll(validRedstone);
		}
	}
	
	public void save() {
		cfg.set("config-version", configVersion);
		
		cfg.set("global", enabled);
		
		cfg.set("options.redstone", options.get("redstone"));
		for (String opt : OPTIONS)
			cfg.set("options." + opt, options.get(opt));
		
		List<String> redstoneComponents = new ArrayList<String>();
		for (Material mat : redstone)
			redstoneComponents.add(mat.toString());
		Collections.sort(redstoneComponents, Collator.getInstance());
		cfg.set("redstone-components", redstoneComponents);
		
		try {
			cfg.save(cfgFile);
		} catch (IOException e) {
			pl.getLogger().warning("Could not save config...");
		}
	}
}
