package uk.co.ElmHoe.Bukkit;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import uk.co.ElmHoe.Bukkit.Utilities.StringUtility;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

public class AntiBow extends JavaPlugin implements Listener{
	File configFile;
	FileConfiguration config;
	private static ArrayList<ProtectedRegion> regions = new ArrayList<>();
	private static List<String> regionList;
	
	private void loadYamls(){
		try{
			this.config.load(this.configFile);
		}catch (Exception e){
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
  
	public void onEnable(){
		getWorldGuard();
		Bukkit.getLogger().info("----------- Attempting to enable Anti-Bow -----------");
		this.configFile = new File(getDataFolder(), "config.yml");
		try{
			firstRun();
			Bukkit.getLogger().info("First Run was initated without issue.");
		}catch (Exception e){
			e.printStackTrace();
		}
		
		this.config = new YamlConfiguration();
		  
		try{
			loadYamls();
			Bukkit.getLogger().info("Attempting to load YAML files.");
		}catch(Exception e){}
		
		try{
			Bukkit.getPluginManager().registerEvents(this, this);
			Bukkit.getLogger().info("Events were initated without issue. All looks good.");
		}catch(Exception e){}
		  
		  
		Bukkit.getLogger().info("This version of AntiBow was built against 1.12.");
		Bukkit.getLogger().info("For any lower or BETA Builds, please go to Github.com/ElmHoe/AntiBow");
		Bukkit.getLogger().info("----------- AntiBow Enabled - v0.3 Build MC-1.12 -----------");
		
		buildRegionsList();
	}
  
	public void onDisable()
	{
		Bukkit.getLogger().info("AntiBow Disabled");
	}
  
	public void saveRegion(){
		try {
			config.save(configFile);
		} catch (IOException e) {}
	}
  
	private void firstRun()
			throws Exception
	{
		if (!this.configFile.exists())
		{
			this.configFile.getParentFile().mkdirs();
			copy(getResource("config.yml"), this.configFile);
		}
	}

  
	private void copy(InputStream in, File file)
	{
		try
		{
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[63];
			int len;
			while ((len = in.read(buf)) > 0)
			{
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
  
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if (cmd.getName().equalsIgnoreCase("antibow")){
			Player p = (Player)sender;
			World ofPlayer = p.getWorld();
			String worldName = ofPlayer.getName();
			
			if (sender.hasPermission("antibow.add") || (sender.isOp() == true)){
				
				
				if (args.length == 0){
					
					
					sender.sendMessage(StringUtility.format("&7&m-----[&4Anti&7-&4Bow&7&m]-----"));
					sender.sendMessage(StringUtility.format("&6&o/antibow add <region>"));
					sender.sendMessage(StringUtility.format("&6&o/antibow add, without anything after the add, will add the current region you are within."));
					sender.sendMessage(StringUtility.format("&6&o/antibow remove <region>"));
					sender.sendMessage(StringUtility.format("&6&o/antibow remove, without anything after remove, will remove the current region you are within."));
					sender.sendMessage(StringUtility.format("&7&m-----[&4Anti&7-&4Bow&7&m]-----"));
				
				
				}else if (args.length == 1){
					if (args[0].equalsIgnoreCase("add")){
						for (ProtectedRegion set : WGBukkit.getRegionManager(ofPlayer).getApplicableRegions(p.getLocation())){
							regions.add(set);
						}
						if (regions.size() == 0){
							sender.sendMessage(StringUtility.format("&6&oYou're currently not in any region at all."));
								
							}else if (regions.size() == 1){
								for (ProtectedRegion r : WGBukkit.getRegionManager(ofPlayer).getApplicableRegions(p.getLocation())){
									if (config.getBoolean("Worlds." + worldName + ".Regions." + r.getId()) ==true){
										sender.sendMessage("The region: " + r.getId() + " is already blocking bows.");
									}else{
										config.set("Worlds." + worldName + ".Regions." + r.getId(), true);
										sender.sendMessage("The region is now blocking bows.");
									}
								}
							
						}else if (regions.size() >= 2){
							sender.sendMessage(StringUtility.format("&6&oYou're currently in multiple regions, please click on which region you'd like to add."));
							for (ProtectedRegion r : WGBukkit.getRegionManager(ofPlayer).getApplicableRegions(p.getLocation())){
								if (config.getBoolean("Worlds." + worldName + ".Regions." + r.getId())==false){
									TextComponent message = new TextComponent("Region: " + r.getId());
									message.setColor(ChatColor.YELLOW);
									message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/antibow add " + r.getId()));
									p.spigot().sendMessage(message);
								}
							}
						}
					
					}else if (args[0].equalsIgnoreCase("remove")){

						
						for (ProtectedRegion set : WGBukkit.getRegionManager(ofPlayer).getApplicableRegions(p.getLocation())){
							regions.add(set);
						}
						if (regions.size() == 0){
							sender.sendMessage(StringUtility.format("&6&oYou're currently not in any region at all."));
								
							}else if (regions.size() == 1){
								for (ProtectedRegion r : WGBukkit.getRegionManager(ofPlayer).getApplicableRegions(p.getLocation())){
									if (config.contains("Worlds." + worldName + ".Regions." + r.getId())){
										config.set("Worlds." + worldName + ".Regions." + r.getId(), false);
										sender.sendMessage(StringUtility.format("&6&oThe region: " + r.getId() + " has been removed."));
									}else{
										sender.sendMessage(StringUtility.format("&6&oThe region " + r.getId() + " isnt' blocking bows."));
									}
								}
							
						}else if (regions.size() >= 2){
							sender.sendMessage(StringUtility.format("&6&oYou're currently in multiple regions, please click on which region you'd like to remove."));
							for (ProtectedRegion r : WGBukkit.getRegionManager(ofPlayer).getApplicableRegions(p.getLocation())){
								if (config.getBoolean("Worlds." + worldName + ".Regions." + r.getId()) == true){
									TextComponent message = new TextComponent("Region: " + r.getId());
									message.setColor(ChatColor.YELLOW);
									message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/antibow remove " + r.getId()));
									p.spigot().sendMessage(message);
								}
							}
						}
					
					}else{
						sender.sendMessage(StringUtility.format("Invalid usage, please check usage by using /antibow"));
					}
				}else if (args.length == 2){
					if (args[0].equalsIgnoreCase("remove")){
						try{
							ProtectedRegion region = WGBukkit.getPlugin().getRegionManager(ofPlayer).getRegion(args[1]);
							config.set("Worlds." + worldName + ".Regions." + region.getId(), false);
							sender.sendMessage(StringUtility.format("&6&oRegion " + region.getId() + " is no longer being blocked"));
						}catch(Exception e){
							sender.sendMessage(StringUtility.format("&6&oThe region you specified wasn't found."));
						}
					}else if (args[0].equalsIgnoreCase("add")){
						try{
							ProtectedRegion region = WGBukkit.getPlugin().getRegionManager(ofPlayer).getRegion(args[1]);
							config.set("Worlds." + worldName + ".Regions." + region.getId(), true);
							sender.sendMessage(StringUtility.format("&6&oRegion " + region.getId() + " is now being blocked"));
						}catch(Exception e){
							sender.sendMessage(StringUtility.format("&6&oThe region you specified wasn't found."));
						}
					}
					  saveRegion();
				}
			}else{
				sender.sendMessage(StringUtility.format(config.getString("Messages.NoPermission")));
			}
			return true;
		}
		return false;	  
	}
  
  

	@EventHandler(priority=EventPriority.MONITOR)
	public void onBowFire(EntityShootBowEvent event){
		/*
		 * DEBUGGING
		 * ENSURE TO REMOVE
		 * 
		 */
		
		for (int i = 0; i < regionList.size(); i++){
			System.out.println(regionList.get(i));
		}

		
		
		
		if (event.getEntityType().equals(org.bukkit.entity.EntityType.PLAYER)){
			UUID PlayerID = event.getEntity().getUniqueId();
			Player parsePlayer;
			System.out.println(PlayerID);
			try{
				parsePlayer = Bukkit.getServer().getPlayer(PlayerID);
			  
				
				if (regions.size() == 0){
				  
				}else if (regions.size() == 1){
					parsePlayer.sendMessage(StringUtility.format(config.getString("Messages.NotAllowed").replaceAll("%REGION%", regions.get(0).getId())));
					event.setCancelled(true);
				}else if (regions.size() >= 2){
					parsePlayer.sendMessage(StringUtility.format(config.getString("Messages.NotAllowed").replaceAll("%REGION%", regions.get(0).getId())));
					event.setCancelled(true);
				}

			  
			}catch(Exception e){
				System.out.println("There has been an error, I'm automatically sending details to ElmHoe to get this resolved. Thank you.");
			}
		}
	}
  
	public boolean isPlayerInBlockedRegion(Player p, String regionName){	    
		return false;
  }
	
	public boolean buildRegionsList(){
		try{
			regionList = config.getStringList("Worlds.");
			for (int i = 0; i < regionList.size(); i++){
				System.out.println(regionList.get(i));
			}
			return true;
		}catch(Exception e){
			return false;
		}
	}
}
