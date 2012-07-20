package com.github.vsams14;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class Config{

	boolean bPlayer, bAnimal, disabled, armor = true;
	int pb, cd = 5;
	List<String> worlds;
	List<String> pwl;
	List<String> pwr = new ArrayList<String>();
	YamlConfiguration worldConfig = new YamlConfiguration();
	YamlConfiguration conf = new YamlConfiguration();
	WorldTime[] wtime;
	private SunBurn sunburn;

	public Config(SunBurn sunburn){
		this.sunburn = sunburn;
	}

	public void loadConf(){
		File p = new File(sunburn.getDataFolder(), "config.yml");
		if(p.exists()){
			try {
				conf.load(p);
			} catch (FileNotFoundException e) {
				//e.printStackTrace();
			} catch (IOException e) {
				//e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				//e.printStackTrace();
			}
		}else{
			InputStream d = sunburn.getResource("config.yml");
			if (d != null) {
				YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(d);
				conf.setDefaults(defConfig);
				saveConf(conf, p);
			}
		}

		bPlayer = conf.getBoolean("burn_players");
		bAnimal = conf.getBoolean("burn_animals");
		sunburn.util.durability = conf.getInt("Durability");
		pb = conf.getInt("burn_damage");
		worlds = conf.getStringList("worlds");
		pwl = conf.getStringList("exclude_players");
		sunburn.util.armtype = conf.getString("Armor_Type");
		armor = conf.getBoolean("Armor_On");
		cd = conf.getInt("chunk_depth");
	}

	public void addExceptions(){
		for(Player player : sunburn.getServer().getOnlinePlayers()){
			if(player.hasPermission("sunburn.protect")){
				if(!sunburn.config.pwl.contains(player.getName())){
					sunburn.config.pwl.add(player.getName());
				}
			}
		}
	}

	public void loadcConf(File p, InputStream d){
		if(p.exists()){
			try {
				worldConfig.load(p);
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			} catch (InvalidConfigurationException e) {
			}
		}else{
			if (d != null) {
				YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(d);
				worldConfig.setDefaults(defConfig);
			}
		}			
	}

	public void saveConf(YamlConfiguration y, File p){
		y.options().copyDefaults(true);
		try {
			y.save(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reConf(){
		String node = conf.getString("version");
		worldConfig = new YamlConfiguration();
		if((node==null)||(!node.equalsIgnoreCase(sunburn.getDescription().getVersion()))){
			File f = new File(sunburn.getDataFolder(), "config.yml");
			f.delete();
			InputStream d = sunburn.getResource("config.yml");
			if (d != null) {
				YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(d);
				worldConfig.setDefaults(defConfig);
			}
			worldConfig.set("version", sunburn.getDescription().getVersion());
			worldConfig.set("burn_damage", pb);
			worldConfig.set("burn_animals", bAnimal);
			worldConfig.set("burn_players", bPlayer);
			worldConfig.set("chunk_depth", cd);
			worldConfig.set("Armor_Type", sunburn.util.armtype);
			worldConfig.set("Durability", sunburn.util.durability);
			worldConfig.set("Armor_On", armor);
			worldConfig.set("worlds", worlds);
			worldConfig.set("exclude_players", pwl);
			saveConf(worldConfig, f);
		}

		for(World world : sunburn.getServer().getWorlds()){
			File f = new File(sunburn.getDataFolder(), world.getName()+".yml");
			worldConfig = new YamlConfiguration();
			loadcConf(f, sunburn.getResource("World.yml"));
			if(sunburn.util.isInteger(worldConfig.getString("wdur"))){
				worldConfig.set("wdur", false);
				saveConf(worldConfig, f);
			}			 
		}
	}

	@SuppressWarnings("unused")
	public void genConf(){
		int i = 0;
		for(World world: sunburn.getServer().getWorlds())
		{
			i+=1;
		}
		wtime = new WorldTime[i];
		for (int x = 0; x < wtime.length; x++) {
			wtime[x] = new WorldTime();
		}
		i=0;
		for(World world: sunburn.getServer().getWorlds()){
			worldConfig = new YamlConfiguration();
			File f = new File(sunburn.getDataFolder(), world.getName()+".yml");
			loadcConf(f, sunburn.getResource("World.yml"));
			worldConfig.set("WorldName", world.getName());

			wtime[i].name = worldConfig.getString("WorldName");
			wtime[i].locked = worldConfig.getBoolean("locked");
			wtime[i].dawn = (18000/worldConfig.getInt("sunrise_length"));
			wtime[i].day = (120000/worldConfig.getInt("day_length"));
			wtime[i].dusk = (18000/worldConfig.getInt("sunset_length"));
			wtime[i].night = (84000/worldConfig.getInt("night_length"));
			wtime[i].locktime = worldConfig.getInt("locktime");
			wtime[i].wdur = worldConfig.getBoolean("wdur");
			saveConf(worldConfig, f);
			i+=1;
		}
	}

}