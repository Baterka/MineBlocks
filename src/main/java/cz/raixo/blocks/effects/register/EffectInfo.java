package cz.raixo.blocks.effects.register;

import cz.raixo.blocks.effects.options.EffectOption;
import org.bukkit.Material;

import java.util.List;

public interface EffectInfo {

    String getName();

    String getRegisterName();

    String getDescription();

    Material getIcon();

    List<EffectOption> getOptions();

    class Simple implements EffectInfo {

        private final String name;
        private final String registerName;
        private final String description;
        private final Material icon;
        private final List<EffectOption> options;

        public Simple(String name, String registerName, String description, Material icon, List<EffectOption> options) {
            this.name = name;
            this.registerName = registerName;
            this.description = description;
            this.icon = icon;
            this.options = options;
        }


        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getRegisterName() {
            return registerName;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public Material getIcon() {
            return icon;
        }

        @Override
        public List<EffectOption> getOptions() {
            return options;
        }
    }

}
