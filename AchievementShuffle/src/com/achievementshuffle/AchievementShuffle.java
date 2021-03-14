
package com.achievementshuffle;

import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;

import java.util.Iterator;
import java.util.Random;

import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Iterators;

import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.plugin.Plugin;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Set;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class AchievementShuffle extends JavaPlugin implements Listener, CommandExecutor
{
    private HashMap<String, Integer> points;
    private Advancement current;
    private ArrayList<Player> finishedPlayers;
    private ArrayList<Player> skips;
    private int timer;
    private int elapsedTime;
    private boolean stopper;
    public AchievementShuffle() {
       
    }
    
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this);
        this.points = new HashMap<>();
        timer = 6000;
        stopper = false;
        skips =  new ArrayList<Player>();
        finishedPlayers = new ArrayList<Player>();
    }
    
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (command.getName().equalsIgnoreCase("ashuffle")) {
           for(Player p : Bukkit.getOnlinePlayers()) {
        	   points.put(p.getDisplayName(), 0);
           }
           this.startRunnable();
           stopper = false;
           reShuffle();
        }
        if (command.getName().equalsIgnoreCase("skip")){
        	skips.add(Bukkit.getPlayer(sender.getName()));
        	Bukkit.broadcastMessage(sender.getName() + " has voted to skip! " + skips.size() + "/" +
        								String.valueOf(Bukkit.getOnlinePlayers().size()) +
        								" players have voted to skip.");
        	if(Bukkit.getOnlinePlayers().size() == skips.size()) {
        		reShuffle();
        		
        	}
        }
        if (command.getName().equalsIgnoreCase("timer") && args[0] != null) {
            timer = Integer.valueOf(args[0]) * 20;
            Bukkit.broadcastMessage("Timer has been set to " + timer/20 + " seconds.");
        }
        if (command.getName().equalsIgnoreCase("stop")){
        	displayPoints();
        	stopper = true;
        }
        return false;
    }
    private void reShuffle() {
   
    	Iterator<Advancement> it = Bukkit.getServer().advancementIterator();
    	int randomNum = ThreadLocalRandom.current().nextInt(0, Iterators.size(it));
    	it = Bukkit.getServer().advancementIterator();
    	
    	for(int i = 0; i < randomNum; i++) {
    		current = it.next(); 
    		
    		}
    	
	    if(current.getKey().getKey().substring(0,6).equals("recipe")) {
			reShuffle();
		}
		else {
            displayPoints();
			skips = new ArrayList<Player>();
	        finishedPlayers = new ArrayList<Player>();
	        elapsedTime = 0;
	    	Bukkit.broadcastMessage("The new advancement is " + current.getKey().getKey());
	    	Bukkit.broadcastMessage("Type /skip to vote to skip.");
    	}
    }
    private void sendInvalid(final CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Invalid usage. Please use:");
        sender.sendMessage(ChatColor.RED + "/ashuffle");
        sender.sendMessage(ChatColor.RED + "/skip");
    }
    @EventHandler
    private void onAchievenentGet(PlayerAdvancementDoneEvent e) {
    	if(e.getAdvancement() == current && !finishedPlayers.contains(e.getPlayer())) {
    		points.put(e.getPlayer().getDisplayName(), points.get(e.getPlayer().getDisplayName()) + 1);
    		Bukkit.broadcastMessage(e.getPlayer().getDisplayName() + " completed the advancement!");
    		finishedPlayers.add(e.getPlayer());
    		if(finishedPlayers.size() == Bukkit.getOnlinePlayers().size()) {
    			reShuffle();
    		}
    	}
    	revokeAllAdvancements(e.getPlayer());
    }
    public void revokeAllAdvancements(Player player) {
    	Iterator<Advancement> advancements = Bukkit.getServer().advancementIterator();

        while (advancements.hasNext()) {
            AdvancementProgress progress = player.getAdvancementProgress(advancements.next());
            for (String s : progress.getAwardedCriteria())
                progress.revokeCriteria(s);
        }
    }
    private void displayPoints() {
    	for (String s : points.keySet()) {
    		Bukkit.broadcastMessage(s + " - " + String.valueOf(points.get(s)) + " points");
    	}
    }
    private void startRunnable() {
        new BukkitRunnable() {
            public void run() {
            	if(stopper) this.cancel();
	            elapsedTime += 20;
	            if(elapsedTime == timer - 1200) {
	                Bukkit.broadcastMessage("One minute left!");
	            }
	            if(elapsedTime == timer) {
	               
	                reShuffle();
	                	
	            }
	            
            	
            }
        }.runTaskTimer((Plugin)this, 0L, 20L);
    }

    
}
