package svenhjol.charmony.travel_journal.client.journal.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charmony.travel_journal.client.journal.Journal;
import svenhjol.charmony.travel_journal.client.journal.Resources;
import svenhjol.charmony.travel_journal.helpers.TextHelper;

public abstract class BaseScreen extends Screen {
    protected final Journal journal;
    protected int midX;
    protected int backgroundWidth;
    protected int backgroundHeight;
    
    public BaseScreen(Component component) {
        super(component);
        this.journal = Journal.feature();
    }

    @Override
    protected void init() {
        super.init();

        if (minecraft == null) return;

        midX = width / 2;
        backgroundWidth = Resources.BACKGROUND_DIMENSIONS.getFirst();
        backgroundHeight = Resources.BACKGROUND_DIMENSIONS.getSecond();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTitle(guiGraphics, midX + 2, 24);
    }
    
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(guiGraphics, mouseX, mouseY, delta);
        var x = (width - backgroundWidth) / 2;
        var y = 5;
        guiGraphics.blit(RenderType::guiTextured, getBackgroundTexture(), x, y, 0.0f, 0.0f, backgroundWidth, backgroundHeight, 256, 256);
    }
    
    @Override
    public void onClose() {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            player.playSound(journal.registers.interactSound, 0.5f, 1.0f);
        }
        super.onClose();
    }
    
    protected void renderTitle(GuiGraphics guiGraphics, int x, int y) {
        MutableComponent title = (MutableComponent)getTitle();
        TextHelper.drawCenteredString(guiGraphics, font, title.withStyle(ChatFormatting.BOLD), x, y, 0x702f20, false);
    }
    
    protected ResourceLocation getBackgroundTexture() {
        return Resources.BACKGROUND;
    }
    
    protected Minecraft minecraft() {
        return Minecraft.getInstance();
    }
}
