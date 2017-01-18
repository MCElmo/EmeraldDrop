package me.MC_Elmo.EmeraldDrop;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Elom on 1/17/17.
 */
public class EmeraldDrop extends JavaPlugin implements Listener,CommandExecutor
{
    private FileConfiguration config;
    private PluginDescriptionFile pdfFile;
    private PluginManager pm;
    private String prefix;
    private String title;
    private File cfile;

    public void onEnable()
    {
        config = getConfig();
        config.options().copyDefaults(true);
        config.options().header("Advanced Drops Format: \r\n    \"Number of emeralds to drop: Percent chance it will drop that many \n" +
                "Percentages must add up to 100 or default_drop amount will be dropped.");
        saveConfig();
        pdfFile = getDescription();
        pm = getServer().getPluginManager();
        pm.registerEvents(this,this);
        getCommand("emeralddrop").setExecutor(this);
        prefix = "EmeraldDrop";
        prefix = ChatColor.RED + "["+ ChatColor.GREEN + prefix + ChatColor.RED + "]" + ChatColor.RESET;
        this.title =  ChatColor.STRIKETHROUGH + "-----" + ChatColor.RESET + prefix + ChatColor.RESET + ChatColor.STRIKETHROUGH + "-----";
        cfile = new File(getDataFolder(), "config.yml");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        if(args.length != 1)
        {
            sender.sendMessage(prefix + ChatColor.RED + "Invalid arguments! Try /emeralddrop help");
            return false;
        }

        if(!sender.hasPermission("emeralddrop.reload"))
        {
            sender.sendMessage(prefix + ChatColor.RED + "You do not have permission to execute this command.");
            return false;
        }


        if(args[0].equalsIgnoreCase("help"))
        {
            sender.sendMessage(title);
            sender.sendMessage(ChatColor.GREEN + "/emeralddrop relaod - " + ChatColor.DARK_GREEN + " Reload config.");
        }

        if(args[0].equalsIgnoreCase("reload"))
        {
            reloadConfig();
            config = getConfig();
            saveConfig();
            sender.sendMessage(prefix + ChatColor.GREEN + "Successfully reloaded config.");
        }
        return true;
    }

    @EventHandler
    public void onVillagerDeath(EntityDeathEvent e)
    {
        if(!(e.getEntity() instanceof Villager))
            return;

        if(!(config.getBoolean("EmeraldDrop.enabled")))
            return;

        e.getDrops().clear();
        if(!(config.getBoolean("EmeraldDrop.Advanced Drop.enabled",false)))
        {

            e.getDrops().add(new ItemStack(Material.EMERALD,config.getInt("EmeraldDrop.default_drop", 1)));
            return;
        }
        List<String> drops = config.getStringList("EmeraldDrop.Advanced Drop.drops");
        if(drops != null)
        {
            HashMap<Integer, Integer> dropHash = new HashMap<Integer, Integer>();
            for (String drop : drops)
            {
                if (drop.contains(":"))
                {
                    String[] split = drop.split(":");
                    if (split.length == 2)
                    {
                        try
                        {
                            dropHash.put(Integer.valueOf(split[0]), Integer.valueOf(split[1]));
                        }catch(NumberFormatException exception)
                        {
                            getLogger().info("Error in config.yml : " + drop);
                            e.getDrops().add(new ItemStack(Material.EMERALD,config.getInt("EmeraldDrop.default_drop", 1)));
                            return;
                        }

                    }
                    else
                    {
                        getLogger().info("Error in config.yml : " + drop);
                        e.getDrops().add(new ItemStack(Material.EMERALD,config.getInt("EmeraldDrop.default_drop", 1)));
                        return;
                    }
                }else
                {
                    getLogger().info("Error in config.yml : " + drop);
                    e.getDrops().add(new ItemStack(Material.EMERALD,config.getInt("EmeraldDrop.default_drop", 1)));
                    return;
                }

            }



                int total = 0;
                for (int i : dropHash.values())
                {
                    total += i;
                }
                if (total == 100)
                {
                    int i = (int)(Math.random() * 100);
                    int prev = 0;
                    for(int drop_amount : dropHash.keySet())
                    {
                        if(i <= dropHash.get(drop_amount) + prev)
                        {
                            e.getDrops().add(new ItemStack(Material.EMERALD,drop_amount));
                            return;
                        }
                        prev += dropHash.get(drop_amount);
                    }

                }else
                {
                    getLogger().info("Error in config.yml, percentage must add up to 100");
                    e.getDrops().add(new ItemStack(Material.EMERALD,config.getInt("EmeraldDrop.default_drop", 1)));
                    return;
                }

        }else
        {
            getLogger().info("Error in config.yml, Drops list cannot be null ");
            e.getDrops().add(new ItemStack(Material.EMERALD,config.getInt("EmeraldDrop.default_drop", 1)));
            return;
        }




    }
}
