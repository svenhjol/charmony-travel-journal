package svenhjol.charmony.travel_journal.client.journal;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import svenhjol.charmony.scaffold.base.Setup;

import java.io.File;

public class Handlers extends Setup<Journal> {
    private static final String TRAVEL_JOURNAL_BASE = "charmony_travel_journal";
    private static final String INTEGRATED_SERVER_BASE = "singpleplayer";

    public Handlers(Journal feature) {
        super(feature);
    }

    public void entityLoad(Entity entity, ClientLevel clientLevel) {
        if (!(entity instanceof LocalPlayer)) return;

        var minecraft = Minecraft.getInstance();
        var localServer = minecraft.getSingleplayerServer();
        var dedicatedServer = minecraft.getCurrentServer();

        String host;
        String name;

        if (localServer != null) {
            host = INTEGRATED_SERVER_BASE;
            name = localServer.getMotd();
        } else if (dedicatedServer != null) {
            host = dedicatedServer.ip;
            name = dedicatedServer.name;
        } else {
            feature().log().error("Could not get server information");
            return;
        }

        if (!checkAndCreateDirectories(host)) {
            feature().log().error("checkAndCreateDirectories failed, giving up");
            return;
        }

        var sessionFile = sessionFile(host, name);
        if (sessionFile.exists()) {
            Bookmarks.instance(sessionFile).load();
        } else {
            Bookmarks.instance(sessionFile).save(); // Create empty bookmarks file.
        }
    }

    public void openJournal(Minecraft minecraft) {
        feature().log().debug("openJournal");
    }

    public void makeBookmark(Minecraft minecraft) {
        feature().log().debug("makeBookmark");
    }

    public void clientTick(Minecraft minecraft) {
        while (feature().registers.openJournalKey.consumeClick()) {
            openJournal(minecraft);
        }
        while (feature().registers.makeBookmarkKey.consumeClick()) {
            makeBookmark(minecraft);
        }
    }

    private boolean checkAndCreateDirectories(String host) {
        // If the local travel journal directory doesn't exist, create it.
        var journalDir = journalDir();
        if (!journalDir.exists()) {
            var result = journalDir.mkdirs();
            if (!result) {
                feature().log().warn("Could not create travel journal directory");
                return false;
            }
        }

        // Make a session directory for this ip/host.
        var sessionDir = sessionDir(host);
        if (!sessionDir.exists()) {
            var result = sessionDir.mkdirs();
            if (!result) {
                feature().log().warn("Could not create journal session directory");
                return false;
            }
        }

        return true;
    }

    private File journalDir() {
        var gameDir = FabricLoader.getInstance().getGameDir().toFile();
        return new File( gameDir + File.separator + TRAVEL_JOURNAL_BASE);
    }

    private File sessionDir(String host) {
        return new File(journalDir() + File.separator + host);
    }

    private File sessionFile(String host, String name) {
        return new File(sessionDir(host) + File.separator + name + ".json");
    }
}
