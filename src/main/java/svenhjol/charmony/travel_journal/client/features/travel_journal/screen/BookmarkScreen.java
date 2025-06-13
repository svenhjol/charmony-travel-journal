package svenhjol.charmony.travel_journal.client.features.travel_journal.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charmony.api.core.Color;
import svenhjol.charmony.core.base.Environment;
import svenhjol.charmony.core.client.CoreButtons;
import svenhjol.charmony.core.helpers.TextComponentHelper;
import svenhjol.charmony.travel_journal.client.features.travel_journal.Buttons;
import svenhjol.charmony.travel_journal.client.features.travel_journal.Resources;
import svenhjol.charmony.travel_journal.common.features.travel_journal.Bookmark;

@SuppressWarnings("unused")
public class BookmarkScreen extends BaseScreen {
    private final Bookmark.Mutable bookmark;
    private final ResourceLocation dimension;
    private final BlockPos pos;
    private EditBox name;
    private MultiLineEditBox description;
    private Button sendToPlayerButton;
    private Button takeNewPhotoButton;
    
    public BookmarkScreen(Bookmark bookmark) {
        super(Component.literal(bookmark.name()));
        this.pos = bookmark.blockPos();
        this.dimension = bookmark.dimensionId();
        this.bookmark = new Bookmark.Mutable(bookmark);
    }

    @Override
    protected void init() {
        super.init();
        var inputWidth = 220;
        var nameHeight = 15;
        var descriptionHeight = 44;
        var top = 107;
        
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
        
        description = new MultiLineEditBox(font, midX - (inputWidth / 2), top + 28, inputWidth, descriptionHeight,
            Resources.EDIT_DESCRIPTION, CommonComponents.EMPTY, new Color(0xffffff).getArgbColor(), false, 0xffff00, true, true);

        description.setFocused(false);
        description.setValue(bookmark.description);
        description.setValueListener(val -> bookmark.description = val);
        description.setCharacterLimit(157);
        description.visible = true;
        addRenderableWidget(description);

        // Add the take new photo button
        sendToPlayerButton = new Buttons.SendToPlayerButton(midX - 84, top + 29 + descriptionHeight + 2,
            b -> sendToNearbyPlayer());
        sendToPlayerButton.active = false;
        sendToPlayerButton.visible = Environment.usesCharmonyServer();
        addRenderableWidget(sendToPlayerButton);

        // Add the send to player button
        takeNewPhotoButton = new Buttons.TakeNewPhotoButton(midX - 110, top + 29 + descriptionHeight + 2,
            b -> takeNewPhoto());
        takeNewPhotoButton.active = false;
        takeNewPhotoButton.visible = true;
        addRenderableWidget(takeNewPhotoButton);

        // Buttons at bottom
        addRenderableWidget(new CoreButtons.DeleteButton((int) (midX - (CoreButtons.DeleteButton.WIDTH * 1.5)) - 5, 216,
            b -> deleteAndClose()));
        addRenderableWidget(new CoreButtons.CancelButton(midX - (CoreButtons.CancelButton.WIDTH / 2), 216,
            b -> onClose()));
        addRenderableWidget(new CoreButtons.SaveButton(midX + (CoreButtons.SaveButton.WIDTH / 2) + 5, 216,
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
        renderDetails(guiGraphics);
        renderUtilityButtons(guiGraphics, mouseX, mouseY, delta);
        
        name.render(guiGraphics, mouseX, mouseY, delta);
        description.render(guiGraphics, mouseX, mouseY, delta);

        // Name and description label
        var textColor = new Color(0x404040);
        guiGraphics.drawString(font, Resources.NAME_TEXT, midX - 109, 98, textColor.getArgbColor(), false);
        guiGraphics.drawString(font, Resources.DESCRIPTION, midX - 109, 126, textColor.getArgbColor(), false);
    }

    private void renderPhoto(GuiGraphics guiGraphics) {
        var pose = guiGraphics.pose();
        var resource = journal.handlers.tryLoadPhoto(bookmark.toImmutable());

        pose.pushMatrix();
        var top = 24; // This is scaled by pose.scale()
        var left = -169; // This is scaled by pose.scale()
        pose.translate(midX - 40f, 33f);
        pose.scale(0.41f, 0.22f);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, resource, left, top, 0.0f, 0.0f, 256, 256, 256, 256);
        pose.popMatrix();
    }
    
    private void renderDetails(GuiGraphics guiGraphics) {
        var pose = guiGraphics.pose();
        var color = getDetailsColor(bookmark.toImmutable());
        
        pose.pushMatrix();
        var top = 23; // This is scaled by pose.scale()
        var left = 43; // This is scaled by pose.scale()
        pose.translate(midX - 25f, 20f);
        pose.scale(0.82f, 0.82f);

        // Author
        if (bookmark.author != null && !bookmark.author.isEmpty()) {
            var authorText = Component.translatable(Resources.AUTHOR, bookmark.author);
            guiGraphics.drawString(font, authorText, left, top, color.getArgbColor(), false);
        } else {
            top = 12;
        }

        // Dimension
        var dimensionText = TextComponentHelper.dimensionAsText(ResourceKey.create(Registries.DIMENSION, dimension));
        guiGraphics.drawString(font, Component.translatable(Resources.DIMENSION).withStyle(ChatFormatting.BOLD), left, top + 20, color.getArgbColor(), false);
        guiGraphics.drawString(font, dimensionText, left, top + 31, color.getArgbColor(), false);

        // Block position
        var positionText = TextComponentHelper.positionAsText(pos);
        guiGraphics.drawString(font, Component.translatable(Resources.POSITION).withStyle(ChatFormatting.BOLD), left, top + 49, color.getArgbColor(), false);
        guiGraphics.drawString(font, positionText, left, top + 60, color.getArgbColor(), false);

        pose.popMatrix();
    }

    private void renderUtilityButtons(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        if (minecraft.level.getGameTime() % 20 == 0) {
            sendToPlayerButton.active = journal.handlers.canSendBookmark();
            takeNewPhotoButton.active = journal.handlers.canTakeNewPhoto(bookmark.toImmutable());
        }
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

    private void sendToNearbyPlayer() {
        journal.handlers.openSendBookmark(bookmark.toImmutable());
    }

    private void takeNewPhoto() {
        journal.handlers.takePhoto(bookmark.toImmutable(), false);
    }

    @Override
    public void onClose() {
        journal.handlers.openJournal();
    }
}
