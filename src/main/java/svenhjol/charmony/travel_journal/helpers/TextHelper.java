package svenhjol.charmony.travel_journal.helpers;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.text.WordUtils;
import svenhjol.charmony.travel_journal.client.features.journal.Resources;

import java.util.Arrays;
import java.util.List;

public final class TextHelper {
    /**
     * Get formatted text component of the given blockpos.
     */
    public static Component positionAsText(BlockPos pos) {
        return Component.translatable(Resources.XYZ_KEY, pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Get formatted text component of the given dimension.
     */
    public static Component dimensionAsText(ResourceKey<Level> dimension) {
        return Component.translatable(dimensionLocaleKey(dimension));
    }

    /**
     * Get a locale key for a dimension.
     */
    public static String dimensionLocaleKey(ResourceKey<Level> dimension) {
        var location = dimension.location();
        var namespace = location.getNamespace();
        var path = location.getPath();
        return "dimension." + namespace + "." + path;
    }

    /**
     * Wrap string at a sensible line length and converts into a list of components.
     * This uses an old version of WordUtils which may be problematic?
     */
    @SuppressWarnings("deprecation")
    public static List<Component> wrap(String str) {
        var wrapped = WordUtils.wrap(str, 30);
        return Arrays.stream(wrapped.split("\n")).map(s -> (Component) Component.literal(s)).toList();
    }

    public static void drawCenteredString(GuiGraphics guiGraphics, Font font, Component component, int x, int y, int color, boolean dropShadow) {
        var formattedCharSequence = component.getVisualOrderText();
        guiGraphics.drawString(font, formattedCharSequence, x - font.width(formattedCharSequence) / 2, y, color, dropShadow);
    }
}
