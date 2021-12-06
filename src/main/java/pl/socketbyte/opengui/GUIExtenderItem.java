package pl.socketbyte.opengui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.socketbyte.opengui.event.ElementResponse;

public abstract class GUIExtenderItem implements ElementResponse {

    private final GUIItemBuilder itemBuilder;
    private boolean pullable;

    public GUIExtenderItem(GUIItemBuilder itemBuilder) {
        this.itemBuilder = itemBuilder;
    }

    public GUIExtenderItem() {
        this.itemBuilder = new GUIItemBuilder(Material.AIR);
    }

    // You can override this based on a player for example.
    public GUIItemBuilder getItemBuilder(Player player) {
        return itemBuilder;
    }

    public boolean isPullable() {
        return pullable;
    }

    public void setPullable(boolean pullable) {
        this.pullable = pullable;
    }

}