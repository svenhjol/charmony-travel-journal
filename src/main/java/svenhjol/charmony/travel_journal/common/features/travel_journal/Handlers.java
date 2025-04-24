package svenhjol.charmony.travel_journal.common.features.travel_journal;

import net.minecraft.world.entity.player.Player;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.travel_journal.common.features.travel_journal.Networking.C2SPlayerSettings;
import svenhjol.charmony.travel_journal.common.features.travel_journal.Networking.C2SSendBookmarkToPlayer;
import svenhjol.charmony.travel_journal.common.features.travel_journal.Networking.S2CSendBookmarkToPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Handlers extends Setup<TravelJournal> {
    private final Map<UUID, Boolean> playerCanReceiveBookmarks = new HashMap<>();
    private final Map<UUID, List<String>> playerAllowBookmarksFrom = new HashMap<>();

    public Handlers(TravelJournal feature) {
        super(feature);
    }

    public void handleSendBookmarkToPlayerPacket(Player player, C2SSendBookmarkToPlayer payload) {
        var server = player.getServer();
        if (server == null) return;

        var bookmark = payload.bookmark();
        var photo = payload.photo();
        var recipient = payload.recipient();
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
    }

    public void handlePlayerSettingsPacket(Player player, C2SPlayerSettings payload) {
        var uuid = player.getUUID();
        playerCanReceiveBookmarks.put(uuid, payload.allowReceiving());
        playerAllowBookmarksFrom.put(uuid, payload.allowFrom());
    }
}
