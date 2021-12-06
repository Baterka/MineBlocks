package cz.raixo.blocks.effects;

import cz.raixo.blocks.effects.executor.ExecutionToken;
import cz.raixo.blocks.models.MineBlock;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.function.Consumer;

public interface Effect {

    static void line(Location point1, Location point2, double space, Consumer<Location> consumer) {
        World world = point1.getWorld();
        if (!point2.getWorld().equals(world)) return;
        double distance = point1.distance(point2);
        Vector p1 = point1.toVector();
        Vector p2 = point2.toVector();
        Vector vector = p2.clone().subtract(p1).normalize().multiply(space);
        double length = 0;
        for (; length < distance; p1.add(vector)) {
            consumer.accept(new Location(world, p1.getX(), p1.getY(), p1.getZ()));
            length += space;
        }
    }

    void make(Block block, MineBlock mineBlock, List<Player> players, ExecutionToken executionToken);

}
