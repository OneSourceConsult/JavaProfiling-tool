package cf.os.javalogger.core;

import cf.os.javalogger.util.Conf;

/**
 *
 * @author Joao Goncalves
 */
public class Log {

    private static boolean init = false;
    private static LogHandler lh = null;
    private static SystemMonitor sm = null;
    
    private static void initialize() {
        Conf.setLogFolder(System.getProperty("lf"));
        String sigarLib = System.getProperty("sigar");
        if(sigarLib != null) {
            System.setProperty("java.library.path", System.getProperty("java.library.path") + ":" + sigarLib);
        }
        
        lh = new LogHandler();
        sm = new SystemMonitor();
        
        Conf.lt = new LogTerminator();
        try {
            Runtime.getRuntime().addShutdownHook(Conf.lt);
        } catch(Exception ex) {
            System.out.println("[CF LOG] Failure to attach shutdown hook. Latest logs can get lost in wonderland...");
            Conf.lt = null;
        }        
        
        lh.setName("LogHandler");
        lh.start();
        
        sm.setName("SystemMonitor");
        sm.start();
    }
    
    /**
     * Closes the logger and terminates all <b>logging</b> related workers
     */
    
    public static void closeLogger() {
        try {
            Thread.sleep(3000);
        } catch(InterruptedException ex) { }
        Conf.work = false;
        lh.close();
        try {
            lh.join(1000);
            sm.join(SystemMonitor.SLEEP_TIME + 1000);
        } catch(InterruptedException ex) { }
        lh.interrupt();
        sm.interrupt();
    }
    
    /**
     * Records a measurement
     * 
     * @param key Key identifying the type of measurement
     * @param value Short description of the type of measurement
     * @param extra <i>(Optional)</i> Additional parameters that should be saved onto the measurement
     */
    
    public static void measure(String key, String value, Object... extra) {
        
        if(!init) {
            init = true;
            initialize();
        }
        
        Thread thread = Thread.currentThread();
        StackTraceElement[] stack = thread.getStackTrace();
        String caller = stack[2].getClassName()+":"+thread.getName();
        
        Object[] newArray = new Object[extra.length + 3];
        newArray[0] = caller;
        newArray[1] = key;
        newArray[2] = value;
        
        for(int i = 3; i < newArray.length; i++) {
            newArray[i] = extra[i-3];
        }
        
        while(lh == null) {
            try {
                Thread.sleep(10);
            } catch(Exception ex) { }
        }
        
        lh.add(newArray);
        
    }
    
}
