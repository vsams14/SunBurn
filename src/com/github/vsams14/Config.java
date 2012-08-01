package com.github.vsams14;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.github.vsams14.extras.WorldTime;
import com.github.vsams14.extras.bChunk;

public class Config{

	public boolean bPlayer = true;
	public boolean bAnimal = true;
	public boolean disabled = false;
	public boolean armor = true;
	public boolean autoburn = false;
	public boolean notify = true;
	public int pb, cd = 5;
	List<String> worlds, wasteworlds;
	public List<String> pwl;
	YamlConfiguration worldConfig = new YamlConfiguration();
	YamlConfiguration conf = new YamlConfiguration();
	WorldTime[] wtime;
	Map<String, Integer> wMap = new HashMap<String, Integer>();
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
			} catch (IOException e) {
			} catch (InvalidConfigurationException e) {
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
		autoburn = conf.getBoolean("auto_waste");
		notify = conf.getBoolean("notify_waste");
		wasteworlds = conf.getStringList("wasteland_worlds");
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
		worldConfig = new YamlConfiguration();

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
		worldConfig.set("auto_waste", autoburn);
		worldConfig.set("notify_waste", notify);
		worldConfig.set("Armor_Type", sunburn.util.armtype);
		worldConfig.set("Durability", sunburn.util.durability);
		worldConfig.set("Armor_On", armor);
		worldConfig.set("worlds", worlds);
		worldConfig.set("wasteland_worlds", wasteworlds);
		worldConfig.set("exclude_players", pwl);
		saveConf(worldConfig, f);

		for(World world : sunburn.getServer().getWorlds()){
			f = new File(sunburn.getDataFolder(), world.getName()+".yml");
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
			wMap.clear();
			wMap.put(world.getName(), i);

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
	
	public void loadChunks(){
		File folder = new File(sunburn.getDataFolder(), File.separator+"chunks");
		if(!folder.exists()){
			folder.mkdir();
		}else{
			for(World w : sunburn.getServer().getWorlds()){
				File in = new File(folder, File.separator+w.getName()+"_chunks.yml");
				try {
					BufferedReader readFile = new BufferedReader(new FileReader(in));
					String s;
					while((s=readFile.readLine())!=null){
						if(s.contains(":")){
							String[] p = s.split(":");
							String[] l = p[0].split(",");
							String[] coord = p[2].split(",");
							boolean b = Boolean.parseBoolean(p[1]);
							int id = sunburn.util.getWorldID(w.getName());
							int quad = Integer.parseInt(coord[0]);
							int x = Integer.parseInt(coord[1]);
							int z = Integer.parseInt(coord[2]);
							bChunk temp = sunburn.util.wC[id][quad][x][z];
							temp.activated = true;
							temp.world = w.getName();
							temp.x2 = x;
							temp.z2 = z;
							temp.quad = quad;
							temp.x = Integer.parseInt(l[0]);
							temp.z = Integer.parseInt(l[1]);
							temp.burnt = b;
							sunburn.util.wC[id][quad][x][z] = temp;
						}
					}
					readFile.close();
					in.delete();
					
				} catch (FileNotFoundException e) {
					sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] "+w.getName()+"_chunks.yml could not be found! Make sure that this is not an error!");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	public void saveChunks(){
		File folder = new File(sunburn.getDataFolder(), File.separator+"chunks");
		if(!folder.exists()){
			folder.mkdir();
		}
		for(World w : sunburn.getServer().getWorlds()){
			File out = new File(folder, File.separator+w.getName()+"_chunks.yml");
			try {
				BufferedWriter fileWriter = new BufferedWriter(new FileWriter(out));
				fileWriter.write("# DO NOT EDIT THIS FILE!\r\n");
				fileWriter.flush();
				String s;
				int id = sunburn.util.getWorldID(w.getName());
				for(int quad = 0; quad < 4; quad+=1){
					for(int x = 0; x<32768; x+=1){
						for(int z = 0; z<32768; z+=1){
							bChunk temp = sunburn.util.wC[id][quad][x][z];
							if(temp.activated){
								s = temp.x+","+temp.z+":"+temp.burnt+":"+quad+","+x+","+z+"\r\n";
								fileWriter.write(s);
								fileWriter.flush();
							}
						}
					}
				}
				fileWriter.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
