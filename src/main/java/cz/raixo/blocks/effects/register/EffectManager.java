package cz.raixo.blocks.effects.register;

import cz.raixo.blocks.effects.Effect;
import cz.raixo.blocks.effects.exceptions.InvalidEffectArgumentsException;
import cz.raixo.blocks.effects.options.EffectOption;
import cz.raixo.blocks.util.Pair;

import java.util.List;

public interface EffectManager {

    Effect create(String args) throws InvalidEffectArgumentsException;

    String save(Effect effect);

    Class<? extends Effect> effectClass();

    EffectInfo getEffectInfo();

    List<Pair<EffectOption, Object>> getEffectData(Effect effect);

    void setEffectData(EffectOption option, Effect effect, String newData);

}
