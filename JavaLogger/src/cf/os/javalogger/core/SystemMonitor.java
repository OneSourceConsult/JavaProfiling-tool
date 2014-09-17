package cf.os.javalogger.core;

import cf.os.javalogger.util.Conf;
import cf.os.javalogger.util.CpuInfo;
import cf.os.javalogger.util.Ulimit;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.ProcMem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 *
 * @author Joao Goncalves
 */
public class SystemMonitor extends Thread {
    
    private class SystemLogFlusher extends Thread {
        private final BufferedOutputStream bos;
        public SystemLogFlusher(BufferedOutputStream bos) {
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

    private final Sigar sigar;
    private final String PID = "$$";
    
    public final static int SLEEP_TIME = 1000 * 10;
    
    private FileOutputStream fos;
    private BufferedOutputStream bos;
    
    public SystemMonitor() {
        sigar = new Sigar();
        initialize();
    }
    
    private void initialize() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
        Date date = new Date();
        
        int iter = 1;
        String fname = Conf.logFolder + dateFormat.format(date) + "_cf_system_log_";
        while(new File(fname+iter).exists()) {
            iter++;
        }
        
        try {
            fos = new FileOutputStream(fname+iter, true);
            bos = new BufferedOutputStream(fos, 128 * 100);
        } catch(Exception ex) {
            System.out.println("[CF LOG] System log file could not be created");
            Conf.work = false;
        }
        
        if(Conf.lt != null) {
            Conf.lt.storeSystemFileDescriptors(fos, bos);
        }
        new SystemLogFlusher(bos).start();
    }
    
    @Override
    public void run() {
        
        try {
            new CpuInfo(bos).print(PID);
            new Ulimit(bos).print();
        } catch(Exception ex) {
            System.out.println("[CF LOG] Error retrieving system stats");
            ex.printStackTrace();
        }
        
        while(Conf.work) {
            try {
                long time = System.currentTimeMillis();
                ProcMem mem = sigar.getProcMem(PID);
                StringBuilder sb = new StringBuilder();
                sb.append("[CF SYSLOG ").append(time).append("][mem_size][").append(mem.getSize()).append("]\n");
                sb.append("[CF SYSLOG ").append(time).append("][mem_resident][").append(mem.getResident()).append("]\n");
                sb.append("[CF SYSLOG ").append(time).append("][mem_share][").append(mem.getShare()).append("]\n");
                
                ProcCpu cpu = sigar.getProcCpu(PID);
                sb.append("[CF SYSLOG ").append(time).append("][cpu_user][").append(cpu.getUser()).append("]\n");
                sb.append("[CF SYSLOG ").append(time).append("][cpu_sys][").append(cpu.getSys()).append("]\n");
                sb.append("[CF SYSLOG ").append(time).append("][cpu_total][").append(cpu.getTotal()).append("]\n");
                
                bos.write(sb.toString().getBytes());
                
                Thread.sleep(SLEEP_TIME);
            } catch(SigarException ex) {
                System.out.println("[CF LOG] Error retrieving system stats");
            } catch(IOException ex) {
                System.out.println("[CF LOG] Problem writing line into file");
            } catch(InterruptedException ex) { }
        }
        
        try {
            bos.flush();
            fos.flush();
            bos.close();
            fos.close();
        } catch(Exception ex) { }
        
    }
    
}
