package cf.os.javalogger.util;

import java.io.BufferedOutputStream;
import org.hyperic.sigar.ResourceLimit;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.cmd.Shell;
import org.hyperic.sigar.cmd.SigarCommandBase;
import org.hyperic.sigar.jmx.SigarInvokerJMX;

/**
 *
 * @author Joao Goncalves
 */
public class Ulimit extends SigarCommandBase {

    private String mode;
    private SigarInvokerJMX invoker;
    private final BufferedOutputStream bos;

    public Ulimit(BufferedOutputStream bos, Shell shell) {
        super(shell);
        this.bos = bos;
    }

    public Ulimit(BufferedOutputStream bos) {
        this.bos = bos;
    }

    private String format(long val) {
        if (val == ResourceLimit.INFINITY()) {
            return "unlimited";
        } else {
            return String.valueOf(val);
        }
    }

    private String getValue(String attr) throws SigarException {
        Long val = (Long) this.invoker.invoke(attr + this.mode);
        return format(val.longValue());
    }

    @Override
    public void output(String[] args) throws SigarException {

        this.mode = "Cur";
        this.invoker = SigarInvokerJMX.getInstance(this.proxy, "Type=ResourceLimit");
        
        for (String arg : args) {
            switch (arg) {
                case "-H":
                    this.mode = "Max";
                    break;
                case "-S":
                    this.mode = "Cur";
                    break;
                default:
                    throw new SigarException("Unknown argument: " + arg);
            }
        }

        try {
            bos.write(("== SYSTEM LIMITS ==\n").getBytes());
            bos.write(("Core file size......." + getValue("Core") + "\n").getBytes());
            bos.write(("Data seg size........" + getValue("Data") + "\n").getBytes());
            bos.write(("File size............" + getValue("FileSize") + "\n").getBytes());
            bos.write(("Pipe size............" + getValue("PipeSize") + "\n").getBytes());
            bos.write(("Max memory size......" + getValue("Memory") + "\n").getBytes());
            bos.write(("Open files..........." + getValue("OpenFiles") + "\n").getBytes());
            bos.write(("Stack size..........." + getValue("Stack") + "\n").getBytes());
            bos.write(("CPU time............." + getValue("Cpu") + "\n").getBytes());
            bos.write(("Max user processes..." + getValue("Processes") + "\n").getBytes());
            bos.write(("Virtual memory......." + getValue("VirtualMemory") + "\n").getBytes());
        } catch(Exception ex) {
            System.out.println("[CF LOG] Error retrieving system stats");
        }
        
    }
    
    public void print() throws Exception {
        processCommand(new String[] {  });
    }

}
