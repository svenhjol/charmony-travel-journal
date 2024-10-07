package svenhjol.charmony.travel_journal;

import svenhjol.charmony.scaffold.base.Mod;

public class TravelJournal extends Mod {
    public static final String ID = "charmony-travel-journal";
    private static TravelJournal instance;

    public static TravelJournal instance() {
        if (instance == null) {
            instance = new TravelJournal();
        }
        return instance;
    }

    @Override
    public String id() {
        return ID;
    }
}
