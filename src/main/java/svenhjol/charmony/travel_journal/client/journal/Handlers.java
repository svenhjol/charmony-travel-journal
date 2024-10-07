package svenhjol.charmony.travel_journal.client.journal;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import svenhjol.charmony.scaffold.base.Setup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class Handlers extends Setup<Journal> {
    private static final String TRAVEL_JOURNAL_BASE = "charmony_travel_journal";
    private static final String INTEGRATED_SERVER_BASE = "singleplayer";

    private File session;
    private Bookmarks bookmarks;
    private Photo takingPhoto = null;

    public Handlers(Journal feature) {
        super(feature);
    }

    public void clientTick(Minecraft minecraft) {
        while (feature().registers.openJournalKey.consumeClick()) {
            openJournal(minecraft);
        }
        while (feature().registers.makeBookmarkKey.consumeClick()) {
            makeBookmark(minecraft);
        }
        if (takingPhoto != null) {
            if (takingPhoto.isFinished()) {
                openBookmark(takingPhoto.bookmark());
                takingPhoto = null;
            } else if (!takingPhoto.isValid()) {
                takingPhoto = null;
            } else {
                takingPhoto.tick();
            }
        }
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

        session = sessionFile(host, name);

        if (session.exists()) {
            bookmarks = Bookmarks.instance(session).load();
        } else {
            bookmarks = Bookmarks.instance(session).save(); // Create empty bookmarks file.
        }
    }

    public void hudRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (takingPhoto != null && takingPhoto.isValid()) {
            takingPhoto.renderCountdown(guiGraphics);
        }
    }

    public File session() {
        if (session == null) {
            throw new RuntimeException("Bookmarks have not been loaded or initialized");
        }
        return session;
    }

    public void openJournal(Minecraft minecraft) {
        feature().log().debug("openJournal");
    }

    public void openBookmark(Bookmark bookmark) {
        feature().log().debug("Opening bookmark " + bookmark.id());
    }

    public void takePhoto(Bookmark bookmark) {
        takingPhoto = new Photo(bookmark);
        Minecraft.getInstance().setScreen(null);
    }

    public void makeBookmark(Minecraft minecraft) {
        var bookmark = Bookmark.create(minecraft.player);
        bookmarks.add(bookmark);
        bookmarks.save();
        feature().log().debug("Made bookmark with UUID " + bookmark.id());

        takePhoto(bookmark);
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

    /**
     * Gets or returns the custom photos directory.
     * We don't want to store scaled photos directly inside minecraft's screenshots folder.
     * Create a subdirectory to store all our custom things in.
     */
    public File getOrCreatePhotosDir() {
        var minecraft = Minecraft.getInstance();
        var defaultDir = new File(minecraft.gameDirectory, "screenshots");
        var photosDir = new File(defaultDir, TRAVEL_JOURNAL_BASE);

        if (!photosDir.exists() && !photosDir.mkdir()) {
            throw new RuntimeException("Could not create custom photos directory in the screenshots folder, giving up");
        }

        return photosDir;
    }

    /**
     * Moves a screenshot into the custom photos folder.
     * Typically this is done after taking a screenshot Screenshot.grab().
     */
    public void moveScreenshotIntoPhotosDir(UUID bookmarkId) {
        var minecraft = Minecraft.getInstance();
        var defaultDir = new File(minecraft.gameDirectory, "screenshots");
        var photosDir = getOrCreatePhotosDir();

        var copyFrom = new File(defaultDir, bookmarkId + ".png");
        var copyTo = new File(photosDir, bookmarkId + ".png");

        try {
            Files.move(copyFrom.toPath(), copyTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log().error("Could not move screenshot into photos dir for bookmark " + bookmarkId + ": " + e.getMessage());
        }
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
