package cf.os.javalogger.util;

import cf.os.javalogger.core.LogTerminator;

/**
 *
 * @author Joao Goncalves
 */
public class Conf {
    
    public static LogTerminator lt;    
    public static boolean work = true;    
    public static String logFolder;
    
    private Conf() { }
    
    public static void setLogFolder(String folder) {
        if(folder == null)  {
            logFolder = "";
        } else {
            if(folder.charAt(folder.length() - 1) == '/') {
                logFolder = folder;
            } else {
                logFolder = folder + "/";
            }
        }
    }
    
}
