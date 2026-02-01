package br.com.intuitivecare;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

/*
 * Utilitário ZIP
 */
public class ZipUtils {

    public static void extrair(byte[] zipBytes, Path destino) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {

                Path arquivo = destino.resolve(entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(arquivo);
                } else {
                    Files.createDirectories(arquivo.getParent());
                    Files.copy(zis, arquivo, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    public static void compactar(Path arquivo, Path zipFinal) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFinal.toFile()))) {
            zos.putNextEntry(new ZipEntry(arquivo.getFileName().toString()));
            Files.copy(arquivo, zos);
            zos.closeEntry();
        }
    }
}
