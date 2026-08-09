package com.nexusuniverse.backpacks.hint;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

/**
 * Detects whether a player is a Bedrock/console player connected through
 * Geyser + Floodgate. Floodgate is a soft-dependency (see plugin.yml) --
 * if it isn't installed on the server, {@link #isFloodgateAvailable()}
 * returns false and every player is treated as Java.
 */
public final class PlatformUtil {

    private static Boolean floodgateAvailable;

    private PlatformUtil() {
    }

    public static boolean isFloodgateAvailable() {
        if (floodgateAvailable == null) {
            floodgateAvailable = Bukkit.getPluginManager().getPlugin("floodgate") != null;
        }
        return floodgateAvailable;
    }

    public static boolean isBedrockPlayer(Player player) {
        if (!isFloodgateAvailable()) return false;
        try {
            return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
        } catch (Throwable t) {
            // Floodgate present but API not ready / mismatched version -- fail safe as Java.
            return false;
        }
    }
}
