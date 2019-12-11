package com.sy;

import java.io.*;

/**
 * 读取指定目录的文件内容到一个文件，将遍历所有子目录。可以指定忽略的目录和指定要读取的文件类型。<br>
 * 默认是用来读取源代码的，所以也去掉了所有空行、注释行和行尾注释
 */
public class Main {

    private static int fileCnt = 0;
    private static int lineCnt = 0;

    public static void main(String[] args) {
        try {
            File dstFile = new File("D:\\sources.txt");
            FileWriter writer = new FileWriter(dstFile);
            String dir = "D:\\project_dir";
            File root = new File(dir);
            copyFiles(root.listFiles(), writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("文件数：" + fileCnt);
        System.out.println("总行数：" + lineCnt);

    }

    private static void copyFiles(File[] files, FileWriter writer) {
        if (files != null && files.length > 0) {
            for (File f : files) {
                if (f.isDirectory()) {
                    String name = f.getName();
                    // 按需修改要忽略的目录
                    if (name.equals(".gradle")
                            || name.equals(".idea")
                            || name.equals("build")
                            || name.equals("gradle")) {
                        continue;
                    }
                    copyFiles(f.listFiles(), writer);
                } else {
                    String name = f.getName();
                    // 按需修改要拷贝源码的源文件后缀
                    if (name.endsWith(".java")
                            || name.endsWith(".kt")
                            || name.endsWith(".xml")/*
                            || name.endsWith(".gradle")*/) {
                        fileCnt++;
                        System.out.println(f.getPath());
                        try {
                            BufferedReader reader = new BufferedReader(new FileReader(f));
                            String line;
                            boolean foundComment = false;
                            while ((line = reader.readLine()) != null) {
                                // 空行和 * 开始的注释行
                                if (line.matches("^\\s*(\\*)*")) {
                                    continue;
                                }
                                // 单行注释： /** */ 或 // 或 <!-- -->
                                if (line.matches("^\\s*/\\*.*\\*/\\s*$")
                                        || line.matches("^\\s*<!--.*-->\\s*$")
                                        || line.matches("^\\s*//.*\\s*$")) {
                                    continue;
                                }
                                // 处理以 /* 或 <!-- 开始的多行注释
                                if (!foundComment && (
                                        line.matches("^\\s*/\\*.*$") || line.matches("^\\s*<!--.*\\s*$"))) {
                                    foundComment = true;
                                    continue;
                                }
                                // 多行注释结束， */ 或 -->
                                if (foundComment && (line.matches(".*\\*/") || line.matches("^\\s*-->\\s*$"))) {
                                    foundComment = false;
                                    continue;
                                }
                                // 多行注释中，没有以*开始的中间行
                                if (foundComment) {
                                    continue;
                                }
                                // 去除行尾注释
                                if (!name.endsWith(".xml")) { // xml中可能包含http://格式的命名空间
                                    line = line.replaceAll("//.*", ""); // 行尾 // 注释
                                }
                                if (line.matches(".*/\\*.*\\*/.*")) {
                                    line = line.replaceAll("/\\*.*\\*/", ""); // 行尾 /* */ 注释
                                }
                                lineCnt++;
                                writer.write(line + "\n");
                            }
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
