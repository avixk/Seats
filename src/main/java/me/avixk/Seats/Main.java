package me.avixk.Seats;

import me.avixk.Seats.Crawling.Crawling;
import me.avixk.Seats.Crawling.CrawlingListeners;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;
import sun.awt.Win32GraphicsConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Main extends JavaPlugin implements Listener {
    public static Plugin plugin;
    static List<ArmorStand> seatStands = new ArrayList<>();
    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new CrawlingListeners(), this);
    }

    @Override
    public void onDisable() {
        for(Player p : Bukkit.getOnlinePlayers()){
            Crawling.stopCrawling(p);
        }
        for(ArmorStand stand : seatStands){
            stand.eject();
            stand.remove();
        }
        for(World w : Bukkit.getWorlds()){
            for(Entity e : w.getEntities()){
                if(e instanceof ArmorStand){
                    ArmorStand arm = (ArmorStand) e;
                    if(arm.getCustomName() != null)
                    if(arm.getCustomName().equals("Seat")){
                        arm.remove();
                    }
                }
            }
        }
    }
    static HashMap<String, String> awaitingClick = new HashMap<>();
    static HashMap<String,Entity> targettedEntities = new HashMap<>();
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage("This is not a console command.");
            return true;
        }
        Player player = (Player) sender;
        if(label.equalsIgnoreCase("sit") || label.equalsIgnoreCase("seat")){
            if(!sender.hasPermission("seats.sit")){
                sender.sendMessage("You do not have permission to run this command.");
                return true;
            }
            if(player.isInsideVehicle() && player.getVehicle() instanceof ArmorStand){
                player.getVehicle().eject();
                player.sendMessage("§7No longer sitting");
            }else {
                if(player.isOnGround()){
                    spawnStand(player,player.getLocation().add(0,-.9,0));
                    player.sendMessage("§7Now sitting");
                }else{
                    player.sendMessage("§cYou need to be on the ground to run this command.");
                }
            }
        }else if(label.equalsIgnoreCase("ride")){
            if(!sender.hasPermission("seats.ride")){
                sender.sendMessage("You do not have permission to run this command.");
                return true;
            }
            if(awaitingClick.containsKey(player.getName()) && awaitingClick.get(player.getName()).equals("RIDE")){
                awaitingClick.remove(player.getName());
                player.sendMessage("Right click to ride disabled.");
            }else{
                awaitingClick.put(player.getName(),"RIDE");
                player.sendMessage("Right click an entity to ride it.");
            }
        }else if(label.equalsIgnoreCase("eject")){
            if(!sender.hasPermission("seats.eject")){
                sender.sendMessage("You do not have permission to run this command.");
                return true;
            }
            String action = "EJECT";
            if(args.length == 0){
                if(player.getVehicle()!=null)player.getVehicle().eject();
                player.eject();
            }else if(args.length == 1){
                if(args[0].equalsIgnoreCase("all")){
                    if(player.getVehicle()!=null)player.getVehicle().eject();
                    player.eject();
                }else if(args[0].equalsIgnoreCase("vehicle")){
                    if(player.getVehicle()!=null)player.getVehicle().eject();
                }else if(args[0].equalsIgnoreCase("passenger")){
                    player.eject();
                }else{
                    return false;
                }
            }else if(args.length > 1){
                return false;
            }
            sender.sendMessage("§aEjecting.");
        }else if(label.equalsIgnoreCase("ejectother")){
            if(!sender.hasPermission("seats.ejectother")){
                sender.sendMessage("§cYou do not have permission to run this command.");
                return true;
            }
            String action = "EJECT";
            if(args.length == 1){
                if(args[0].equalsIgnoreCase("all")){

                }else if(args[0].equalsIgnoreCase("vehicle")){
                    action += "_VEHICLE";
                }else if(args[0].equalsIgnoreCase("passenger")){
                    action += "_PASSENGER";
                }else{
                    return false;
                }
            }else if(args.length > 1){
                return false;
            }
            if(awaitingClick.containsKey(player.getName()) && awaitingClick.get(player.getName()).equals(action)){
                awaitingClick.remove(player.getName());
                player.sendMessage("§aRight click to eject §cdisabled§a.");
            }else{
                awaitingClick.put(player.getName(),action);
                player.sendMessage("§aRight click an entity to eject it.");
            }
        }else if(label.equalsIgnoreCase("wear")){
            if(!sender.hasPermission("seats.wear")){
                sender.sendMessage("§cYou do not have permission to run this command.");
                return true;
            }
            if(args.length > 0)return false;
            if(awaitingClick.containsKey(player.getName()) && awaitingClick.get(player.getName()).equals("WEAR")){
                awaitingClick.remove(player.getName());
                player.sendMessage("§aRight click to wear §cdisabled.");
            }else{
                awaitingClick.put(player.getName(),"WEAR");
                player.sendMessage("§aRight click an entity to wear it.");
            }
        }else if(label.equalsIgnoreCase("stack")){
            if(!sender.hasPermission("seats.stack")){
                sender.sendMessage("§cYou do not have permission to run this command.");
                return true;
            }
            if(args.length > 0)return false;
            if(awaitingClick.containsKey(player.getName()) && awaitingClick.get(player.getName()).startsWith("STACK")){
                awaitingClick.remove(player.getName());
                player.sendMessage("§aRight click to stack §cdisabled§a.");
            }else{
                awaitingClick.put(player.getName(),"STACK_1");
                player.sendMessage("§aRight click the entity you want to put on top.");
            }
        }else if(label.equalsIgnoreCase("crawl")){
            if(!sender.hasPermission("seats.crawl")){
                sender.sendMessage("§cYou do not have permission to run this command.");
                return true;
            }
            if(args.length > 0)return false;
            if(Crawling.isCrawling(player)){
                Crawling.stopCrawling(player);
                player.sendMessage("§7No longer crawling");
            }else{
                Crawling.beginCrawling(player);
                player.sendMessage("§7Now crawling");
            }
        }
        return true;
    }
    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        if(e.getPlayer().getVehicle() != null){
            if(seatStands.contains(e.getPlayer().getVehicle())){
                seatStands.remove(e.getPlayer().getVehicle());
                e.getPlayer().getVehicle().remove();
                e.getPlayer().teleport(e.getPlayer().getLocation().add(0,1.6,0));
            }
            Crawling.stopCrawling(e.getPlayer());
        }
    }
    final List<EntityDamageEvent.DamageCause> blacklisted_causes = Arrays.asList(EntityDamageEvent.DamageCause.FIRE_TICK, EntityDamageEvent.DamageCause.STARVATION, EntityDamageEvent.DamageCause.FIRE, EntityDamageEvent.DamageCause.WITHER, EntityDamageEvent.DamageCause.THORNS, EntityDamageEvent.DamageCause.CONTACT, EntityDamageEvent.DamageCause.POISON);
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent e){
        if(e.isCancelled())return;
        if(e.getEntity().getVehicle() != null && seatStands.contains(e.getEntity().getVehicle())){
            if(blacklisted_causes.contains(e.getCause()))return;
            //Vector velocity = e.getEntity().getVelocity();
            //e.getEntity().getVehicle().eject();

            if(e instanceof EntityDamageByEntityEvent){
                EntityDamageByEntityEvent ee = (EntityDamageByEntityEvent) e;
                if(ee.getDamager() instanceof Projectile){
                    Vector velocity = ee.getDamager().getVelocity().multiply(0.2);
                    dismount(e.getEntity(),e.getEntity().getVehicle(),velocity);
                    return;
                }else if(ee.getDamager() instanceof LivingEntity){
                    Vector velocity = ((LivingEntity) ee.getDamager()).getEyeLocation().getDirection().multiply(0.5);
                    dismount(e.getEntity(),e.getEntity().getVehicle(),velocity);
                    return;
                }
            }
            dismount(e.getEntity(), e.getEntity().getVehicle(),e.getEntity().getVelocity());
        }
    }

    @EventHandler
    public void onDismount(EntityDismountEvent e){
        if(ejecting != null && e.getDismounted().equals(ejecting))return;
        dismount( e.getEntity(), e.getDismounted(),null);
    }

    public static Entity ejecting = null;
    public static void dismount(Entity entity, Entity vehicle, Vector velocity){
        if (vehicle != null) {
            if (seatStands.contains(vehicle)) {

                ejecting = vehicle;
                vehicle.eject();
                ejecting = null;

                seatStands.remove(vehicle);
                vehicle.remove();
                Location endloc = entity.getLocation().clone();
                endloc = endloc.add(0, 0.8, 0);
                Location finalEndloc = endloc;
                Bukkit.getScheduler().runTaskLater(Main.plugin, new Runnable() {
                    @Override
                    public void run() {
                        if (entity.getVehicle() == null){
                            if (entity.getWorld().equals(finalEndloc.getWorld())
                                    && finalEndloc.distance(entity.getLocation()) < 3) {
                                entity.teleport(finalEndloc);
                                if (velocity != null) entity.setVelocity(velocity);
                            }
                        }
                    }
                }, 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityInteract(PlayerInteractEntityEvent e){
        if(awaitingClick.containsKey(e.getPlayer().getName())){
            if(e.getPlayer().isSneaking())return;
            e.setCancelled(true);
            Entity currentEnt = e.getRightClicked();
            String command = awaitingClick.get(e.getPlayer().getName());
            if(command.equals("RIDE")){
                currentEnt.setPassenger(e.getPlayer());
                awaitingClick.remove(e.getPlayer().getName());
                e.getPlayer().sendMessage("§d§oZoop");
            }else if(command.startsWith("EJECT")){
                if(command.endsWith("VEHICLE")){
                    if(currentEnt.getVehicle()!=null)currentEnt.getVehicle().eject();
                }else if(command.endsWith("PASSENGER")){
                    currentEnt.eject();
                }else{
                    currentEnt.eject();
                    if(currentEnt.getVehicle()!=null)currentEnt.getVehicle().eject();
                }
                awaitingClick.remove(e.getPlayer().getName());
                e.getPlayer().sendMessage("§2§oZoop");
            }else if(command.equals("WEAR")){
                e.getPlayer().setPassenger(currentEnt);
                awaitingClick.remove(e.getPlayer().getName());
                e.getPlayer().sendMessage("§b§oZoop");
            }else if(command.startsWith("STACK")){
                if(command.endsWith("1")){
                    targettedEntities.put(e.getPlayer().getName(),currentEnt);
                    awaitingClick.put(e.getPlayer().getName(),"STACK_2");
                    e.getPlayer().sendMessage("§aNow right click the entity you want to put it on.");
                }else if(command.endsWith("2")){
                    awaitingClick.remove(e.getPlayer().getName());
                    Entity entity = !targettedEntities.containsKey(e.getPlayer().getName())?null:targettedEntities.get(e.getPlayer().getName());
                    if(entity == null || entity.isDead()){
                        awaitingClick.put(e.getPlayer().getName(),"STACK_1");
                        e.getPlayer().sendMessage("§cThe original entity died, please pick a new one.");
                    }else {
                        awaitingClick.remove(e.getPlayer().getName());
                        currentEnt.setPassenger(entity);
                        e.getPlayer().sendMessage("§5§oZoop");
                    }
                }
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent e){
        if(e.isCancelled())return;
        Vector loc = e.getBlock().getLocation().clone().add(.5,0.35,.5).toVector();
        for(ArmorStand stand : new ArrayList<>(seatStands)){
            if(stand.getLocation().toVector().equals(loc)){
                stand.remove();
                seatStands.remove(stand);
            }
        }
    }
    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e){
        if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            if(e.getPlayer().isSneaking())return;
            if(e.getItem() == null){
                if(e.getClickedBlock().getBlockData() instanceof Stairs){
                    Block target = (e.getPlayer().getTargetBlockExact(10, FluidCollisionMode.NEVER));
                    if(target == null || !(target.getBlockData() instanceof Stairs)){
                        return;
                    }
                    Location loc = e.getClickedBlock().getLocation().clone().add(.5,-0.4,.5);
                    if(e.getClickedBlock().getBlockData() instanceof Stairs){
                        Stairs stairs = (Stairs)e.getClickedBlock().getBlockData();
                        if(stairs.getHalf().equals(Bisected.Half.TOP))return;
                        if(!e.getClickedBlock().getRelative(0,1,0).isPassable())return;
                        if(e.getClickedBlock().getRelative(stairs.getFacing().getOppositeFace()).getType().isOccluding())return;

                        if(stairs.getFacing().equals(BlockFace.NORTH)){
                            loc.setYaw(0);
                        }else if(stairs.getFacing().equals(BlockFace.EAST)){
                            loc.setYaw(90);
                        }else if(stairs.getFacing().equals(BlockFace.SOUTH)){
                            loc.setYaw(180);
                        }else if(stairs.getFacing().equals(BlockFace.WEST)){
                            loc.setYaw(270);
                        }
                    }
                    for(ArmorStand s : seatStands){
                        if(s.getLocation().equals(loc.clone().add(0,0.75,0))){
                            return;
                        }
                    }
                    spawnStand(e.getPlayer(),loc);
                }
            }
        }
    }

    @EventHandler
    public void onSitAnywhere(PlayerInteractEvent e){
        if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            if(e.getPlayer().isSneaking())return;
            if(e.getItem() == null){
                if(getConfig().getBoolean("look_down_and_right_click_to_sit_on_anything")){
                    if(!e.getClickedBlock().getType().isInteractable() || e.getClickedBlock().getBlockData() instanceof Stairs){
                        if(e.getPlayer().getLocation().getPitch() == 90){
                            if(e.getPlayer().isOnGround()){
                                Crawling.stopCrawling(e.getPlayer());
                                spawnStand(e.getPlayer(),e.getPlayer().getLocation().add(0,-.9,0));
                            }
                        }
                    }
                }
            }
        }

    }


    public ArmorStand spawnStand(Player p, Location loc){
        loc = loc.clone().add(0,.75,0);
        ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setMarker(true);
        });
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setCustomNameVisible(false);
        stand.setCustomName("Seat");
        seatStands.add(stand);
        p.setGliding(false);
        stand.addPassenger(p);
        return stand;
    }
}
