package cz.raixo.blocks.storage;

import cz.raixo.blocks.models.MineBlock;
import cz.raixo.blocks.models.reward.PlayerRewardData;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StorageData {

    private long health;
    private List<PlayerRewardData> rewardData = new ArrayList<>();
    private List<PlayerRewardData> lastTop;
    private Date blockedUntil;

    public StorageData(JSONObject jsonObject) {
        this.health = jsonObject.getInt("health");
        this.blockedUntil = Optional.of(jsonObject.optLong("blockedUntil")).map(Date::new).filter(date -> date.getTime() > System.currentTimeMillis()).orElse(null);
        JSONArray rewardDataArray = jsonObject.getJSONArray("rewardData");
        for (Object o : rewardDataArray) {
            if (o instanceof JSONObject) {
                this.rewardData.add(new PlayerRewardData((JSONObject) o));
            }
        }
        if (jsonObject.has("lastTop")) {
            this.lastTop = new ArrayList<>();
            for (Object o : jsonObject.getJSONArray("lastTop")) {
                if (o instanceof JSONObject) {
                    lastTop.add(new PlayerRewardData((JSONObject) o));
                }
            }
        }
    }
    public StorageData(MineBlock mineBlock) {
        this.health = mineBlock.getHealth();
        this.rewardData = new ArrayList<>(mineBlock.getRewardData().values());
        this.blockedUntil = mineBlock.getBlockedUntil();
        this.lastTop = mineBlock.getTopPlayers();
    }

    public JSONObject toJson() {
        JSONObject data = new JSONObject();
        data.put("health", this.health);
        JSONArray jsonArray = new JSONArray();
        for (PlayerRewardData reward : this.rewardData) {
            jsonArray.put(reward.toJson());
        }
        data.put("rewardData", jsonArray);
        if (blockedUntil != null && blockedUntil.getTime() > System.currentTimeMillis()) {
            data.put("blockedUntil", blockedUntil.getTime());
        }
        if (lastTop != null) {
            JSONArray lastTopArray = new JSONArray();
            for (PlayerRewardData reward : this.lastTop) {
                lastTopArray.put(reward.toJson());
            }
            data.put("lastTop", lastTopArray);
        }
        return data;
    }

    public void cloneTo(MineBlock mineBlock) {
        for (PlayerRewardData reward : rewardData) {
            mineBlock.setRewardData(reward.getPlayerData().getUuid(), reward);
        }
        mineBlock.updateTopPlayers(true);
        mineBlock.setHealth(this.health);
        mineBlock.setBlockedUntil(this.blockedUntil);
        mineBlock.setLastTopPlayers(lastTop);
    }

}
