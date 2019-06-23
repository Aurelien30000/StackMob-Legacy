package uk.antiperson.stackmob.api.tools;

import org.bukkit.Bukkit;

public class VersionHelper {

    public static BukkitVersion getVersion() {
        if (Bukkit.getVersion().contains("1.12")) {
            return BukkitVersion.V1_12;
        } else if (Bukkit.getVersion().contains("1.13")) {
            return BukkitVersion.V1_13;
        } else if (Bukkit.getVersion().contains("1.14")) {
            return BukkitVersion.V1_14;
        }
        return BukkitVersion.UNSUPPORTED;
    }

    public static boolean isVersionNewerThan(BukkitVersion version) {
        return version.getId() >= getVersion().getId();
    }
}
