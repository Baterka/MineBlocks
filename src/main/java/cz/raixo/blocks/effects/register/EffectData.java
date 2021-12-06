package cz.raixo.blocks.effects.register;

import cz.raixo.blocks.effects.Effect;
import cz.raixo.blocks.effects.exceptions.InvalidEffectArgumentsException;
import cz.raixo.blocks.effects.options.EffectOption;
import cz.raixo.blocks.util.Pair;

import java.util.List;

public class EffectData implements EffectManager {

    private final EffectCreator effectCreator;
    private final EffectSaver effectSaver;
    private final Class<? extends Effect> effectClass;
    private final EffectInfo effectInfo;
    private final EffectEditor effectEditor;
    public EffectData(EffectCreator effectCreator, EffectSaver effectSaver, Class<? extends Effect> effectClass, EffectInfo effectInfo, EffectEditor effectEditor) {
        this.effectCreator = effectCreator;
        this.effectSaver = effectSaver;
        this.effectClass = effectClass;
        this.effectInfo = effectInfo;
        this.effectEditor = effectEditor;
    }

    @Override
    public Effect create(String args) throws InvalidEffectArgumentsException {
        return effectCreator.create(args);
    }

    @Override
    public String save(Effect effect) {
        return effectSaver.save(effect);
    }

    @Override
    public Class<? extends Effect> effectClass() {
        return effectClass;
    }

    @Override
    public EffectInfo getEffectInfo() {
        return effectInfo;
    }

    @Override
    public List<Pair<EffectOption, Object>> getEffectData(Effect effect) {
        return effectEditor.getEffectData(effect);
    }

    @Override
    public void setEffectData(EffectOption option, Effect effect, String newData) {
        effectEditor.setEffectData(option, effect, newData);
    }

    public interface EffectCreator {
        Effect create(String args) throws InvalidEffectArgumentsException;
    }

    public interface EffectSaver {
        String save(Effect effect);
    }

}
