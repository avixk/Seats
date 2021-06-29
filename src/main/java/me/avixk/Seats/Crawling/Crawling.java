package me.avixk.Seats.Crawling;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Crawling {
    public static final double yoffset = 1.6;
    public static final double xzoffset = 0.45;
    public static final Material ceilingMaterial = Material.BARRIER;
    static HashMap<Player, List<Block>> crawling_players = new HashMap<>();
    public static void beginCrawling(Player player){
        if(crawling_players.containsKey(player))return;
        player.setSwimming(true);
        crawling_players.put(player,updateBarrierCeiling(player,null));
        Bukkit.getPluginManager().callEvent((Event)new PlayerMoveEvent(player, player.getLocation(), player.getLocation()));
    }

    public static void stopCrawling(Player player){
        if(!crawling_players.containsKey(player))return;
        List<Block> blocks = crawling_players.get(player);
        crawling_players.remove(player);
        clearCeiling(player,blocks);
        player.setSwimming(false);
    }

    private static void clearCeiling(Player player, List<Block> blocks) {
        for(Block block : blocks){
            player.sendBlockChange(block.getLocation(), block.getBlockData());
            if(block.getState() instanceof Sign){
                Sign sign = (Sign) block.getState();
                if(sign.getColor() != null)
                    player.sendSignChange(block.getLocation(),sign.getLines(),sign.getColor());
                else
                    player.sendSignChange(block.getLocation(),sign.getLines());
            }
        }
    }

    public static boolean isCrawling(Player player){
        return crawling_players.containsKey(player);
    }

    public static List<Block> updateBarrierCeiling(Player player, List<Block> oldblocks){
        if(oldblocks == null) oldblocks = new ArrayList<>();
        Location localOffset =player.getLocation().clone().subtract(player.getLocation().getBlock().getLocation());
        boolean nX = localOffset.getX() < Crawling.xzoffset;
        boolean pX = localOffset.getX() > 1 - Crawling.xzoffset;
        boolean nZ = localOffset.getZ() < Crawling.xzoffset;
        boolean pZ = localOffset.getZ() > 1 - Crawling.xzoffset;
        List<Block> ceilingBlocks = new ArrayList<>();
        ceilingBlocks.add(player.getLocation().clone().add(0,Crawling.yoffset,0).getBlock());
        if(nX){
            ceilingBlocks.add(player.getLocation().clone().add(-1,Crawling.yoffset,0).getBlock());
            if(nZ)
                ceilingBlocks.add(player.getLocation().clone().add(-1,Crawling.yoffset,-1).getBlock());
            else if(pZ)
                ceilingBlocks.add(player.getLocation().clone().add(-1,Crawling.yoffset,1).getBlock());
        }else if(pX){
            ceilingBlocks.add(player.getLocation().clone().add(1,Crawling.yoffset,0).getBlock());
            if(nZ)
                ceilingBlocks.add(player.getLocation().clone().add(1,Crawling.yoffset,-1).getBlock());
            else if(pZ)
                ceilingBlocks.add(player.getLocation().clone().add(1,Crawling.yoffset,1).getBlock());
        }
        if(nZ){
            ceilingBlocks.add(player.getLocation().clone().add(0,Crawling.yoffset,-1).getBlock());
            if(nX)
                ceilingBlocks.add(player.getLocation().clone().add(-1,Crawling.yoffset,-1).getBlock());
            else if(pX)
                ceilingBlocks.add(player.getLocation().clone().add(1,Crawling.yoffset,-1).getBlock());
        }else if(pZ){
            ceilingBlocks.add(player.getLocation().clone().add(0,Crawling.yoffset,1).getBlock());
            if(nX)
                ceilingBlocks.add(player.getLocation().clone().add(-1,Crawling.yoffset,1).getBlock());
            else if(pX)
                ceilingBlocks.add(player.getLocation().clone().add(1,Crawling.yoffset,1).getBlock());
        }
        for(Block block : new ArrayList<>(ceilingBlocks)){
            if(!block.isPassable())ceilingBlocks.remove(block);
        }
        List<Block> newblocks = new ArrayList<>(ceilingBlocks);
        for(Block block : oldblocks){
            if(ceilingBlocks.contains(block)){
                newblocks.remove(block);
                continue;
            }
            player.sendBlockChange(block.getLocation(),block.getBlockData());
            if(block.getState() instanceof Sign){
                Sign sign = (Sign) block.getState();
                if(sign.getColor() != null)
                    player.sendSignChange(block.getLocation(),sign.getLines(),sign.getColor());
                else
                    player.sendSignChange(block.getLocation(),sign.getLines());
            }
        }
        for(Block block : newblocks){
            player.sendBlockChange(block.getLocation(),ceilingMaterial.createBlockData());
        }
        return ceilingBlocks;
    }
}
