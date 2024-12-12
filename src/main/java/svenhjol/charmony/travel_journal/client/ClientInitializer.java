package svenhjol.charmony.travel_journal.client;

import net.fabricmc.api.ClientModInitializer;
import svenhjol.charmony.core.enums.Side;
import svenhjol.charmony.travel_journal.TravelJournal;
import svenhjol.charmony.travel_journal.client.features.journal.Journal;

public class ClientInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Init charmony first.
        svenhjol.charmony.core.client.ClientInitializer.init();

        // Bootstrap and run the mod.
        var travelJournal = TravelJournal.instance();
        travelJournal.addSidedFeature(Journal.class);
        travelJournal.run(Side.Client);
    }
}
