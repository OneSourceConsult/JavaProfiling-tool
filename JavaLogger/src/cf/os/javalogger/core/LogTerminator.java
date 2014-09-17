package cf.os.javalogger.core;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

/**
 *
 * @author Joao Goncalves
 */
public class LogTerminator extends Thread {
    
    private FileOutputStream fos;
    private BufferedOutputStream bos;
    private FileOutputStream sfos;
    private BufferedOutputStream sbos;
    
    public LogTerminator() { }
    
    public void storeFileDescriptors(FileOutputStream fos, BufferedOutputStream bos) {
        this.fos = fos;
        this.bos = bos;
    }
    
    public void storeSystemFileDescriptors(FileOutputStream fos, BufferedOutputStream bos) {
        this.sfos = fos;
        this.sbos = bos;
    }
    
    @Override
    public void run() {
        System.out.println("[CF LOG] Signal caught");
        try {
            bos.flush();
            bos.close();
        } catch(Exception ex) { }
        try {
            sbos.flush();
            sbos.close();
        } catch(Exception ex) { }
        try {
            fos.flush();
            fos.close();
        } catch(Exception ex) { }
        try {
            sfos.flush();
            sfos.close();
        } catch(Exception ex) { }
    }
    
}
