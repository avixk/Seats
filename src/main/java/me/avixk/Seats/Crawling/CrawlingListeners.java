package me.avixk.Seats.Crawling;

import me.avixk.Seats.Cooldown;
import me.avixk.Seats.Main;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.List;

public class CrawlingListeners implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e){
        if(!Crawling.isCrawling(e.getPlayer()))return;
        List<Block> oldblocks = Crawling.crawling_players.get(e.getPlayer());
        Crawling.crawling_players.put(e.getPlayer(),Crawling.updateBarrierCeiling(e.getPlayer(), oldblocks));
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e){
        if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)){
            if(e.getItem() == null){
                if(e.getPlayer().isSneaking() && e.getPlayer().hasPermission("seats.crawl")){
                    if(Main.plugin.getConfig().getBoolean("look_down_and_shift_right_click_to_crawl")){
                        if(!e.getClickedBlock().getType().isInteractable()){
                            if(e.getPlayer().getLocation().getPitch() == 90){
                                if(Cooldown.getTimeRemaining("crawl:" + e.getPlayer().getName()) > 0)return;
                                new Cooldown("crawl:" + e.getPlayer().getName(),100).start();
                                Crawling.beginCrawling(e.getPlayer());
                            }
                        }
                    }
                }

            }
        }
    }

    @EventHandler
    public void onUnSwim(EntityToggleSwimEvent e){
        if (e.getEntityType().equals(EntityType.PLAYER)  && !e.isSwimming() && Crawling.isCrawling(((Player)e.getEntity())))
            e.setCancelled(true);
    }

    @EventHandler
    public void onUnSneak(PlayerToggleSneakEvent e){
        if (!e.isSneaking() && Crawling.isCrawling(((Player)e.getPlayer())))
            Crawling.stopCrawling(e.getPlayer());
    }
}
