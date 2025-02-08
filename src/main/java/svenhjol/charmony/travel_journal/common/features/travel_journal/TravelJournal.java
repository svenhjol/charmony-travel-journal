package svenhjol.charmony.travel_journal.common.features.travel_journal;

import svenhjol.charmony.core.annotations.FeatureDefinition;
import svenhjol.charmony.core.base.Mod;
import svenhjol.charmony.core.base.SidedFeature;
import svenhjol.charmony.core.enums.Side;
import svenhjol.charmony.travel_journal.TravelJournalMod;

@FeatureDefinition(side = Side.Common, canBeDisabledInConfig = false, description = """
    A journal that holds bookmarks to places of interest.""")
public final class TravelJournal extends SidedFeature {
    public final Registers registers;
    public final Handlers handlers;
    public final Networking networking;

    public TravelJournal(Mod mod) {
        super(mod);
        registers = new Registers(this);
        handlers = new Handlers(this);
        networking = new Networking(this);
    }

    public static TravelJournal feature() {
        return Mod.getSidedFeature(TravelJournal.class);
    }
}
