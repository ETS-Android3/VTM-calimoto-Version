package java.io;

import org.oscim.debug.Logger;

public class FileInputStream extends InputStream {
    
    static final Logger log = new Logger(FileInputStream.class);

    public FileInputStream(File f) {

    }

    public FileInputStream(String s) throws FileNotFoundException {
        log.debug("FileInputStream {}", s);
    }

    @Override
    public int read() throws IOException {
        return 0;
    }
}
