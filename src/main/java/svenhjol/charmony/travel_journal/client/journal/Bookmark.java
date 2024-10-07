package svenhjol.charmony.travel_journal.client.journal;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

import java.util.UUID;

public record Bookmark(
    UUID id, String name, ResourceKey<Level> dimension, BlockPos pos,
    String description, long timestamp, DyeColor color) {
}
