package com.github.vsams14;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.vsams14.extras.bChunk;

public class Commands {

	private SunBurn sunburn;

	public Commands(SunBurn sunburn){
		this.sunburn = sunburn;
	}

	public boolean com(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(cmd.getName().equalsIgnoreCase("burn")){
			if(sender instanceof Player){
				Player sendee = (Player)sender;
				switch(args.length){

				case 0:
					if((sendee.hasPermission("sunburn.toggle"))||(sendee.isOp())){
						toggleAll();
						return true;
					}
					return false;

				case 1:
					Player target = Bukkit.getServer().getPlayer(args[0]);
					if(target==null){
						if(args[0].equalsIgnoreCase("player")){
							if((sendee.hasPermission("sunburn.toggle.player"))||(sendee.isOp())){
								togglePburn();
								return true;
							}
						}else if(args[0].equalsIgnoreCase("animal")){
							if((sendee.hasPermission("sunburn.toggle.mob"))||(sendee.isOp())){
								toggleMburn();
								return true;
							}
						}else if(args[0].equalsIgnoreCase("lock")){
							if((sendee.hasPermission("sunburn.time"))||(sendee.isOp())){
								lockWorld(sendee.getWorld());
								return true;
							}
						}else if(args[0].equalsIgnoreCase("armor")){
							if((sendee.hasPermission("sunburn.toggle.armor"))||(sendee.isOp())){
								toggleArmor();
								return true;
							}
						}else if(args[0].equalsIgnoreCase("world")){
							if((sendee.hasPermission("sunburn.toggle"))||(sendee.isOp())){
								toggleWburn(sendee.getWorld().getName());
								return true;
							}
						}
					}else{
						if((sendee.hasPermission("sunburn.toggle.player"))||(sendee.isOp())){
							togglePException(target, sendee.getName());
							return true;
						}
					}
					return false;

				case 2:
					target = Bukkit.getServer().getPlayer(args[0]);
					if(args[0].equalsIgnoreCase("world")){
						if((sendee.hasPermission("sunburn.toggle"))||(sendee.isOp())){
							toggleWburn(args[1]);
							return true;
						}
					}else if(args[0].equalsIgnoreCase("armor")){
						if((sendee.hasPermission("sunburn.toggle.armor"))||(sendee.isOp())){
							changeArmor(args[1]);
							return true;
						}
					}
				}
				return false;
			}else{
				switch(args.length){

				case 0:
					toggleAll();
					return true;

				case 1:
					Player target = Bukkit.getServer().getPlayer(args[0]);
					if(target==null){
						if(args[0].equalsIgnoreCase("player")){
							togglePburn();
							return true;
						}else if(args[0].equalsIgnoreCase("animal")){
							toggleMburn();
							return true;
						}else if(args[0].equalsIgnoreCase("armor")){
							toggleArmor();
							return true;
						}
					}else{
						togglePException(target, "CONSOLE");
						return true;
					}
					return false;

				case 2:
					target = Bukkit.getServer().getPlayer(args[0]);
					if(args[0].equalsIgnoreCase("world")){
						toggleWburn(args[1]);
						return true;
					}else if(args[0].equalsIgnoreCase("armor")){
						changeArmor(args[1]);
						return true;
					}else if(args[0].equalsIgnoreCase("lock")){
						World w = sunburn.getServer().getWorld(args[1]);
						lockWorld(w);
						return true;
					}
				}
			}
			return false;

		}else if(cmd.getName().equalsIgnoreCase("usmite")){
			if(sender instanceof Player){
				Player sendee = (Player)sender;
				if((sendee.hasPermission("sunburn.usmite"))||(sendee.isOp())){
					if(args.length==2){
						Player t2 = Bukkit.getServer().getPlayer(args[0]);
						if(t2==null){
							return false;
						}
						sunburn.burn.smite.put(t2, Integer.parseInt(args[1]));
						t2.sendMessage("[\u00A74Sunburn\u00A7f] You were smitten "+Integer.parseInt(args[1])+" times by "+sendee.getName());
						return true;
					}
				}
				return false;

			}else{
				if(args.length==2){
					Player t2 = Bukkit.getServer().getPlayer(args[0]);
					if(t2==null){
						return false;
					}
					sunburn.burn.smite.put(t2, Integer.parseInt(args[1]));
					t2.sendMessage("[\u00A74Sunburn\u00A7f] You were smitten "+Integer.parseInt(args[1])+" times by [CONSOLE]");
					return true;
				}
				return false;
			}
			
		}else if(cmd.getName().equalsIgnoreCase("waste")){
			if(sender instanceof Player){
				Player sendee = (Player)sender;
				if(sendee.hasPermission("sunburn.waste")||sendee.isOp()){
					switch(args.length){
					
					case 0:
						toggleAWaste();
						return true;
						
					case 1:
						if(args[0].equalsIgnoreCase("chunk")){
							burnChunkAtPlayer(sendee);
							return true;
						}else if(args[0].equalsIgnoreCase("world")){
							toggleWwaste(sendee.getWorld().getName());
							return true;
						}else if(args[0].equalsIgnoreCase("notify")){
							toggleNotify();
							return true;
						}
						return false;
						
					case 2:
						if(args[0].equalsIgnoreCase("world")){
							toggleWwaste(args[1]);
							return true;
						}
					}
				}
				return false;
			}else{
				switch(args.length){
				
				case 0:
					toggleAWaste();
					return true;
					
				case 1:
					if(args[0].equalsIgnoreCase("notify")){
						toggleNotify();
						return true;
					}
					return false;
					
				case 2:
					if(args[0].equalsIgnoreCase("world")){
						toggleWwaste(args[1]);
						return true;
					}
				}
			}
		}
		return false;
	}

	public void toggleAll(){
		if(sunburn.config.disabled){
			sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] Enabled");
			sunburn.config.loadConf();
			sunburn.config.disabled = false;
		}else{
			sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] Disabled");
			sunburn.config.bPlayer = false;
			sunburn.config.bAnimal = false;
			sunburn.config.autoburn = false;
			sunburn.config.disabled = true;
		}
	}

	public void togglePburn(){
		if(sunburn.config.bPlayer){
			sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] PlayerBurn Disabled");
			sunburn.config.bPlayer = false;
		}else{
			sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] PlayerBurn Enabled");
			sunburn.config.bPlayer = true;
		}
	}

	public void toggleMburn(){
		if(sunburn.config.bAnimal){
			sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] MobBurn Disabled");
			sunburn.config.bAnimal = false;
		}else{
			sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] MobBurn Enabled");
			sunburn.config.bAnimal = true;
		}
	}

	public void lockWorld(World w){
		if(sunburn.config.wMap.containsKey(w.getName())){
			int x = sunburn.config.wMap.get(w.getName());
			if(sunburn.config.wtime[x].locked){
				sunburn.config.wtime[x].locked = false;
				sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] World "+sunburn.config.wtime[x].name+" unlocked!");
			}else{
				sunburn.config.wtime[x].locked = true;
				sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] World "+sunburn.config.wtime[x].name+" locked!");
				sunburn.config.wtime[x].locktime = w.getTime();
				sunburn.config.wtime[x].wdur = w.hasStorm();
			}
			File p = new File(sunburn.getDataFolder(), sunburn.config.wtime[x].name+".yml");
			sunburn.config.loadcConf(p, sunburn.getResource("World.yml"));
			sunburn.config.worldConfig.set("locktime", sunburn.config.wtime[x].locktime);
			sunburn.config.worldConfig.set("wdur", sunburn.config.wtime[x].wdur);
			sunburn.config.worldConfig.set("locked", sunburn.config.wtime[x].locked);
			sunburn.config.saveConf(sunburn.config.worldConfig, p);
		}
	}

	public void toggleArmor(){
		if(sunburn.config.armor){
			sunburn.config.armor = false;
			sunburn.config.conf.set("Armor_On", false);
			sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] Armor Disabled");
			File p = new File(sunburn.getDataFolder(), "config.yml");
			sunburn.config.saveConf(sunburn.config.conf, p);
		}else{
			sunburn.config.armor = true;
			sunburn.config.conf.set("Armor_On", true);
			sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] Armor Enabled");
			File p = new File(sunburn.getDataFolder(), "config.yml");
			sunburn.config.saveConf(sunburn.config.conf, p);
		}
	}

	public void toggleWburn(String name){
		if(sunburn.config.worlds.contains(name)){
			sunburn.config.worlds.remove(name);
			sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] World "+name+" is no longer being burned.");
		}else{
			sunburn.config.worlds.add(name);
			sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] World "+name+" is now being burned.");
		}
		sunburn.config.conf.set("worlds", sunburn.config.worlds);
		File p = new File(sunburn.getDataFolder(), "config.yml");
		sunburn.config.saveConf(sunburn.config.conf, p);
		sunburn.config.loadConf();
	}

	public void toggleAWaste(){
		if(sunburn.config.autoburn){
			sunburn.config.autoburn = false;
			sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] Automatic Wasteland Generation turned off!");
		}else{
			sunburn.config.autoburn = true;
			sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] Automatic Wasteland Generation turned on!");
		}
		sunburn.config.conf.set("auto_waste", sunburn.config.autoburn);
		File p = new File(sunburn.getDataFolder(), "config.yml");
		sunburn.config.saveConf(sunburn.config.conf, p);
		sunburn.config.loadConf();
	}

	public void togglePException(Player target, String s){
		String name = target.getName();
		if(sunburn.config.pwl.contains(name)){
			sunburn.config.pwl.remove(name);
			target.sendMessage("[\u00A74Sunburn\u00A7f] Removed from exception list by "+s);
		}else{
			sunburn.config.pwl.add(name);
			target.sendMessage("[\u00A74Sunburn\u00A7f] Added to exception list by "+s);
		}
		sunburn.config.conf.set("exclude_players", sunburn.config.pwl);
		File p = new File(sunburn.getDataFolder(), "config.yml");
		sunburn.config.saveConf(sunburn.config.conf, p);
		sunburn.config.loadConf();
	}

	public void changeArmor(String s){
		if(sunburn.util.isInteger(s)){
			sunburn.util.durability = Integer.parseInt(s);
			sunburn.config.conf.set("Durability", sunburn.util.durability);
			sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] Durability set to: "+sunburn.util.durability);
			File p = new File(sunburn.getDataFolder(), "config.yml");
			sunburn.config.saveConf(sunburn.config.conf, p);
			sunburn.config.loadConf();
		}else{
			sunburn.config.conf.set("Armor_Type", s);
			sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] Armor set to: "+s.toUpperCase());
			File p = new File(sunburn.getDataFolder(), "config.yml");
			sunburn.config.saveConf(sunburn.config.conf, p);
			sunburn.config.loadConf();
		}
	}

	public void toggleWwaste(String name){
		if(sunburn.config.wasteworlds.contains(name)){
			sunburn.config.wasteworlds.remove(name);
			sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] World "+name+" can no longer generate wasteland chunks!");
		}else{
			sunburn.config.wasteworlds.add(name);
			sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] World "+name+" can now generate wasteland chunks!");
		}
		sunburn.config.conf.set("wasteland_worlds", sunburn.config.wasteworlds);
		File p = new File(sunburn.getDataFolder(), "config.yml");
		sunburn.config.saveConf(sunburn.config.conf, p);
		sunburn.config.loadConf();
	}

	public void burnChunkAtPlayer(Player p){
		Location loc = p.getLocation();
		Chunk c = loc.getChunk();
		sunburn.util.burnChunk(c);
		int id = sunburn.util.getWorldID(p.getWorld().getName());
		int quad = sunburn.util.getQuadrant(c.getX(), c.getZ());
		int x = sunburn.util.getXQ(c.getX(), quad);
		int z = sunburn.util.getZQ(c.getZ(), quad);
		bChunk temp = sunburn.util.wC[id][quad][x][z];
		temp.activated = true;
		temp.burnt = true;
		temp.quad = quad;
		temp.world = p.getWorld().getName();
		temp.x = c.getX();
		temp.z = c.getZ();
		temp.x2 = x;
		temp.z2 = z;
		sunburn.util.wC[id][quad][x][z] = temp;
		if(sunburn.config.notify){
			sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] Burned Chunk at ("+c.getX()+", "+c.getZ()+") in world: "+p.getWorld().getName());
		}
	}

	public void toggleNotify(){
		if(sunburn.config.notify){
			sunburn.config.notify = false;
			sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] Will stop notifying about wasteland generation!");
		}else{
			sunburn.config.notify = true;
			sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] Will notify about wasteland generation!");
		}
		sunburn.config.conf.set("notify_waste", sunburn.config.notify);
		File p = new File(sunburn.getDataFolder(), "config.yml");
		sunburn.config.saveConf(sunburn.config.conf, p);
		sunburn.config.loadConf();
	}
}
