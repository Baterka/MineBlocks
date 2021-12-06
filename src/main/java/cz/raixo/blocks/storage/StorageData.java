package cz.raixo.blocks.storage;

import cz.raixo.blocks.models.MineBlock;
import cz.raixo.blocks.models.reward.PlayerRewardData;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StorageData {

    private long health;
    private List<PlayerRewardData> rewardData = new ArrayList<>();

    public StorageData(JSONObject jsonObject) {
        this.health = jsonObject.getInt("health");
        JSONArray rewardDataArray = jsonObject.getJSONArray("rewardData");
        for (Object o : rewardDataArray) {
            if (o instanceof JSONObject) {
                this.rewardData.add(new PlayerRewardData((JSONObject) o));
            }
        }
    }
    public StorageData(MineBlock mineBlock) {
        this.health = mineBlock.getHealth();
        this.rewardData = new ArrayList<>(mineBlock.getRewardData().values());
    }

    public long getHealth() {
        return health;
    }

    public void setHealth(long health) {
        this.health = health;
    }

    public JSONObject toJson() {
        JSONObject data = new JSONObject();
        data.put("health", this.health);
        JSONArray jsonArray = new JSONArray();
        for (PlayerRewardData reward : this.rewardData) {
            jsonArray.put(reward.toJson());
        }
        data.put("rewardData", jsonArray);
        return data;
    }

    public void cloneTo(MineBlock mineBlock) {
        for (PlayerRewardData reward : rewardData) {
            mineBlock.setRewardData(reward.getPlayerData().getUuid(), reward);
        }
        mineBlock.updateTopPlayers(true);
        mineBlock.setHealth(this.health);
    }

}
