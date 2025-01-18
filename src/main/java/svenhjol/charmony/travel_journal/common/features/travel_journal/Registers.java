package svenhjol.charmony.travel_journal.common.features.travel_journal;

import net.minecraft.sounds.SoundEvent;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.core.common.CommonRegistry;
import svenhjol.charmony.core.enums.Side;
import svenhjol.charmony.travel_journal.common.features.travel_journal.Networking.C2SPlayerSettings;
import svenhjol.charmony.travel_journal.common.features.travel_journal.Networking.C2SSendBookmarkToPlayer;
import svenhjol.charmony.travel_journal.common.features.travel_journal.Networking.S2CSendBookmarkToPlayer;

import java.util.function.Supplier;

public final class Registers extends Setup<TravelJournal> {
    public Supplier<SoundEvent> interactSound;
    public Supplier<SoundEvent> photoSound;

    public Registers(TravelJournal feature) {
        super(feature);
        var registry = CommonRegistry.forFeature(feature);

        interactSound = registry.sound("interact");
        photoSound = registry.sound("photo");
    }

    @Override
    public Runnable boot() {
        return () -> {
            var registry = CommonRegistry.forFeature(feature());
            // Server-to-client packets.
            registry.packetSender(Side.Common, S2CSendBookmarkToPlayer.TYPE, S2CSendBookmarkToPlayer.CODEC);

            // Client-to-server packets.
            registry.packetSender(Side.Client, C2SSendBookmarkToPlayer.TYPE, C2SSendBookmarkToPlayer.CODEC);
            registry.packetSender(Side.Client, C2SPlayerSettings.TYPE, C2SPlayerSettings.CODEC);

            // Client packet receivers.
            registry.packetReceiver(C2SSendBookmarkToPlayer.TYPE, () -> feature().handlers::handleSendBookmarkToPlayerPacket);
            registry.packetReceiver(C2SPlayerSettings.TYPE, () -> feature().handlers::handlePlayerSettingsPacket);
        };
    }
}
