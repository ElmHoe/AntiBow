package uk.co.ElmHoe.Bukkit;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import uk.co.ElmHoe.Bukkit.Utilities.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiBow extends JavaPlugin implements Listener {
	private final static String version = "1.3.0";
	private static ArrayList<ProtectedRegion> regions = new ArrayList<>();
	private static HashMap<String, Boolean> regionList;
	private static String blocked_region;
	private static String region;
	private static Boolean OPsToBypass;
	
	public AntiBow get() {
		return this;
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
		Bukkit.getPluginManager().registerEvents(this, this);
		try {
			Bukkit.getLogger().info("Checking for an update...");
			updateChecking();
		} catch (Exception e) {Bukkit.getLogger().warning("Failed to check for an update.");}
		try {
			ConfigUtility.get();
			ConfigUtility.firstRun(get());
		} catch (Exception e) {e.printStackTrace();}

		Bukkit.getLogger().info("Send me improvements, bugs or anything else to https://github.com/ElmHoe/AntiBow");
		buildRegionsList();
		Bukkit.getLogger().info("----------- AntiBow Enabled - v " + version + " Build MC-1.12 -----------");
	}

		
	public void onDisable() {
		Bukkit.getLogger().info("AntiBow Disabled");
	}

	public void saveRegion() {
		Bukkit.getLogger().info(ConfigUtility.saveConfiguration());
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("antibow")) {
			if (!(sender instanceof Player)){
				switch(args.length){
				case 0:
					
					
					sender.sendMessage(StringUtility.format("&7&m-----[&4Anti&7-&4Bow&7&m]-----"));
					sender.sendMessage(StringUtility.format("&6Unfortunately, some commands are in-game only. Access from console is limited for now."));
					sender.sendMessage(StringUtility.format("&6'/antibow msg <message>' &7&owill change the default message when a player attempts to shoot."));
					sender.sendMessage(StringUtility.format("&6'/antibow msg-reset' &7&owill set the config message back to the default."));
					sender.sendMessage(StringUtility.format("&6'/antibow reload' &7&owill reload the configuration file."));
					sender.sendMessage(StringUtility.format("&7&m-----[&4Anti&7-&4Bow&7&m]-----"));
					break;
					
					
				case 1:
					
					
					if (args[0].equalsIgnoreCase("msg-reset")) {
						ConfigUtility.populateConfig("DefaultMessages.NotAllowed", 
								"&7[&4Anti&7-&4Bow&7] &6&oSorry %PLAYER%&6&o, but you're not allowed to use the bow in the region: %REGION%", "string");
						blocked_region = "&7[&4Anti&7-&4Bow&7] &6&oSorry %PLAYER%&6&o, but you're not allowed to use the bow in the region: %REGION%";
						sender.sendMessage(StringUtility.format("&6Default message has been set."));
						saveRegion();
					}else if (args[0].equalsIgnoreCase("reload")){
						/*
						 * Reloads the configuration from file.
						 */
						ConfigUtility.loadYamls();
						buildRegionsList();
						sender.sendMessage(StringUtility.format("&7[&4Anti&7-&4Bow&7] &6&oI've reloaded the configuration for you."));
					} else {
						sender.sendMessage(StringUtility.format("Invalid usage, please check usage by using /antibow"));
					}
					break;
					
					
				default:
					if (args[0].equalsIgnoreCase("msg")) {
						String newMsg = "";
						for (int i = 1; i < args.length; i++) {
							newMsg = newMsg + args[i] + " ";
						}

					ConfigUtility.populateConfig("DefaultMessages.NotAllowed", newMsg, "String");
					blocked_region = newMsg;
					sender.sendMessage(StringUtility.format("&6&oSuccess, message has now been set to: " + newMsg));
					
					saveRegion();
					}else{
						sender.sendMessage(StringUtility.format("&7&m-----[&4Anti&7-&4Bow&7&m]-----"));
						sender.sendMessage(StringUtility.format("Invalid usage, please check usage by using /antibow"));
						sender.sendMessage(StringUtility.format("&7&m-----[&4Anti&7-&4Bow&7&m]-----"));
					}
					break;
					
				}
			}else{
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
									if (ConfigUtility.configReadBoolean("Worlds." + worldName + ".Regions." + r.getId()) == true) {
										sender.sendMessage("The region: " + r.getId() + " is already blocking bows.");
									} else {
										ConfigUtility.populateConfig("Worlds." + worldName + ".Regions." + r.getId(), "true", "boolean");
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
									if (ConfigUtility.configReadBoolean("Worlds." + worldName + ".Regions." + r.getId()) == false) {
										TextComponent message = new TextComponent("Region: " + r.getId());
										message.setColor(ChatColor.YELLOW);
										message.setClickEvent(
												new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/antibow add " + r.getId()));
										p.spigot().sendMessage(message);
									}
								}
							}
						} else if (args[0].equalsIgnoreCase("msg-reset")) {
							
							ConfigUtility.populateConfig("DefaultMessages.NotAllowed",
									"&7[&4Anti&7-&4Bow&7] &6&oSorry %PLAYER%&6&o, but you're not allowed to use the bow in the region: %REGION%", "string");
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
									if (ConfigUtility.configContainsBoolean("Worlds." + worldName + ".Regions." + r.getId())) {
										ConfigUtility.populateConfig("Worlds." + worldName + ".Regions." + r.getId(), "false", "Boolean");
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
									if (ConfigUtility.configReadBoolean("Worlds." + worldName + ".Regions." + r.getId()) == true) {
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
							ConfigUtility.loadYamls();
							buildRegionsList();
							sender.sendMessage(StringUtility.format("&7[&4Anti&7-&4Bow&7] &6&oHey, I've reloaded the configuration for you."));
						} else {
							sender.sendMessage(StringUtility.format("Invalid usage, please check usage by using /antibow"));
						}
					} else if (args.length == 2) {
						if (args[0].equalsIgnoreCase("remove")) {
							try {
								ProtectedRegion region = WGBukkit.getPlugin().getRegionManager(ofPlayer).getRegion(args[1]);
								ConfigUtility.populateConfig("Worlds." + worldName + ".Regions." + region.getId(), "false", "Boolean");
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
								ConfigUtility.populateConfig("Worlds." + worldName + ".Regions." + region.getId(), "true", "boolean");
								sender.sendMessage(
										StringUtility.format("&6&oRegion " + region.getId() + " is now being blocked"));
								regionList.put("Worlds." + worldName + ".Regions." + region.getId(), true);
								saveRegion();
							} catch (Exception e) {
								// sendLogs("Error #5 on /ab add "+ "<br>" + "Usage: " + cmd.getName() + " "
								// +args[0] + " " +args[1]);
								sender.sendMessage(StringUtility.format(
										"&6&oThe region you specified wasn't found, please ensure you're in the same world as the region."));
							}
						}else if (args[0].equalsIgnoreCase("msg")) {
							String newMsg = "";
							for (int i = 1; i < args.length; i++) {
								newMsg = newMsg + args[i] + " ";
							}
	
							ConfigUtility.populateConfig("DefaultMessages.NotAllowed", newMsg, "String");
							blocked_region = newMsg;
							sender.sendMessage(StringUtility.format("&6&oSuccess, message has now been set to: " + newMsg));
						
							saveRegion();
						}
					}else if (args.length >= 3) {
						if (args[0].equalsIgnoreCase("msg")) {
							String newMsg = "";
							for (int i = 1; i < args.length; i++) {
								newMsg = newMsg + args[i] + " ";
							}
	
							ConfigUtility.populateConfig("DefaultMessages.NotAllowed", newMsg, "String");
							blocked_region = newMsg;
							sender.sendMessage(StringUtility.format("&6&oSuccess, message has now been set to: " + newMsg));
						
							saveRegion();
						}else {
							sender.sendMessage(StringUtility.format("&7&m-----[&4Anti&7-&4Bow&7&m]-----"));
							sender.sendMessage(StringUtility.format("Invalid usage, please check usage by using /antibow"));
							sender.sendMessage(StringUtility.format("&7&m-----[&4Anti&7-&4Bow&7&m]-----"));
						}
					}else {
					}
				} else {
					try{
						sender.sendMessage(StringUtility.format(ConfigUtility.configReadString("DefaultMessages.NoPermission")));
					}catch(Exception e) {
						ConfigUtility.populateConfig("DefaultMessages.NoPermission", "&7[&4Anti&7-&4Bow&7] &6&oNo Permission. OP/Antibow.add required.", "String");
						sender.sendMessage(StringUtility.format("&7[&4Anti&7-&4Bow&7] &6&oNo Permission. OP/Antibow.add required."));
						saveRegion();
					}
				}
				return true;
			}
			return true;
		}
		return false;
	}

	/**
	 * <h3  style="font-style: italic;">onBowFire() Documentation</h3>
	 * 
	 * @param event		When the EntityShootBowEvent is triggered.
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBowFire(EntityShootBowEvent event) {

		if (event.getEntityType().equals(org.bukkit.entity.EntityType.PLAYER)) {
			try {
				Player parsePlayer = Bukkit.getServer().getPlayer(event.getEntity().getName().toString());
				if (!(parsePlayer.isOp()) || (!(parsePlayer.hasPermission("antbow.bypass")))) {
					if (isPlayerInBlockedRegion(parsePlayer)) {
						event.setCancelled(true);
						parsePlayer.getInventory().removeItem(new ItemStack(Material.ARROW, 1));
						parsePlayer.getInventory().addItem(new ItemStack(Material.ARROW, 1));
						String formattedMsg = blocked_region.replaceAll("%REGION%", region.toString())
								.replaceAll("%PLAYER%", parsePlayer.getDisplayName());
						parsePlayer.sendMessage(StringUtility.format(formattedMsg));
					}
				}else if ((parsePlayer.isOp() || parsePlayer.hasPermission("antibow.bypass")) && (OPsToBypass == false)) {
					if (isPlayerInBlockedRegion(parsePlayer)) {
						event.setCancelled(true);
						parsePlayer.getInventory().removeItem(new ItemStack(Material.ARROW, 1));
						parsePlayer.getInventory().addItem(new ItemStack(Material.ARROW, 1));
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

	/** 
	 * <h3 style="font-style: italic;">isPlayerInBlockedRegion() Documentation. </h3>
	 * 
	 * This function will check if a player is in a blocked region or not.
	 * 
	 * @param	p		The player to check if they're in a blocked region.
	 * @return	Boolean	True/False. True if the player IS in a blocked region, false otherwise.
	 */
	public boolean isPlayerInBlockedRegion(Player p) {
		ApplicableRegionSet playerRegions = WGBukkit.getRegionManager(p.getWorld())
				.getApplicableRegions(p.getLocation());
		String mapQuery = "Worlds." + p.getWorld().getName() + ".Regions.";
		for (ProtectedRegion reg : playerRegions.getRegions()) {
			region = reg.getId();
			Boolean value = regionList.get(mapQuery + reg.getId());
			
			if (value) { return true;	}
		}
		return false;
	}

	/**
	 * <h3 style="font-style: italic;">buildRegionList() Documentation </h3>
	 * <a style=color:lightgreen;> 
	 * The tool used to pre-define the configuration file.
	 * <br>
	 * Rather than loading the configuration multiple times during use.
	 * Load the config as a whole when the plugin initiates and only write to it when in use.
	 * <br>
	 * This helps take the stress away from the server when in-use.
	 * </a>
	 *  
	 */
	public void buildRegionsList(){
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
				if (!(ConfigUtility.configContainsBoolean("Worlds." + worldName + ".Regions." + Regions.get(key).getId()))) {
					ConfigUtility.populateConfig("Worlds." + worldName + ".Regions." + Regions.get(key).getId(), "false", "boolean");
					Bukkit.getLogger().warning("Wrote: '" + "Worlds." + worldName + ".Regions."
							+ Regions.get(key).getId() + "' to config.");
					saveConfigOrNah = 1;
					regionList.put("Worlds." + worldName + ".Regions." + Regions.get(key).getId(),
							ConfigUtility.configReadBoolean("Worlds." + worldName + ".Regions." + Regions.get(key).getId()));
				} else {
					regionList.put("Worlds." + worldName + ".Regions." + Regions.get(key).getId(),
							ConfigUtility.configReadBoolean("Worlds." + worldName + ".Regions." + Regions.get(key).getId()));
				}
			}
		}	
		try {
			if (!(ConfigUtility.configContainsBoolean("OPsToBypass"))) {
				ConfigUtility.populateConfig("OPsToBypass", "false", "Boolean");
				//populateConfig("OPsToBypass", false);
				Bukkit.getLogger().warning("Invalid or no option received for OPsToBypass. Wrote as false to config.");
				OPsToBypass = false;
				saveConfigOrNah = 1;
			}else {
				OPsToBypass = ConfigUtility.configReadBoolean("OPsToBypass");
			}
		}catch (Exception e) {
			saveConfigOrNah = 1;
			ConfigUtility.populateConfig("OPsToBypass", "false", "Boolean");
			OPsToBypass = false;
			Bukkit.getLogger().warning("Invalid or no option received for OPsToBypass. Wrote as false to config.");
		}
		
		if (!(ConfigUtility.configContainsBoolean("DefaultMessages.NotAllowed"))) {
			ConfigUtility.populateConfig("DefaultMessages.NotAllowed", 
					"&7[&4Anti&7-&4Bow&7] &6&oSorry %PLAYER%&6&o, but you're not allowed to use the bow in the region: %REGION%", "string");
			blocked_region = "&7[&4Anti&7-&4Bow&7] &6&oSorry %PLAYER%&6&o, but you're not allowed to use the bow in the region: %REGION%";
			saveConfigOrNah = 1;
		}else {
			blocked_region = ConfigUtility.configReadString("DefaultMessages.NotAllowed");
		}
		if (saveConfigOrNah == 1) {
			saveRegion();
		}
	}

	/**
	 * <h3 style="font-style: italic;">updateChecking() Documentation</h3>
	 * On Enable - I will check for an update.
	 * <p>
	 * If there is an update, this won't yet download but will tell the user that there is an update.
	 * 
	 * 

	 * @author Joshua Fennell.
	 * @throws Exception 
	 * 
	 * 
	 */
	public void updateChecking() throws Exception {
		String spigotVersion = HTTPUtility.sendGet("https://api.spigotmc.org/legacy/update.php?resource=18925");
		if (spigotVersion.contains(version)) {
			Bukkit.getLogger().info("Your version of AntiBow is up-to-date. Current version: " + spigotVersion);
		}else {
			Bukkit.getLogger().warning("\nThis version of AntiBow is outdated, current version is: " + spigotVersion + "\n" + "You're running version: " + version + "\nPlease update via going to: https://rd.elmhoe.co.uk/AntiBow");
		}
	}
}