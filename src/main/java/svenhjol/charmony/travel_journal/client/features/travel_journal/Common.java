package svenhjol.charmony.travel_journal.client.features.travel_journal;

import svenhjol.charmony.travel_journal.common.features.travel_journal.Registers;
import svenhjol.charmony.travel_journal.common.features.travel_journal.TravelJournal;

public class Common {
    public TravelJournal feature;
    public Registers registers;

    public Common() {
        feature = TravelJournal.feature();
        registers = feature.registers;
    }
}
