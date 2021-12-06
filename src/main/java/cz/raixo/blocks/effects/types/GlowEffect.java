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

import java.util.*;
import java.util.function.Consumer;

public class GlowEffect implements Effect {

    private Particle.DustOptions dustOptions;

    public GlowEffect(String argString) throws InvalidEffectArgumentsException {
        parseFromArgs(argString);
    }

    public static EffectEditor editor() {
        return new EffectEditor() {
            @Override
            public List<Pair<EffectOption, Object>> getEffectData(Effect effect) {
                if (!(effect instanceof GlowEffect)) return null;
                GlowEffect glowEffect = (GlowEffect) effect;
                Particle.DustOptions dustOptions = glowEffect.getDustOptions();
                Color color = dustOptions.getColor();
                return Arrays.asList(
                        new Pair<>(new DefaultOption(EffectOption.Type.COLOR, "Color"), color),
                        new Pair<>(new DefaultOption(EffectOption.Type.FLOAT, "Size"), dustOptions.getSize())
                );
            }

            @Override
            public void setEffectData(EffectOption option, Effect effect, String newData) {
                if (!(effect instanceof GlowEffect)) return;
                GlowEffect glowEffect = (GlowEffect) effect;
                switch (option.getName().toLowerCase(Locale.ROOT)) {
                    case "color": {
                        Particle.DustOptions dustOptions = glowEffect.getDustOptions();
                        try {
                            glowEffect.parseFromArgs(newData + "," + dustOptions.getSize());
                        } catch (InvalidEffectArgumentsException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case "size": {
                        Particle.DustOptions dustOptions = glowEffect.getDustOptions();
                        Color color = dustOptions.getColor();
                        try {
                            glowEffect.parseFromArgs(color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + newData);
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
            if (!(effect instanceof GlowEffect)) return null;
            GlowEffect glowEffect = (GlowEffect) effect;
            Particle.DustOptions dustOptions = glowEffect.getDustOptions();
            Color color = dustOptions.getColor();
            String s = "glow;";
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
        if (players.size() > 0) {
            Location blockLocation = block.getLocation();
            horizontal(blockLocation, mineBlock, players, executionToken);
            vertical(blockLocation, mineBlock, players, executionToken);
        } else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            executionToken.executeNow();
            return;
        }
        executionToken.executeAfter(500);
    }

    private void spawnParticle(Location location, Player player) {
        player.spawnParticle(Particle.REDSTONE, location, 1, dustOptions);
    }

    private void vertical(Location block, MineBlock mineBlock, List<Player> players, ExecutionToken executionToken) {
        final List<Location> locations = new LinkedList<>();
        locations.add(block.clone());
        locations.add(block.clone().add(0, 0, 1));
        locations.add(block.clone().add(1, 0, 1));
        locations.add(block.clone().add(1, 0, 0));
        for (Location location : locations) {
            if (executionToken.shouldStop()) return;
            Effect.line(location, location.clone().add(0, 1, 0), 0.1, loc -> {
                for (Player player : players) {
                    spawnParticle(loc, player);
                }
            });
        }
        locations.clear();
    }

    private void horizontal(Location block, MineBlock mineBlock, List<Player> players, ExecutionToken executionToken) {
        final List<Location> locations = new ArrayList<>();
        locations.add(block.clone());
        locations.add(block.clone().add(0, 0, 1));
        locations.add(block.clone().add(1, 0, 1));
        locations.add(block.clone().add(1, 0, 0));
        for (int i = 0; i < locations.size(); i++) {
            if (executionToken.shouldStop()) return;
            Location l1 = locations.get(i);
            Location l2;
            if (i + 1 < locations.size()) {
                l2 = locations.get(i + 1);
            } else {
                l2 = locations.get(0);
            }
            Consumer<Location> locationConsumer = (loc) -> {
                for (Player player : players) {
                    spawnParticle(loc, player);
                }
            };
            Effect.line(l1.clone(), l2.clone(), 0.1, locationConsumer);
            Effect.line(l1.clone().add(0, 1, 0), l2.clone().add(0, 1, 0), 0.1, locationConsumer);
        }
        locations.clear();
    }

    public Particle.DustOptions getDustOptions() {
        return dustOptions;
    }

}
