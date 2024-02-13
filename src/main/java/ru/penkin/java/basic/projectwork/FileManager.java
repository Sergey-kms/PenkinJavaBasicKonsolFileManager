package ru.penkin.java.basic.projectwork;

// Курсовой проект по теме "Консольный файловый менеджер"

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileManager {

    // Создание константных переменных с обозначением команд консольного файлового менеджера.

    private static final String LS_COMMAND = "ls";
    private static final String LS_LONG_COMMAND = "ls -l";
    private static final String CD_COMMAND = "cd";
    private static final String MKDIR_COMMAND = "mkdir";
    private static final String RM_COMMAND = "rm";
    private static final String MV_COMMAND = "mv";
    private static final String CP_COMMAND = "cp";
    private static final String FINFO_COMMAND = "finfo";
    private static final String HELP_COMMAND = "help";
    private static final String FIND_COMMAND = "find";
    private static final String EXIT_COMMAND = "exit";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private static final String CURRENT_DIRECTORY = ".";
    private static final String PARENT_DIRECTORY = "..";

    private static String currentDirectory;

    public static void main(String[] args) {
        currentDirectory = System.getProperty("user.dir");
        Scanner scanner = new Scanner(System.in);

        // Через бесконечный цикл реализован ввод комманд, пока не будет введена команда exit
        while (true) {
            System.out.print(currentDirectory + "> ");
            String command = scanner.nextLine().trim();

            // Проверка ввода нужной команды. startsWith будет нужен для ввода определенного файла или пути
            if (command.startsWith(LS_COMMAND)) { // Обработка команды ls
                boolean longFormat = command.equals(LS_LONG_COMMAND);
                listFiles(longFormat);
            } else if (command.startsWith(CD_COMMAND)) { // Обработка команды cd
                String path = command.substring(CD_COMMAND.length()).trim();
                changeDirectory(path);
            } else if (command.startsWith(MKDIR_COMMAND)) {  // Обработка команды mkdir
                String name = command.substring(MKDIR_COMMAND.length()).trim();
                createDirectory(name);
            } else if (command.startsWith(RM_COMMAND)) {  // Обработка команды rm
                String filename = command.substring(RM_COMMAND.length()).trim();
                deleteFile(filename);
            } else if (command.startsWith(MV_COMMAND)) { // Обработка команды mv
                String[] parts = command.substring(MV_COMMAND.length()).trim().split("\\s+", 2);
                String source = parts[0];
                String destination = parts[1];
                moveFile(source, destination);
            } else if (command.startsWith(CP_COMMAND)) {  // Обработка команды cp
                String[] parts = command.substring(CP_COMMAND.length()).trim().split("\\s+", 2);
                String source = parts[0];
                String destination = parts[1];
                copyFile(source, destination);
            } else if (command.startsWith(FINFO_COMMAND)) { // Обработка команды finfo
                String filename = command.substring(FINFO_COMMAND.length()).trim();
                printFileInfo(filename);
            } else if (command.equals(HELP_COMMAND)) {  // Обработка команды help
                printHelp();
            } else if (command.startsWith(FIND_COMMAND)) { // Обработка команды find
                String filename = command.substring(FIND_COMMAND.length()).trim();
                findFile(filename);
            } else if (command.equals(EXIT_COMMAND)) { // Обработка команды exit
                break;
            } else { // Обработка неизвестной команды
                System.out.println("Неизвестная команда: Напишите комаду help для отображения списка команд");
            }
        }

        scanner.close();
    }

    // Вся работа программы направлена на текущую директорию проекта
    // Метод для вывода списка файлов в текущей директории
    private static void listFiles(boolean longFormat) {
        File directory = new File(currentDirectory);
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (longFormat) {
                    printFileDetails(file);
                } else {
                    System.out.println(file.getName());
                }
            }
        }
    }

    // Метод для изменения текущей директории
    private static void changeDirectory(String path) {
        if (path.equals(PARENT_DIRECTORY)) {
            currentDirectory = new File(currentDirectory).getParent();
        } else {
            File newDirectory = new File(currentDirectory, path);
            if (newDirectory.isDirectory()) {
                currentDirectory = newDirectory.getAbsolutePath();
            } else {
                System.out.println("Директория не найдена: " + newDirectory.getAbsolutePath());
            }
        }
    }

    // Метод для создания новой директории
    private static void createDirectory(String name) {
        File newDirectory = new File(currentDirectory, name);
        if (newDirectory.mkdir()) {
            System.out.println("Директория создана: " + newDirectory.getAbsolutePath());
        } else {
            System.out.println("Невозможно создать директорию: " + newDirectory.getAbsolutePath());
        }
    }

    // Метод для удаления файла или директории
    private static void deleteFile(String filename) {
        File file = new File(currentDirectory, filename);
        if (file.exists()) {
            if (file.isDirectory()) {
                deleteDirectory(file);
            } else {
                if (file.delete()) {
                    System.out.println("Файл удален: " + file.getAbsolutePath());
                } else {
                    System.out.println("Ошибка удаления файла: " + file.getAbsolutePath());
                }
            }
        } else {
            System.out.println("Файл не найден: " + file.getAbsolutePath());
        }
    }

    // Метод для рекурсивного удаления директории
    private static void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (file.delete()) {
                        System.out.println("Файл удален: " + file.getAbsolutePath());
                    } else {
                        System.out.println("Ошибка удаления файла: " + file.getAbsolutePath());
                    }
                }
            }
        }

        if (directory.delete()) {
            System.out.println("Директория удалена: " + directory.getAbsolutePath());
        } else {
            System.out.println("Ошибка удаления директории: " + directory.getAbsolutePath());
        }
    }

    // Метод для перемещения файла
    public static void moveFile(String filePath, String folderPath) {
        try {
            File file = new File(filePath);
            File folder = new File(folderPath);
            if (file.exists() && folder.exists()) {
                Path destinationPath = folder.toPath().resolve(file.getName());
                Files.move(file.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Файл успешно перемещен");
            } else {
                System.out.println("Невозможно переместить файл");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод для копирования файла
    private static void copyFile(String sourceFile, String destinationFolder) {
        File fileToCopy = new File(sourceFile);
        File destinationDir = new File(destinationFolder);

        // Проверка, существует ли исходный файл
        if (!fileToCopy.exists()) {
            System.out.println("Исходный файл не найден");
        }

        // Проверка, является ли путь назначения директорией
        if (!destinationDir.isDirectory()) {
            System.out.println("Путь назначения не является директорией.");
        }

        // Создание пути для файла в директории назначения
        Path destinationPath = Path.of(destinationDir.getAbsolutePath(), fileToCopy.getName());

        // Копирование файла
        try {
            Files.copy(fileToCopy.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Файл успешно скопирован в " + destinationPath);
        } catch (IOException e) {
            System.out.println("Ошибка копирования");
        }
    }

    // Метод для вывода информации о файле
    private static void printFileInfo(String filename) {
        File file = new File(currentDirectory, filename);
        if (file.exists()) {
            printFileDetails(file);
        } else {
            System.out.println("Файл не найден: " + file.getAbsolutePath());
        }
    }

    // Метод для вывода детальной информации о файле
    private static void printFileDetails(File file) {
        BasicFileAttributes attributes;
        try {
            attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        } catch (IOException e) {
            System.out.println("Невозможно прочитать файл: " + file.getAbsolutePath());
            return;
        }

        String name = file.getName();
        long size = attributes.size();
        Date lastModified = new Date(attributes.lastModifiedTime().toMillis());

        System.out.println(name + " - " + size + " bytes - " + DATE_FORMAT.format(lastModified));
    }

    // Метод для поиска файла
    private static void findFile(String filename) {
        File directory = new File(currentDirectory);
        File[] files = directory.listFiles();

        if (files != null) {
            boolean found = false;
            for (File file : files) {
                if (file.getName().equals(filename)) {
                    System.out.println("Файл найден в каталоге: " + file.getAbsolutePath());
                    found = true;
                }

                if (file.isDirectory()) {
                    findFileInDirectory(file, filename);
                }
            }

            if (!found) {
                System.out.println("Файл в каталоге не найден");
            }
        } else {
            System.out.println("Ошибка: директория пуста или не существует");
        }
    }

    // Метод для поиска файла в директории
    private static void findFileInDirectory(File directory, String filename) {
        File[] files = directory.listFiles();
        if (files != null) {
            boolean found = false;
            for (File file : files) {
                if (file.getName().equals(filename)) {
                    System.out.println("Файл найден в подкаталоге: " + file.getAbsolutePath());
                    found = true;
                }

                if (file.isDirectory()) {
                    findFileInDirectory(file, filename);
                }
            }

            if (!found) {
                System.out.println("Файл в подкаталоге не найден ");
            }
        } else {
            System.out.println("Ошибка: директория пуста или не существует");
        }
    }

    private static void printHelp() {
        System.out.println("Помощь в коммандах:");
        System.out.println("ls - список файлов в текущем каталоге");
        System.out.println("ls -l - список файлов в текущем каталоге с подробной информацией");
        System.out.println("cd [path] - изменить каталог");
        System.out.println("mkdir [name] - создать новый каталог");
        System.out.println("rm [filename] - удалить файл или каталог");
        System.out.println("mv [source] [destination] - переместить файл или каталог");
        System.out.println("cp [source] [destination] - скопировать файл");
        System.out.println("finfo [filename] - получить подробную информацию о файле");
        System.out.println("help - отобразить это справочное сообщение");
        System.out.println("find [filename] - найти файл в текущем каталоге или любом из его подкаталогов");
        System.out.println("exit - выход из файлового менеджера");
    }
}