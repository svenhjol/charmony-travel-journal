package svenhjol.charmony.travel_journal.fabric;

import net.fabricmc.api.ClientModInitializer;
import svenhjol.charmony.scaffold.enums.Side;
import svenhjol.charmony.travel_journal.TravelJournal;
import svenhjol.charmony.travel_journal.client.journal.Journal;

public class ClientInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Init charmony first.
        svenhjol.charmony.scaffold.fabric.ClientInitializer.init();

        // Bootstrap and run the mod.
        var travelJournal = TravelJournal.instance();
        travelJournal.addFeature(Journal.class);
        travelJournal.run(Side.Client);
    }
}
