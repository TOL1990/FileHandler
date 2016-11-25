import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * Created by Тарас on 24.11.2016.
 */
public class Transfer implements Callable<CallResult> {

    private static final int LOCK_WAIT_SEC = 5;

    private final String dirPath;
    private final ExecutorService executor;


    public Transfer(ExecutorService executor, String dirPath) {
        this.executor = executor;
        this.dirPath = dirPath;
    }

    @Override
    public CallResult call() throws Exception {

        StringBuffer filePaths = new StringBuffer(); // список файлов в отчет
        long size = 0; //размер для папки

        CallResult callResult = null;

        filePaths.append(dirPath + " это папка.\n");

        File dir = new File(dirPath);
        File[] files = dir.listFiles();

        List<String> subDirs = new ArrayList<>(); // в нее сложим все папки

        if (files.length > 0) {
            for (File file : files) {

                if (file.isDirectory())  // если папка отдаем выполняться потоку
                {
                    subDirs.add(file.getAbsolutePath());
//                    filePaths.append(file.getPath() + " is directory.\n");

                } else {
                    size += file.length();
                    filePaths.append(file.getPath() + " это файл. Размер = " + file.length() + " байт \n");
                }
            }
        }
        if (!subDirs.isEmpty()) {

            Queue<Future<CallResult>> futures = new ConcurrentLinkedQueue<>();

            for (String subDir : subDirs) {
                futures.add(executor.submit(new Transfer(executor, subDir)));
            }
            if (!futures.isEmpty()) {
                callResult = getFurureResult(futures);
                size += callResult.getSize();
                filePaths.append(callResult.getFiles());
            }
        }

        return new CallResult(size, filePaths.toString());
    }

    private CallResult getFurureResult(Queue<Future<CallResult>> futures) throws InterruptedException, ExecutionException {

        StringBuffer filePaths = new StringBuffer(); // список файлов в отчет
        long size = 0; //размер для папки
        CallResult tempRes = null;

        Future<CallResult> future;
        while (!futures.isEmpty()) {
            future = futures.poll();
            if (future.isDone()) {
                tempRes = future.get();

                size += tempRes.getSize();
                filePaths.append(tempRes.getFiles());
            } else {
                if (futures.isEmpty()) {
                    tempRes = future.get();

                    size += tempRes.getSize();
                    filePaths.append(tempRes.getFiles());
                } else {
                    futures.add(future);
                }
            }
        }
        return new CallResult(size, filePaths.toString());
    }

}
