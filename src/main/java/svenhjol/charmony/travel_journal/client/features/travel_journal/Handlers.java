package svenhjol.charmony.travel_journal.client.features.travel_journal;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import svenhjol.charmony.core.base.Environment;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.travel_journal.TravelJournalMod;
import svenhjol.charmony.travel_journal.client.features.travel_journal.screen.BookmarkScreen;
import svenhjol.charmony.travel_journal.client.features.travel_journal.screen.JournalScreen;
import svenhjol.charmony.travel_journal.client.features.travel_journal.screen.SendBookmarkScreen;
import svenhjol.charmony.travel_journal.common.features.travel_journal.Bookmark;
import svenhjol.charmony.travel_journal.common.features.travel_journal.Bookmarks;
import svenhjol.charmony.travel_journal.common.features.travel_journal.Networking;
import svenhjol.charmony.travel_journal.common.features.travel_journal.Networking.S2CSendBookmarkToPlayer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

@SuppressWarnings("unused")
public final class Handlers extends Setup<TravelJournal> {
    private static final String SEP = File.separator;
    private static final String CHARMONY_BASE = "charmony";
    private static final String TRAVEL_JOURNAL_BASE = "travel_journal";

    private final Map<UUID, ResourceLocation> cachedPhotos = new WeakHashMap<>();
    private int lastViewedPage = 1;
    private long lastSentBookmarkTime = 0;
    private boolean sentPlayerSettings = false;
    private UUID journalId;
    private Bookmarks bookmarks = null;
    private TakePhoto takePhoto = null;

    public Handlers(TravelJournal feature) {
        super(feature);
    }

    public void playerTick(Player player) {
        if (!player.level().isClientSide()) return;

        // Listen to key bindings.
        while (feature().registers.openJournalKey.consumeClick()) {
            openJournal(lastViewedPage);
        }
        while (feature().registers.makeBookmarkKey.consumeClick()) {
            makeBookmark();
        }

        // Update the server once with client settings.
        if (!sentPlayerSettings) {
            Networking.C2SPlayerSettings.send(feature().canReceiveBookmarks(), feature().canReceiveFrom());
            sentPlayerSettings = true;
        }

        // Tick the photo being taken.
        if (takePhoto != null) {
            if (takePhoto.isFinished()) {
                openBookmark(takePhoto.bookmark());
                takePhoto = null;
            } else if (!takePhoto.isValid()) {
                openBookmark(takePhoto.bookmark());
                takePhoto = null;
            } else {
                takePhoto.tick();
            }
        }

        feature().registers.hudRenderer.tick(player);
    }

    /**
     * Use to set the initial state of the client.
     * Don't send network packets here because they don't work.
     */
    public void clientLogin(ClientboundLoginPacket packet) {
        this.journalId = uuidFromSeed(packet.commonPlayerSpawnInfo().seed());

        if (!checkAndCreateDirectories()) {
            feature().log().error("checkAndCreateDirectories failed, giving up");
            return;
        }

        var session = sessionFile();
        if (session.exists()) {
            bookmarks = Bookmarks.instance(session).load();
        } else {
            bookmarks = Bookmarks.instance(session).save(); // Create empty bookmarks file.
        }

        sentPlayerSettings = false;
    }

    public void hudRender(LayeredDrawerWrapper drawers) {
        drawers.attachLayerAfter(
            IdentifiedLayer.MISC_OVERLAYS,
            TravelJournalMod.id("take_photo"),
            (this::takePhotoHudRender));

        drawers.attachLayerAfter(
            IdentifiedLayer.MISC_OVERLAYS,
            TravelJournalMod.id("show_closest_bookmark"),
            ((guiGraphics, deltaTracker) -> {
                var minecraft = Minecraft.getInstance();
                if (feature().showClosestBookmark() && takePhoto == null && !minecraft.options.hideGui) {
                    feature().registers.hudRenderer.render(guiGraphics, deltaTracker);
                }
            }));
    }

    public void takePhotoHudRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (takePhoto != null && takePhoto.isValid()) {
            takePhoto.renderCountdown(guiGraphics);
        }
    }

    public Bookmarks bookmarks() {
        if (bookmarks == null) {
            throw new RuntimeException("Bookmarks have not been setup on the client");
        }
        return bookmarks;
    }

    public void openJournal() {
        openJournal(lastViewedPage);
    }

    public void openJournal(int page) {
        var minecraft = Minecraft.getInstance();
        minecraft.setScreen(new JournalScreen(page));
    }

    public void openBookmark(Bookmark bookmark) {
        var minecraft = Minecraft.getInstance();
        minecraft.setScreen(new BookmarkScreen(bookmark));
    }

    public void openSendBookmark(Bookmark bookmark) {
        var minecraft = Minecraft.getInstance();
        minecraft.setScreen(new SendBookmarkScreen(bookmark));
    }

    public void setLastViewedPage(int page) {
        this.lastViewedPage = page;
    }

    /**
     * Handle incoming bookmark from the server.
     */
    public void handleSendBookmarkToPlayerPacket2(Player player, S2CSendBookmarkToPlayer payload) {
        Component message;
        var bookmark = payload.bookmark();
        var sender = payload.sender();
        var debug = Environment.isDebugMode();

        if (!bookmarks.exists(bookmark.id()) || debug) {
            if (debug) {
                // bookmarks.add() doesn't allow duplicates so we delete it first
                bookmarks.remove(bookmark.id());
            }

            message = Component.translatable("gui.charmony-travel-journal.receiveFromPlayer", sender, bookmark.name());
            bookmarks.add(bookmark);
            savePhoto(bookmark, payload.photo());
        } else {
            message = Component.translatable("gui.charmony-travel-journal.alreadyHaveTheBookmark", sender, bookmark.name());
        }

        player.displayClientMessage(message, false);
    }

    public void takePhoto(Bookmark bookmark) {
        takePhoto = new TakePhoto(bookmark);
        Minecraft.getInstance().setScreen(null);
    }

    public void makeBookmark() {
        var minecraft = Minecraft.getInstance();
        var bookmark = Bookmark.create(minecraft.player);
        bookmarks.add(bookmark);
        feature().log().debug("Made bookmark with UUID " + bookmark.id());
        takePhoto(bookmark);
    }

    public boolean canSendBookmark() {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        if (level == null) return false;

        return Environment.usesCharmonyServer()
            && level.getGameTime() > lastSentBookmarkTime + 40
            && !nearbyPlayers().isEmpty();
    }

    public boolean belongsToPlayer(Bookmark bookmark) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null) return false;

        return player.getScoreboardName().equals(bookmark.author());
    }

    public List<Player> nearbyPlayers() {
        return nearbyPlayers(Environment.isDebugMode());
    }

    public List<Player> nearbyPlayers(boolean includeCurrentPlayer) {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        var player = minecraft.player;
        if (level == null || player == null) return List.of();

        var nearbyPlayers = level.getEntitiesOfClass(Player.class, (new AABB(player.blockPosition())).inflate(5.0d));
        if (includeCurrentPlayer) {
            return nearbyPlayers;
        }

        return nearbyPlayers.stream().filter(p -> !p.getUUID().equals(player.getUUID())).toList();
    }

    private boolean checkAndCreateDirectories() {
        boolean result = true;
        result &= tryMakeDir(baseDir());
        result &= tryMakeDir(sessionDir());
        result &= tryMakeDir(photosDir());
        return result;
    }

    private boolean tryMakeDir(File dir) {
        if (!dir.exists()) {
            var result = dir.mkdirs();
            if (!result) {
                feature().log().warn("Could not create " + dir);
                return false;
            }
        }
        return true;
    }

    /**
     * Moves a screenshot into the custom photos folder.
     * Typically this is done after taking a screenshot Screenshot.grab().
     */
    public void moveScreenshotIntoPhotosDir(UUID bookmarkId) {
        var minecraft = Minecraft.getInstance();
        var screenshotsDir = new File(minecraft.gameDirectory, "screenshots");

        var copyFrom = new File(screenshotsDir, bookmarkId + ".png");
        var copyTo = new File(photosDir(), bookmarkId + ".png");

        try {
            Files.move(copyFrom.toPath(), copyTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log().error("Could not move screenshot into photos dir for bookmark " + bookmarkId + ": " + e.getMessage());
        }
    }

    /**
     * Try and send a bookmark to a nearby player.
     */
    public void trySendBookmark(Bookmark bookmark, Player player) {
        if (!Environment.usesCharmonyServer()) return;

        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        if (level == null) return;

        var path = new File(photosDir(), bookmark.id() + ".png");
        BufferedImage image;

        try {
            image = ImageIO.read(path);
        } catch (Exception e) {
            log().error("Could not load photo for bookmark " + bookmark.id());
            return;
        }

        lastSentBookmarkTime = level.getGameTime();
        Networking.C2SSendBookmarkToPlayer.send(bookmark, image, player.getUUID());
    }

    /**
     * Try and get a texture resource location for a given bookmark UUID.
     * While a photo isn't available, a placeholder is used.
     */
    @SuppressWarnings("ConstantValue")
    public @NotNull ResourceLocation tryLoadPhoto(Bookmark bookmark) {
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
        var file = new File(photosDir(), bookmark.id() + ".png");
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
            var dynamicTexture = new DynamicTexture(() -> "Photo for bookmark", photo);
            var photoId = TravelJournalMod.id("bookmark_photo_" + bookmark.id());
            minecraft.getTextureManager().register(photoId, dynamicTexture);
            stream.close();

            cachedPhotos.put(id, photoId);

        } catch (Exception e) {
            log().error(e.getMessage());
        }

        return fallback;
    }

    public boolean savePhoto(Bookmark bookmark, BufferedImage photo) {
        boolean success;
        var path = new File(photosDir(), bookmark.id() + ".png");

        try {
            success = ImageIO.write(photo, "png", path);
        } catch (Exception e) {
            success = false;
        }

        return success;
    }

    public void deletePhoto(Bookmark bookmark) {
        var file = new File(photosDir(), bookmark.id() + ".png");
        if (file.exists()) {
            if (!file.delete()) {
                feature().log().warn("Error while deleting the bookmark photo");
            }
        }
    }

    public File baseDir() {
        var gameDir = FabricLoader.getInstance().getGameDir().toFile();
        return new File( gameDir + SEP + CHARMONY_BASE + SEP + TRAVEL_JOURNAL_BASE);
    }

    public File sessionDir() {
        return new File(baseDir() + SEP + journalId);
    }

    public File sessionFile() {
        return new File(sessionDir() + SEP + "journal.json");
    }

    public File photosDir() {
        return new File(sessionDir() + SEP + "photos");
    }

    /**
     * Get the closest bookmark to the given position.
     * @param pos Position to check.
     * @return Closest bookmark or empty optional.
     */
    public Optional<Bookmark> closestBookmark(BlockPos pos) {
        var distance = feature().closestBookmarkDistance();

        return bookmarks.all().stream()
            .filter(bookmark -> bookmark.pos().distManhattan(pos) < distance)
            .min((a, b) -> {
                var ap = a.pos().distManhattan(pos);
                var bp = b.pos().distManhattan(pos);
                return Integer.compare(ap, bp);
            });
    }

    /**
     * Copypasta from UUID so that we can create a basic guid from a fixed seed.
     */
    private UUID uuidFromSeed(long seed) {
        var rand = new Random(seed);
        byte[] randomBytes = new byte[16];
        rand.nextBytes(randomBytes);
        randomBytes[6] &= 0x0f;
        randomBytes[6] |= 0x40;
        randomBytes[8] &= 0x3f;
        randomBytes[8] |= (byte) 0x80;
        long msb = 0;
        long lsb = 0;
        for (int i=0; i<8; i++)
            msb = (msb << 8) | (randomBytes[i] & 0xff);
        for (int i=8; i<16; i++)
            lsb = (lsb << 8) | (randomBytes[i] & 0xff);
        return new UUID(msb, lsb);
    }
}
