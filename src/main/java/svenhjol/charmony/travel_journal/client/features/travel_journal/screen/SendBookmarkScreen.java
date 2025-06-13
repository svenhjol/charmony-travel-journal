package svenhjol.charmony.travel_journal.client.features.travel_journal.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import svenhjol.charmony.core.base.Environment;
import svenhjol.charmony.core.client.CoreButtons;
import svenhjol.charmony.travel_journal.client.features.travel_journal.Resources;
import svenhjol.charmony.travel_journal.common.features.travel_journal.Bookmark;

import java.util.ArrayList;
import java.util.List;

public class SendBookmarkScreen extends BaseScreen {
    private final Bookmark bookmark;
    private final List<Button> playerButtons = new ArrayList<>();

    public SendBookmarkScreen(Bookmark bookmark) {
        super(Resources.SEND_TO_PLAYER);
        this.bookmark = bookmark;
    }

    @Override
    protected void init() {
        super.init();

        // Add button to go back
        addRenderableWidget(new CoreButtons.BackButton(midX - (CoreButtons.BackButton.WIDTH / 2), 216,
            b -> back()));
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
                guiGraphics.drawCenteredString(minecraft.font, Resources.WAITING_FOR_PLAYERS, midX, 100, 0x909090);
            }
        }

        renderPlayerList(guiGraphics, mouseX, mouseY, delta);
    }

    private void renderPlayerList(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        for (Button button : playerButtons) {
            button.render(guiGraphics, mouseX, mouseY, delta);
        }
    }

    private void sendToPlayer(Player player) {
        journal.handlers.trySendBookmark(bookmark, player);
        var playerName = player.getScoreboardName();
        var message = Component.translatable("gui.charmony.sent_to_player", bookmark.name(), playerName);
        player.displayClientMessage(message, false);
        onClose();
    }

    private void back() {
        journal.handlers.openBookmark(bookmark);
    }
}
