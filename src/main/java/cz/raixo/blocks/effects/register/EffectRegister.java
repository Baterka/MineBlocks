package cz.raixo.blocks.effects.register;

import cz.raixo.blocks.effects.Effect;
import cz.raixo.blocks.effects.exceptions.InvalidEffectArgumentsException;
import cz.raixo.blocks.effects.options.DefaultOption;
import cz.raixo.blocks.effects.options.EffectOption;
import cz.raixo.blocks.effects.types.CircleComboEffect;
import cz.raixo.blocks.effects.types.GlowEffect;
import cz.raixo.blocks.effects.types.ShineEffect;
import cz.raixo.blocks.effects.types.UpDownCircleEffect;
import org.bukkit.Material;

import java.util.*;

public class EffectRegister {

    private static final Map<String, EffectManager> EFFECTS = new HashMap<>();

    static {
        EFFECTS.put("shine", new EffectData(ShineEffect::new, ShineEffect.effectSaver(), ShineEffect.class, new EffectInfo.Simple("Shine", "shine", "Animated", Material.NETHER_STAR, Arrays.asList(new DefaultOption(EffectOption.Type.COLOR, "Color"), new DefaultOption(EffectOption.Type.FLOAT, "Size"))), ShineEffect.editor()));
        EFFECTS.put("glow", new EffectData(GlowEffect::new, GlowEffect.effectSaver(), GlowEffect.class, new EffectInfo.Simple("Glow", "glow", "Static", Material.GLOWSTONE, Arrays.asList(new DefaultOption(EffectOption.Type.COLOR, "Color"), new DefaultOption(EffectOption.Type.FLOAT, "Size"))), GlowEffect.editor()));
        EFFECTS.put("circlecombo", new EffectData(CircleComboEffect::new, CircleComboEffect.effectSaver(), CircleComboEffect.class, new EffectInfo.Simple("Circle Combo", "circlecombo", "Animated", Material.LEAD, Arrays.asList(new DefaultOption(EffectOption.Type.COLOR, "Color"), new DefaultOption(EffectOption.Type.FLOAT, "Size"))), CircleComboEffect.editor()));
        EFFECTS.put("updowncircle", new EffectData(UpDownCircleEffect::new, UpDownCircleEffect.effectSaver(), UpDownCircleEffect.class, new EffectInfo.Simple("Up Down Circle", "updowncircle", "Animated", Material.GOLD_NUGGET, Arrays.asList(new DefaultOption(EffectOption.Type.COLOR, "Color"), new DefaultOption(EffectOption.Type.FLOAT, "Size"))), UpDownCircleEffect.editor()));
    }

    public static Effect create(String effectString) throws InvalidEffectArgumentsException {
        String[] args = effectString.split(";", 2);
        if (args.length < 2) return null;
        if (!EFFECTS.containsKey(args[0].toLowerCase(Locale.ROOT))) return null;
        EffectManager effectManager = EFFECTS.get(args[0].toLowerCase(Locale.ROOT));
        return effectManager.create(args[1]);
    }

    public static String save(Effect effect) {
        for (EffectManager effectManager : EFFECTS.values()) {
            if (!effectManager.effectClass().isInstance(effect)) continue;
            return effectManager.save(effect);
        }
        return null;
    }

    public static EffectManager getManager(Effect effect) {
        for (EffectManager effectManager : EFFECTS.values()) {
            if (!effectManager.effectClass().isInstance(effect)) continue;
            return effectManager;
        }
        return null;
    }

    public static Collection<EffectManager> getEffects() {
        return EFFECTS.values();
    }

}
