package cz.raixo.blocks.afk;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.earth2me.essentials.Essentials;
import net.lapismc.afkplus.AFKPlus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AfkAdapter {

    private final List<AFKPluginType> afkPlugins = new ArrayList<>();

    public AfkAdapter(Plugin plugin) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        List<Plugin> plugins = Arrays.stream(pluginManager.getPlugins()).collect(Collectors.toList());
        for (AFKPluginType value : AFKPluginType.values()) {
            if (plugins.stream().anyMatch(pl -> pl.getName().equalsIgnoreCase(value.getPluginName()))) {
                try {
                    AFKChecker afkChecker = value.getAfkChecker();
                    afkChecker.activate();
                    afkPlugins.add(value);
                    plugin.getLogger().info("AFK plugin registered: " + value.getPluginName());
                } catch (Throwable ignore) {}
            }
        }
    }

    public boolean isAFK(Player player) {
        for (AFKPluginType afkChecker : afkPlugins) {
            if (afkChecker.isAFK(player)) return true;
        }
        return false;
    }

    public List<String> getAfkPlugins() {
        return afkPlugins.stream().map(AFKPluginType::getPluginName).collect(Collectors.toList());
    }

    public enum AFKPluginType {
        PLUGIN_CMI("CMI", new AFKChecker() {

            private CMI cmi;

            @Override
            public boolean isAFK(Player player) {
                if (cmi == null) return false;
                CMIUser cmiUser = cmi.getPlayerManager().getUser(player);
                return cmiUser.isAfk();
            }

            @Override
            public void activate() {
                this.cmi = (CMI) Bukkit.getServer().getPluginManager().getPlugin("CMI");
            }
        }),
        PLUGIN_ESSENTIALS("Essentials", new AFKChecker() {

            private Essentials essentials;

            @Override
            public boolean isAFK(Player player) {
                if (essentials == null) return false;
                return essentials.getUser(player).isAfk();
            }

            @Override
            public void activate() {
                this.essentials = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
            }

        }),
        PLUGIN_AFK_PLUS("AFKPlus", new AFKChecker() {

            private AFKPlus afkPlus;

            @Override
            public boolean isAFK(Player player) {
                if (afkPlus == null) return false;
                return afkPlus.getPlayer(player).isAFK();
            }

            @Override
            public void activate() {
                this.afkPlus = (AFKPlus) Bukkit.getServer().getPluginManager().getPlugin("AFKPlus");
            }
        });

        private final String pluginName;
        private final AFKChecker afkChecker;

        AFKPluginType(String pluginName, AFKChecker afkChecker) {
            this.pluginName = pluginName;
            this.afkChecker = afkChecker;
        }

        public boolean isAFK(Player player) {
            return getAfkChecker().isAFK(player);
        }

        public String getPluginName() {
            return pluginName;
        }

        public AFKChecker getAfkChecker() {
            return afkChecker;
        }
    }

    public interface AFKChecker {

        boolean isAFK(Player player);

        void activate();

    }
}
