package svenhjol.charmony.travel_journal.client.features.travel_journal;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charmony.core.Charmony;

public class Resources {
    public static final Pair<Integer, Integer> BACKGROUND_DIMENSIONS = Pair.of(256, 208);
    public static final ResourceLocation BACKGROUND = Charmony.id("textures/gui/travel_journal.png");
    public static final ResourceLocation PHOTO_BACKGROUND = Charmony.id("textures/gui/photo_background.png");
    public static final Component DESCRIPTION = Component.translatable("gui.charmony.description");
    public static final Component DETAILS = Component.translatable("gui.charmony.details");
    public static final Component EDIT_DESCRIPTION = Component.translatable("gui.charmony.edit_description");
    public static final Component EDIT_NAME = Component.translatable("gui.charmony.edit_name");
    public static final Component NAME_TEXT = Component.translatable("gui.charmony.name");
    public static final Component NEW_BOOKMARK = Component.translatable("gui.charmony.new_bookmark");
    public static final Component NEXT_PAGE = Component.translatable("gui.charmony.next_page");
    public static final Component PREVIOUS_PAGE = Component.translatable("gui.charmony.previous_page");
    public static final Component SEND_TO_PLAYER = Component.translatable("gui.charmony.send_to_player");
    public static final Component TAKE_NEW_PHOTO = Component.translatable("gui.charmony.take_new_photo");
    public static final Component TRAVEL_JOURNAL = Component.translatable("gui.charmony.travel_journal");
    public static final Component WAITING_FOR_PLAYERS = Component.translatable("gui.charmony.waiting_for_players");
    public static final String AUTHOR = "gui.charmony.author";
    public static final String DIMENSION = "gui.charmony.dimension";
    public static final String POSITION = "gui.charmony.position";
}
