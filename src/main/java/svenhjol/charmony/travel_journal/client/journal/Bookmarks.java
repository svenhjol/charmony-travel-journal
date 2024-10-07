package svenhjol.charmony.travel_journal.client.journal;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import svenhjol.charmony.scaffold.base.Log;
import svenhjol.charmony.travel_journal.TravelJournal;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Bookmarks {
    private static final Log LOGGER = new Log(TravelJournal.ID, "Bookmarks");
    private static final Map<File, Bookmarks> instances = new HashMap<>();
    private final List<Bookmark> bookmarks = new LinkedList<>();
    private final File sessionFile;

    public static Bookmarks instance(File sessionFile) {
        if (!instances.containsKey(sessionFile)) {
            instances.put(sessionFile, new Bookmarks(sessionFile));
        }
        return instances.get(sessionFile);
    }

    private Bookmarks(File sessionFile) {
        this.sessionFile = sessionFile;
    }

    public Bookmarks load() {
        try {
            var file = Files.lines(sessionFile.toPath());
            var in = file.collect(Collectors.joining("\n"));
            file.close();

            var listType = new TypeToken<LinkedList<Bookmark>>() {}.getType();
            List<Bookmark> bookmarks = new Gson().fromJson(in, listType);

            this.bookmarks.clear();
            this.bookmarks.addAll(bookmarks);
            LOGGER.info("Loaded all bookmarks from " + sessionFile);

        } catch (Exception e) {
            LOGGER.error("Failed to read bookmarks file: " + e.getMessage());
        }

        return this;
    }

    public Bookmarks save() {

        try {
            var out = new Gson().toJson(bookmarks);
            var writer = new FileWriter(sessionFile);
            writer.write(out);
            writer.close();
            LOGGER.info("Saved all bookmarks to " + sessionFile);
        } catch (Exception e) {
            LOGGER.error("Failed to write bookmarks file: " + e.getMessage());
        }

        return this;
    }
}
