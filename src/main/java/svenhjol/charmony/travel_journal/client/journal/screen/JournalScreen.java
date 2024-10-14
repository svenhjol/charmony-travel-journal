package svenhjol.charmony.travel_journal.client.journal.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import svenhjol.charmony.core.client.CoreButtons;
import svenhjol.charmony.travel_journal.client.journal.Bookmark;
import svenhjol.charmony.travel_journal.client.journal.Buttons;
import svenhjol.charmony.travel_journal.client.journal.Resources;
import svenhjol.charmony.travel_journal.helpers.TextHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class JournalScreen extends BaseScreen {
    private static final String DATE_FORMAT = "dd-MMM-yy";
    
    private final int columns;
    private boolean renderedButtons = false;
    private int page;
    
    public JournalScreen(int page) {
        super(Resources.TRAVEL_JOURNAL);
        this.page = page;
        this.columns = 2;
    }

    @Override
    protected void init() {
        super.init();

        // Add footer buttons
        addRenderableWidget(new CoreButtons.CloseButton(midX + 5, 216, b -> onClose()));
        addRenderableWidget(new Buttons.NewBookmarkButton(midX - (Buttons.NewBookmarkButton.WIDTH + 5), 216,
            b -> journal.handlers.makeBookmark()));

        renderedButtons = false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        
        var bookmarks = journal.handlers.bookmarks();
        var index = (page - 1) * columns;
            
        while (page > 1 && index >= bookmarks.size()) {
            --page;
            index = (page - 1) * columns;
        }

        for (var x = 0; x < columns; x++) {
            if (index >= bookmarks.size()) continue;
            
            var opt = bookmarks.get(index);
            if (opt.isEmpty()) continue;
            var bookmark = opt.get();
            
            renderTitleDetails(guiGraphics, bookmark, x);
            renderPhoto(guiGraphics, bookmark, x);
            renderPhotoDescriptionHover(guiGraphics, bookmark, mouseX, mouseY, x);
            renderDimension(guiGraphics, bookmark, x);
            renderPosition(guiGraphics, bookmark, x);
            renderDetailsButton(bookmark, x);
            
            index++;
        }

        if (bookmarks.isEmpty()) {
            renderWhenNoBookmarks();
        } else {
            renderPaginationButtons(index);
        }
        
        renderedButtons = true;
    }
    
    private void renderTitleDetails(GuiGraphics guiGraphics, Bookmark bookmark, int x) {
        var pose = guiGraphics.pose();
        var name = bookmark.name();
        var timestamp = bookmark.timestamp();
        var date = new Date(timestamp * 1000L);
        var format = new SimpleDateFormat(DATE_FORMAT);
        var titleColor = 0x444444;
        var extraColor = 0xa7a7a7;
        var maxTitleLength = 21;
        var left = -70 + (x * 114);
        var top = 25;
        
        // Render top text.
        pose.pushPose();
        pose.translate(midX - 40f, 20f, 1.0f);
        pose.scale(1.0f, 1.0f, 1.0f);

        if (name.length() > maxTitleLength) {
            name = name.substring(0, maxTitleLength - 1);
        }

        guiGraphics.drawString(font, Component.literal(name), left, top, titleColor, false);

        if (timestamp >= 0) {
            top += 12;
            guiGraphics.drawString(font, Component.literal(format.format(date)), left, top, extraColor, false);
        }
        pose.popPose();
    }
    
    private void renderPhoto(GuiGraphics guiGraphics, Bookmark bookmark, int x) {
        var pose = guiGraphics.pose();
        
        var resource = journal.handlers.tryLoadPhoto(bookmark);
        if (resource != null) {
            var top = 127;
            var left = -168 + (x * 272);
            pose.pushPose();
            pose.translate(midX - 40f, 40f, 1.0f);
            pose.scale(0.42f, 0.24f, 1.0f);
            RenderSystem.setShaderTexture(0, resource);
            guiGraphics.blit(RenderType::guiTextured, resource, left, top, 0.0f, 0.0f, 256, 256, 256, 256);
            pose.popPose();
        }
    }
    
    private void renderPhotoDescriptionHover(GuiGraphics guiGraphics, Bookmark bookmark, int mouseX, int mouseY, int x) {
        var description = bookmark.description();
        if (description.isEmpty()) return;

        var x1 = midX - 110 + (x * 114);
        var y1 = 71;
        var x2 = midX - 110 + (x * 114) + 106;
        var y2 = 131;
        
        if (mouseX >= x1 && mouseX <= x2
            && mouseY >= y1 && mouseY <= y2) {
            guiGraphics.renderTooltip(font, TextHelper.wrap(description), Optional.empty(), mouseX, mouseY);
        }
    }
    
    private void renderPosition(GuiGraphics guiGraphics, Bookmark bookmark, int x) {
        var pose = guiGraphics.pose();
        var top = 174;
        var left = -88 + (x * 138);
        var color = 0xb8907a;
        
        pose.pushPose();
        pose.translate(midX - 25f, 20f, 1.0f);
        pose.scale(0.82f, 0.82f, 1.0f);
        
        var positionText = TextHelper.positionAsText(bookmark.pos());

        TextHelper.drawCenteredString(guiGraphics, font, positionText, left + 50, top + 12, color, false);
        pose.popPose();
    }

    private void renderDimension(GuiGraphics guiGraphics, Bookmark bookmark, int x) {
        var pose = guiGraphics.pose();
        var top = 140;
        var left = -82 + (x * 114);
        var color = 0xb8907a;

        pose.pushPose();
        pose.translate(midX - 25f, 20f, 1.0f);
        pose.scale(1.0f, 1.0f, 1.0f);

        var dimensionText = TextHelper.dimensionAsText(bookmark.dimension());
        TextHelper.drawCenteredString(guiGraphics, font, dimensionText, left + 50, top, color, false);
        pose.popPose();
    }
    
    private void renderDetailsButton(Bookmark bookmark, int x) {
        var left = midX - 110 + (x * 114);
        var top = 135;

        if (!renderedButtons) {
            addRenderableWidget(new CoreButtons.EditButton(left, top, 107,
                b -> journal.handlers.openBookmark(bookmark), Resources.DETAILS));
        }
    }
    
    private void renderWhenNoBookmarks() {
        if (!renderedButtons) {
            addRenderableWidget(new Buttons.NewWhenEmptyButton(midX - (Buttons.NewWhenEmptyButton.WIDTH / 2), 45,
                b -> journal.handlers.makeBookmark()));
        }
    }
    
    private void renderPaginationButtons(int index) {
        var size = journal.handlers.bookmarks().size();
        var pages = size / columns;
        
        if (!renderedButtons) {
            if (page > 1) {
                addRenderableWidget(new CoreButtons.PreviousPageButton(midX - 110, 180,
                    b -> journal.handlers.openJournal(page - 1)));
            }
            if (page < pages || index < size) {
                addRenderableWidget(new CoreButtons.NextPageButton(midX + 80, 180,
                    b -> journal.handlers.openJournal(page + 1)));
            }
        }
    }
}
