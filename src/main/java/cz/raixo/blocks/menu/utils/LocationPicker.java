package cz.raixo.blocks.menu.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class LocationPicker implements Listener {

    private static LocationPicker instance;
    private final Map<Player, Consumer<Location>> queue = new HashMap<>();

    public LocationPicker() {
        instance = this;
    }

    public static LocationPicker getInstance() {
        return instance;
    }

    public void pickLocation(Player player, Consumer<Location> locationConsumer) {
        this.queue.put(player, locationConsumer);
    }

    @EventHandler
    public void on(PlayerInteractEvent event) {
        if (queue.containsKey(event.getPlayer())) {
            if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                Block block = event.getClickedBlock();
                if (block == null) return;
                event.setCancelled(true);
                queue.get(event.getPlayer()).accept(block.getLocation());
                queue.remove(event.getPlayer());
            }
        }
    }

}
