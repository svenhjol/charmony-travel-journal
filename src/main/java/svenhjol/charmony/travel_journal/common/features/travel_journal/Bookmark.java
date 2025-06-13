package svenhjol.charmony.travel_journal.common.features.travel_journal;

import com.google.gson.GsonBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import svenhjol.charmony.core.helpers.WorldHelper;

import java.util.UUID;

public record Bookmark(
    UUID id, String name, String dimension, long pos,
    String author, String description, long timestamp, DyeColor color) {
    public static final DyeColor DEFAULT_COLOR = DyeColor.WHITE;

    public static Bookmark create(Player player) {
        return new Bookmark(
            UUID.randomUUID(),
            WorldHelper.biomeName(player),
            player.level().dimension().location().toString(),
            player.blockPosition().asLong(),
            player.getScoreboardName(),
            "",
            System.currentTimeMillis() / 1000L,
            DEFAULT_COLOR
        );
    }

    public ResourceLocation dimensionId() {
        return ResourceLocation.parse(dimension);
    }

    public BlockPos blockPos() {
        return BlockPos.of(pos);
    }

    public String toJsonString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }

    public static class Mutable {
        public final UUID id;
        public final String dimension;
        public final long pos;
        public final long timestamp;
        public String name;
        public String description;
        public String author;
        public DyeColor color;

        public Mutable(Bookmark bookmark) {
            this.id = bookmark.id();
            this.dimension = bookmark.dimension();
            this.pos = bookmark.pos();
            this.timestamp = bookmark.timestamp();
            this.name = bookmark.name();
            this.description = bookmark.description();
            this.author = bookmark.author();
            this.color = bookmark.color();
        }

        public Bookmark toImmutable() {
            return new Bookmark(id, name, dimension, pos, author, description, timestamp, color);
        }
    }
}
