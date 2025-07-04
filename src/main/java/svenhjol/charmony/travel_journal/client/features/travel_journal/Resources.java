package svenhjol.charmony.travel_journal.client.features.travel_journal;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charmony.travel_journal.TravelJournalMod;

public class Resources {
    public static final Pair<Integer, Integer> BACKGROUND_DIMENSIONS = Pair.of(256, 208);
    public static final ResourceLocation BACKGROUND = TravelJournalMod.id("textures/gui/travel_journal.png");
    public static final ResourceLocation PHOTO_BACKGROUND = TravelJournalMod.id("textures/gui/photo_background.png");
    public static final Component DESCRIPTION = Component.translatable("gui.charmony-travel-journal.description");
    public static final Component DETAILS = Component.translatable("gui.charmony-travel-journal.details");
    public static final Component EDIT_DESCRIPTION = Component.translatable("gui.charmony-travel-journal.editDescription");
    public static final Component EDIT_NAME = Component.translatable("gui.charmony-travel-journal.editName");
    public static final Component NAME_TEXT = Component.translatable("gui.charmony-travel-journal.name");
    public static final Component NEW_BOOKMARK = Component.translatable("gui.charmony-travel-journal.newBookmark");
    public static final Component SEND_TO_PLAYER = Component.translatable("gui.charmony-travel-journal.sendToPlayer");
    public static final Component TRAVEL_JOURNAL = Component.translatable("gui.charmony-travel-journal.travelJournal");
    public static final Component NEXT_PAGE = Component.translatable("gui.charmony-travel-journal.nextPage");
    public static final Component PREVIOUS_PAGE = Component.translatable("gui.charmony-travel-journal.previousPage");
    public static final String AUTHOR = "gui.charmony-travel-journal.author";
    public static final String DIMENSION = "gui.charmony-travel-journal.dimension";
    public static final String POSITION = "gui.charmony-travel-journal.position";
    public static final String XYZ_KEY = "gui.charmony-travel-journal.xyz";
}
