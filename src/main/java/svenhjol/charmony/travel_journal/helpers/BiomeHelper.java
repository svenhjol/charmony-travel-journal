package svenhjol.charmony.travel_journal.helpers;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class BiomeHelper {
    /**
     * Gets the nice name of the biome that the recipient is in.
     */
    public static String biomeName(Player player) {
        return Component.translatable(biomeLocaleKey(player)).getString();
    }

    /**
     * Get a locale key for the biome at the recipient's current position.
     */
    public static String biomeLocaleKey(Player player) {
        var registry = player.level().registryAccess();
        var biome = player.level().getBiome(player.blockPosition());
        var key = registry.lookupOrThrow(Registries.BIOME).getKey(biome.value());

        if (key == null) {
            throw new RuntimeException("Cannot get recipient biome");
        }

        var namespace = key.getNamespace();
        var path = key.getPath();
        return "biome." + namespace + "." + path;
    }
}
