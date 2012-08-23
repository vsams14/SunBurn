package com.github.vsams14.sunburn;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.text.BadLocationException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.vsams14.sunburn.extras.LoginListener;
import com.github.vsams14.sunburn.extras.Metrics;
import com.github.vsams14.sunburn.extras.WorldTime;

public class Main extends JavaPlugin {

	public Logger log;	

	public Util util = new Util(this);
	public Commands com = new Commands(this);
	public Burn burn = new Burn(this);
	public Config config = new Config(this);
	public Update update = new Update(this);
	int timer = 15;
	int runs = 0;

	public void onEnable(){
		log = this.getLogger();
		new LoginListener(this);
		
		config.loadConf();
		config.reConf();
		util.loadArmor();
		config.genConf();
		config.loadChunks();
		
		update.readRSS();
		
		try {
		    Metrics metrics = new Metrics(this);
		    metrics.findCustomData(this);
		    metrics.start();
		} catch (IOException e) {
		    // Failed to submit the data :-(
		}

		//Player-burn + usmite, 1/2 Second, 1 second delay
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
		{
			public void run() {
				burn.BurnMain();
				burn.usmite();
				if(config.autoburn){
					util.getAutoBurnedChunks();
					util.wasteOneChunk();
				}
			}

		}
		, 20L , 10L);

		//Updates, 15 Minutes
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
		{
			public void run() {
				if(config.checkforup){
					if(update.isCurrent()){
						if(runs == 0){
							log.info("");
							log.info("");
							if(update.changes<0){
								log.info("You have a developer beta version.");
								log.info("If you are not vsams14 or a beta tester,");
								log.info("please use a regular version!");
							}else{
								log.info("This plugin version is current. No need to update.");
							}
							log.info("");
							log.info("");
							runs += 1;
						}
					}else{
						if(runs == 0){
							log.info("");
							log.info("");
						}

						if(update.changes<10){
							log.info("There have been minor changes or bugfixes. UPDATING!");
						}else if(update.changes<100){
							log.info("There may be some new features and major changes. UPDATING!");
						}else if(update.changes<1000){
							log.info("There are some really major changes. UPDATING!");
						}
						try {
							if(config.canupdate){
								update.update();
								log.info("The server will now shut down.");	
							}else{
								log.info("Updating is DISABLED! Please change your configuration!");
							}
						} catch (IOException e) {
							e.printStackTrace();
						} catch (BadLocationException e) {
							e.printStackTrace();
						}

						if(runs == 0){
							log.info("");
							log.info("");
							runs += 1;
						}
					}	
				}else{
					if(runs==0){
						log.info("Checking for updates is DISABLED! Please change your config!");
						runs++;
					}
				}
			}

		}
		, 0L , 18000L);

		//Auto-off, 1 second
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
		{
			public void run() {					
				if(update.off){
					if(timer>0){
						if(timer<=10){
							log.info("Shutdown in: "+timer);
						}
						timer-=1;
					}else{
						update.unloadServer();
					}
				}
			}
		}
		, 0L , 20L);

		//Armor damage, 8 seconds
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
		{
			public void run() {					
				burn.armor();
			}
		}
		, 0L , 160L);
		
		//Status messages, 2 seconds
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable()
		{
			public void run() {					
				if(config.notify.equalsIgnoreCase("broadcast")){
					String s = util.count();
					if(s!=null){
						com.broadcast("Autoburn: "+s);
					}
				}else if(config.notify.equalsIgnoreCase("log")){
					String s = util.count();
					if(s!=null){
						log.info("Autoburn: "+s);
					}
				}
			}
		}
		, 0L , 40L);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(!update.off){
			boolean ret = com.com(sender, cmd, commandLabel, args);
			config.reConf();
			return ret;
		}else{
			com.broadcast("Waiting for update. Sunburn is disabled");
			log.info("Waiting for update. Sunburn is disabled");
			return true;
		}
	}

	public void onDisable(){
		if(!update.off){
			//log.info("Saving Configuration...");
			config.saveChunks();
			config.reConf();
			WorldTime[] wtime = config.wtime;
			for(int x = 0; x < wtime.length; x++){
				config.worldConfig = new YamlConfiguration();
				File p = new File(getDataFolder(), wtime[x].name+".yml");
				config.loadcConf(p, getResource("World.yml"));
				config.worldConfig.set("locktime", wtime[x].locktime);
				config.worldConfig.set("wdur", wtime[x].wdur);
				config.worldConfig.set("locked", wtime[x].locked);
				config.saveConf(config.worldConfig, p);
			}
		}
	}
}