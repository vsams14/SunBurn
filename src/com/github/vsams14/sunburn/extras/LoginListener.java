package com.github.vsams14.sunburn.extras;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;

import com.github.vsams14.sunburn.Main;

public class LoginListener implements Listener {

	private Main sunburn;

	public LoginListener(Main plugin){
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.sunburn = plugin;
	}

	@EventHandler
	public void Login(PlayerLoginEvent event){
		Player player = event.getPlayer();
		if(player.hasPermission("sunburn.protect")){
			if(!sunburn.config.pwl.contains(player.getName())){
				sunburn.config.pwl.add(player.getName());
			}
		}
	}
}
