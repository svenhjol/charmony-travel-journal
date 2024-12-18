package svenhjol.charmony.travel_journal.common.features.travel_journal;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.travel_journal.common.features.travel_journal.Networking.*;

public final class Registers extends Setup<TravelJournal> {
    public Registers(TravelJournal feature) {
        super(feature);
    }

    @Override
    public Runnable boot() {
        return () -> {
            // Server-to-client packets
            PayloadTypeRegistry.playS2C().register(S2CSendBookmarkToPlayer.TYPE, S2CSendBookmarkToPlayer.CODEC);

            // Client-to-server packets
            PayloadTypeRegistry.playC2S().register(C2SSendBookmarkToPlayer.TYPE, C2SSendBookmarkToPlayer.CODEC);
            PayloadTypeRegistry.playC2S().register(C2SPlayerSettings.TYPE, C2SPlayerSettings.CODEC);

            // Handle packets being sent from the client
            ServerPlayNetworking.registerGlobalReceiver(C2SSendBookmarkToPlayer.TYPE, feature().handlers::handleSendBookmarkToPlayerPacket);
            ServerPlayNetworking.registerGlobalReceiver(C2SPlayerSettings.TYPE, feature().handlers::handlePlayerSettingsPacket);
        };
    }
}
