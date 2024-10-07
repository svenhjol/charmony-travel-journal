package svenhjol.charmony.travel_journal.client.journal;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;

import java.util.UUID;
import java.util.logging.Level;

public record Bookmark(
    UUID id, String name, ResourceKey<Level> dimension, BlockPos pos,
    String description, long timestamp, DyeColor color) {
}
