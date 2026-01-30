package br.com.intuitivecare;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

/*
 * Classe utilitária para lidar com arquivos ZIP.
 * Mantive separada para deixar o código mais organizado.
 */
public class ZipUtils {

    /*
     * Extrai um ZIP recebido em memória (byte[])
     * para um diretório temporário.
     */
    public static void extrair(byte[] zipBytes, Path destino)
            throws IOException {

        // ByteArrayInputStream permite trabalhar com ZIP
        // sem salvar o arquivo no disco antes
        try (ZipInputStream zis =
                     new ZipInputStream(
                             new ByteArrayInputStream(zipBytes))) {

            ZipEntry entry;

            // Percorre todos os arquivos do ZIP
            while ((entry = zis.getNextEntry()) != null) {

                Path arquivo = destino.resolve(entry.getName());

                // Se for diretório, apenas cria
                if (entry.isDirectory()) {
                    Files.createDirectories(arquivo);
                } else {
                    // Garante que o diretório pai exista
                    Files.createDirectories(arquivo.getParent());

                    // Copia o conteúdo do ZIP para o arquivo
                    Files.copy(zis, arquivo,
                            StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    /*
     * Compacta o CSV final em um arquivo ZIP,
     * conforme solicitado no teste.
     */
    public static void compactar(Path arquivo, Path zipFinal)
            throws IOException {

        try (ZipOutputStream zos =
                     new ZipOutputStream(
                             new FileOutputStream(zipFinal.toFile()))) {

            // Cria a entrada do ZIP com o nome do CSV
            zos.putNextEntry(
                    new ZipEntry(arquivo.getFileName().toString()));

            // Copia o CSV para dentro do ZIP
            Files.copy(arquivo, zos);
            zos.closeEntry();
        }
    }
}