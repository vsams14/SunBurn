package com.github.vsams14.sunburn;

import java.util.ArrayList;
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

import com.github.vsams14.sunburn.extras.WorldTime;

public class Util {

	ItemStack helm, chest, pants, boots;
	int durability, al = 4096;
	String armtype, counter="";
	private Main sunburn;
	ArrayList<String> bchunks = new ArrayList<String>();
	Map<Integer, Integer> armors = new HashMap<Integer, Integer>();

	public Util(Main sunburn){
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

	public void getArmor(Player e) {
		helm = e.getInventory().getHelmet();
		chest = e.getInventory().getChestplate();
		pants = e.getInventory().getLeggings();
		boots = e.getInventory().getBoots();
	}

	public boolean hasArmor(Player player){
		getArmor(player);
		if(helm != null){
			return true;
		}
		if(chest != null){
			return true;
		}
		if(pants != null){
			return true;
		}
		if(boots != null){
			return true;
		}
		return false;
	}

	public boolean isPlayer(LivingEntity e) {
		return e instanceof Player;
	}

	public boolean isWater(Block l){
		if((l.getType()==Material.STATIONARY_WATER)||(l.getType()==Material.WATER)){
			return true;
		}else{
			return false;
		}
	}

	@SuppressWarnings("deprecation")
	public void run8(Player player){
		if(sunburn.config.armor){
			if(hasArmor(player)){
				Block b = player.getLocation().getBlock().getRelative(BlockFace.UP);
				byte B = b.getLightFromBlocks();
				byte T = b.getLightLevel();
				float tdur = 6, damage;
				boolean fireres;

				getArmor(player);
				PlayerInventory inv = player.getInventory();

				if(player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)){
					fireres = true;
				}else{
					fireres = false;
				}

				if((T>=14)&&(B<14)){
					if((sunburn.config.bPlayer) && (!fireres)){
						if(!isWater(player.getLocation().getBlock())){
							tdur -= (float) durability;
							tdur *= .02;
						}
					}
				}
				
				if(helm!=null){
					damage = tdur;
					damage *= armors.get(helm.getTypeId());
					if(!((helm.getDurability() + damage) >= armors.get(helm.getTypeId()))){
						helm.setDurability((short) (helm.getDurability() + damage));
					}else{
						inv.setHelmet(null);
						player.updateInventory();
						helm = null;
					}
				}
				
				if(chest!=null){
					damage = tdur;
					damage *= armors.get(chest.getTypeId());
					if(!((chest.getDurability() + damage) >= armors.get(chest.getTypeId()))){
						chest.setDurability((short) (chest.getDurability() + damage));
					}else{
						inv.setChestplate(null);
						player.updateInventory();
						chest = null;
					}
				}
				
				if(pants!=null){
					damage = tdur;
					damage *= armors.get(pants.getTypeId());
					if(!((pants.getDurability() + damage) >= armors.get(pants.getTypeId()))){
						pants.setDurability((short) (pants.getDurability() + damage));
					}else{
						inv.setLeggings(null);
						player.updateInventory();
						pants = null;
					}
				}
				
				if(boots!=null){
					damage = tdur;
					damage *= armors.get(boots.getTypeId());
					if(!((boots.getDurability() + damage) >= armors.get(boots.getTypeId()))){
						boots.setDurability((short) (boots.getDurability() + damage));
					}else{
						inv.setBoots(null);
						player.updateInventory();
						boots = null;
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

		for(int ix = 0; ix<2; ix++){
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

	public void getAutoBurnedChunks(){
		for(String worlds : sunburn.config.wasteworlds){
			World w = sunburn.getServer().getWorld(worlds);
			if(w!=null){
				for(Chunk c : w.getLoadedChunks()){
					String s = getData(c);
					if((!bchunks.contains(s+"q"))&&(!bchunks.contains(s+"b"))&&(!bchunks.contains(s+"r"))){
						bchunks.add(s+"q");
						//sunburn.com.broadcast("Added chunk "+s+"q!");
					}
				}	
			}
		}
		sortChunks();
	}
	
	public void sortChunks(){
		int lx = 0, lz = 0, mx = 0, mz = 0;
		for(String s : bchunks){
			String[] p = s.split(":");
			if(Integer.parseInt(p[1])<lx){
				lx = Integer.parseInt(p[1]);
			}
			if(Integer.parseInt(p[2])<lz){
				lz = Integer.parseInt(p[2]);
			}
			if(Integer.parseInt(p[1])>mx){
				mx = Integer.parseInt(p[1]);
			}
			if(Integer.parseInt(p[2])>mz){
				mz = Integer.parseInt(p[2]);
			}
		}
		ArrayList<String> chunks = new ArrayList<String>();
		for(int x = lx; x<=mx; x++){
			for(int z = lz; z<=mz; z++){
				String c = ":"+x+":"+z+":";
				for(String s : bchunks){
					if(s.contains(c)){
						chunks.add(s);
						break;
					}
				}
			}
		}
		bchunks = chunks;
	}

	public String getData(Chunk c){
		return c.getWorld().getName()+":"+c.getX()+":"+c.getZ()+":";
	}

	@SuppressWarnings("unchecked")
	public String wasteOneChunk(World w){
		if(sunburn.config.wasteworlds.contains(w.getName().toLowerCase())){
			for(Chunk c : w.getLoadedChunks()){
				String s = getData(c) + "q";
				if(bchunks.contains(s)){
					burnChunk(c);
					w.refreshChunk(c.getX(), c.getZ());
					bchunks.remove(s);
					bchunks.add(getData(c)+"b");
					return ("Wasted Chunk at ("+w.getName()+", "+c.getX()+", "+c.getZ()+")");
				}
			}
			for(String s : (ArrayList<String>)bchunks.clone()){
				if((s.contains(":q"))&&(s.contains(w.getName()+":"))){
					String[] p = s.split(":");
					Chunk c = w.getChunkAt(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
					burnChunk(c);
					w.refreshChunk(c.getX(), c.getZ());
					c.unload(true);
					bchunks.remove(s);
					bchunks.add(getData(c)+"b");
					return ("Wasted Chunk at ("+w.getName()+", "+c.getX()+", "+c.getZ()+")");
				}
			}
		}else{
			int c = 0;
			for(String s : (ArrayList<String>)bchunks.clone()){
				if((s.contains(":r"))&&(s.contains(w.getName()+":"))){
					String[] p = s.split(":");
					w.regenerateChunk(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
					w.refreshChunk(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
					bchunks.remove(s);
					c++;
					if(c==25){
						sunburn.com.broadcast(count());
						c = 0;
					}
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public String count(){
		int q=0, b=0, r=0;
		for(String s : (ArrayList<String>)bchunks.clone()){
			if(s.contains(":q")){
				q++;
			}else if(s.contains(":b")){
				b++;
			}else if(s.contains(":r")){
				r++;
			}
		}
		if(counter.equalsIgnoreCase(q+" in queue, "+b+" finished, "+r+" for regen")){
			return null;
		}else{
			counter = q+" in queue, "+b+" finished, "+r+" for regen";
			return counter;
		}
	}
}