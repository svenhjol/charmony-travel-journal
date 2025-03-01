package svenhjol.charmony.travel_journal.client.features.travel_journal;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import svenhjol.charmony.core.client.BaseHudRenderer;
import svenhjol.charmony.travel_journal.common.features.travel_journal.Bookmark;

import javax.annotation.Nullable;
import java.util.List;

public class HudRenderer extends BaseHudRenderer {
    private static final List<DyeColor> BRIGHT_COLORS = List.of(
        DyeColor.BLACK, DyeColor.RED, DyeColor.BLUE, DyeColor.GREEN
    );

    public @Nullable Bookmark bookmark;

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (ticksFade == 0) return;
        if (bookmark == null) return;

        var minecraft = Minecraft.getInstance();
        var gui = minecraft.gui;
        var window = minecraft.getWindow();
        var font = gui.getFont();
        var alpha = Math.max(4, Math.min(MAX_FADE_TICKS, ticksFade)) << 24 & 0xff000000;
        var x = 16;
        var y = window.getGuiScaledHeight() - 16;
        var name = Component.literal(bookmark.name());

        var bookmarkColor = bookmark.color();
        int textColor;

        if (BRIGHT_COLORS.contains(bookmarkColor)) {
            textColor = Math.min(0xffffff, bookmarkColor.getTextColor() | 0x505050);
        } else {
            textColor = bookmarkColor.getTextColor();
        }

        guiGraphics.drawString(font, name, x, y, textColor | alpha);
        doFadeTicks();
    }

    @Override
    protected boolean isValid(Player player) {
        var bookmark = feature().handlers.closestBookmark(player.blockPosition()).orElse(null);
        if (bookmark != null) {
            this.bookmark = bookmark;
        }

        return bookmark != null;
    }

    private TravelJournal feature() {
        return TravelJournal.feature();
    }
}
