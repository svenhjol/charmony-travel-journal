package svenhjol.charmony.travel_journal.client.journal;

import svenhjol.charmony.scaffold.annotations.Feature;
import svenhjol.charmony.scaffold.base.Mod;
import svenhjol.charmony.scaffold.base.ModFeature;
import svenhjol.charmony.scaffold.enums.Side;

@Feature(side = Side.Client, canBeDisabled = false)
public class Journal extends ModFeature {
    public final Registers registers;
    public final Handlers handlers;

    public Journal(Mod mod) {
        super(mod);
        this.registers = new Registers(this);
        this.handlers = new Handlers(this);
    }
}
