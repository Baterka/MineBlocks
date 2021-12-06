package cz.raixo.blocks.hologram.manager;

import cz.raixo.blocks.hologram.Hologram;
import cz.raixo.blocks.hologram.cmi.CMIHologram;
import cz.raixo.blocks.hologram.decentholograms.DecentHologram;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HologramManager {

    private final HologramPluginType hologramPlugin;

    public HologramManager(Plugin plugin) throws NoHologramPluginException {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        List<Plugin> plugins = Arrays.stream(pluginManager.getPlugins()).collect(Collectors.toList());
        for (HologramPluginType value : HologramPluginType.values()) {
            if (plugins.stream().filter(pl -> pl.getName().equalsIgnoreCase(value.getPluginName())).count() > 0) {
                hologramPlugin = value;
                value.getHologramCreator().activate();
                return;
            }
        }
        throw new NoHologramPluginException();
    }

    public Hologram createHologram(Location location) {
        return hologramPlugin.getHologramCreator().createHologram(location);
    }

    public HologramPluginType getHologramPlugin() {
        return this.hologramPlugin;
    }

    public enum HologramPluginType {
        DECENT_HOLOGRAMS("DecentHolograms", new HologramCreator() {
            @Override
            public Hologram createHologram(Location location) {
                return new DecentHologram(location);
            }

            @Override
            public void activate() {

            }
        }),
        CMI("CMI", new HologramCreator() {
            @Override
            public Hologram createHologram(Location location) {
                return new CMIHologram(location);
            }

            @Override
            public void activate() {

            }
        });

        private final String pluginName;
        private final HologramCreator hologramCreator;

        HologramPluginType(String pluginName, HologramCreator hologramCreator) {
            this.pluginName = pluginName;
            this.hologramCreator = hologramCreator;
        }

        public String getPluginName() {
            return pluginName;
        }

        public HologramCreator getHologramCreator() {
            return hologramCreator;
        }
    }

    public interface HologramCreator {
        Hologram createHologram(Location location);

        void activate();
    }

    public static class NoHologramPluginException extends Exception {
        public NoHologramPluginException() {
            super("Missing hologram plugin! Please use CMI or DecentHolograms!");
        }
    }

}
