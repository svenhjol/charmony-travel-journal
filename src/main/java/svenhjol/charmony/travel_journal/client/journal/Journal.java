package svenhjol.charmony.travel_journal.client.journal;

import net.minecraft.util.Mth;
import svenhjol.charmony.scaffold.annotations.Configurable;
import svenhjol.charmony.scaffold.annotations.Feature;
import svenhjol.charmony.scaffold.base.Mod;
import svenhjol.charmony.scaffold.base.ModFeature;
import svenhjol.charmony.scaffold.enums.Side;
import svenhjol.charmony.travel_journal.TravelJournal;

@Feature(side = Side.Client, canBeDisabled = false)
public class Journal extends ModFeature {
    public final Registers registers;
    public final Handlers handlers;

    @Configurable(
        name = "Scaled photo width",
        description = """
            Width (in pixels) that photos will be scaled down to before being uploaded to the server.
            This affects the storage size of the world data and network packet size when a client wants
            to download a copy of the photo. Smaller sizes optimize space and speed but reduce quality.
            The scaled width should be double the scaled height."""
    )
    private static int scaledPhotoWidth = 192;

    @Configurable(
        name = "Scaled photo height",
        description = """
            Height (in pixels) that photos will be scaled down to before being uploaded to the server.
            This affects the storage size of the world data and network packet size when a client wants
            to download a copy of the photo. Smaller sizes optimize space and speed but reduce quality.
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
