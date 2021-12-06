package cz.raixo.blocks.models.reward;

import cz.raixo.blocks.models.MineBlock;
import cz.raixo.blocks.models.player.PlayerData;
import org.bukkit.entity.Player;
import org.json.JSONObject;

public class PlayerRewardData {

    private final PlayerData playerData;
    private int breaks = 0;

    public PlayerRewardData(Player player) {
        this.playerData = new PlayerData(player);
    }

    public PlayerRewardData(JSONObject jsonObject) {
        this.playerData = new PlayerData(jsonObject.getJSONObject("playerData"));
        this.breaks = jsonObject.getInt("breaks");
    }

    public void addBreak() {
        breaks++;
    }

    public void reward(MineBlock mineBlock) {
        for (RewardSection reward : mineBlock.getRewards()) {
            reward.executeFor(playerData.getName(), breaks);
        }
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public int getBreaks() {
        return breaks;
    }

    public void setBreaks(int breaks) {
        this.breaks = breaks;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("playerData", this.playerData.toJson());
        jsonObject.put("breaks", this.breaks);
        return jsonObject;
    }

}
