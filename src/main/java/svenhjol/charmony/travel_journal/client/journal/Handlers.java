package svenhjol.charmony.travel_journal.client.journal;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import svenhjol.charmony.scaffold.base.Setup;
import svenhjol.charmony.travel_journal.client.journal.screen.BookmarkScreen;
import svenhjol.charmony.travel_journal.client.journal.screen.JournalScreen;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class Handlers extends Setup<Journal> {
    private static final String TRAVEL_JOURNAL_BASE = "charmony_travel_journal";
    private static final String INTEGRATED_SERVER_BASE = "singleplayer";

    private final Map<UUID, ResourceLocation> cachedPhotos = new WeakHashMap<>();
    private File session;
    private Bookmarks bookmarks;
    private TakePhoto takePhoto = null;

    public Handlers(Journal feature) {
        super(feature);
    }

    public void clientTick(Minecraft minecraft) {
        while (feature().registers.openJournalKey.consumeClick()) {
            openJournal();
        }
        while (feature().registers.makeBookmarkKey.consumeClick()) {
            makeBookmark();
        }

        if (takePhoto != null) {
            if (takePhoto.isFinished()) {
                openBookmark(takePhoto.bookmark());
                takePhoto = null;
            } else if (!takePhoto.isValid()) {
                takePhoto = null;
            } else {
                takePhoto.tick();
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
        if (takePhoto != null && takePhoto.isValid()) {
            takePhoto.renderCountdown(guiGraphics);
        }
    }

    public Bookmarks bookmarks() {
        return bookmarks;
    }

    public void openJournal() {
        openJournal(1);
    }

    public void openJournal(int page) {
        var minecraft = Minecraft.getInstance();
        minecraft.setScreen(new JournalScreen(page));
    }

    public void openBookmark(Bookmark bookmark) {
        var minecraft = Minecraft.getInstance();
        minecraft.setScreen(new BookmarkScreen(bookmark));
    }

    public void takePhoto(Bookmark bookmark) {
        takePhoto = new TakePhoto(bookmark);
        Minecraft.getInstance().setScreen(null);
    }

    public void makeBookmark() {
        var minecraft = Minecraft.getInstance();
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

    /**
     * Try and get a texture resource location for a given bookmark UUID.
     * While a photo isn't available, a placeholder is used.
     */
    @SuppressWarnings("ConstantValue")
    @Nullable
    public ResourceLocation tryLoadPhoto(Bookmark bookmark) {
        var fallback = Resources.PHOTO_BACKGROUND;
        var id = bookmark.id();
        var minecraft = Minecraft.getInstance();

        // Check for cached photo data, use if present.
        if (cachedPhotos.containsKey(id)) {
            var resource = cachedPhotos.get(id);
            if (resource != null) {
                return resource;
            }
        }

        // Load the local file, give up if it can't be found.
        var file = new File(getOrCreatePhotosDir(), bookmark.id() + ".png");
        if (!file.exists()) {
            return fallback;
        }

        // Open local photo file, load dynamic texture into cache.
        try {
            var raf = new RandomAccessFile(file, "r");
            if (raf != null) {
                raf.close();
            }

            var stream = new FileInputStream(file);
            var photo = NativeImage.read(stream);
            var dynamicTexture = new DynamicTexture(photo);
            var registeredTexture = minecraft.getTextureManager().register("charmony_photo", dynamicTexture);
            stream.close();

            cachedPhotos.put(id, registeredTexture);
            if (registeredTexture == null) {
                throw new Exception("Problem with image texture / registered texture for bookmarkId: " + id);
            }

        } catch (Exception e) {
            log().error(e.getMessage());
        }

        return fallback;
    }

    public void deletePhoto(Bookmark bookmark) {
        var file = new File(getOrCreatePhotosDir(), bookmark.id() + ".png");
        if (file.exists()) {
            if (!file.delete()) {
                feature().log().warn("Error while deleting the bookmark photo");
            }
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
