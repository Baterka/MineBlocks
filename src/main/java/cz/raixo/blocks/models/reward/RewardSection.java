package cz.raixo.blocks.models.reward;

import cz.raixo.blocks.util.SimpleRandom;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RewardSection {

    private final List<Reward> rewards = new LinkedList<>();
    private int from;
    private int to;
    private String name;

    public RewardSection(int from, int to, String name) {
        this.from = from;
        this.to = to;
        this.name = name;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addReward(Reward reward) {
        this.rewards.add(reward);
    }

    public void clearReward() {
        this.rewards.clear();
    }

    public void executeFor(PlayerRewardData data) {
        int breaks = data.getBreaks();
        if (breaks <= this.to && breaks > this.from) {
            if (!this.rewards.isEmpty()) {
                SimpleRandom<Reward> rewards = new SimpleRandom<>();
                for (Reward reward : this.rewards) {
                    rewards.add(reward.getChance(), reward);
                }
                rewards.next().executeFor(data);
            }
        }

    }

    public List<String> toStringList() {
        List<String> list = new LinkedList<>();
        for (Reward reward : this.rewards) {
            list.add(reward.toString());
        }
        return list;
    }

    public List<Reward> getRewards() {
        return new ArrayList<>(rewards);
    }

    public void removeReward(Reward reward) {
        this.rewards.remove(reward);
    }

}
