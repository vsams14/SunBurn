package com.github.vsams14;

import org.bukkit.Location;
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

	public double getGround(double x, double z, Player p){
		int count = 0;
		double y;
		Location loc3 = p.getLocation();
		loc3.setX(x);
		loc3.setZ(z);
		Block block2;
		for(y = 255; count <2; y-=1){
			if(y>0){
				loc3.setY(y);
				block2 = p.getWorld().getBlockAt(loc3);
				if(block2.getTypeId()!=0){
					if(block2.getType()==Material.LEAVES){
						loc3.setY(y+1);
						block2 = p.getWorld().getBlockAt(loc3);
						if(block2.getTypeId()==0){
							block2.setType(Material.FIRE);
						}
						count=0;
					}else{
						count+=1;
					}
					
				}else{
					count=0;
					block2.setTypeId(0);
					loc3.setY(y+1);
					block2 = p.getWorld().getBlockAt(loc3);
					if((!(block2.getTypeId()==0))&&(block2.getType()!=Material.BEDROCK)){
						block2.setTypeId(0);
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

}

