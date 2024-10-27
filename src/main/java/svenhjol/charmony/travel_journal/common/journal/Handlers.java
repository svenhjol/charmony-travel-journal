package svenhjol.charmony.travel_journal.common.journal;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.travel_journal.common.journal.Networking.C2SPlayerSettings;
import svenhjol.charmony.travel_journal.common.journal.Networking.C2SSendBookmarkToPlayer;
import svenhjol.charmony.travel_journal.common.journal.Networking.S2CSendBookmarkToPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Handlers extends Setup<Journal> {
    private final Map<UUID, Boolean> playerCanReceiveBookmarks = new HashMap<>();
    private final Map<UUID, List<String>> playerAllowBookmarksFrom = new HashMap<>();

    public Handlers(Journal feature) {
        super(feature);
    }

    public void handleSendBookmarkToPlayerPacket(C2SSendBookmarkToPlayer packet, ServerPlayNetworking.Context context) {
        var player = context.player();
        var server = player.server;

        server.execute(() -> {
            var bookmark = packet.bookmark();
            var photo = packet.photo();
            var recipient = packet.recipient();
            var sender = player.getScoreboardName();

            // If target player has disabled the ability to receive bookmarks, return early.
            if (!playerCanReceiveBookmarks.containsKey(recipient) || !playerCanReceiveBookmarks.get(recipient)) {
                log().debug("Player " + recipient + " is not accepting bookmarks");
                return;
            }

            if (playerAllowBookmarksFrom.containsKey(recipient)) {
                var whitelist = playerAllowBookmarksFrom.get(recipient);

                // If target player has not added this sender to their whitelist, return early.
                if (!whitelist.isEmpty() && !whitelist.contains(sender)) {
                    log().debug("Player " + recipient + " is not accepting bookmarks from " + sender);
                    return;
                }
            }

            var players = server.getPlayerList();
            var targetPlayer = players.getPlayer(recipient);
            if (targetPlayer == null) return;

            log().debug("Passed checks. " + sender + " will send bookmark to target " + recipient);
            S2CSendBookmarkToPlayer.send(targetPlayer, bookmark, photo, sender);
        });
    }

    public void handlePlayerSettingsPacket(C2SPlayerSettings packet, ServerPlayNetworking.Context context) {
        var player = context.player();
        var server = player.server;

        server.execute(() -> {
            var uuid = player.getUUID();
            playerCanReceiveBookmarks.put(uuid, packet.allowReceiving());
            playerAllowBookmarksFrom.put(uuid, packet.allowFrom());
        });
    }
}
