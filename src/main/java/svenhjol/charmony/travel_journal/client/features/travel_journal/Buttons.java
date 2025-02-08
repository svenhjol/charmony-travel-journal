package svenhjol.charmony.travel_journal.client.features.travel_journal;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import svenhjol.charmony.travel_journal.TravelJournalMod;

public class Buttons {
    public static final WidgetSprites NEXT_PAGE_BUTTON = makeButton("next_page");
    public static final WidgetSprites PREVIOUS_PAGE_BUTTON = makeButton("previous_page");
    public static final WidgetSprites SEND_TO_PLAYER_BUTTON = makeButtonWithDisabled("send_to_player");

    public static class NewWhenEmptyButton extends Button {
        public static int WIDTH = 100;
        public static int HEIGHT = 20;
        static Component TEXT = Resources.NEW_BOOKMARK;

        public NewWhenEmptyButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class NewBookmarkButton extends Button {
        public static int WIDTH = 110;
        public static int HEIGHT = 20;
        static Component TEXT = Resources.NEW_BOOKMARK;

        public NewBookmarkButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class SendToPlayerButton extends ImageButton {
        public static int WIDTH = 20;
        public static int HEIGHT = 18;
        static WidgetSprites SPRITES = SEND_TO_PLAYER_BUTTON;
        static Component TEXT = Resources.SEND_TO_PLAYER;

        public SendToPlayerButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    public static class NextPageButton extends ImageButton {
        public static int WIDTH = 20;
        public static int HEIGHT = 19;
        static WidgetSprites SPRITES;
        static Component TEXT;

        public NextPageButton(int x, int y, Button.OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            this.setTooltip(Tooltip.create(TEXT));
        }

        static {
            SPRITES = Buttons.NEXT_PAGE_BUTTON;
            TEXT = Resources.NEXT_PAGE;
        }
    }

    public static class PreviousPageButton extends ImageButton {
        public static int WIDTH = 20;
        public static int HEIGHT = 19;
        static WidgetSprites SPRITES;
        static Component TEXT;

        public PreviousPageButton(int x, int y, Button.OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            this.setTooltip(Tooltip.create(TEXT));
        }

        static {
            SPRITES = Buttons.PREVIOUS_PAGE_BUTTON;
            TEXT = Resources.PREVIOUS_PAGE;
        }
    }

    static WidgetSprites makeButton(String name) {
        return new WidgetSprites(
            TravelJournalMod.id("widget/buttons/" + name + "_button"),
            TravelJournalMod.id("widget/buttons/" + name + "_button_highlighted"));
    }

    static WidgetSprites makeButtonWithDisabled(String name) {
        return new WidgetSprites(
            TravelJournalMod.id("widget/buttons/" + name + "_button"),
            TravelJournalMod.id("widget/buttons/" + name + "_button_disabled"),
            TravelJournalMod.id("widget/buttons/" + name + "_button_highlighted"));
    }
}
