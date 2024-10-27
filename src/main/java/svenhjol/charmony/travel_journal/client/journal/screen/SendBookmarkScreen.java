package svenhjol.charmony.travel_journal.client.journal.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import svenhjol.charmony.core.base.Environment;
import svenhjol.charmony.core.client.CoreButtons;
import svenhjol.charmony.travel_journal.common.journal.Bookmark;

import java.util.ArrayList;
import java.util.List;

public class SendBookmarkScreen extends BaseScreen {
    private final Bookmark bookmark;
    private final List<Button> playerButtons = new ArrayList<>();

    public SendBookmarkScreen(Bookmark bookmark) {
        super(Component.translatable("gui.charmony-travel-journal.sendToPlayer"));
        this.bookmark = bookmark;
    }

    @Override
    protected void init() {
        super.init();

        // Add button to go back
        addRenderableWidget(new CoreButtons.BackButton(midX - (CoreButtons.BackButton.WIDTH / 2), 216,
            b -> onClose()));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);

        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        var player = minecraft.player;
        if (level == null || player == null) return;

        // Poll all nearby players
        if (level.getGameTime() % 20 == 0) {
            var nearbyPlayers = journal.handlers.nearbyPlayers();
            for (var button : playerButtons) {
                removeWidget(button);
            }

            playerButtons.clear();

            if (journal.handlers.canSendBookmark()) {
                var top = 32;
                var i = 0;
                for (var nearbyPlayer : nearbyPlayers) {
                    if (i++ >= 7) continue;
                    String playerName;
                    var buttonWidth = 150;

                    if (Environment.isDebugMode() && nearbyPlayer.getUUID().equals(player.getUUID())) {
                        playerName = "DEBUG: Current player";
                    } else {
                        playerName = nearbyPlayer.getScoreboardName();
                    }

                    var button = Button.builder(Component.literal(playerName), b -> sendToPlayer(nearbyPlayer))
                        .width(buttonWidth)
                        .pos(midX - (buttonWidth / 2), top + (i * 12))
                        .build();

                    playerButtons.add(button);
                    addRenderableWidget(button);
                }
            }

            if (playerButtons.isEmpty()) {
                guiGraphics.drawCenteredString(minecraft.font, Component.translatable("gui.charmony-travel-journal.waitingForPlayers"), midX, 100, 0x909090);
            }
        }

        renderPlayerList(guiGraphics, mouseX, mouseY, delta);
    }

    private void renderPlayerList(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        for (Button button : playerButtons) {
            button.render(guiGraphics, mouseX, mouseY, delta);
        }
    }

    private void sendToPlayer(LocalPlayer player) {
        journal.handlers.trySendBookmark(bookmark, player);
        onClose();
    }

    @Override
    public void onClose() {
        journal.handlers.openBookmark(bookmark);
    }
}
