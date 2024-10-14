package svenhjol.charmony.travel_journal.client.journal;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.lwjgl.glfw.GLFW;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.core.events.ClientLoginPlayerCallback;
import svenhjol.charmony.core.events.ClientRespawnPlayerCallback;
import svenhjol.charmony.travel_journal.TravelJournal;

public class Registers extends Setup<Journal> {
    public KeyMapping openJournalKey;
    public KeyMapping makeBookmarkKey;
    public SoundEvent interactSound;
    public SoundEvent photoSound;

    public Registers(Journal feature) {
        super(feature);

        openJournalKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.charmony-travel-journal.openJournal",
            GLFW.GLFW_KEY_J,
            "key.categories.misc"));
        makeBookmarkKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.charmony-travel-journal.makeBookmark",
            GLFW.GLFW_KEY_B,
            "key.categories.misc"));
        interactSound = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(TravelJournal.ID, "interact"));
        photoSound = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(TravelJournal.ID, "photo"));
    }

    @Override
    public Runnable boot() {
        return () -> {
            ClientLoginPlayerCallback.EVENT.register(feature().handlers::clientLogin);
            ClientTickEvents.END_CLIENT_TICK.register(feature().handlers::clientTick);
            HudRenderCallback.EVENT.register(feature().handlers::hudRender);
        };
    }
}
