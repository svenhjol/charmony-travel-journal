package svenhjol.charmony.travel_journal.client.features.travel_journal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import svenhjol.charmony.api.core.Color;
import svenhjol.charmony.travel_journal.common.features.travel_journal.Bookmark;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TakePhoto {
    private final Bookmark bookmark;
    private final TravelJournal journal;
    private int ticks;
    private boolean valid;
    private boolean finished;
    private boolean isTakingPhoto;

    public TakePhoto(Bookmark bookmark) {
        this.bookmark = bookmark;
        this.journal = TravelJournal.feature();
        this.valid = true;
        this.finished = false;
        this.isTakingPhoto = false;
    }

    public Bookmark bookmark() {
        return bookmark;
    }

    public void tick() {
        ticks++;

        if (ticks < 60) {
            return;
        }

        if (ticks < 62) {
            hideGui();
            return;
        }

        if (ticks > 100) {
            // Escape if something is wrong.
            valid = false;
            return;
        }

        takePhoto();
    }

    private void takePhoto() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            valid = false;
            return;
        }

        // Give up if there's a screen in the way. We don't want a photo of that!
        if (minecraft.screen != null) {
            showGui();
            valid = false;
            return;
        }

        if (isTakingPhoto) return;
        isTakingPhoto = true;

        Screenshot.grab(
            minecraft.gameDirectory,
            bookmark.id() + ".png",
            minecraft.getMainRenderTarget(),
            1,
            component -> {
                journal.log().debug("Photo taken for bookmark " + bookmark.id());
                finish();
            }
        );
    }

    public void renderCountdown(GuiGraphics guiGraphics) {
        var minecraft = Minecraft.getInstance();
        int x = (guiGraphics.guiWidth() / 8) + 1;
        int y = 20;
        String str = "";

        if (ticks <= 20) {
            str = "3";
        } else if (ticks <= 40) {
            str = "2";
        } else if (ticks <= 60) {
            str = "1";
        }

        if (!str.isEmpty()) {
            var color = new Color(0xffffff);
            var pose = guiGraphics.pose();
            pose.pushMatrix();
            pose.scale(4.0f, 4.0f);
            guiGraphics.drawCenteredString(minecraft.font, str, x, y, color.getArgbColor());
            pose.popMatrix();
        }
    }

    public void finish() {
        showGui();

        var minecraft = Minecraft.getInstance();
        var id = bookmark.id();

        if (minecraft.player != null) {
            minecraft.player.playSound(journal.common.get().registers.photoSound.get());
        }

        // Move the screenshot into the custom photos folder.
        journal.handlers.moveScreenshotIntoPhotosDir(id);

        // Downscale the screenshot.
        BufferedImage image;
        var dir = journal.handlers.photosDir();
        var path = new File(dir, id + ".png");
        try {
            image = ImageIO.read(path);
        } catch (IOException e) {
            journal.log().warnIfDebug("Could not read photo for bookmark " + id + ": " + e.getMessage());
            return;
        }

        var scaledWidth = journal.scaledPhotoWidth();
        var scaledHeight = journal.scaledPhotoHeight();
        var scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
        var graphics2D = scaledImage.createGraphics();

        graphics2D.drawImage(image, 0, 0, scaledWidth, scaledHeight, null);
        graphics2D.dispose();

        // Save the photo
        var success = journal.handlers.savePhoto(bookmark, image);
        if (!success) {
            journal.log().error("Writing image failed for bookmark " + id);
            return;
        }

        finished = true;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isFinished() {
        return finished;
    }

    private void hideGui() {
        Minecraft.getInstance().options.hideGui = true;
    }

    private void showGui() {
        Minecraft.getInstance().options.hideGui = false;
    }
}
