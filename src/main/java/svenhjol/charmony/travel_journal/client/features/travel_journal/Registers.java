package svenhjol.charmony.travel_journal.client.features.travel_journal;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.lwjgl.glfw.GLFW;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.core.events.ClientLoginPlayerCallback;
import svenhjol.charmony.travel_journal.TravelJournalMod;
import svenhjol.charmony.travel_journal.common.features.travel_journal.Networking.S2CSendBookmarkToPlayer;

public final class Registers extends Setup<TravelJournal> {
    public KeyMapping openJournalKey;
    public KeyMapping makeBookmarkKey;
    public SoundEvent interactSound;
    public SoundEvent photoSound;

    public Registers(TravelJournal feature) {
        super(feature);
    }

    @Override
    public Runnable boot() {
        return () -> {
            interactSound = SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(TravelJournalMod.ID, "interact"));
            photoSound = SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(TravelJournalMod.ID, "photo"));

            openJournalKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.charmony-travel-journal.openJournal",
                GLFW.GLFW_KEY_J,
                "key.categories.misc"));
            makeBookmarkKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.charmony-travel-journal.makeBookmark",
                GLFW.GLFW_KEY_B,
                "key.categories.misc"));

            ClientLoginPlayerCallback.EVENT.register(feature().handlers::clientLogin);
            ClientTickEvents.END_CLIENT_TICK.register(feature().handlers::clientTick);
            HudRenderCallback.EVENT.register(feature().handlers::hudRender);

            // Handle packets being sent from the server
            ClientPlayNetworking.registerGlobalReceiver(S2CSendBookmarkToPlayer.TYPE, feature().handlers::handleSendBookmarkToPlayerPacket);
        };
    }
}
