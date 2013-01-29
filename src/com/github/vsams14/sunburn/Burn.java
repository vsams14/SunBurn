package com.github.vsams14.sunburn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class Burn {

	Map <Player, Integer> smite = new HashMap<Player, Integer>();
	private Main sunburn;

	public Burn(Main sunburn){
		this.sunburn = sunburn;
	}

	public void BurnMain(){
		for(Player player :  sunburn.getServer().getOnlinePlayers()){
			World w = player.getWorld();
			for(World wd : sunburn.getServer().getWorlds()){
				sunburn.util.lockTime(wd);
			}
			if(sunburn.config.worlds.contains(w.getName())){
				
				Iterator<LivingEntity> creatures = w.getLivingEntities().iterator();
				
				while (creatures.hasNext())
				{
					LivingEntity m = creatures.next();

					if (!sunburn.util.isPlayer(m)) {
						burnAnimal(m);
					}
				}

				if(!sunburn.config.pwl.contains(player.getName())){
					Block b = player.getLocation().getBlock().getRelative(BlockFace.UP);
					byte B = b.getLightFromBlocks();
					byte T = b.getLightLevel();

					boolean fireres;
					if(player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)){
						fireres = true;
					}else{
						fireres = false;
					}
					
					if((T>=14)&&(B<14)){
						if((player.getFireTicks()<=0) && (sunburn.config.bPlayer) && (!fireres)){
							if(!sunburn.util.isWater(player.getLocation().getBlock())){
								player.setFireTicks(99999999);
							}
						}
						if((player.getFireTicks()>=100000) && (sunburn.config.bPlayer)){
							if(sunburn.util.hasArmor(player)&&(sunburn.config.armor)){
								player.setFireTicks(0);
							}
						}
						
					}else{
						if(player.getFireTicks()>=100000){
							player.setFireTicks(0);
						}
					}
					if(!sunburn.config.bPlayer){
						if(player.getFireTicks()>=100000){
							player.setFireTicks(0);
						}
					}

				}else{
					if(player.getFireTicks()>=100000){
						player.setFireTicks(0);
					}
				}
			}
		}
	}

	public void burnAnimal(LivingEntity m){
		Block creatureBlock = m.getLocation().getBlock().getRelative(BlockFace.UP);
		byte B2 = creatureBlock.getLightFromBlocks();
		byte T2 = creatureBlock.getLightLevel();
		if ((T2 == 15) && (B2<15)) {
			if((m.getFireTicks()<=0) && (sunburn.config.bAnimal)){
				m.setFireTicks(99999999);
			}
		}else if((T2 == 14) && (B2<14)){
			if((m.getFireTicks()<=0) && (sunburn.config.bAnimal)){
				m.setFireTicks(99999999);
			}
		}else{
			if(m.getFireTicks()>=100000){
				m.setFireTicks(0);
			}
		}
		if(!sunburn.config.bAnimal){
			if(m.getFireTicks()>=100000){
				m.setFireTicks(0);
			}
		}
	}

	public void usmite(){		
		for(Player p : sunburn.getServer().getOnlinePlayers()){
			if(smite.containsKey(p)){
				if(smite.get(p)>0){
					Location loc2 = p.getLocation();
					World w = loc2.getWorld();
					w.strikeLightningEffect(loc2);
					if(p.getHealth()>=5){
						p.setHealth(p.getHealth()-5);
					}else{
						p.setHealth(0);
						smite.put(p, 0);
					}
					smite.put(p, smite.get(p)-1);
				}
			}
		}
	}

	public void armor(){
		for(Player player : sunburn.getServer().getOnlinePlayers()){
			World w = player.getWorld();
			if(sunburn.config.worlds.contains(w.getName())){
				if(!sunburn.config.pwl.contains(player.getName())){
					sunburn.util.run8(player);
					if(!player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)){
						sunburn.util.extraDamage(player);
					}
				}
			}
		}
	}

}
