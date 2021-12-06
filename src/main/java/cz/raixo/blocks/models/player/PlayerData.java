package cz.raixo.blocks.models.player;

import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.util.UUID;

public class PlayerData {

    private String name;
    private String displayName;
    private UUID uuid;

    public PlayerData(Player player) {
        this.name = player.getName();
        this.displayName = player.getDisplayName();
        this.uuid = player.getUniqueId();
    }

    public PlayerData(String name, String displayName, UUID uuid) {
        this.name = name;
        this.displayName = displayName;
        this.uuid = uuid;
    }

    public PlayerData(JSONObject jsonObject) {
        this.name = jsonObject.getString("name");
        this.displayName = jsonObject.getString("displayName");
        this.uuid = UUID.fromString(jsonObject.getString("uuid"));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", this.name);
        jsonObject.put("displayName", this.displayName);
        jsonObject.put("uuid", this.uuid.toString());
        return jsonObject;
    }

}
