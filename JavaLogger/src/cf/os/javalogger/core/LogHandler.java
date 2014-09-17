package cf.os.javalogger.core;

import cf.os.javalogger.util.Conf;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *
 * @author jonas
 */
public class LogHandler extends Thread {
    
    private class LogFlusher extends Thread {
        private final BufferedOutputStream bos;
        public LogFlusher(BufferedOutputStream bos) {
            this.bos = bos;
        }
        
        public void initialize() {
            this.start();
        }
        
        @Override
        public void run() {
            while(Conf.work) {
                try {
                    Thread.sleep(5000);
                } catch(InterruptedException ex) { }
                try {
                    bos.flush();
                    fos.flush();
                } catch(IOException ex) {
                    System.out.println("[CF LOG] Trouble flushing");
                }
            }
        }
    }
    
    private final List<Object[]> queue;
    
    private FileOutputStream fos;
    private BufferedOutputStream bos;
    
    public LogHandler() {
        queue = Collections.synchronizedList(new ArrayList<Object[]>());
        initialize();
    }
    
    private void initialize() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
        Date date = new Date();
        
        int iter = 1;
        String fname = Conf.logFolder + dateFormat.format(date) + "_cf_external_log_";
        while(new File(fname+iter).exists()) {
            iter++;
        }
        
        try {
            fos = new FileOutputStream(fname+iter, true);
            bos = new BufferedOutputStream(fos, 128 * 100);
        } catch(Exception ex) {
            System.out.println("[CF LOG] Log file could not be created");
            Conf.work = false;
        }
        
        if(Conf.lt != null) {
            Conf.lt.storeFileDescriptors(fos, bos);
        }
        new LogFlusher(bos).start();
    }
    
    public synchronized void close() {
        try {
            bos.flush();
            fos.flush();
            bos.close();
            fos.close();
        } catch(Exception ex) { }
        this.notifyAll();
    }
    
    public synchronized void add(Object[] log) {
        queue.add(log);
        this.notifyAll();
    }
    
    @Override
    public synchronized void run() {
        while(Conf.work) {
            
            try {
                while(queue.isEmpty()) {
                    this.wait(500);
                }
            } catch(InterruptedException ex) {
                //System.out.println("[CF LOG] Interruption exception");
                Conf.work = false;
                break;
            }
            
            if(queue.isEmpty()) {
                continue;
            }
            
            Object[] log = queue.remove(0);
            
            String caller = log[0].toString();
            StringBuilder sb = new StringBuilder();
            sb.append("[CF LOG ").append(System.currentTimeMillis()).append("][").append(caller).append("] [");
            sb.append(log[1].toString()).append("][");
            sb.append(log[2].toString()).append("] ");

            for(int i = 3; i < log.length - 1; i++) {
                sb.append(log[i].toString()).append("; ");
            }

            if(log.length > 3) {
                if(log[log.length - 1] instanceof Exception) {
                    sb.append(((Exception)log[log.length - 1]).getMessage());
                } else {
                    sb.append(log[log.length - 1].toString());
                }
            }
            
            sb.append("\n");
            
            try {
                bos.write(sb.toString().getBytes());
            } catch(Exception ex) {
                System.out.println("[CF LOG] Problem writing line into file");
            }
            
        }
    }
    
}
