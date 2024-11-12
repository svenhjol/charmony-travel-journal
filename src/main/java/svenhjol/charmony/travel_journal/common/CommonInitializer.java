package svenhjol.charmony.travel_journal.common;

import net.fabricmc.api.ModInitializer;
import svenhjol.charmony.core.enums.Side;
import svenhjol.charmony.travel_journal.TravelJournal;
import svenhjol.charmony.travel_journal.common.features.journal.Journal;

public class CommonInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        // Init charmony first.
        svenhjol.charmony.core.common.CommonInitializer.init();

        // Bootstrap and run the common features.
        var travelJournal = TravelJournal.instance();
        travelJournal.addFeature(Journal.class);
        travelJournal.run(Side.Common);
    }
}
