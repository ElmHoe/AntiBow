package uk.co.ElmHoe.Bukkit;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import uk.co.ElmHoe.Bukkit.Utilities.HTTPUtility;
import uk.co.ElmHoe.Bukkit.Utilities.StringUtility;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public class AntiBow extends JavaPlugin implements Listener {
	File configFile;
	FileConfiguration config;
	private final static String version = "1.2 Beta";
	private static ArrayList<ProtectedRegion> regions = new ArrayList<>();
	private static HashMap<String, Boolean> regionList;
	private static String blocked_region;
	private static String region;
	private static Boolean OPsToBypass;
	
	private void loadYamls() {
		try {
			this.config.load(this.configFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private WorldGuardPlugin getWorldGuard() {
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

		// WorldGuard may not be loaded
		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
			return null; // Maybe you want throw an exception instead
		}

		return (WorldGuardPlugin) plugin;
	}

	public void onEnable() {
		getWorldGuard();
		Bukkit.getLogger().info("----------- Attempting to enable Anti-Bow -----------");
		this.configFile = new File(getDataFolder(), "config.yml");
		try {
			firstRun();
			Bukkit.getLogger().info("First Run was initated without issue.");
		} catch (Exception e) {

		}

		this.config = new YamlConfiguration();

		try {
			loadYamls();
			Bukkit.getLogger().info("Attempting to load YAML files.");
		} catch (Exception e) {
		} // sendLogs("Error #1 whilst loading YAML Files" + "<br>" + e.getMessage());}

		try {
			Bukkit.getPluginManager().registerEvents(this, this);
			Bukkit.getLogger().info("Events were initated without issue. All looks good.");
		} catch (Exception e) {
		} // sendLogs("Error #2 on Registering Events." + "<br>" + e.getMessage());}

		Bukkit.getLogger().info("This version of AntiBow was built against & for 1.12.");
		Bukkit.getLogger().info("");
		Bukkit.getLogger().info("Send me improvements, bugs or anything else to https://github.com/ElmHoe/AntiBow");
		buildRegionsList();
		Bukkit.getLogger().info("----------- AntiBow Enabled - v " + version + " Build MC-1.12 -----------");
	}

	public void onDisable() {
		Bukkit.getLogger().info("AntiBow Disabled");
	}

	public void saveRegion() {
		try {
			config.save(configFile);
			Bukkit.getLogger().info("Configuration has been saved.");
		} catch (IOException e) {
		} // sendLogs("Error #3 on SaveRegion()" + "<br>" + e.getMessage());}
	}

	private void firstRun() throws Exception {
		if (!this.configFile.exists()) {
			this.configFile.getParentFile().mkdirs();
			copy(getResource("config.yml"), this.configFile);
		}
	}

	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[63];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("antibow")) {
			Player p = (Player) sender;
			World ofPlayer = p.getWorld();
			String worldName = ofPlayer.getName();

			if (sender.hasPermission("antibow.add") || (sender.isOp() == true)) {

				if (args.length == 0) {

					sender.sendMessage(StringUtility.format("&7&m-----[&4Anti&7-&4Bow&7&m]-----"));
					sender.sendMessage(StringUtility.format("&6'/antibow add <region>' &7&owill add a user-defined region."));
					sender.sendMessage(StringUtility.format("&6'/antibow add' &7&owill add the current region you are within."));
					sender.sendMessage(StringUtility.format("&6'/antibow remove <region>' &7&owill remove a user-defined region"));
					sender.sendMessage(StringUtility.format("&6'/antibow remove' &7&owill remove the current region you are within."));
					sender.sendMessage(StringUtility.format("&6'/antibow msg <message>' &7&owill change the default message when a player attempts to shoot."));
					sender.sendMessage(StringUtility.format("&6'/antibow msg-reset' &7&owill set the config message back to the default."));
					sender.sendMessage(StringUtility.format("&6'/antibow reload' &7&owill reload the configuration file."));
					sender.sendMessage(StringUtility.format("&7&m-----[&4Anti&7-&4Bow&7&m]-----"));

				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("add")) {
						regions.clear();
						for (ProtectedRegion set : WGBukkit.getRegionManager(ofPlayer)
								.getApplicableRegions(p.getLocation())) {
							regions.add(set);
						}
						if (regions.size() == 0) {
							sender.sendMessage(StringUtility.format("&6&oYou're currently not in any region at all."));
						} else if (regions.size() == 1) {
							for (ProtectedRegion r : WGBukkit.getRegionManager(ofPlayer)
									.getApplicableRegions(p.getLocation())) {
								if (config.getBoolean("Worlds." + worldName + ".Regions." + r.getId()) == true) {
									sender.sendMessage("The region: " + r.getId() + " is already blocking bows.");
								} else {
									config.set("Worlds." + worldName + ".Regions." + r.getId(), true);
									sender.sendMessage(StringUtility
											.format("&6&oThe region: " + r.getId() + " is now blocking bows."));
									regionList.put("Worlds." + worldName + ".Regions." + r.getId(), true);
									saveRegion();
								}
							}

						} else if (regions.size() >= 2) {
							sender.sendMessage(StringUtility.format(
									"&6&oYou're currently in multiple regions, please click on which region you'd like to add."));
							for (ProtectedRegion r : WGBukkit.getRegionManager(ofPlayer)
									.getApplicableRegions(p.getLocation())) {
								if (config.getBoolean("Worlds." + worldName + ".Regions." + r.getId()) == false) {
									TextComponent message = new TextComponent("Region: " + r.getId());
									message.setColor(ChatColor.YELLOW);
									message.setClickEvent(
											new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/antibow add " + r.getId()));
									p.spigot().sendMessage(message);
								}
							}
						}
					} else if (args[0].equalsIgnoreCase("msg-reset")) {
						config.set("DefaultMessages.NotAllowed",
								"&7[&4Anti&7-&4Bow&7] &6&oSorry %PLAYER%&6&o, but you're not allowed to use the bow in the region: %REGION%");
						blocked_region = "&7[&4Anti&7-&4Bow&7] &6&oSorry %PLAYER%&6&o, but you're not allowed to use the bow in the region: %REGION%";
						sender.sendMessage(StringUtility.format("&6Default message has been set."));
						saveRegion();
					} else if (args[0].equalsIgnoreCase("remove")) {
						regions.clear();
						for (ProtectedRegion set : WGBukkit.getRegionManager(ofPlayer)
								.getApplicableRegions(p.getLocation())) {
							regions.add(set);
						}
						if (regions.size() == 0) {
							sender.sendMessage(StringUtility.format("&6&oYou're currently not in any region at all."));

						} else if (regions.size() == 1) {
							for (ProtectedRegion r : WGBukkit.getRegionManager(ofPlayer)
									.getApplicableRegions(p.getLocation())) {
								if (config.contains("Worlds." + worldName + ".Regions." + r.getId())) {
									config.set("Worlds." + worldName + ".Regions." + r.getId(), false);
									sender.sendMessage(StringUtility
											.format("&6&oThe region: " + r.getId() + " has been removed."));
									regionList.remove("Worlds." + worldName + ".Regions." + r.getId(), true);
									saveRegion();
								} else {
									sender.sendMessage(StringUtility
											.format("&6&oThe region " + r.getId() + " isnt' blocking bows."));
								}
							}

						} else if (regions.size() >= 2) {
							sender.sendMessage(StringUtility.format(
									"&6&oYou're currently in multiple regions, please click on which region you'd like to remove."));
							for (ProtectedRegion r : WGBukkit.getRegionManager(ofPlayer)
									.getApplicableRegions(p.getLocation())) {
								if (config.getBoolean("Worlds." + worldName + ".Regions." + r.getId()) == true) {
									TextComponent message = new TextComponent("Region: " + r.getId());
									message.setColor(ChatColor.YELLOW);
									message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
											"/antibow remove " + r.getId()));
									p.spigot().sendMessage(message);
								}
							}
						}

					} else if (args[0].equalsIgnoreCase("reload")){
						/*
						 * Reloads the configuration from file.
						 */
						loadYamls();
						buildRegionsList();
						sender.sendMessage(StringUtility.format("&7[&4Anti&7-&4Bow&7] &6&oHey, I've reloaded the configuration for you."));
					} else {
						sender.sendMessage(StringUtility.format("Invalid usage, please check usage by using /antibow"));
					}
				} else if (args.length == 2) {
					if (args[0].equalsIgnoreCase("remove")) {
						try {
							ProtectedRegion region = WGBukkit.getPlugin().getRegionManager(ofPlayer).getRegion(args[1]);
							config.set("Worlds." + worldName + ".Regions." + region.getId(), false);
							sender.sendMessage(StringUtility
									.format("&6&oRegion " + region.getId() + " is no longer being blocked"));
							regionList.remove("Worlds." + worldName + ".Regions." + region.getId(), true);
							saveRegion();
						} catch (Exception e) {
							e.printStackTrace();
							// sendLogs("Error #4 on /ab remove"+ "<br>" + "Usage: " + cmd.getName() + " "
							// +args[0] + " " +args[1]);
							sender.sendMessage(StringUtility.format(
									"&6&oThe region you specified wasn't found, please ensure you're in the same world as the region."));
						}
					} else if (args[0].equalsIgnoreCase("add")) {
						try {
							ProtectedRegion region = WGBukkit.getPlugin().getRegionManager(ofPlayer).getRegion(args[1]);
							config.set("Worlds." + worldName + ".Regions." + region.getId(), true);
							sender.sendMessage(
									StringUtility.format("&6&oRegion " + region.getId() + " is now being blocked"));
							regionList.put("Worlds." + worldName + ".Regions." + region.getId(), true);
							saveRegion();
						} catch (Exception e) {
							e.printStackTrace();
							// sendLogs("Error #5 on /ab add "+ "<br>" + "Usage: " + cmd.getName() + " "
							// +args[0] + " " +args[1]);
							sender.sendMessage(StringUtility.format(
									"&6&oThe region you specified wasn't found, please ensure you're in the same world as the region."));
						}
					}
				}if (args.length >= 2) {
					if (args[0].equalsIgnoreCase("msg")) {
						String newMsg = "";
						for (int i = 1; i < args.length; i++) {
							newMsg = newMsg + args[i] + " ";
						}

					sender.sendMessage(newMsg);
					config.set("DefaultMessages.NotAllowed", newMsg);
					blocked_region = newMsg;
					sender.sendMessage(StringUtility.format("&6&oSuccess, message has now been set to: " + newMsg));
					
					saveRegion();
					}

				}
			} else {
				try{
					sender.sendMessage(StringUtility.format(config.getString("DefaultMessages.NoPermission")));
				}catch(Exception e) {
					config.set("DefaultMessages.NoPermission", "&7[&4Anti&7-&4Bow&7] &6&oNo Permission. OP/Antibow.add required.");
					sender.sendMessage(StringUtility.format("&7[&4Anti&7-&4Bow&7] &6&oNo Permission. OP/Antibow.add required."));
					saveRegion();
				}
			}
			return true;
		}
		return false;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBowFire(EntityShootBowEvent event) {

		if (event.getEntityType().equals(org.bukkit.entity.EntityType.PLAYER)) {
			try {
				Player parsePlayer = Bukkit.getServer().getPlayer(event.getEntity().getName().toString());
				if (!(parsePlayer.isOp()) || (!(parsePlayer.hasPermission("antbow.bypass")))) {
					if (isPlayerInBlockedRegion(parsePlayer)) {
						event.setCancelled(true);
						String formattedMsg = blocked_region.replaceAll("%REGION%", region.toString())
								.replaceAll("%PLAYER%", parsePlayer.getDisplayName());
						parsePlayer.sendMessage(StringUtility.format(formattedMsg));
					}
				}else if ((parsePlayer.isOp() || parsePlayer.hasPermission("antibow.bypass")) && (OPsToBypass == false)) {
					if (isPlayerInBlockedRegion(parsePlayer)) {
						event.setCancelled(true);
						String formattedMsg = blocked_region.replaceAll("%REGION%", region.toString())
								.replaceAll("%PLAYER%", parsePlayer.getDisplayName());
						parsePlayer.sendMessage(StringUtility.format(formattedMsg));
					}
				}
			} catch (Exception e) {
				Bukkit.getLogger().warning("Unknown error...");
				e.printStackTrace();
				// sendLogs("Error #6 (EntityShootBowEvent)" + "\n" + e.getMessage());
			}
		}
	}

	public boolean isPlayerInBlockedRegion(Player p) {
		ApplicableRegionSet playerRegions = WGBukkit.getRegionManager(p.getWorld())
				.getApplicableRegions(p.getLocation());
		String mapQuery = "Worlds." + p.getWorld().getName() + ".Regions.";
		for (ProtectedRegion reg : playerRegions.getRegions()) {
			region = reg.getId();
			Boolean value = regionList.get(mapQuery + reg.getId());
			if (value) {
				return true;
			}
		}
		return false;
	}

	public void buildRegionsList() {
		/*
		 * Used for building the region list.
		 * 
		 * It'll load all date from the config, and store it in an arraylist
		 */
		regionList = new HashMap<String, Boolean>();
		List<World> worlds = Bukkit.getWorlds();
		int saveConfigOrNah = 0;
		for (int i = 0; i < worlds.size(); i++) {
			Map<String, ProtectedRegion> Regions = WGBukkit.getRegionManager(worlds.get(i)).getRegions();
			String worldName = worlds.get(i).getName();

			for (String key : Regions.keySet()) {
				if (!(config.contains("Worlds." + worldName + ".Regions." + Regions.get(key).getId()))) {
					config.set("Worlds." + worldName + ".Regions." + Regions.get(key).getId(), false);
					Bukkit.getLogger().warning("Wrote: '" + "Worlds." + worldName + ".Regions."
							+ Regions.get(key).getId() + "' to config.");
					saveConfigOrNah = 1;
					regionList.put("Worlds." + worldName + ".Regions." + Regions.get(key).getId(),
							config.getBoolean("Worlds." + worldName + ".Regions." + Regions.get(key).getId()));
				} else {
					regionList.put("Worlds." + worldName + ".Regions." + Regions.get(key).getId(),
							config.getBoolean("Worlds." + worldName + ".Regions." + Regions.get(key).getId()));
				}
			}
		}
		try {
			if (!(config.contains("OPsToBypass"))) {
				config.set("OPsToBypass", false);
				Bukkit.getLogger().warning("Invalid or no option received for OPsToBypass. Wrote as false to config.");
				OPsToBypass = false;
				saveConfigOrNah = 1;
			}else {
				OPsToBypass = config.getBoolean("OPsToBypass");
			}
		}catch (Exception e) {
			saveConfigOrNah = 1;
			config.set("OPsToBypass", false);
			OPsToBypass = false;
			Bukkit.getLogger().warning("Invalid or no option received for OPsToBypass. Wrote as false to config.");
		}
		
		if (!(config.contains("DefaultMessages.NotAllowed"))) {
			config.set("DefaultDefaultMessages.NotAllowed",
					"&7[&4Anti&7-&4Bow&7] &6&oSorry %PLAYER%&6&o, but you're not allowed to use the bow in the region: %REGION%");
			blocked_region = "&7[&4Anti&7-&4Bow&7] &6&oSorry %PLAYER%&6&o, but you're not allowed to use the bow in the region: %REGION%";
			saveConfigOrNah = 1;
		}else {
			blocked_region = config.getString("DefaultMessages.NotAllowed");
		}
		if (saveConfigOrNah == 1) {
			saveRegion();
		}
	}

	/*
	 * public void sendLogs(String error){
	 * 
	 * Used for sending errors to myself to investigate.
	 * 
	 * 
	 * if (config.contains("AutomaticallySendLogs")){ if
	 * (config.getBoolean("AutomaticallySendLogs") == false){ }else{ DateFormat
	 * dateFormat = new SimpleDateFormat("[dd/MM/yyyy - HH:mm:ss]"); Date date = new
	 * Date();
	 * 
	 * try{ HTTPUtility.sendPost("Time it went wrong: " + dateFormat.format(date) +
	 * defaultMSG + error + "<br>"+"------------END OF LOG------------");
	 * }catch(Exception e1){ } } }else{ config.set("AutomaticallySendLogs", true); }
	 * }
	 */
}
