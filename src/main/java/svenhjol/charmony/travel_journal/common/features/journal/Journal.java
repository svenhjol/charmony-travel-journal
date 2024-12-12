package svenhjol.charmony.travel_journal.common.features.journal;

import svenhjol.charmony.core.annotations.FeatureDefinition;
import svenhjol.charmony.core.base.Mod;
import svenhjol.charmony.core.base.SidedFeature;
import svenhjol.charmony.core.enums.Side;
import svenhjol.charmony.travel_journal.TravelJournal;

@FeatureDefinition(side = Side.Common, showInConfig = false, description = """
    A journal that holds bookmarks to places of interest.""")
public final class Journal extends SidedFeature {
    public final Registers registers;
    public final Handlers handlers;
    public final Networking networking;

    public Journal(Mod mod) {
        super(mod);
        registers = new Registers(this);
        handlers = new Handlers(this);
        networking = new Networking(this);
    }

    public static Journal feature() {
        return TravelJournal.instance().sidedFeature(Journal.class);
    }
}
