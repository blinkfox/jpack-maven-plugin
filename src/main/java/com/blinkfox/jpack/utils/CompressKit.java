package com.blinkfox.jpack.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

/**
 * 压缩工具类.
 *
 * @author blinkfox on 2019-04-23.
 */
public final class CompressKit {

    /**
     * 私有构造方法.
     */
    private CompressKit() {
    }

    /**
     * 压缩文件夹为 zip 格式.
     *
     * @param dir     文件夹
     * @param zipPath zip全路径名
     */
    public static void zip(String dir, String zipPath) {
        compressFilesZip(getDirFiles(dir), zipPath, dir);
    }

    /**
     * 递归取到当前目录所有文件.
     *
     * @param dir 文件夹
     * @return 文件集合
     */
    private static List<String> getDirFiles(String dir) {
        File[] files = new File(dir).listFiles();
        if (files == null) {
            return new ArrayList<>();
        }

        List<String> filePaths = new ArrayList<>(files.length);
        for (File file : files) {
            if (file.isDirectory()) {
                filePaths.add(file.getAbsolutePath());
                filePaths.addAll(getDirFiles(file.getAbsolutePath()));
            } else {
                filePaths.add(file.getAbsolutePath());
            }
        }
        return filePaths;
    }

    /**
     * 文件名处理.
     *
     * @param dir  文件夹
     * @param path 路径
     * @return 文件名
     */
    private static String getFilePathName(String dir, String path) {
        String p = path.replace(dir + File.separator, "");
        p = p.replace("\\", "/");
        return p;
    }

    /**
     * 把文件压缩成 zip 格式.
     *
     * @param files       需要压缩的文件数组
     * @param zipFilePath 压缩后的 zip 文件路径,如"D:/test/aa.zip";
     * @param dir         待压缩的目录
     */
    private static void compressFilesZip(List<String> files, String zipFilePath, String dir) {
        if (files == null || files.isEmpty()) {
            return;
        }

        try (ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(new File(zipFilePath))) {
            zaos.setUseZip64(Zip64Mode.AsNeeded);

            //将每个文件用ZipArchiveEntry封装
            //再用ZipArchiveOutputStream写到压缩文件中
            for (String strfile : files) {
                File file = new File(strfile);
                zaos.putArchiveEntry(new ZipArchiveEntry(file, getFilePathName(dir, strfile)));
                if (!file.isDirectory()) {
                    compressFile(zaos, file);
                }
            }
            zaos.finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 压缩写入单个文件.
     *
     * @param zaos ZipArchiveOutputStream对象
     * @param file 单个的文件对象
     */
    private static void compressFile(ZipArchiveOutputStream zaos, File file) {
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                //把缓冲区的字节写入到 ZipArchiveEntry.
                zaos.write(buffer, 0, len);
            }
            zaos.closeArchiveEntry();
        } catch (IOException e) {
            throw new RuntimeException("压缩文件异常.", e);
        }
    }

    /**
     * 将给定目录压缩成 `.tar.gz` 格式.
     *
     * @param dirPath 文件夹路径
     * @param tarGzPath 压缩文件的路径
     * @throws FileNotFoundException FileNotFoundException
     * @throws IOException IOException
     */
    public static void tarGz(String dirPath, String tarGzPath) throws FileNotFoundException, IOException {
        try (FileOutputStream fOut = new FileOutputStream(new File(tarGzPath));
                BufferedOutputStream bOut = new BufferedOutputStream(fOut);
                GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(bOut);
                TarArchiveOutputStream tOut = new TarArchiveOutputStream(gzOut)) {
            addFileToTarGz(tOut, dirPath, "");
            tOut.finish();
        }
    }

    /**
     * 将文件添加到 tar.gz 压缩文件夹中.
     *
     * @param tOut TarArchiveOutputStream实例
     * @param dirPath 文件夹路径
     * @param base 基础路径
     */
    private static void addFileToTarGz(TarArchiveOutputStream tOut, String dirPath, String base)
            throws IOException {
        File f = new File(dirPath);
        String entryName = base + f.getName();
        tOut.putArchiveEntry(new TarArchiveEntry(f, entryName));

        if (f.isFile()) {
            FileInputStream in = new FileInputStream(f);
            IOUtils.copy(in, tOut);
            in.close();
            tOut.closeArchiveEntry();
        } else {
            tOut.closeArchiveEntry();
            File[] children = f.listFiles();
            if (children != null && children.length > 0) {
                for (File child : children) {
                    addFileToTarGz(tOut, child.getAbsolutePath(), entryName + "/");
                }
            }
        }
    }

}
