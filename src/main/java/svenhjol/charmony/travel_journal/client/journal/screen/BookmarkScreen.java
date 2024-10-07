package svenhjol.charmony.travel_journal.client.journal.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import svenhjol.charmony.scaffold.client.CoreButtons;
import svenhjol.charmony.travel_journal.client.journal.Bookmark;
import svenhjol.charmony.travel_journal.client.journal.Resources;
import svenhjol.charmony.travel_journal.helpers.TextHelper;

public class BookmarkScreen extends BaseScreen {
    private final Bookmark.Mutable bookmark;
    private EditBox name;
    private MultiLineEditBox description;
    
    public BookmarkScreen(Bookmark bookmark) {
        super(Component.literal(bookmark.name()));
        this.bookmark = new Bookmark.Mutable(bookmark);
    }

    @Override
    protected void init() {
        super.init();
        var inputWidth = 220;
        var nameHeight = 15;
        var descriptionHeight = 46;
        var top = 110;
        
        name = new EditBox(font, midX - (inputWidth / 2), top, inputWidth, nameHeight, Resources.EDIT_NAME);
        name.setFocused(true);
        name.setValue(bookmark.name);
        name.setResponder(val -> bookmark.name = val);
        name.setCanLoseFocus(true);
        name.setTextColor(-1);
        name.setTextColorUneditable(-1);
        name.setBordered(true);
        name.setMaxLength(32);
        name.setEditable(true);

        addRenderableWidget(name);
        setFocused(name);
        
        description = new MultiLineEditBox(font, midX - (inputWidth / 2), top + 29, inputWidth, descriptionHeight,
            Resources.EDIT_DESCRIPTION, Component.empty());

        description.setFocused(false);
        description.setValue(bookmark.description);
        description.setValueListener(val -> bookmark.description = val);
        description.setCharacterLimit(140);
        
        addRenderableWidget(description);

        top = 216;
        addRenderableWidget(new CoreButtons.DeleteButton((int) (midX - (CoreButtons.DeleteButton.WIDTH * 1.5)) - 5, top,
            b -> deleteAndClose()));
        addRenderableWidget(new CoreButtons.CancelButton(midX - (CoreButtons.CancelButton.WIDTH / 2), top,
            b -> onClose()));
        addRenderableWidget(new CoreButtons.SaveButton(midX + (CoreButtons.SaveButton.WIDTH / 2) + 5, top,
            b -> saveAndClose()));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);

        renderPhoto(guiGraphics);
        renderDimensionAndPosition(guiGraphics);
        
        name.render(guiGraphics, mouseX, mouseY, delta);
        description.render(guiGraphics, mouseX, mouseY, delta);
        
        var textColor = 0x404040;
        guiGraphics.drawString(font, Resources.NAME_TEXT, midX - 109, 101, textColor, false);
        guiGraphics.drawString(font, Resources.DESCRIPTION, midX - 109, 129, textColor, false);
    }

    private void renderPhoto(GuiGraphics guiGraphics) {
        var pose = guiGraphics.pose();
        var resource = journal.handlers.tryLoadPhoto(bookmark.toImmutable());
        
        if (resource != null) {
            pose.pushPose();
            var top = 24; // This is scaled by pose.scale()
            var left = -169; // This is scaled by pose.scale()
            pose.translate(midX - 40f, 33f, 1.0f);
            pose.scale(0.41f, 0.22f, 1.0f);
            RenderSystem.setShaderTexture(0, resource);
            guiGraphics.blit(RenderType::guiTextured, resource, left, top, 0.0f, 0.0f, 256, 256, 256, 256);
            pose.popPose();
        }
    }
    
    private void renderDimensionAndPosition(GuiGraphics guiGraphics) {
        var pose = guiGraphics.pose();
        var color = 0xb8907a;
        
        pose.pushPose();
        var top = 30; // This is scaled by pose.scale()
        var left = 43; // This is scaled by pose.scale()
        pose.translate(midX - 25f, 20f, 1.0f);
        pose.scale(0.82f, 0.82f, 1.0f);

        var positionText = TextHelper.positionAsText(bookmark.pos);
        var dimensionText = TextHelper.dimensionAsText(bookmark.dimension);
        
        guiGraphics.drawString(font, Component.translatable(Resources.DIMENSION).withStyle(ChatFormatting.BOLD), left, top, color, false);
        guiGraphics.drawString(font, dimensionText, left, top + 12, color, false);

        guiGraphics.drawString(font, Component.translatable(Resources.POSITION).withStyle(ChatFormatting.BOLD), left, top + 30, color, false);
        guiGraphics.drawString(font, positionText, left, top + 42, color, false);
        pose.popPose();
    }
    
    private void saveAndClose() {
        // Validation first.
        if (bookmark.name.isEmpty()) {
            bookmark.name = Resources.NEW_BOOKMARK.getString();
        }

        journal.handlers.bookmarks().update(bookmark.toImmutable());
        onClose();
    }
    
    private void deleteAndClose() {
        journal.handlers.bookmarks().remove(bookmark.id);
        onClose();
    }

    @Override
    public void onClose() {
        journal.handlers.openJournal();
    }
}
