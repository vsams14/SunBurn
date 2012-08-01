package com.github.vsams14;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;

public class Util {

	ItemStack helm, chest, pants, boots;
	Material MattH, MattC, MattL, MattB;
	int totald, hd1, cd1, ld1, bd1;
	int durability;
	float hd, cd, ld, bd;
	String armtype;
	private SunBurn sunburn;
	Map<World, Map<Chunk, Boolean>> worldChunks = new HashMap<World, Map<Chunk, Boolean>>();


	public Util(SunBurn sunburn){
		this.sunburn = sunburn;
	}

	public boolean isInteger(String s){
		try{  
			Integer.parseInt(s);  
			return true;  
		}catch(Exception e){  
			return false;  
		}  
	}

	public double getGround(int x, int z, Chunk c){
		int count = 0, y;
		Block b2;
		for(y = 255; count <2; y-=1){
			if(y>0){
				b2 = c.getBlock(x, y, z);
				if(b2.getTypeId()!=0){
					if(b2.getType()==Material.LEAVES){
						b2 = c.getBlock(x, y+1, z);
						if(b2.getTypeId()==0){
							b2.setType(Material.FIRE);
						}
						count=0;
					}else{
						count+=1;
					}
					
				}else{
					count=0;
					b2.setTypeId(0);
					b2 = c.getBlock(x, y+1, z);
					if(b2.getType()!=Material.BEDROCK){
						b2.setTypeId(0);
					}
				}
			}else{
				count = 2;
			}
		}
		return (y+count);
	}

	public int getQuad(double x, double z){
		int quad = 1;
		if((x>=0)&&(z>=0)){
			quad = 1;
		}else if((x<0)&&(z>=0)){
			quad = 2;
		}else if((x<0)&&(z<0)){
			quad = 3;
		}else{
			quad = 4;
		}
		return quad;
	}

	public void getArmor(Player e) {
		helm = e.getInventory().getHelmet();
		chest = e.getInventory().getChestplate();
		pants = e.getInventory().getLeggings();
		boots = e.getInventory().getBoots();
	}
	
	public boolean hasArmor(Player player){
		getArmor(player);
		if(helm != null){
			if(helm.getType() == MattH){
				return true;
			}
		}
		if(chest != null){
			if(chest.getType() == MattC){
				return true;
			}
		}
		if(pants != null){
			if(pants.getType() == MattL){
				return true;
			}
		}
		if(boots != null){
			if(boots.getType() == MattB){
				return true;
			}
		}
		return false;
	}

	public boolean isPlayer(LivingEntity e) {
		return e instanceof Player;
	}

	public void addArmor(){
		totald=0;
		if(helm != null){
			if(helm.getType() == MattH){
				totald+=hd;
			}
		}
		if(chest != null){
			if(chest.getType() == MattC){
				totald+=cd;
			}
		}
		if(pants != null){
			if(pants.getType() == MattL){
				totald+=ld;
			}
		}
		if(boots != null){
			if(boots.getType() == MattB){
				totald+=bd;
			}
		}
	}

	public boolean isWater(Block l){
		if((l.getType()==Material.STATIONARY_WATER)||(l.getType()==Material.WATER)){
			return true;
		}else{
			return false;
		}
	}

	public void loadArmor(){
		armtype = sunburn.config.conf.getString("Armor_Type");
		if(armtype.equalsIgnoreCase("leather")){
			MattH = Material.LEATHER_HELMET;
			MattC = Material.LEATHER_CHESTPLATE;
			MattL = Material.LEATHER_LEGGINGS;
			MattB = Material.LEATHER_BOOTS;
			hd=56;hd1=56;
			cd=82;cd1=82;
			ld=76;ld1=76;
			bd=66;bd1=66;
		}else if(armtype.equalsIgnoreCase("iron")){
			MattH = Material.IRON_HELMET;
			MattC = Material.IRON_CHESTPLATE;
			MattL = Material.IRON_LEGGINGS;
			MattB = Material.IRON_BOOTS;
			hd=166;hd1=166;
			cd=242;cd1=242;
			ld=226;ld1=226;
			bd=196;bd1=196;
		}else if(armtype.equalsIgnoreCase("gold")){
			MattH = Material.GOLD_HELMET;
			MattC = Material.GOLD_CHESTPLATE;
			MattL = Material.GOLD_LEGGINGS;
			MattB = Material.GOLD_BOOTS;
			hd=78;hd1=78;
			cd=114;cd1=114;
			ld=106;ld1=106;
			bd=92;bd1=92;
		}else if(armtype.equalsIgnoreCase("diamond")){
			MattH = Material.DIAMOND_HELMET;
			MattC = Material.DIAMOND_CHESTPLATE;
			MattL = Material.DIAMOND_LEGGINGS;
			MattB = Material.DIAMOND_BOOTS;
			hd=364;hd1=364;
			cd=529;cd1=529;
			ld=496;ld1=496;
			bd=430;bd1=430;
		}else if(armtype.equalsIgnoreCase("chain")){
			MattH = Material.CHAINMAIL_HELMET;
			MattC = Material.CHAINMAIL_CHESTPLATE;
			MattL = Material.CHAINMAIL_LEGGINGS;
			MattB = Material.CHAINMAIL_BOOTS;
			hd=78;hd1=78;
			cd=114;cd1=114;
			ld=106;ld1=106;
			bd=92;bd1=92;
		}
	}

	public void run8(Player player){
		if(sunburn.config.armor){
			
			Block b = player.getLocation().getBlock().getRelative(BlockFace.UP);
			byte B = b.getLightFromBlocks();
			byte T = b.getLightLevel();

			loadArmor();
			float tdur = 0;

			getArmor(player);
			addArmor();

			boolean fireres;
			if(player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)){
				fireres = true;
			}else{
				fireres = false;
			}

			if((T>=14)&&(B<14)){
				if((sunburn.config.bPlayer) && (!fireres)){
					if(!isWater(player.getLocation().getBlock())){
						if(totald>0){
							tdur += 163/durability;
						}
					}
				}
			}

			float p2 = (float) (hd+cd+ld+bd);
			float proportion = (float) 1819/p2;
			PlayerInventory inv = player.getInventory();

			if(helm != null){
				if(helm.getType() == MattH){
					hd /= totald;
					hd *= tdur;
					hd /= proportion;
					helm.setDurability((short) Math.round(helm.getDurability()+hd));
					if(helm.getDurability()>=hd1){
						inv.setHelmet(null);
					}
				}
			}
			if(chest != null){
				if(chest.getType() == MattC){
					cd /= totald;
					cd *= tdur;
					cd /= proportion;
					chest.setDurability((short) Math.round(chest.getDurability()+cd));
					if(chest.getDurability()>=cd1){
						inv.setChestplate(null);
					}
				}
			}
			if(pants != null){
				if(pants.getType() == MattL){
					ld /= totald;
					ld *= tdur;
					ld /= proportion;
					pants.setDurability((short) Math.round(pants.getDurability()+ld));
					if(pants.getDurability()>=ld1){
						inv.setLeggings(null);
					}
				}
			}
			if(boots != null){
				if(boots.getType() == MattB){
					bd /= totald;
					bd *= tdur;
					bd /= proportion;
					boots.setDurability((short) Math.round(boots.getDurability()+bd));
					if(boots.getDurability()>=bd1){
						inv.setBoots(null);
					}
				}
			}
		}
	}

	public void extraDamage(Player player){
		if(player.getFireTicks()>100000){
			if((player.getHealth()-sunburn.config.pb)<=0){
				player.setHealth(0);
			}else{
				player.setHealth(player.getHealth()-sunburn.config.pb);
			}
		}
	}

	public void lockTime(World world){
		WorldTime[] wtime = sunburn.config.wtime;
		for(int x = 0; x<wtime.length;x++){
			if(world.getName().equalsIgnoreCase(wtime[x].name)){
				if(wtime[x].locked){
					if(wtime[x].locktime==0){
						wtime[x].locktime=world.getTime();
					}
					if(world.getTime()!=wtime[x].locktime){
						world.setTime(wtime[x].locktime);
					}
					if(world.hasStorm()!=wtime[x].wdur){
						world.setStorm(wtime[x].wdur);
					}
				}else{

					if((world.getTime()>=0)&&(world.getTime()<12000)){
						world.setTime(world.getTime()-10+(wtime[x].day));
					}else if((world.getTime()>=12000)&&(world.getTime()<13800)){
						world.setTime(world.getTime()-10+(wtime[x].dusk));
					}else if((world.getTime()>=13800)&&(world.getTime()<22200)){
						world.setTime(world.getTime()-10+(wtime[x].night));
					}else{
						world.setTime(world.getTime()-10+(wtime[x].dawn));
					}
					wtime[x].locktime = world.getTime();
					wtime[x].wdur = world.hasStorm();
				}
			}
		}
	}

	public void burnChunk(Chunk chunk){
		Block b3;
		
		for(int ix = 0; ix<3; ix+=1){
			for(int x = 0; x<16; x+=1){
				for(int z = 0; z<16; z+=1){
					
					double y = getGround(x, z, chunk);
					for(double cy = y; (y-cy) < sunburn.config.cd; cy-=1){
						b3 = chunk.getBlock(x,  (int) cy,  z);
						
						if(cy>0){
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
								b3 = chunk.getBlock(x,  (int) cy+1,  z);
								if(b3.getType()==Material.AIR){
									b3.setType(Material.FIRE);
								}
							}
						}
					}
				}
			}
		}
	}

	public void initializeMap(){
		for(World w : sunburn.getServer().getWorlds()){
			Map<Chunk, Boolean> burnedChunks = new HashMap<Chunk, Boolean>();
			worldChunks.put(w, burnedChunks);
		}
	}
	
	public void getAutoBurnedChunks(){
		for(World w : sunburn.getServer().getWorlds()){
			if(sunburn.config.wasteworlds.contains(w.getName())){
				for(Chunk c : w.getLoadedChunks()){
					Map<Chunk, Boolean> burnedChunks = worldChunks.get(w);
					if(c!=null){
						if(!(burnedChunks.containsKey(c))){
							burnedChunks.put(c, false);
							worldChunks.put(w, burnedChunks);
						}
					}
				}
			}
		}
	}
	
	public void wasteOneChunk(){
		for(World w : sunburn.getServer().getWorlds()){
			if(sunburn.config.wasteworlds.contains(w.getName())){
				Map<Chunk, Boolean> burnedChunks = worldChunks.get(w);
				for(Chunk c : w.getLoadedChunks()){
					if(burnedChunks.containsKey(c)){
						if(burnedChunks.get(c)){
							continue;
						}else{
							burnedChunks.put(c,  true);
							burnChunk(c);
							if(sunburn.config.notify){
								sunburn.getServer().broadcastMessage("[\u00A74Sunburn\u00A7f] Burning Chunk: "+c.getX()+", "+c.getZ()+" in World:"+w.getName());	
							}
							break;
						}
					}
				}
				worldChunks.put(w,  burnedChunks);
			}
		}
	}
	
}

