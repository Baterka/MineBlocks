package cz.raixo.blocks.effects.options;

public class DefaultOption implements EffectOption {

    private Type type;
    private String name;

    public DefaultOption(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

}
