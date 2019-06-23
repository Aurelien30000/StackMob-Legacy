package uk.antiperson.stackmob.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import uk.antiperson.stackmob.StackMob;
import uk.antiperson.stackmob.api.config.IConfigLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Created by nathat on 01/06/17.
 */
public class ConfigLoader implements IConfigLoader {

    private FileConfiguration fc;
    private File file;
    private File defaultFile;
    private StackMob sm;
    private String filename;

    public ConfigLoader(StackMob sm, String filename) {
        this.sm = sm;
        this.filename = filename;
        this.file = new File(sm.getDataFolder(), filename + ".yml");
        this.defaultFile = new File(sm.getDataFolder() + File.separator + "defaults", filename + ".yml");
    }

    @Override
    public boolean check(String config, String toCheck) {
        if (getCustomConfig().getStringList("no-" + config)
                .contains(toCheck)) {
            return true;
        }
        if (getCustomConfig().getStringList(config).size() > 0) {
            return !getCustomConfig().getStringList(config).contains(toCheck);
        }
        return false;
    }

    @Override
    public void reloadCustomConfig() {
        if (!file.exists()) {
            sm.saveResource(filename + ".yml", false);
        }
        if (!defaultFile.exists()) {
            try {
                copyDefault();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        fc = YamlConfiguration.loadConfiguration(file);
        if (updateConfig()) {
            sm.getLogger().info("Configuration file (" + file.getName() + ") has been updated with the latest values.");
        }
    }

    @Override
    public FileConfiguration getCustomConfig() {
        if (fc == null) {
            reloadCustomConfig();
        }
        return fc;
    }

    @Override
    public File getF() {
        return file;
    }

    @Override
    public void generateNewVersion() {
        File file = new File(sm.getDataFolder(), filename + ".old");
        try {
            Files.move(getF().toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            getF().delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
        reloadCustomConfig();
    }

    public void copyDefault() throws IOException {
        File parentFile = defaultFile.getParentFile();
        if (!parentFile.exists()) {
            Files.createDirectories(parentFile.toPath());
        }
        InputStream is = sm.getResource(filename + ".yml");
        Files.copy(is, defaultFile.toPath());
    }

    @Override
    public boolean updateConfig() {
        // Get the latest version of the file from the jar.
        InputStream is = sm.getResource(filename + ".yml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        FileConfiguration includedFile = YamlConfiguration.loadConfiguration(reader);
        // Load a copy of the current file to check for later.
        FileConfiguration originalFile = YamlConfiguration.loadConfiguration(file);
        // Loop through the values of the latest version and set any that are not present.
        for (String key : includedFile.getKeys(true)) {
            if (!(getCustomConfig().contains(key))) {
                getCustomConfig().set(key, includedFile.get(key));
            }
        }
        // Save the changes made, copy the default file.
        if (!(getCustomConfig().saveToString().equals(originalFile.saveToString()))) {
            try {
                copyDefault();
                fc.save(file);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }
}
