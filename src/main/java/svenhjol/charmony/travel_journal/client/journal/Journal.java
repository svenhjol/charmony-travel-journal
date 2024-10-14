package svenhjol.charmony.travel_journal.client.journal;

import net.minecraft.util.Mth;
import svenhjol.charmony.core.annotations.Configurable;
import svenhjol.charmony.core.annotations.FeatureDefinition;
import svenhjol.charmony.core.base.Mod;
import svenhjol.charmony.core.base.SidedFeature;
import svenhjol.charmony.core.enums.Side;
import svenhjol.charmony.travel_journal.TravelJournal;

@FeatureDefinition(side = Side.Client, canBeDisabled = false, description = """
    A journal that holds bookmarks to places of interest.""")
public class Journal extends SidedFeature {
    public final Registers registers;
    public final Handlers handlers;

    @Configurable(
        name = "Scaled photo width",
        description = """
            Width (in pixels) to which photos will be scaled down.
            This affects the storage size of the journal data.
            Smaller sizes optimize space and speed but reduce quality.
            The scaled width should be double the scaled height."""
    )
    private static int scaledPhotoWidth = 192;

    @Configurable(
        name = "Scaled photo height",
        description = """
            Height (in pixels) to which photos will be scaled down.
            This affects the storage size of the journal data.
            Smaller sizes optimize space and speed but reduce quality.
            The scaled height should be half the scaled width."""
    )
    private static int scaledPhotoHeight = 96;

    public Journal(Mod mod) {
        super(mod);
        this.registers = new Registers(this);
        this.handlers = new Handlers(this);
    }

    public static Journal feature() {
        return TravelJournal.instance().feature(Journal.class);
    }

    public int scaledPhotoWidth() {
        return Mth.clamp(scaledPhotoWidth, 64, 2048);
    }

    public int scaledPhotoHeight() {
        return Mth.clamp(scaledPhotoHeight, 64, 2048);
    }
}
