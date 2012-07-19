package com.github.vsams14;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
						if(sunburn.config.disabled){
							sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] Enabled");
							sunburn.config.bPlayer = true;
							sunburn.config.bAnimal = true;
							sunburn.config.disabled = false;
						}else{
							sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] Disabled");
							sunburn.config.bPlayer = false;
							sunburn.config.bAnimal = false;
							sunburn.config.disabled = true;
						}
						return true;
					}
					return false;

				case 1:
					Player target = Bukkit.getServer().getPlayer(args[0]);
					if(target==null){
						if(args[0].equalsIgnoreCase("player")){
							if((sendee.hasPermission("sunburn.toggle.player"))||(sendee.isOp())){
								if(sunburn.config.bPlayer){
									sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] PlayerBurn Disabled");
									sunburn.config.bPlayer = false;
								}else{
									sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] PlayerBurn Enabled");
									sunburn.config.bPlayer = true;
								}
								return true;
							}
							return false;

						}else if(args[0].equalsIgnoreCase("animal")){
							if((sendee.hasPermission("sunburn.toggle.mob"))||(sendee.isOp())){
								if(sunburn.config.bAnimal){
									sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] MobBurn Disabled");
									sunburn.config.bAnimal = false;
								}else{
									sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] MobBurn Enabled");
									sunburn.config.bAnimal = true;
								}
								return true;
							}
							return false;
						}else if(args[0].equalsIgnoreCase("lock")){
							if((sendee.hasPermission("sunburn.time"))||(sendee.isOp())){
								for(int x = 0; x < sunburn.config.wtime.length; x++){
									if(sunburn.config.wtime[x].name.equalsIgnoreCase(sendee.getWorld().getName())){
										if(sunburn.config.wtime[x].locked){
											sunburn.config.wtime[x].locked = false;
											sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] World "+sunburn.config.wtime[x].name+" unlocked!");
										}else{
											sunburn.config.wtime[x].locked = true;
											sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] World "+sunburn.config.wtime[x].name+" locked!");
											World world = sendee.getWorld();
											sunburn.config.wtime[x].locktime = world.getTime();
											sunburn.config.wtime[x].wdur = world.hasStorm();
										}
										File p = new File(sunburn.getDataFolder(), sunburn.config.wtime[x].name+".yml");
										sunburn.config.loadcConf(p, sunburn.getResource("World.yml"));
										sunburn.config.worldConfig.set("locktime", sunburn.config.wtime[x].locktime);
										sunburn.config.worldConfig.set("wdur", sunburn.config.wtime[x].wdur);
										sunburn.config.worldConfig.set("locked", sunburn.config.wtime[x].locked);
										sunburn.config.saveConf(sunburn.config.worldConfig, p);
										return true;
									}
								}
							}
							return false;
							
						}else if(args[0].equalsIgnoreCase("armor")){
							if((sendee.hasPermission("sunburn.toggle.armor"))||(sendee.isOp())){
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
								return true;
							}
							return false;
							
						}else if(args[0].equalsIgnoreCase("world")){
							if((sendee.hasPermission("sunburn.toggle"))||(sendee.isOp())){
								World w = sendee.getWorld();
								String name = w.getName();
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
								return true;
							}
							return false;
							
						}else if(args[0].equalsIgnoreCase("chunk")){
							if((sendee.hasPermission("sunburn.waste"))||(sendee.isOp())){
								sendee.sendMessage("[\u00A74Sunburn\u00A7f] Starting...");
								Location edge = sendee.getLocation();
								edge.setX((int)(edge.getX()));
								edge.setY((int)(edge.getY()));
								edge.setZ((int)(edge.getZ()));
								Location loc = edge;
								Location block = edge;

								double chunkx = loc.getX()%16;
								double chunkz = loc.getZ()%16;
								double chunky = 0;

								switch(sunburn.util.getQuad(block.getX(), block.getZ())){

								case 1:
									if(chunkx!=0){
										edge.setX((int)(edge.getX()-chunkx));
									}
									if(chunkz!=0){
										edge.setZ((int)(edge.getZ()-chunkz));
									}
									break;

								case 2:
									if(chunkx!=0){
										edge.setX((int)(edge.getX()+chunkx-16));
									}
									if(chunkz!=0){
										edge.setZ((int)(edge.getZ()-chunkz));
									}
									break;

								case 3:
									if(chunkx!=0){
										edge.setX((int)(edge.getX()+chunkx-16));
									}
									if(chunkz!=0){
										edge.setZ((int)(edge.getZ()+chunkz-16));
									}
									break;

								case 4:
									if(chunkx!=0){
										edge.setX((int)(edge.getX()-chunkx));
									}
									if(chunkz!=0){
										edge.setZ((int)(edge.getZ()+chunkz-16));
									}
									break;
								}
								edge.setX(edge.getX()-16);
								edge.setZ(edge.getZ()-16);

								//sunburn.getServer().broadcastMessage("Chunk starts at: X: "+edge.getX()+" Z: "+edge.getZ());
								chunkx = edge.getX();
								chunky = edge.getY();
								chunkz = edge.getZ();

								for(int ix = 0; ix < 3; ix+=1){
									//sunburn.getServer().broadcastMessage("Pass: "+ix);
									for(double cx = 0; cx < 48; cx+=1){
										for(double cz = 0; cz < 48; cz+=1){
											chunky = sunburn.util.getGround(chunkx+cx,chunkz+cz,sendee);
											for(double cy = chunky; (chunky-cy) < sunburn.config.cd; cy-=1){


												block.setX(chunkx+cx);
												block.setZ(chunkz+cz);
												block.setY(cy);

												//sunburn.getServer().broadcastMessage("Block at X: "+block.getX()+" Z: "+block.getZ());

												if(cy>0){
													//sunburn.getServer().broadcastMessage("In main loop!");
													Block b3 = sendee.getWorld().getBlockAt(block);
													if(b3.getType()==Material.BEDROCK){
														continue;
													}

													double rand = 0;

													if(b3.getType() == Material.GRASS){
														b3.setType(Material.DIRT);
													}else if((b3.getType() == Material.WATER)||(b3.getType() == Material.STATIONARY_WATER)){
														b3.setType(Material.AIR);
													}else if(b3.getType() == Material.STONE){
														b3.setType(Material.SANDSTONE);
													}else if(b3.getType() == Material.DIRT){
														rand = Math.random()*100;
														rand = (int) rand;
														if(rand%4 == 0){
															b3.setType(Material.SAND);
														}
													}else if((b3.getType()==Material.CACTUS)||
															(b3.getType()==Material.CROPS)||
															(b3.getType()==Material.DEAD_BUSH)||
															(b3.getType()==Material.BOOKSHELF)||
															(b3.getType()==Material.FENCE)||(b3.getType()==Material.ICE)||
															(b3.getType()==Material.FENCE_GATE)||
															(b3.getType()==Material.GRASS)||(b3.getType()==Material.SNOW)||
															(b3.getType()==Material.HUGE_MUSHROOM_1)||
															(b3.getType()==Material.HUGE_MUSHROOM_2)||
															(b3.getType()==Material.LONG_GRASS)||
															(b3.getType()==Material.MELON_BLOCK)||
															(b3.getType()==Material.MYCEL)||(b3.getType()==Material.PUMPKIN)||
															(b3.getType()==Material.RED_MUSHROOM)||(b3.getType()==Material.RED_ROSE)||
															(b3.getType()==Material.SAPLING)||(b3.getType()==Material.TNT)||
															(b3.getType()==Material.VINE)||
															(b3.getType()==Material.WOOD_DOOR)||(b3.getType()==Material.WOOD_STAIRS)||
															(b3.getType()==Material.WOODEN_DOOR)||(b3.getType()==Material.WOOL)||
															(b3.getType()==Material.WORKBENCH)||(b3.getType()==Material.YELLOW_FLOWER)){

														b3.setType(Material.AIR);

													}else if((b3.getType()==Material.LEAVES)||
															(b3.getType()==Material.WOOD)||
															(b3.getType()==Material.LOG)){
														block.setY(cy+1);
														b3 = sendee.getWorld().getBlockAt(block);
														if(b3.getType()==Material.AIR){
															b3.setType(Material.FIRE);
														}
													}
												}
											}		
										}
									}
								}
								sendee.sendMessage("[\u00A74Sunburn\u00A7f] Done!");
								return true;
							}
							return false;
						}
					}else{
						if((sendee.hasPermission("sunburn.toggle.player"))||(sendee.isOp())){
							if(sunburn.config.pwl.contains(args[0])){
								sunburn.config.pwl.remove(args[0]);
								if(!sunburn.config.pwr.contains(args[0])){
									sunburn.config.pwr.add(args[0]);
								}
								target.sendMessage("[\u00A74Sunburn\u00A7f] Removed from exception list by "+sendee.getName());
							}else{
								sunburn.config.pwl.add(args[0]);
								if(sunburn.config.pwr.contains(args[0])){
									sunburn.config.pwr.remove(args[0]);
								}
								target.sendMessage("[\u00A74Sunburn\u00A7f] Added to exception list by "+sendee.getName());
							}
							sunburn.config.conf.set("exclude_players", sunburn.config.pwl);
							File p = new File(sunburn.getDataFolder(), "config.yml");
							sunburn.config.saveConf(sunburn.config.conf, p);
							sunburn.config.loadConf();
							return true;
						}
						return false;
					}

				case 2:
					target = Bukkit.getServer().getPlayer(args[0]);
					if(target==null){
						if(args[0].equalsIgnoreCase("world")){
							if((sendee.hasPermission("sunburn.toggle"))||(sendee.isOp())){
								if(sunburn.config.worlds.contains(args[1])){
									sunburn.config.worlds.remove(args[1]);
									sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] World "+args[1]+" is no longer being burned.");
								}else{
									sunburn.config.worlds.add(args[1]);
									sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] World "+args[1]+" is now being burned.");
								}
								sunburn.config.conf.set("worlds", sunburn.config.worlds);
								File p = new File(sunburn.getDataFolder(), "config.yml");
								sunburn.config.saveConf(sunburn.config.conf, p);
								sunburn.config.loadConf();
								return true;
							}
							return false;
						}else if(args[0].equalsIgnoreCase("armor")){
							if((sendee.hasPermission("sunburn.toggle.armor"))||(sendee.isOp())){
								if(sunburn.util.isInteger(args[1])){
									sunburn.util.durability = Integer.parseInt(args[1]);
									sunburn.config.conf.set("Durability", sunburn.util.durability);
									sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] Durability set to: "+sunburn.util.durability);
									File p = new File(sunburn.getDataFolder(), "config.yml");
									sunburn.config.saveConf(sunburn.config.conf, p);
									sunburn.config.loadConf();
								}else{
									sunburn.config.conf.set("Armor_Type", args[1]);
									sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] Armor set to: "+args[1].toUpperCase());
									File p = new File(sunburn.getDataFolder(), "config.yml");
									sunburn.config.saveConf(sunburn.config.conf, p);
									sunburn.config.loadConf();
								}
								return true;
							}
							return false;
						}
					}else{
						if((sendee.hasPermission("sunburn.pburn"))||(sendee.isOp())){
							int FT = Integer.parseInt(args[1]);
							if(FT>=100000){
								return false;
							}
							int FS = FT/20;
							if(target==sendee){
								sendee.setFireTicks(FT);
							}else{
								target.sendMessage("[\u00A74Sunburn\u00A7f] You were set on fire for: "+FS+" seconds by: "+sendee.getName());
								target.setFireTicks(FT);
							}
							return true;
						}
						return false;
					}
				}
				return false;
			}else{
				switch(args.length){

				case 0:
					if(sunburn.config.disabled){
						sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] Enabled");
						sunburn.config.bPlayer = true;
						sunburn.config.bAnimal = true;
						sunburn.config.disabled = false;
					}else{
						sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] Disabled");
						sunburn.config.bPlayer = false;
						sunburn.config.bAnimal = false;
						sunburn.config.disabled = true;
					}
					break;

				case 1:
					Player target = Bukkit.getServer().getPlayer(args[0]);
					if(target==null){
						if(args[0].equalsIgnoreCase("player")){
							if(sunburn.config.bPlayer){
								sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] PlayerBurn Disabled");
								sunburn.config.bPlayer = false;
							}else{
								sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] PlayerBurn Enabled");
								sunburn.config.bPlayer = true;
							}
							return true;
						}else if(args[0].equalsIgnoreCase("animal")){
							if(sunburn.config.bAnimal){
								sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] MobBurn Disabled");
								sunburn.config.bAnimal = false;
							}else{
								sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] MobBurn Enabled");
								sunburn.config.bAnimal = true;
							}
							return true;
						}else if(args[0].equalsIgnoreCase("armor")){
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
							return true;
						}
						return false;
					}else{
						if(sunburn.config.pwl.contains(args[0])){
							sunburn.config.pwl.remove(args[0]);
							if(!sunburn.config.pwr.contains(args[0])){
								sunburn.config.pwr.add(args[0]);
							}
							target.sendMessage("[\u00A74Sunburn\u00A7f] Removed from exception list by [CONSOLE]");
						}else{
							sunburn.config.pwl.add(args[0]);
							if(sunburn.config.pwr.contains(args[0])){
								sunburn.config.pwr.remove(args[0]);
							}
							target.sendMessage("[\u00A74Sunburn\u00A7f] Added to exception list by [CONSOLE]");
						}
						sunburn.config.conf.set("exclude_players", sunburn.config.pwl);
						File p = new File(sunburn.getDataFolder(), "config.yml");
						sunburn.config.saveConf(sunburn.config.conf, p);
						sunburn.config.loadConf();
						return true;
					}

				case 2:
					target = Bukkit.getServer().getPlayer(args[0]);
					if(target==null){
						if(args[0].equalsIgnoreCase("world")){
							if(sunburn.config.worlds.contains(args[1])){
								sunburn.config.worlds.remove(args[1]);
								sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] World "+args[1]+" is no longer being burned.");
							}else{
								sunburn.config.worlds.add(args[1]);
								sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] World "+args[1]+" is now being burned.");
							}
							sunburn.config.conf.set("worlds", sunburn.config.worlds);
							File p = new File(sunburn.getDataFolder(), "config.yml");
							sunburn.config.saveConf(sunburn.config.conf, p);
							sunburn.config.loadConf();
							return true;
						}else if(args[0].equalsIgnoreCase("armor")){
							if(sunburn.util.isInteger(args[1])){
								sunburn.util.durability = Integer.parseInt(args[1]);
								sunburn.config.conf.set("Durability", sunburn.util.durability);
								sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] Durability set to: "+sunburn.util.durability);
								File p = new File(sunburn.getDataFolder(), "config.yml");
								sunburn.config.saveConf(sunburn.config.conf, p);
								sunburn.config.loadConf();
							}else{
								sunburn.config.conf.set("Armor_Type", args[1]);
								sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] Armor set to: "+args[1].toUpperCase());
								File p = new File(sunburn.getDataFolder(), "config.yml");
								sunburn.config.saveConf(sunburn.config.conf, p);
								sunburn.config.loadConf();
							}
							return true;
						}else if(args[0].equalsIgnoreCase("lock")){
							for(int x = 0; x < sunburn.config.wtime.length; x++){
								if(sunburn.config.wtime[x].name.equalsIgnoreCase(args[1])){
									if(sunburn.config.wtime[x].locked){
										sunburn.config.wtime[x].locked = false;
										sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] World "+sunburn.config.wtime[x].name+" unlocked!");
									}else{
										sunburn.config.wtime[x].locked = true;
										sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] World "+sunburn.config.wtime[x].name+" locked!");
										World world = sunburn.getServer().getWorld(args[1]);
										sunburn.config.wtime[x].locktime = world.getTime();
										sunburn.config.wtime[x].wdur = world.hasStorm();
									}
									File p = new File(sunburn.getDataFolder(), sunburn.config.wtime[x].name+".yml");
									sunburn.config.loadcConf(p, sunburn.getResource("World.yml"));
									sunburn.config.worldConfig.set("locktime", sunburn.config.wtime[x].locktime);
									sunburn.config.worldConfig.set("wdur", sunburn.config.wtime[x].wdur);
									sunburn.config.saveConf(sunburn.config.worldConfig, p);
									return true;
								}
							}	
						}
						return false;
					}else{
						int FT = Integer.parseInt(args[1]);
						if(FT>=100000){
							return false;
						}
						int FS = FT/20;
						target.sendMessage("[\u00A74Sunburn\u00A7f] You were set on fire for: "+FS+" seconds by: [CONSOLE]");
						target.setFireTicks(FT);
					}

					break;

				}
				return true;
			}

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
		}
		return false;
	}

}
