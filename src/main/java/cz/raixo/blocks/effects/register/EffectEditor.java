package cz.raixo.blocks.effects.register;

import cz.raixo.blocks.effects.Effect;
import cz.raixo.blocks.effects.options.EffectOption;
import cz.raixo.blocks.util.Pair;

import java.util.List;

public interface EffectEditor {

    List<Pair<EffectOption, Object>> getEffectData(Effect effect);

    void setEffectData(EffectOption option, Effect effect, String newData);

}
