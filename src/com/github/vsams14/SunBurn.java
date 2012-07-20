package com.github.vsams14;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.text.BadLocationException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class SunBurn extends JavaPlugin {

	Logger log;	

	Util util = new Util(this);
	Commands com = new Commands(this);
	Burn burn = new Burn(this);
	Config config = new Config(this);
	Update update = new Update(this);
	int timer = 15;
	int runs = 0;

	public void onEnable(){
		log = this.getLogger();
		new LoginListener(this);
		
		try {
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		} catch (IOException e) {
		    // Failed to submit the data :-(
		}
		
		config.loadConf();
		config.reConf();
		util.loadArmor();
		config.genConf();
		
		update.readRSS();

		if((!config.bPlayer) && (!config.bAnimal)){
			config.disabled = true;
		}

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
		{
			public void run() {
				burn.BurnMain();
				burn.usmite();
			}

		}
		, 0L , 10L);

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
		{
			public void run() {
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
						update.update();
						log.info("The server will now shut down.");
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
			}

		}
		, 0L , 18000L);

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

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
		{
			public void run() {					
				burn.armor();
			}
		}
		, 0L , 160L);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(!update.off){
			return com.com(sender, cmd, commandLabel, args);
		}else{
			getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] Waiting for update. Sunburn is disabled");
			log.info("Waiting for update. Sunburn is disabled");
			return true;
		}
	}

	public void onDisable(){
		if(!update.off){
			log.info("Saving Configuration...");
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