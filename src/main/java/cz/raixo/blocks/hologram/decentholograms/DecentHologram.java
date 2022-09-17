package cz.raixo.blocks.hologram.decentholograms;

import cz.raixo.blocks.hologram.Hologram;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class DecentHologram implements Hologram {

    private static final AtomicInteger ID = new AtomicInteger();
    private final eu.decentsoftware.holograms.api.holograms.Hologram defaultHologram;

    public DecentHologram(Location location) {
        this.defaultHologram = DHAPI.createHologram("mb_hologram_" + ID.getAndIncrement(), location);
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
        DHAPI.setHologramLine(defaultHologram.getPage(0).getLine(i), content);
    }

    @Override
    public void addLine(String content) {
        DHAPI.addHologramLine(defaultHologram, content);
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
        DHAPI.moveHologram(defaultHologram, location);
    }

    @Override
    public double getHeight() {
        defaultHologram.getPage(0).realignLines();
        return defaultHologram.getPage(0).getHeight();
    }

    @Override
    public void refreshAllLines() {
        HologramPage hologramPage = defaultHologram.getPage(0);
        List<HologramLine> hologramLines = hologramPage.getLines();
        for (HologramLine line : hologramLines) {
            line.setContent(line.getContent());
        }
    }

}
