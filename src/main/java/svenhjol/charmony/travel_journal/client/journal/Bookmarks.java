package svenhjol.charmony.travel_journal.client.journal;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import svenhjol.charmony.scaffold.base.Log;
import svenhjol.charmony.travel_journal.TravelJournal;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class Bookmarks {
    private static final Log LOGGER = new Log(TravelJournal.ID, "Bookmarks");
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

    public Bookmarks add(Bookmark bookmark) {
        if (exists(bookmark.id())) {
            remove(bookmark.id());
        }
        bookmarks.add(bookmark);
        return this;
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

    public Optional<Bookmark> bookmark(UUID id) {
        return bookmarks.stream().filter(b -> b.id().equals(id)).findFirst();
    }

    public boolean exists(UUID id) {
        return bookmark(id).isPresent();
    }

    public Bookmarks remove(UUID id) {
        bookmark(id).ifPresent(bookmarks::remove);
        return this;
    }
}
