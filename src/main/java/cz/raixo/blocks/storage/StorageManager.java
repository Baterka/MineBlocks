package cz.raixo.blocks.storage;

import org.bukkit.plugin.Plugin;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class StorageManager {

    private static final String FILE_NAME = "storage.json";
    private JSONObject data;

    public StorageManager(Plugin plugin) throws IOException {
        reload(plugin);
    }


    public void reload(Plugin plugin) throws IOException {
        File configFile = new File(plugin.getDataFolder(), FILE_NAME);
        if (!configFile.exists()) {
            configFile.createNewFile();
            Files.writeString(configFile.toPath(), "{}");
        }
        this.data = new JSONObject(new String(Files.readAllBytes(configFile.toPath())));
    }

    public StorageData load(String name) {
        if (data.isNull(name)) return null;
        return new StorageData(data.getJSONObject(name));
    }

    public void save(String name, StorageData data) {
        this.data.put(name, data.toJson());
    }

    public void saveStorage(Plugin plugin) throws IOException {
        File configFile = new File(plugin.getDataFolder(), FILE_NAME);
        if (!configFile.exists()) {
            configFile.createNewFile();
        }
        Files.writeString(configFile.toPath(), this.data.toString());
    }

}
