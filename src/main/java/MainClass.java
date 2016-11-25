import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Тарас on 24.11.2016.
 * Подсчёт занятого места в папке/на диске.
 * После запуска программа ожидает ввода стартовой папки. Это может быть как просто корень диска (напр. D:\),
 * так и путь к какой-то папке (напр. D:\work\). Впрочем, это не важно. После этого необходимо подсчитать,
 * сколько места занимают вложенные папки по указанному адресу. Место считать суммируя размер всех файлов
 * во всех вложеных папках (любой глубины).
 * Например: пользователь ввёл "D:\". Программа получает список всех папок и файлов по этому пути.
 * Для файла - просто запоминает размер, для папки - заходит в неё и вычисляет суммарный размер всех файлов.
 * Если есть вложенные папки - проделывает для них такую же операцию. Но на выходе нас интересует только размер того,
 * что лежит по адресу "D:\".
 * После того, как подсчёт закончен, программа сохраняет в файл результаты: исходный путь,
 * список содержимого с указанием размера и того, является ли объект файлом или папкой.
 * Разбор содержимого каждой папки производить в отдельном потоке. Используй для этого ExecutorService.
 * <p>
 * Усложнение. Подсчёт занятого места в папке/на диске.
 * Необходимо добавь возможность работы не в многопоточном режиме
 * (т.е. все вычисления должны производится в основном потоке программы).
 * Вместо клавиатурного ввода программа теперь будет принимать аргументы командной строки.
 * -source <path> - путь к папке, содержимое которой необходимо обработать.
 * -output <path> - пусть к файлу, в который будет сохранён результат
 * -mode s | m - однопоточный (s) или многопоточный (m) режим работ
 * При отсутствии параметра -source программа мгновенно завершает работу,
 * при отсутствии любого другого параметра -mode работает в многопоточном режиме,
 * при отсутствии параметра -output сохраняет результат туда-же, куда и сейчас.
 */
public class MainClass {
    private static String SOURCE_PATH = null;
    private static String OUTPUT_PATH = "Listing.txt";
    private static Boolean IS_MULTITHREADING = true;

    public static void main(String[] args) {

        executeConsoleReader(args);

    }

    private static void executeConsoleReader(String[] consoleString) {
        for (int i = 0; i < consoleString.length; i++) {

            if (consoleString[i].equals("-source")) SOURCE_PATH = consoleString[i + 1];
            if (consoleString[i].equals("-output")) OUTPUT_PATH = consoleString[i + 1];
            if (consoleString[i].equals("-mode")) {
                if (consoleString[i + 1].equals("m")) {
                    IS_MULTITHREADING = true;
                } else {
                    if (consoleString[i + 1].equals("s")) IS_MULTITHREADING = false;
                }
            }
        }
        if (SOURCE_PATH == null) System.exit(0);
        if (IS_MULTITHREADING == false) startSingleThread();
        else startMultiThread();

    }

    private static void startMultiThread() {
        StringBuilder filesTree = new StringBuilder();
        filesTree.append(SOURCE_PATH + "\n");

        CallResult result = getSizeAndTree(SOURCE_PATH);
        filesTree.append(result.getFiles());

        filesTree.append("Все файлы весят = " + result.getSize() + " байт.");
        System.out.println(filesTree);
        writeToFile(filesTree.toString());

    }

    private static void startSingleThread() {
        StringBuilder filesTree = new StringBuilder();

        filesTree.append("Все файлы весят = " + getFilesSize(new File(SOURCE_PATH), filesTree) + " байт.");
        writeToFile(filesTree.toString());
    }

    private static long getFilesSize(File dir) {
        StringBuilder filesTree = new StringBuilder();

        long size = 0;
        if (dir.isFile()) {
            size = dir.length();
            filesTree.append(dir.getName() + " это файл. Размер = " + dir.length() + "\n");

        } else {
            filesTree.append(dir.getName() + " это папка: " + "\n");
            File[] subFiles = dir.listFiles();
            for (File f : subFiles) {

                size += getFilesSize(f);
            }
            filesTree.append("\n");
        }
        return size;
    }
    private static long getFilesSize(File dir, StringBuilder filesTree) {

        long size = 0;
        if (dir.isFile()) {
            size = dir.length();
            filesTree.append(dir.getName() + " это файл. Размер = " + dir.length() + "\n");

        } else {
            filesTree.append(dir.getName() + " это папка: " + "\n");
            File[] subFiles = dir.listFiles();
            for (File f : subFiles) {

                size += getFilesSize(f, filesTree);
            }
            filesTree.append("\n");
        }
        return size;
    }


    private static CallResult getSizeAndTree(String dir) {
        CallResult result = null;

        ExecutorService executor = Executors.newCachedThreadPool();
        Future<CallResult> future = executor.submit(new Transfer(executor, dir));

        try {
            result = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        executor.shutdown();

        return result;
    }

    private static String readFilePathFromConsole() {
        String path = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            path = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    private static void writeToFile(String text) {
        try (FileOutputStream fos = new FileOutputStream(OUTPUT_PATH)) {
            // перевод строки в байты
            byte[] buffer = text.getBytes();

            fos.write(buffer, 0, buffer.length);
        } catch (IOException ex) {

            System.out.println(ex.getMessage());
        }
    }

}
