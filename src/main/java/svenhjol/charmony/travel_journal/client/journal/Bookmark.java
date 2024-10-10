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

    public static class Mutable {
        public final UUID id;
        public final ResourceKey<Level> dimension;
        public final BlockPos pos;
        public final long timestamp;
        public String name;
        public String description;
        public DyeColor color;

        public Mutable(Bookmark bookmark) {
            this.id = bookmark.id();
            this.dimension = bookmark.dimension();
            this.pos = bookmark.pos();
            this.timestamp = bookmark.timestamp();
            this.name = bookmark.name();
            this.description = bookmark.description();
            this.color = bookmark.color();
        }

        public Bookmark toImmutable() {
            return new Bookmark(id, name, dimension, pos, description, timestamp, color);
        }
    }
}
