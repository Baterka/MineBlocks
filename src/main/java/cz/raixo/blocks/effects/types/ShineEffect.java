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
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public class ShineEffect implements Effect {

    private int side = 0;
    private Particle.DustOptions dustOptions;

    public ShineEffect(String argString) throws InvalidEffectArgumentsException {
        parseFromArgs(argString);
    }

    public static EffectEditor editor() {
        return new EffectEditor() {
            @Override
            public List<Pair<EffectOption, Object>> getEffectData(Effect effect) {
                if (!(effect instanceof ShineEffect)) return null;
                ShineEffect shineEffect = (ShineEffect) effect;
                Particle.DustOptions dustOptions = shineEffect.getDustOptions();
                Color color = dustOptions.getColor();
                return Arrays.asList(
                        new Pair<>(new DefaultOption(EffectOption.Type.COLOR, "Color"), color),
                        new Pair<>(new DefaultOption(EffectOption.Type.FLOAT, "Size"), dustOptions.getSize())
                );
            }

            @Override
            public void setEffectData(EffectOption option, Effect effect, String newData) {
                if (!(effect instanceof ShineEffect)) return;
                ShineEffect shineEffect = (ShineEffect) effect;
                switch (option.getName().toLowerCase(Locale.ROOT)) {
                    case "color": {
                        Particle.DustOptions dustOptions = shineEffect.getDustOptions();
                        try {
                            shineEffect.parseFromArgs(newData + "," + dustOptions.getSize());
                        } catch (InvalidEffectArgumentsException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case "size": {
                        Particle.DustOptions dustOptions = shineEffect.getDustOptions();
                        Color color = dustOptions.getColor();
                        try {
                            shineEffect.parseFromArgs(color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + newData);
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
            if (!(effect instanceof ShineEffect)) return null;
            ShineEffect shineEffect = (ShineEffect) effect;
            Particle.DustOptions dustOptions = shineEffect.getDustOptions();
            Color color = dustOptions.getColor();
            String s = "shine;";
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

    @Override
    public void make(Block block, MineBlock mineBlock, List<Player> players, ExecutionToken executionToken) {
        if (!players.isEmpty()) {
            executionToken.executeAfter(500);
            Material blockType = block.getType();
            Location blockLocation = block.getLocation();
            List<Pair<Location, Location>> pointsList = getPoints(blockLocation);
            for (Pair<Location, Location> points : pointsList) {
                if (executionToken.shouldStop()) return;
                Effect.line(points.getKey(), points.getValue(), 0.1, (location) -> {
                    for (Player player : players) {
                        spawnParticle(location, blockType, player);
                    }
                });
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            pointsList.clear();
        } else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            executionToken.executeNow();
        }
    }

    private void spawnParticle(Location location, Material material, Player player) {
        player.spawnParticle(Particle.REDSTONE, location, 1, dustOptions);
    }

    public List<Pair<Location, Location>> getPoints(Location block) {
        List<Pair<Location, Location>> points = new LinkedList<>();
        if (side == 0) {
            for (double i = 0; i < 1; i += 0.1) {
                points.add(new Pair<>(block.clone().add(1, 1 - i, 0), block.clone().add(1 - i, 1, 0)));
            }
            for (double i = .1; i <= 1; i += 0.1) {
                points.add(new Pair<>(block.clone().add(1 - i, 0, 0), block.clone().add(0, 1 - i, 0)));
            }
            side++;
        } else if (side == 1) {
            for (double i = 0; i < 1; i += 0.1) {
                points.add(new Pair<>(block.clone().add(0, 1 - i, 0), block.clone().add(0, 1, i)));
            }
            for (double i = .1; i <= 1; i += 0.1) {
                points.add(new Pair<>(block.clone().add(0, 0, i), block.clone().add(0, 1 - i, 1)));
            }
            side++;
        } else if (side == 2) {
            for (double i = 0; i < 1; i += 0.1) {
                points.add(new Pair<>(block.clone().add(0, 1 - i, 1), block.clone().add(i, 1, 1)));
            }
            for (double i = .1; i <= 1; i += 0.1) {
                points.add(new Pair<>(block.clone().add(i, 0, 1), block.clone().add(1, 1 - i, 1)));
            }
            side++;
        } else {
            for (double i = .1; i <= 1; i += 0.1) {
                points.add(new Pair<>(block.clone().add(1, 1 - i, 1), block.clone().add(1, 1, 1 - i)));
            }
            for (double i = 0; i < 1; i += 0.1) {
                points.add(new Pair<>(block.clone().add(1, 0, 1 - i), block.clone().add(1, 1 - i, 0)));
            }
            side = 0;
        }
        return points;
    }

    public Particle.DustOptions getDustOptions() {
        return dustOptions;
    }

    public void setDustOptions(Particle.DustOptions dustOptions) {
        this.dustOptions = dustOptions;
    }
}
