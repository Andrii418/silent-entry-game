package pl.detektyw;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.Properties;

/** Jeden zapis pierwszej sprawy. Plik powstaje poza katalogiem projektu. */
final class CaseSave {
    enum Room { LABORATORIUM, KORYTARZ, BIURO, MAGAZYN }

    record Progress(Investigation investigation, Room room, boolean solved) {}

    private CaseSave() {}

    static Path path() {
        String override = System.getProperty("casefile.save.path");
        return override == null ? Path.of(System.getProperty("user.home"),
                ".casefile-21-14", "save.properties") : Path.of(override);
    }

    static Optional<Progress> load() {
        if (!Files.exists(path())) return Optional.empty();
        Properties data = new Properties();
        try (InputStream input = Files.newInputStream(path())) {
            data.load(input);
            if (!"1".equals(data.getProperty("version"))) return Optional.empty();
            Investigation investigation = Investigation.readState(data);
            Room room = Room.valueOf(data.getProperty("room"));
            boolean solved = Boolean.parseBoolean(data.getProperty("solved", "false"));
            if (solved && !investigation.canAccuse()) return Optional.empty();
            return Optional.of(new Progress(investigation, room, solved));
        } catch (IOException | IllegalArgumentException | NullPointerException error) {
            return Optional.empty();
        }
    }

    static void save(Progress progress) throws IOException {
        Properties data = new Properties();
        data.setProperty("version", "1");
        data.setProperty("room", progress.room().name());
        data.setProperty("solved", Boolean.toString(progress.solved()));
        progress.investigation().writeState(data);

        Path target = path();
        if (target.getParent() != null) Files.createDirectories(target.getParent());
        Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
        try (OutputStream output = Files.newOutputStream(temporary)) {
            data.store(output, "Casefile 21:14");
        }
        try {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException error) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
