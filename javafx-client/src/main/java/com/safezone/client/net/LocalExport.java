package com.safezone.client.net;

import com.safezone.application.service.ExportDB;
import com.safezone.infrastructure.data.envRead;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** Isolates the existing exporter's working files from the chosen destination. */
final class LocalExport {
    private LocalExport() { }

    static Path exportTo(Path destination) throws Exception {
        Path target = destination.toAbsolutePath().normalize();
        Path temporary = Files.createTempDirectory("safezone-export-");
        Process worker = null;
        try {
            String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
            Path java = Path.of(System.getProperty("java.home"), "bin", executable);
            worker = new ProcessBuilder(java.toString(), "-cp", absoluteClassPath(), Worker.class.getName())
                    .directory(temporary.toFile()).redirectErrorStream(true)
                    .redirectOutput(temporary.resolve("worker.log").toFile()).start();
            if (!worker.waitFor(60, TimeUnit.SECONDS)) throw new IOException("Export timed out");
            if (worker.exitValue() != 0) throw new IOException("Export failed");
            Files.move(temporary.resolve("export.xlsx"), target, StandardCopyOption.REPLACE_EXISTING);
            return target;
        } finally {
            if (worker != null && worker.isAlive()) {
                worker.destroyForcibly();
                try { worker.waitFor(5, TimeUnit.SECONDS); }
                catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
            }
            cleanup(temporary);
        }
    }

    private static String absoluteClassPath() {
        Set<String> entries = new LinkedHashSet<>();
        for (String property : new String[]{"java.class.path", "jdk.module.path"}) {
            String value = System.getProperty(property);
            if (value == null) continue;
            for (String entry : value.split(Pattern.quote(File.pathSeparator), -1)) {
                File file = new File(entry.isEmpty() ? "." : entry).getAbsoluteFile();
                entries.add(file.getPath());
                if (property.equals("jdk.module.path") && file.isDirectory()) entries.add(new File(file, "*").getPath());
            }
        }
        return String.join(File.pathSeparator, entries);
    }

    private static void cleanup(Path directory) {
        // The worker owns this directory; walk does not follow symbolic links.
        try (Stream<Path> files = Files.walk(directory)) {
            for (Path file : files.sorted(Comparator.reverseOrder()).toList()) {
                try { Files.deleteIfExists(file); } catch (IOException ignored) { }
            }
        } catch (IOException ignored) { }
    }

    public static final class Worker {
        private Worker() { }

        public static void main(String[] args) {
            try {
                Properties configuration = envRead.readEnv();
                Path generated = ExportDB.exportDatabaseToXlsx(configuration.getProperty("DB_URL"),
                        configuration.getProperty("DB_USER"), configuration.getProperty("DB_PASSWORD"));
                Files.move(generated, Path.of("export.xlsx"));
            } catch (Exception | LinkageError failure) {
                System.err.println("Export failed");
                System.exit(1);
            }
        }
    }
}
