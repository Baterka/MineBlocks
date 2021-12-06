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

public class UpDownCircleEffect implements Effect {

    private Particle.DustOptions dustOptions;

    public UpDownCircleEffect(String argString) throws InvalidEffectArgumentsException {
        parseFromArgs(argString);
    }

    public static EffectEditor editor() {
        return new EffectEditor() {
            @Override
            public List<Pair<EffectOption, Object>> getEffectData(Effect effect) {
                if (!(effect instanceof UpDownCircleEffect)) return null;
                UpDownCircleEffect updownCircleEffect = (UpDownCircleEffect) effect;
                Particle.DustOptions dustOptions = updownCircleEffect.getDustOptions();
                Color color = dustOptions.getColor();
                return Arrays.asList(
                        new Pair<>(new DefaultOption(EffectOption.Type.COLOR, "Color"), color),
                        new Pair<>(new DefaultOption(EffectOption.Type.FLOAT, "Size"), dustOptions.getSize())
                );
            }

            @Override
            public void setEffectData(EffectOption option, Effect effect, String newData) {
                if (!(effect instanceof UpDownCircleEffect)) return;
                UpDownCircleEffect updownCircleEffect = (UpDownCircleEffect) effect;
                switch (option.getName().toLowerCase(Locale.ROOT)) {
                    case "color": {
                        Particle.DustOptions dustOptions = updownCircleEffect.getDustOptions();
                        try {
                            updownCircleEffect.parseFromArgs(newData + "," + dustOptions.getSize());
                        } catch (InvalidEffectArgumentsException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case "size": {
                        Particle.DustOptions dustOptions = updownCircleEffect.getDustOptions();
                        Color color = dustOptions.getColor();
                        try {
                            updownCircleEffect.parseFromArgs(color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + newData);
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
            if (!(effect instanceof UpDownCircleEffect)) return null;
            UpDownCircleEffect updownCircleEffect = (UpDownCircleEffect) effect;
            Particle.DustOptions dustOptions = updownCircleEffect.getDustOptions();
            Color color = dustOptions.getColor();
            String s = "updowncircle;";
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

    @Override
    public void make(Block block, MineBlock mineBlock, List<Player> players, ExecutionToken executionToken) {
        if (players.size() > 0) {
            Location center = block.getLocation().clone().add(.5, 0, .5);
            List<Location> circleLocation = new LinkedList<>();
            double point = Math.PI / 40;
            for (double i = 0; i < Math.PI * 2; i += point) {
                circleLocation.add(center.clone().add(Math.sin(i) * .85, 0, Math.cos(i) * .85));
            }
            for (double i = 0; i < 2; i += .1) {
                if (executionToken.shouldStop()) return;
                for (Location location : circleLocation) {
                    Location l = location.clone().add(0, i > 1 ? (i % 1) * -1 + 1 : i, 0);
                    for (Player player : players) spawnParticle(l, player);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            executionToken.executeAfter(1000);
            return;
        }
        executionToken.executeNow();
    }

    private void spawnParticle(Location location, Player player) {
        player.spawnParticle(Particle.REDSTONE, location, 1, dustOptions);
    }

}
