package com.evandev.spicedcider.resource;

import com.evandev.spicedcider.SpicedCider;
import com.google.gson.Gson;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ResourceBaker {

    public static void bakeFromManifest(Path cacheDir, Path manifestPath, Path resourcePacksDir) {
        if (!Files.exists(manifestPath)) return;

        Path cacheZip = cacheDir.resolve("spicedcider_global_jit.zip");

        if (Files.exists(cacheZip)) return;

        try {
            Files.createDirectories(cacheDir);

            try (Reader reader = Files.newBufferedReader(manifestPath);
                 ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(cacheZip))) {

                Gson gson = new Gson();
                PackManifest manifestObj = gson.fromJson(reader, PackManifest.class);

                if (manifestObj == null || manifestObj.packs == null) return;

                Set<String> addedEntries = new HashSet<>();

                ZipEntry metaEntry = new ZipEntry("pack.mcmeta");
                zos.putNextEntry(metaEntry);
                String mcmeta = "{\"pack\":{\"pack_format\":34,\"description\":\"Spiced Cider Global JIT\"}}";
                zos.write(mcmeta.getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
                addedEntries.add("pack.mcmeta");

                for (Map.Entry<String, List<String>> entry : manifestObj.packs.entrySet()) {
                    String sourceZipName = entry.getKey();
                    List<String> filesToExtract = entry.getValue();

                    Path packFile = resourcePacksDir.resolve(sourceZipName);
                    if (!Files.exists(packFile)) continue;

                    try (ZipFile zip = new ZipFile(packFile.toFile())) {
                        for (String targetPath : filesToExtract) {
                            if (addedEntries.contains(targetPath)) continue;

                            ZipEntry zipEntry = zip.getEntry(targetPath);
                            if (zipEntry != null) {
                                zos.putNextEntry(new ZipEntry(targetPath));
                                try (InputStream is = zip.getInputStream(zipEntry)) {
                                    is.transferTo(zos);
                                }
                                zos.closeEntry();
                                addedEntries.add(targetPath);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            SpicedCider.LOGGER.error("Failed to bake global JIT resource pack", e);
            try {
                Files.deleteIfExists(cacheZip);
            } catch (Exception ignored) {
            }
        }
    }
}