package yoreni.CH.main;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.minecraft.server.v1_12_R1.ChatMessageType;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;


public class Main extends JavaPlugin implements Listener
{
	DecimalFormat comma = new DecimalFormat("#,###");
	BossBar hp = Bukkit.createBossBar("null", BarColor.GREEN, BarStyle.SOLID);

	public void onEnable()
	{
		Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();
		for(Player player : players)
		{		
			if(getConfig().getString("Data." + player.getUniqueId().toString() + ".Display").equals("BossBar"))
			{
				EntityLiving eplayer = ((CraftPlayer)player).getHandle();
				if (eplayer.getAbsorptionHearts() > 0) hp.setTitle((ChatColor.translateAlternateColorCodes('&',getConfig().getString("AbsorptionHPmsg"))).replace("%hp%",((int) player.getHealth())+ "").replace("%maxhp%",(int) player.getMaxHealth() + "").replace("%ab%",(int) eplayer.getAbsorptionHearts() + ""));
				else hp.setTitle((ChatColor.translateAlternateColorCodes('&',getConfig().getString("HPmsg"))).replace("%hp%",((int) player.getHealth())+ "").replace("%maxhp%",(int) player.getMaxHealth() + "").replace("%ab%",(int) eplayer.getAbsorptionHearts() + ""));
				hp.setProgress(player.getHealth() / player.getMaxHealth());
				hp.addPlayer(player);
				hp.show();
			}
		}

		createConfig();
		Bukkit.getPluginManager().registerEvents(this, this);
		getConfig().options().copyDefaults(true);
		//Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
		//{
		//	public void run() 
		//	{
		//		Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();
		//		for(Player player : players)
		//		{
		//		} 
		//	}
		//}, 0L, 1L);
	}

	public void onDisable()
	{
		hp.removeAll();
	}

	public void sendActionBar(Player player, String msg) 
	{
		IChatBaseComponent one = ChatSerializer.a("{\"text\": \"" + msg + "\"}");
		//PacketPlayOutChat two = new PacketPlayOutChat(one);
		PacketPlayOutChat two = new PacketPlayOutChat(one, ChatMessageType.GAME_INFO);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(two);
	}

	private void createConfig() 
	{
		try 
		{
			if (!getDataFolder().exists()) 
			{
				getDataFolder().mkdirs();
			}
			File file = new File(getDataFolder(), "config.yml");
			if (!file.exists()) 
			{
				getLogger().info("Config.yml not found, creating!");
				saveDefaultConfig();
			} 
			else 
			{
				getLogger().info("Config.yml found, loading!");
			}
		}
		catch (Exception Exception) 
		{
			Exception.printStackTrace();
		}
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event) 
	{
		Player player = event.getPlayer();
		if(!getConfig().isSet("Data." + player.getUniqueId().toString() + ".Compressed")) getConfig().set("Data." + player.getUniqueId().toString() + ".Compressed",getConfig().getBoolean("DefaultScaled"));
		if(!getConfig().isSet("Data." + player.getUniqueId().toString() + ".Display")) getConfig().set("Data." + player.getUniqueId().toString() + ".Display",getConfig().getString("DefaultDisplay"));
		saveConfig();
		if(getConfig().getBoolean("Data." + player.getUniqueId().toString() + ".Compressed"))player.setHealthScale(20);
		EntityLiving eplayer = ((CraftPlayer)player).getHandle();
		if(getConfig().getString("Data." + player.getUniqueId().toString() + ".Display").equals("HotBar"))
		{
			if (eplayer.getAbsorptionHearts() > 0) sendActionBar(player,(ChatColor.translateAlternateColorCodes('&',getConfig().getString("AbsorptionHPmsg"))).replace("%hp%",((int) player.getHealth())+ "").replace("%maxhp%",(int) player.getMaxHealth() + "").replace("%ab%",(int) eplayer.getAbsorptionHearts() + ""));
			else sendActionBar(player,(ChatColor.translateAlternateColorCodes('&',getConfig().getString("HPmsg"))).replace("%hp%",((int) player.getHealth())+ "").replace("%maxhp%",(int) player.getMaxHealth() + ""));
		}
		else if(getConfig().getString("Data." + player.getUniqueId().toString() + ".Display").equals("BossBar"))
		{
			//BossBar hp = Bukkit.createBossBar(null, BarColor.GREEN, BarStyle.SOLID);
			if (eplayer.getAbsorptionHearts() > 0) hp.setTitle((ChatColor.translateAlternateColorCodes('&',getConfig().getString("AbsorptionHPmsg"))).replace("%hp%",((int) player.getHealth())+ "").replace("%maxhp%",(int) player.getMaxHealth() + "").replace("%ab%",(int) eplayer.getAbsorptionHearts() + ""));
			else hp.setTitle((ChatColor.translateAlternateColorCodes('&',getConfig().getString("HPmsg"))).replace("%hp%",((int) player.getHealth())+ "").replace("%maxhp%",(int) player.getMaxHealth() + "").replace("%ab%",(int) eplayer.getAbsorptionHearts() + ""));
			hp.setProgress(player.getHealth() / player.getMaxHealth());
			hp.addPlayer(player);
			hp.show();
		}
	}

	@EventHandler
	public void playerDamge(EntityDamageEvent event)
	{
		if (!(event.getEntity() instanceof Player))
		{
			return;
		}
		Player player = (Player) event.getEntity();
		EntityLiving eplayer = ((CraftPlayer)player).getHandle();
		//player.sendMessage((int)(eplayer.getAbsorptionHearts() - event.getDamage()) + "");
		if(getConfig().getString("Data." + player.getUniqueId().toString() + ".Display").equals("HotBar"))
		{
			if (eplayer.getAbsorptionHearts() - (int) event.getDamage() > 0) sendActionBar(player,(ChatColor.translateAlternateColorCodes('&',getConfig().getString("AbsorptionHPmsg"))).replace("%hp%",(comma.format((int) player.getHealth() - (int) event.getDamage())) + "").replace("%maxhp%",comma.format((int) player.getMaxHealth()) + "").replace("%ab%",comma.format((int) (eplayer.getAbsorptionHearts() - (int) event.getDamage())) + ""));
			else 
			{
				if(((int) player.getHealth() - (int) event.getDamage()) > 0) sendActionBar(player,(ChatColor.translateAlternateColorCodes('&',getConfig().getString("HPmsg"))).replace("%hp%",(comma.format((int) player.getHealth() - (int) event.getDamage()))+ "").replace("%maxhp%",comma.format((int) player.getMaxHealth()) + ""));		
			}
		}
		else if(getConfig().getString("Data." + player.getUniqueId().toString() + ".Display").equals("BossBar"))
		{
			//BossBar hp = Bukkit.createBossBar(null, BarColor.GREEN, BarStyle.SOLID);
			if (eplayer.getAbsorptionHearts() - (int) event.getDamage() > 0) hp.setTitle((ChatColor.translateAlternateColorCodes('&',getConfig().getString("AbsorptionHPmsg"))).replace("%hp%",(comma.format((int) player.getHealth() - (int) event.getDamage())) + "").replace("%maxhp%",comma.format((int) player.getMaxHealth()) + "").replace("%ab%",comma.format((int) (eplayer.getAbsorptionHearts() - (int) event.getDamage())) + ""));
			else 
			{
				if(((int) player.getHealth() - (int) event.getDamage()) > 0) hp.setTitle((ChatColor.translateAlternateColorCodes('&',getConfig().getString("HPmsg"))).replace("%hp%",(comma.format((int) player.getHealth() - (int) event.getDamage()))+ "").replace("%maxhp%",comma.format((int) player.getMaxHealth()) + ""));		
			}
			hp.setProgress((player.getHealth() - event.getDamage()) / player.getMaxHealth());
			hp.addPlayer(player);
			hp.show();
		}
	}

	@EventHandler
	public void playerHeal(EntityRegainHealthEvent event)
	{
		if (!(event.getEntity() instanceof Player))
		{
			return;
		}
		Player player = (Player) event.getEntity();
		boolean over = false;
		if(player.getHealth() + event.getAmount() > player.getMaxHealth())
		{
			player.setHealth(player.getMaxHealth());
			over = true;	
		}
		double life = player.getHealth() + event.getAmount();
		if(over) life = player.getMaxHealth();
		EntityLiving eplayer = ((CraftPlayer)player).getHandle();
		if(getConfig().getString("Data." + player.getUniqueId().toString() + ".Display").equals("HotBar"))
		{
			if (eplayer.getAbsorptionHearts() > 0) sendActionBar(player,(ChatColor.translateAlternateColorCodes('&',getConfig().getString("AbsorptionHPmsg"))).replace("%hp%",(comma.format(life)) + "").replace("%maxhp%",comma.format((int) player.getMaxHealth()) + "").replace("%ab%",comma.format((int) eplayer.getAbsorptionHearts()) + ""));
			else sendActionBar(player,(ChatColor.translateAlternateColorCodes('&',getConfig().getString("HPmsg"))).replace("%hp%",(comma.format(life)) + "").replace("%maxhp%",comma.format((int) player.getMaxHealth()) + ""));
		}
		else if(getConfig().getString("Data." + player.getUniqueId().toString() + ".Display").equals("BossBar"))
		{
			//BossBar hp = Bukkit.createBossBar(null, BarColor.GREEN, BarStyle.SOLID);
			if (eplayer.getAbsorptionHearts() > 0) hp.setTitle((ChatColor.translateAlternateColorCodes('&',getConfig().getString("AbsorptionHPmsg"))).replace("%hp%",(comma.format(life)) + "").replace("%maxhp%",comma.format((int) player.getMaxHealth()) + "").replace("%ab%",comma.format((int) eplayer.getAbsorptionHearts()) + ""));
			else hp.setTitle((ChatColor.translateAlternateColorCodes('&',getConfig().getString("HPmsg"))).replace("%hp%",(comma.format(life)) + "").replace("%maxhp%",comma.format((int) player.getMaxHealth()) + ""));
			hp.setProgress(life / player.getMaxHealth());
			hp.addPlayer(player);
			hp.show();
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		Player player = (Player) sender;
		if(sender instanceof Player)
		{
			if(cmd.getLabel().equalsIgnoreCase("CompressedHearts"))
			{
				if(args.length > 0)
				{
					if(args[0].equalsIgnoreCase("compress"))
					{
						if(player.isHealthScaled())
						{
							player.setHealthScaled(false);
							player.sendMessage(ChatColor.GREEN + "You have unscaled your healthbar");
							getConfig().set("Data." + player.getUniqueId().toString() + ".Compressed",false);
							saveConfig();
							return true;
						}
						else
						{
							player.setHealthScaled(true);
							player.setHealthScale(20);
							player.sendMessage(ChatColor.GREEN + "You have scaled your healthbar");
							getConfig().set("Data." + player.getUniqueId().toString() + ".Compressed",true);
							saveConfig();
							return true;
						}
					}
					if(args[0].equalsIgnoreCase("display"))
					{
						if (args.length == 2)
						{
							if(args[1].equalsIgnoreCase("Boss"))
							{
								hp.addPlayer(player);
								EntityLiving eplayer = ((CraftPlayer)player).getHandle();
								if (eplayer.getAbsorptionHearts() > 0) hp.setTitle((ChatColor.translateAlternateColorCodes('&',getConfig().getString("AbsorptionHPmsg"))).replace("%hp%",((int) player.getHealth())+ "").replace("%maxhp%",(int) player.getMaxHealth() + "").replace("%ab%",(int) eplayer.getAbsorptionHearts() + ""));
								else hp.setTitle((ChatColor.translateAlternateColorCodes('&',getConfig().getString("HPmsg"))).replace("%hp%",((int) player.getHealth())+ "").replace("%maxhp%",(int) player.getMaxHealth() + "").replace("%ab%",(int) eplayer.getAbsorptionHearts() + ""));
								hp.setProgress(player.getHealth() / player.getMaxHealth());
								getConfig().set("Data." + player.getUniqueId().toString() + ".Display","BossBar");
								saveConfig();
							}
							else if(args[1].equalsIgnoreCase("Hotbar"))
							{
								hp.removePlayer(player);
								getConfig().set("Data." + player.getUniqueId().toString() + ".Display","HotBar");
								saveConfig();
							}
							else if(args[1].equalsIgnoreCase("None"))
							{
								hp.removePlayer(player);
								getConfig().set("Data." + player.getUniqueId().toString() + ".Display","None");
								saveConfig();
							}
							else player.sendMessage(ChatColor.RED + "Usage /compressedhearts display (Boss|Hotbar|None)");
						}
						else player.sendMessage(ChatColor.RED + "Usage /compressedhearts display (Boss|Hotbar|None)");
						return true;
					}
					else
					{
						player.sendMessage(ChatColor.YELLOW + "/compressedhearts compress " + ChatColor.WHITE + "Compress or Uncompress hearts");
						player.sendMessage(ChatColor.YELLOW + "/compressedhearts display (Boss|Hotbar|None) " + ChatColor.WHITE + "Change the display of HP");
						return true;	
					}
				}
				else
				{
					player.sendMessage(ChatColor.YELLOW + "/compressedhearts compress " + ChatColor.WHITE + "Compress or Uncompress hearts");
					player.sendMessage(ChatColor.YELLOW + "/compressedhearts display (Boss|Hotbar|None) " + ChatColor.WHITE + "Change the display of HP");
					return true;
				}
			}
		}
		else getServer().getLogger().info("Only players can run that command");
		return false;

	}
}
