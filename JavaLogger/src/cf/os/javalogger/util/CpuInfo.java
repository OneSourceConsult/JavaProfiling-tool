package cf.os.javalogger.util;

import java.io.BufferedOutputStream;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.cmd.Shell;
import org.hyperic.sigar.cmd.SigarCommandBase;

/**
 *
 * @author Joao Goncalves
 */
public class CpuInfo extends SigarCommandBase {

    private final BufferedOutputStream bos;
    
    public CpuInfo(BufferedOutputStream bos, Shell shell) {
        super(shell);
        this.bos = bos;
    }

    public CpuInfo(BufferedOutputStream bos) {
        super();
        this.bos = bos;
    }

    @Override
    public void output(String[] args) throws SigarException {
        org.hyperic.sigar.CpuInfo[] infos = this.sigar.getCpuInfoList();
        org.hyperic.sigar.CpuInfo info = infos[0];
        long cacheSize = info.getCacheSize();
        try {
            bos.write(("== MACHINE INFORMATION ==\n").getBytes());
            bos.write(("Vendor........." + info.getVendor() + "\n").getBytes());
            bos.write(("Model.........." + info.getModel() + "\n").getBytes());
            bos.write(("Mhz............" + info.getMhz() + "\n").getBytes());
            bos.write(("Total CPUs....." + info.getTotalCores() + "\n").getBytes());
            if ((info.getTotalCores() != info.getTotalSockets())
                    || (info.getCoresPerSocket() > info.getTotalCores())) {
                bos.write(("Physical CPUs.." + info.getTotalSockets() + "\n").getBytes());
                bos.write(("Cores per CPU.." + info.getCoresPerSocket() + "\n").getBytes());
            }

            if (cacheSize != Sigar.FIELD_NOTIMPL) {
                bos.write(("Cache size....." + cacheSize + "\n").getBytes());
            }
        } catch(Exception ex) {
            System.out.println("[CF LOG] Error retrieving system stats");
        }
    }
    
    public void print(String pid) throws Exception {
        processCommand(new String[] {  });
    }

}
