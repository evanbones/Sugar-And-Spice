package com.evandev.spicedcider.resource;

import com.evandev.spicedcider.SpicedCider;
import com.google.gson.Gson;

import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ResourceBaker {

    public static void bakeFromManifest(Path cacheDir, Path manifestPath, Path resourcePacksDir) {
        if (!Files.exists(manifestPath)) return;

        try {
            Files.createDirectories(cacheDir);

            try (Reader reader = Files.newBufferedReader(manifestPath)) {
                Gson gson = new Gson();
                PackManifest manifestObj = gson.fromJson(reader, PackManifest.class);

                if (manifestObj == null || manifestObj.packs == null) return;

                for (Map.Entry<String, List<String>> entry : manifestObj.packs.entrySet()) {
                    String sourceZipName = entry.getKey();
                    List<String> filesToExtract = entry.getValue();

                    Path packFile = resourcePacksDir.resolve(sourceZipName);
                    if (!Files.exists(packFile)) continue;

                    String cacheZipName = sourceZipName.replace(".zip", "_jit.zip");
                    Path cacheZip = cacheDir.resolve(cacheZipName);

                    if (Files.exists(cacheZip)) continue;

                    try (ZipFile zip = new ZipFile(packFile.toFile());
                         ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(cacheZip))) {

                        ZipEntry metaEntry = zip.getEntry("pack.mcmeta");
                        if (metaEntry != null) {
                            zos.putNextEntry(new ZipEntry("pack.mcmeta"));
                            try (InputStream is = zip.getInputStream(metaEntry)) {
                                is.transferTo(zos);
                            }
                            zos.closeEntry();
                        }

                        for (String targetPath : filesToExtract) {
                            if (targetPath.equals("pack.mcmeta")) continue;

                            ZipEntry zipEntry = zip.getEntry(targetPath);
                            if (zipEntry != null) {
                                zos.putNextEntry(new ZipEntry(targetPath));
                                try (InputStream is = zip.getInputStream(zipEntry)) {
                                    is.transferTo(zos);
                                }
                                zos.closeEntry();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            SpicedCider.LOGGER.error("Failed to bake resources", e);
        }
    }
}