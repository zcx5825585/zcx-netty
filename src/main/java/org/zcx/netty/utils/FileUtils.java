package org.zcx.netty.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class FileUtils {
    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    public static boolean fileExists(String path){
        File file=new File(path);
        return file.exists();
    }

    public static void clearFiles(String workspaceRootPath) {
        File file = new File(workspaceRootPath);
        deleteFile(file);
    }

    public static void deleteFile(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteFile(files[i]);
                }
            }
        }
        file.delete();
    }

    public static void fileWrite(String str, String fileNamePath) throws IOException {
        FileWriter writer = null;
        try {
            File file = new File(fileNamePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            writer = new FileWriter(file, true);
            writer.write(str + System.getProperty("line.separator"));

        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public static void changePermission(File dirFile, int mode) throws IOException {
        char[] modes = Integer.toOctalString(mode).toCharArray();
        if (modes.length != 3) {
            return;
        }
        Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
        switch (modes[0]) {
            case '1':
                perms.add(PosixFilePermission.OWNER_EXECUTE);
                break;
            case '2':
                perms.add(PosixFilePermission.OWNER_WRITE);
                break;
            case '4':
                perms.add(PosixFilePermission.OWNER_READ);
                break;
            case '5':
                perms.add(PosixFilePermission.OWNER_READ);
                perms.add(PosixFilePermission.OWNER_EXECUTE);
                break;
            case '6':
                perms.add(PosixFilePermission.OWNER_READ);
                perms.add(PosixFilePermission.OWNER_WRITE);
                break;
            case '7':
                perms.add(PosixFilePermission.OWNER_READ);
                perms.add(PosixFilePermission.OWNER_WRITE);
                perms.add(PosixFilePermission.OWNER_EXECUTE);
                break;

            default:
                break;
        }
        switch (modes[1]) {
            case '1':
                perms.add(PosixFilePermission.GROUP_EXECUTE);
                break;
            case '2':
                perms.add(PosixFilePermission.GROUP_WRITE);
                break;
            case '4':
                perms.add(PosixFilePermission.GROUP_READ);
                break;
            case '5':
                perms.add(PosixFilePermission.GROUP_READ);
                perms.add(PosixFilePermission.GROUP_EXECUTE);
                break;
            case '6':
                perms.add(PosixFilePermission.GROUP_READ);
                perms.add(PosixFilePermission.GROUP_WRITE);
                break;
            case '7':
                perms.add(PosixFilePermission.GROUP_READ);
                perms.add(PosixFilePermission.GROUP_WRITE);
                perms.add(PosixFilePermission.GROUP_EXECUTE);
                break;
            default:
                break;
        }
        switch (modes[2]) {
            case '1':
                perms.add(PosixFilePermission.OTHERS_EXECUTE);
                break;
            case '2':
                perms.add(PosixFilePermission.OTHERS_WRITE);
                break;
            case '4':
                perms.add(PosixFilePermission.OTHERS_READ);
                break;
            case '5':
                perms.add(PosixFilePermission.OTHERS_EXECUTE);
                perms.add(PosixFilePermission.OTHERS_READ);
                break;
            case '6':
                perms.add(PosixFilePermission.OTHERS_READ);
                perms.add(PosixFilePermission.OTHERS_WRITE);
                break;
            case '7':
                perms.add(PosixFilePermission.OTHERS_EXECUTE);
                perms.add(PosixFilePermission.OTHERS_READ);
                perms.add(PosixFilePermission.OTHERS_WRITE);
                break;
            default:
                break;
        }

        try {
            Path path = Paths.get(dirFile.getAbsolutePath());
            Files.setPosixFilePermissions(path, perms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File mkFile(String fileName) {
        File f = new File(fileName);
        try {
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }


    public static void copyDirAndFile(String oldPath, String newPath) throws IOException {
        if (!(new File(newPath)).exists()) {
            (new File(newPath)).mkdir();
        }
        File file = new File(oldPath);
        //file name list
        String[] filePaths = file.list();
        for (String filePath : filePaths) {
            String oldFullPath = oldPath + file.separator + filePath;
            String newFullPath = newPath + file.separator + filePath;
            File oldFile = new File(oldFullPath);
            File newFile = new File(newFullPath);
            if (oldFile.isDirectory()) {
                copyDirAndFile(oldFullPath, newFullPath);
            } else if (oldFile.isFile()) {
                copyFile(oldFile, newFile);
            }
        }
    }

    public static void copyFile(File source, File dest) throws IOException {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            inputChannel.close();
            outputChannel.close();
        }
    }

    /**
     * @author panchaoyuan
     * @param srcFile    Unzipped file
     * @param destDirPath   Unzipped destination folder
     * @throws RuntimeException
     * @throws IOException
     */
    public static void unZip(MultipartFile  srcFile, String destDirPath,String savePath) throws RuntimeException, IOException {
        long startTime = System.currentTimeMillis();

        File file = null;
        //InputStream ins = srcFile.getInputStream();
        //InputStream ins = new FileInputStream(savePath+srcFile.getOriginalFilename());
        file=new File(savePath+srcFile.getOriginalFilename());
        log.info("MultipartFile transform to File,MultipartFile name:"+srcFile.getOriginalFilename());
        //inputStreamToFile(ins, file);

        if (!file.exists()) {
            throw new RuntimeException(file.getPath() + ",file is not found");
        }
        ZipFile zipFile = null;
        try {
            Charset gbk = Charset.forName("GBK");
            zipFile = new ZipFile(file,gbk);
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                log.info("zipFile context name:"+entry.getName());
                if (entry.isDirectory()) {
                    String dirPath = destDirPath + File.separator+ entry.getName();
                    File dir = new File(dirPath);
                    dir.mkdirs();
                }else {
                    File targetFile = new File(destDirPath + File.separator + entry.getName());
                    targetFile.setExecutable(true);
                    if(!targetFile.getParentFile().exists()){
                        targetFile.getParentFile().mkdirs();
                    }
                    targetFile.createNewFile();
                    InputStream is = zipFile.getInputStream(entry);
                    FileOutputStream fos = new FileOutputStream(targetFile);
                    int len;
                    byte[] buf = new byte[1024];
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    fos.close();
                    is.close();
                }
            }
            long endTime = System.currentTimeMillis();
            log.info("unZip time-->" + (endTime - startTime) + " ms");
        }catch(Exception e) {
            throw new RuntimeException("unzip error from FileUtil", e);
        } finally {
            if(zipFile != null){
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //MultipartFile change to file may create a temp file in the project root folder(delete the temp file)
            File del = new File(file.toURI());
            del.delete();
        }
    }

    /**
     * MultipartFile changed to File
     * @author panchaoyuan
     * @return
     */
    private static void inputStreamToFile(InputStream ins, File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
            log.info("MultipartFile transform to File completed!");
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @author panchaoyuan
     */
    public List<File> getSubFiles(String  desFile,List<File> fileList) {
        File file = new File(desFile);
        File[] files = file.listFiles();
        for (File fileIndex : files) {
            if (!fileIndex.exists()) {
                throw new NullPointerException("Cannot find " + fileIndex);
            } else if (fileIndex.isFile()) {
                fileList.add(fileIndex);
            } else {
                if (fileIndex.isDirectory()) {
                    getSubFiles(fileIndex.getAbsolutePath(),fileList);
                }
            }
        }
        return fileList;
    }


}
