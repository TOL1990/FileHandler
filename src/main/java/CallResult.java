/**
 * Created by Тарас on 24.11.2016.
 */
    public class CallResult {

        private long size; // размер файлов в папке
        private String files; // дерево файлов

    public CallResult()
    {
        size = 0;
        files = "";
    }

    public CallResult(long size, String files) {
        this.size = size;
        this.files = files;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getFiles() {
        return files;
    }

    public void setFiles(String files) {
        this.files = files;
    }
}
