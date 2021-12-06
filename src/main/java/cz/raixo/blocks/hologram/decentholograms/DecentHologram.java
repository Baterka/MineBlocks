package cz.raixo.blocks.hologram.decentholograms;

import cz.raixo.blocks.hologram.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class DecentHologram implements Hologram {

    private final eu.decentsoftware.holograms.api.holograms.Hologram defaultHologram;

    public DecentHologram(Location location) {
        this.defaultHologram = new eu.decentsoftware.holograms.api.holograms.Hologram("mb_hologram", location);
    }


    @Override
    public void show(Player... players) {
        for (Player player : players) {
            defaultHologram.show(player, 0);
        }
    }

    @Override
    public void hide(Player... players) {
        for (Player player : players) {
            defaultHologram.hide(player);
        }
    }

    @Override
    public String getLine(int i) {
        return defaultHologram.getPage(0).getLine(i).getContent();
    }

    @Override
    public void setLine(int i, String content) {
        defaultHologram.getPage(0).getLine(i).setContent(content);
    }

    @Override
    public void addLine(String content) {
        defaultHologram.getPage(0).addLine(new HologramLine(defaultHologram.getPage(0), defaultHologram.getLocation(), content));
    }

    @Override
    public void delete() {
        defaultHologram.delete();
    }

    @Override
    public void realignLines() {
        defaultHologram.realignLines();
    }

    @Override
    public Location getLocation() {
        return defaultHologram.getLocation();
    }

    @Override
    public void setLocation(Location location) {
        defaultHologram.setLocation(location);
        defaultHologram.realignLines();
        defaultHologram.updateAll();
    }

    @Override
    public double getHeight() {
        defaultHologram.getPage(0).realignLines();
        return defaultHologram.getPage(0).getHeight();
    }

}
