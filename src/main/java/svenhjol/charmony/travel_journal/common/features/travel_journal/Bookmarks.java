package svenhjol.charmony.travel_journal.common.features.travel_journal;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.BlockPos;
import svenhjol.charmony.core.base.Log;
import svenhjol.charmony.travel_journal.TravelJournalMod;
import svenhjol.charmony.travel_journal.client.features.travel_journal.TravelJournal;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class Bookmarks {
    private static final Log LOGGER = new Log(TravelJournalMod.ID, "Bookmarks");
    private static final Map<File, Bookmarks> instances = new HashMap<>();
    private final List<Bookmark> bookmarks = new LinkedList<>();
    private final File session;

    public static Bookmarks instance(File session) {
        if (!instances.containsKey(session)) {
            instances.put(session, new Bookmarks(session));
        }
        return instances.get(session);
    }

    private Bookmarks(File session) {
        this.session = session;
    }

    public int size() {
        return bookmarks.size();
    }

    public boolean isEmpty() {
        return bookmarks.isEmpty();
    }

    public Optional<Bookmark> closest(BlockPos pos) {
        return bookmarks.stream().filter(bookmark -> bookmark.pos().distManhattan(pos) < 32).min((a, b) -> {
            var ap = a.pos().distManhattan(pos);
            var bp = b.pos().distManhattan(pos);
            return Integer.compare(ap, bp);
        });
    }

    public Optional<Bookmark> get(int index) {
        return Optional.ofNullable(bookmarks.get(index));
    }

    /**
     * Get a bookmark by its unique ID.
     * @param id Unique ID of bookmark.
     * @return A bookmark if exists.
     */
    public Optional<Bookmark> get(UUID id) {
        return bookmarks.stream().filter(b -> b.id().equals(id)).findFirst();
    }

    public Bookmarks add(Bookmark bookmark) {
        if (exists(bookmark.id())) {
            throw new RuntimeException("A bookmark with this id already exists: " + bookmark.id());
        }
        bookmarks.add(bookmark);
        return save();
    }

    public Bookmarks update(Bookmark bookmark) {
        var existing = get(bookmark.id()).orElseThrow();
        bookmarks.set(bookmarks.indexOf(existing), bookmark);
        return save();
    }

    public Bookmarks load() {
        try {
            var file = Files.lines(session.toPath());
            var in = file.collect(Collectors.joining("\n"));
            file.close();

            var listType = new TypeToken<LinkedList<Bookmark>>() {}.getType();
            List<Bookmark> bookmarks = new Gson().fromJson(in, listType);

            this.bookmarks.clear();
            this.bookmarks.addAll(bookmarks);
            LOGGER.info("Loaded " + this.bookmarks.size() + " bookmark(s)");

        } catch (Exception e) {
            LOGGER.error("Failed to read bookmarks file: " + e.getMessage());
        }

        return this;
    }

    public Bookmarks save() {
        try {
            var out = new GsonBuilder().setPrettyPrinting().create().toJson(bookmarks);
            var writer = new FileWriter(session);
            writer.write(out);
            writer.close();
            LOGGER.info("Saved " + bookmarks.size() + " bookmark(s)");
        } catch (Exception e) {
            LOGGER.error("Failed to write bookmarks file: " + e.getMessage());
        }

        return this;
    }

    public boolean exists(UUID id) {
        return get(id).isPresent();
    }

    public Bookmarks remove(UUID id) {
        get(id).ifPresent(bookmark -> {
            TravelJournal.feature().handlers.deletePhoto(bookmark);
            bookmarks.remove(bookmark);
        });
        return save();
    }
}
