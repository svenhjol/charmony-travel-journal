package svenhjol.charmony.travel_journal.client.features.travel_journal;

import net.minecraft.util.Mth;
import svenhjol.charmony.core.annotations.Configurable;
import svenhjol.charmony.core.annotations.FeatureDefinition;
import svenhjol.charmony.core.base.Mod;
import svenhjol.charmony.core.base.SidedFeature;
import svenhjol.charmony.core.enums.Side;
import svenhjol.charmony.travel_journal.TravelJournalMod;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@FeatureDefinition(side = Side.Client, description = """
    A journal that holds bookmarks to places of interest.""")
public final class TravelJournal extends SidedFeature {
    public final Supplier<Common> common;
    public final Registers registers;
    public final Handlers handlers;

    @Configurable(
        name = "Scaled photo width",
        description = """
            Width (in pixels) to which photos will be scaled down.
            This affects the storage size of the journal files on your disk
            as well as the size of the network data if you send a bookmark
            to another player. Smaller sizes optimize space and speed but
            reduce image quality.
            The scaled width should be double the scaled height.""",
        requireRestart = false
    )
    private static int scaledPhotoWidth = 192;

    @Configurable(
        name = "Scaled photo height",
        description = """
            Height (in pixels) to which photos will be scaled down.
            This affects the storage size of the journal files on your disk
            as well as the size of the network data if you send a bookmark
            to another player. Smaller sizes optimize space and speed but
            reduce image quality.
            The scaled height should be half the scaled width.""",
        requireRestart = false
    )
    private static int scaledPhotoHeight = 96;

    @Configurable(
        name = "Allow receiving bookmarks",
        description = """
            If true, other players can send a bookmark to you when nearby.""",
        requireRestart = false
    )
    private static boolean allowReceivingBookmarks = true;

    @Configurable(
        name = "Players who may send bookmarks",
        description = """
            A list of player names who are allowed to send you bookmarks when nearby.
            Leave empty to allow any player to send you bookmarks.
            If 'Allow receiving bookmarks' is disabled then not even the
            players in this list will be able to send a bookmark to you.""",
        requireRestart = false
    )
    private static List<String> allowReceivingFrom = new ArrayList<>();

    @Configurable(
        name = "Show closest bookmark",
        description = "If true, the closest bookmark to the player will be shown on the hud.",
        requireRestart = false
    )
    private static boolean showClosestBookmark = true;

    public TravelJournal(Mod mod) {
        super(mod);
        common = Common::new;
        registers = new Registers(this);
        handlers = new Handlers(this);
    }

    public static TravelJournal feature() {
        return TravelJournalMod.instance().sidedFeature(TravelJournal.class);
    }

    public int scaledPhotoWidth() {
        return Mth.clamp(scaledPhotoWidth, 64, 2048);
    }

    public int scaledPhotoHeight() {
        return Mth.clamp(scaledPhotoHeight, 64, 2048);
    }

    public boolean canReceiveBookmarks() {
        return allowReceivingBookmarks;
    }

    public List<String> canReceiveFrom() {
        return allowReceivingFrom;
    }

    public boolean showClosestBookmark() {
        return showClosestBookmark;
    }
}
