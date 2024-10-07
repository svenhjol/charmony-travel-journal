package svenhjol.charmony.travel_journal.client.journal;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import svenhjol.charmony.travel_journal.helpers.BiomeHelper;

import java.util.UUID;

public record Bookmark(
    UUID id, String name, ResourceKey<Level> dimension, BlockPos pos,
    String description, long timestamp, DyeColor color) {
    public static final DyeColor DEFAULT_COLOR = DyeColor.GRAY;


    public static Bookmark create(Player player) {
        return new Bookmark(
            UUID.randomUUID(),
            BiomeHelper.biomeName(player),
            player.level().dimension(),
            player.blockPosition(),
            "",
            System.currentTimeMillis() / 1000L,
            DEFAULT_COLOR
        );
    }
}
