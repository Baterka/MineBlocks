package cz.raixo.blocks.effects.types;

import cz.raixo.blocks.effects.Effect;
import cz.raixo.blocks.effects.exceptions.InvalidEffectArgumentsException;
import cz.raixo.blocks.effects.executor.ExecutionToken;
import cz.raixo.blocks.effects.options.DefaultOption;
import cz.raixo.blocks.effects.options.EffectOption;
import cz.raixo.blocks.effects.register.EffectData;
import cz.raixo.blocks.effects.register.EffectEditor;
import cz.raixo.blocks.models.MineBlock;
import cz.raixo.blocks.util.NumberUtil;
import cz.raixo.blocks.util.Pair;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public class CircleComboEffect implements Effect {

    private Particle.DustOptions dustOptions;

    public CircleComboEffect(String argString) throws InvalidEffectArgumentsException {
        parseFromArgs(argString);
    }

    public static EffectEditor editor() {
        return new EffectEditor() {
            @Override
            public List<Pair<EffectOption, Object>> getEffectData(Effect effect) {
                if (!(effect instanceof CircleComboEffect)) return null;
                CircleComboEffect circleCombo = (CircleComboEffect) effect;
                Particle.DustOptions dustOptions = circleCombo.getDustOptions();
                Color color = dustOptions.getColor();
                return Arrays.asList(
                        new Pair<>(new DefaultOption(EffectOption.Type.COLOR, "Color"), color),
                        new Pair<>(new DefaultOption(EffectOption.Type.FLOAT, "Size"), dustOptions.getSize())
                );
            }

            @Override
            public void setEffectData(EffectOption option, Effect effect, String newData) {
                if (!(effect instanceof CircleComboEffect)) return;
                CircleComboEffect circleCombo = (CircleComboEffect) effect;
                switch (option.getName().toLowerCase(Locale.ROOT)) {
                    case "color": {
                        Particle.DustOptions dustOptions = circleCombo.getDustOptions();
                        try {
                            circleCombo.parseFromArgs(newData + "," + dustOptions.getSize());
                        } catch (InvalidEffectArgumentsException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case "size": {
                        Particle.DustOptions dustOptions = circleCombo.getDustOptions();
                        Color color = dustOptions.getColor();
                        try {
                            circleCombo.parseFromArgs(color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + newData);
                        } catch (InvalidEffectArgumentsException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        };
    }

    public static EffectData.EffectSaver effectSaver() {
        return effect -> {
            if (!(effect instanceof CircleComboEffect)) return null;
            CircleComboEffect circleCombo = (CircleComboEffect) effect;
            Particle.DustOptions dustOptions = circleCombo.getDustOptions();
            Color color = dustOptions.getColor();
            String s = "circlecombo;";
            s += color.getRed() + ",";
            s += color.getGreen() + ",";
            s += color.getBlue() + ",";
            s += dustOptions.getSize();
            return s;
        };
    }

    public void parseFromArgs(String argString) throws InvalidEffectArgumentsException {
        String[] args = argString.replace(" ", "").split(",", 4);
        if (args.length < 4) {
            throw new InvalidEffectArgumentsException();
        }
        Optional<Integer> r = NumberUtil.parseInt(args[0]);
        Optional<Integer> g = NumberUtil.parseInt(args[1]);
        Optional<Integer> b = NumberUtil.parseInt(args[2]);
        Optional<Float> size = NumberUtil.parseFloat(args[3]);
        if (r.isEmpty() || g.isEmpty() || b.isEmpty() || size.isEmpty()) {
            throw new InvalidEffectArgumentsException();
        }
        Color color = Color.fromRGB(r.get(), g.get(), b.get());
        this.dustOptions = new Particle.DustOptions(color, size.get());
    }

    public Particle.DustOptions getDustOptions() {
        return dustOptions;
    }

    private void spawnParticle(Location location, Player player) {
        player.spawnParticle(Particle.REDSTONE, location, 1, dustOptions);
    }

    @Override
    public void make(Block block, MineBlock mineBlock, List<Player> players, ExecutionToken executionToken) {
        try {
            if (players.size() > 0) {
                Location blockLoc = block.getLocation().clone();
                Location center = blockLoc.clone().add(0.5, 0.5, 0.5);
                render(blockLoc, center, mineBlock, players, executionToken);
            } else {
                executionToken.executeAfter(1000);
                return;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        executionToken.executeNow();
    }

    private Location rotateHorizontal(Location v, Location center, double angle) {
        Vector x = v.clone()
                .subtract(center.toVector())
                .toVector();
        x.multiply(new Vector(1, 0, 1));
        return x.rotateAroundX(angle)
                .add(center.toVector())
                .toLocation(v.getWorld());
    }

    private Location rotateVertical(Location v, Location center, double angle) {
        Vector x = v.clone()
                .subtract(center.toVector())
                .toVector();
        x.multiply(new Vector(0, 1, 1));
        return x.rotateAroundY(angle)
                .add(center.toVector())
                .toLocation(v.getWorld());
    }


    public void render(Location block, Location center, MineBlock mineBlock, List<Player> players, ExecutionToken executionToken) {
        double points = Math.PI / 40;
        List<Location> locations1 = new ArrayList<>();
        List<Location> locations2 = new ArrayList<>();
        for (double i = 0; i < Math.PI * 2; i += points) {
            locations1.add(center.clone().add(Math.sin(i) * .85, 0, Math.cos(i) * .85));
            locations2.add(center.clone().add(0, Math.cos(i) * .85, Math.sin(i) * .85));
        }
        for (double i = 0; i < Math.PI * 2; i += points) {
            if (executionToken.shouldStop()) return;
            for (Location location : locations1) {
                Location loc = rotateHorizontal(location, center, i);
                for (Player player : players) {
                    spawnParticle(loc, player);
                }
            }
            for (Location location : locations2) {
                Location loc = rotateVertical(location, center, i);
                for (Player player : players) {
                    spawnParticle(loc, player);
                }
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
