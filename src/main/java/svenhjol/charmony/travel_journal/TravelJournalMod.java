package svenhjol.charmony.travel_journal;

import svenhjol.charmony.api.core.ModDefinition;
import svenhjol.charmony.api.core.Side;
import svenhjol.charmony.core.base.Mod;

@ModDefinition(id = TravelJournalMod.ID, sides = {Side.Common, Side.Client},
    name = "Travel journal",
    description = "A journal to record interesting places around the world. Compatible with vanilla servers such as Realms.")
public class TravelJournalMod extends Mod {
    public static final String ID = "charmony-travel-journal";
    private static TravelJournalMod instance;

    public static TravelJournalMod instance() {
        if (instance == null) {
            instance = new TravelJournalMod();
        }
        return instance;
    }
}
