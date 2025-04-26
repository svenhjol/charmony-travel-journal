package svenhjol.charmony.travel_journal.client.features.travel_journal;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.core.client.ClientRegistry;
import svenhjol.charmony.api.events.ClientLoginPlayerCallback;
import svenhjol.charmony.api.events.PlayerTickCallback;
import svenhjol.charmony.travel_journal.common.features.travel_journal.Networking.S2CSendBookmarkToPlayer;

public class Registers extends Setup<TravelJournal> {
    public KeyMapping openJournalKey;
    public KeyMapping makeBookmarkKey;
    public final HudRenderer hudRenderer;

    public Registers(TravelJournal feature) {
        super(feature);
        hudRenderer = new HudRenderer();
    }

    @Override
    public Runnable boot() {
        return () -> {
            var registry = ClientRegistry.forFeature(feature());

            registry.packetReceiver(S2CSendBookmarkToPlayer.TYPE,
                () -> feature().handlers::handleSendBookmarkToPlayerPacket2);

            openJournalKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.charmony-travel-journal.openJournal",
                GLFW.GLFW_KEY_J,
                "key.categories.misc"));
            makeBookmarkKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.charmony-travel-journal.makeBookmark",
                GLFW.GLFW_KEY_B,
                "key.categories.misc"));

            ClientLoginPlayerCallback.EVENT.register(feature().handlers::clientLogin);
            HudLayerRegistrationCallback.EVENT.register(feature().handlers::hudRender);
            PlayerTickCallback.EVENT.register(feature().handlers::playerTick);
        };
    }
}
