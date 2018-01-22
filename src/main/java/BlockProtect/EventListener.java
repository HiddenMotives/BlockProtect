package BlockProtect;

import BlockProtect.Storage.Areas;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityExplodeEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EventListener implements Listener {

    private int radius;
    private int maximumprotections;
    private int protectionblock;
    private BlockProtect plugin;

    public EventListener(BlockProtect plugin) {
        this.plugin = plugin;
        this.radius = plugin.config.getInt("protection-radius");
        this.maximumprotections = plugin.config.getInt("maxium-protections");
        this.protectionblock = plugin.config.getInt("protection-blockid");
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onExplode(EntityExplodeEvent event) {
        List list = event.getBlockList();

        for (Iterator<Block> i = list.iterator(); i.hasNext();) {
            Block block = i.next();
            if(blockInProtection(block.getLocation())) {
                event.setCancelled(true);
            }

        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (playerInRangeProtection(block.getLocation(), player)) {
            player.sendMessage(plugin.msg("You can't use: " + block.getName() + " in this area."));
            event.setCancelled(true);
        }


    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (block.getId() == protectionblock) {
            if(playerRemoveProtection(block.getLocation(), player)) {
//                removeCircle(ProtectionArea(block.getLocation()), block.getLevel());
                player.sendMessage(plugin.msg("You have broken your protection block!"));
            } else {
                if (playerInRangeProtection(block.getLocation(), player)) {
                    player.sendMessage(plugin.msg("You can't break that protection block!"));
                    event.setCancelled(true);
                }
            }
        } else if (playerInRangeProtection(block.getLocation(), player)) {
            event.setCancelled(true);
        }

    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (block.getId() == protectionblock) {
            int numPlayerProtections = getNumPlayerProtections(player);
            if (numPlayerProtections >= maximumprotections) {
                player.sendMessage(plugin.msg("You have already placed too many protection"
                        + " blocks! Remove some to place more."));
                event.setCancelled(true);
            } else {
                if (playerInRangeProtection(block.getLocation(), player)) {
                    event.setCancelled(true);
                } else {

                    List protectionarea = ProtectionArea(block.getLocation());
                    boolean isProtected = false;
                    for (Iterator<Location> i = protectionarea.iterator(); i.hasNext();) {
                        if(blockInProtection(i.next())) {
                            isProtected = true;
                            break;
                        }
                    }

                    if(isProtected) {
                        player.sendMessage(plugin.msg("Your protection overlaps "
                                + "another protected area, move away further."));
                        event.setCancelled(true);
                    } else {
                        int remaining = ((maximumprotections - numPlayerProtections)-1);
                        playerPlaceProtection(block.getLocation(), player);
//                        makeCircle(protectionarea, block.getLevel());
                        player.sendMessage(plugin.msg("You have placed a protection block!"));
                        player.sendMessage(plugin.msg("You have " + remaining
                                + " protection areas remaining."));
                        player.sendMessage(plugin.msg("A protection radius of "
                                + radius + " blocks has been created around your protection block"));
//                        player.sendPopup(plugin.msg("The area inside the glass circle is protected."));
                    }
                }
            }
        } else if (playerInRangeProtection(block.getLocation(), player)) {
            event.setCancelled(true);
        }
    }

    /**
     * Get the number of total player protections
     * @param player = Player
     * @return int - num protections
     */
    public int getNumPlayerProtections(Player player) {
        List list;
        try {
            list = (List) plugin.map.get(player.getName());
            return list.size();
        } catch (NullPointerException e) {
            plugin.map.put(player.getName(), new ArrayList<>());
            return 0;
        }
    }

    /**
     * Place a protection block
     * @param loc
     * @param player
     */
    public void playerPlaceProtection(Location loc, Player player) {
        List list = (List) plugin.map.get(player.getName());
        list.add(new Areas(loc));
        plugin.map.put(player.getName(), list);
    }

    /**
     * Verify and remove the protection block the player is trying to remove.
     * @param loc - Location of the Protection Block
     * @param player - Player attempting to remove
     * @return bool
     */
    public boolean playerRemoveProtection(Location loc, Player player) {
        Set set = plugin.map.entrySet();
        Iterator i = set.iterator();

        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();
            List list = (List) me.getValue();
            for (int index = 0, d = list.size(); index < d; index++) {
                Areas area = (Areas) list.get(index);
                Location x = area.getLocation();
                if ((x.equals(loc)) && (me.getKey().equals(player.getName()))) {
                    list.remove(area);
                    plugin.map.put(player.getName(), list);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Same as block method blockInProtection but determines by boolean value
     * if player can build or not.
     * @param loc - Location being compared to.
     * @param player - Player trying to access
     * @return bool
     */
    public boolean playerInRangeProtection(Location loc, Player player) {
        Set set = plugin.map.entrySet();
        Iterator i = set.iterator();

        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();
            List list = (List) me.getValue();
            for (int index = 0, d = list.size(); index < d; index++) {
                Areas area = (Areas)list.get(index);
                Location x = area.getLocation();
                if (loc.distance(x) < radius) {
                    if(me.getKey().equals(player.getName())) {
                        return false;
                    } else {
                        player.sendPopup(plugin.msg("You are in a protected area owned by: " + me.getKey()));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check if a block is protected by a protection block based on a given
     * location
     * @param loc - Location being compared to.
     * @return bool
     */
    public boolean blockInProtection(Location loc) {
        Set set = plugin.map.entrySet();
        Iterator i = set.iterator();

        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();
            List list = (List) me.getValue();
            for (int index = 0, d = list.size(); index < d; index++) {
                Areas area = (Areas)list.get(index);
                Location x = area.getLocation();
                if(loc.distance(x) < radius) {
                    return true;
                }
            }
        }
        return false;

    }
    /**
     * Grabs the outer blocks for the protected area forming a circle
     * @param loc - Location of the protected block
     * @return list - List of outer blocks in the circle
     */
    public List ProtectionArea(Location loc) {
        List<Location> list = new ArrayList<>();

        double x;
        double y = loc.y;
        double z;

        for (double i = 0.0; i < 360.0; i += 0.1) {
        double angle = i * Math.PI / 180;
            x = (loc.getX() + radius * Math.cos(angle));
            z = (loc.getZ() + radius * Math.sin(angle));
            list.add(new Location(x,y,z));
        }

        return list;
    }

    /**
     * Makes a glass circle around the protected block of the radius
     * specified in the config.yml
     *
     * @param list - The list of outer blocks in the circle
     * @param level - The level the block is in.
     */
    public void makeCircle(List list, Level level) {
        Block block = Block.get(20);

        for (Iterator<Location> i = list.iterator(); i.hasNext();) {
            Location loc = i.next();
            Block current = level.getBlock(loc);

            if(current.getId() == 0) {
//                level.setBlock(loc, block);
            }
        }
    }

    /**
     * Removes the glass circle made around the protected block
     * @param list - The list of outer blocks in the circle
     * @param level - The level the block is in.
     */
    public void removeCircle(List list, Level level) {
        Block block = Block.get(0);

        for (Iterator<Location> i = list.iterator(); i.hasNext();) {
            Location loc = i.next();
            Block current = level.getBlock(loc);

            if(current.getId() == 20) {
//                level.setBlock(loc, block);
            }
        }
    }

}
