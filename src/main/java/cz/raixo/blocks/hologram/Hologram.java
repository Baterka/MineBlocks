package cz.raixo.blocks.hologram;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface Hologram {

    void show(Player... players);

    void hide(Player... players);

    String getLine(int i);

    void setLine(int i, String content);

    void addLine(String content);

    void delete();

    void realignLines();

    Location getLocation();

    void setLocation(Location location);

    double getHeight();

    void refreshAllLines();

}
