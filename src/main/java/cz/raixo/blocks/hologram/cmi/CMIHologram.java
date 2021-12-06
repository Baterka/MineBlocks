package cz.raixo.blocks.hologram.cmi;

import com.Zrips.CMI.CMI;
import cz.raixo.blocks.hologram.Hologram;
import net.Zrips.CMILib.Container.CMILocation;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CMIHologram implements Hologram {

    private final com.Zrips.CMI.Modules.Holograms.CMIHologram cmiHologram;

    public CMIHologram(Location location) {
        this.cmiHologram = new com.Zrips.CMI.Modules.Holograms.CMIHologram("mb_hologram", new CMILocation(location));
        CMI.getInstance().getHologramManager().addHologram(cmiHologram);
        cmiHologram.update();
    }

    @Override
    public void show(Player... players) {

    }

    @Override
    public void hide(Player... players) {

    }

    @Override
    public String getLine(int i) {
        return cmiHologram.getLine(i);
    }

    @Override
    public void setLine(int i, String content) {
        cmiHologram.setLine(i, content);
    }

    @Override
    public void addLine(String content) {
        cmiHologram.addLine(content);
        cmiHologram.refresh();
    }

    @Override
    public void delete() {
        cmiHologram.remove();
    }

    @Override
    public void realignLines() {

    }

    @Override
    public Location getLocation() {
        return cmiHologram.getLocation().getBukkitLoc();
    }

    @Override
    public void setLocation(Location location) {
        cmiHologram.setLoc(location);
        cmiHologram.refresh();
    }

    @Override
    public double getHeight() {
        return cmiHologram.getHeight();
    }

}
