package client;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

public class sparkCPUTest {
    public static void main(String[] args) throws InterruptedException {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();

        while (true) {
            long usedMemory = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getFreePhysicalMemorySize();
            long maxMemory = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();

            java.util.concurrent.TimeUnit.SECONDS.sleep(1);

            System.out.println("Application load: " + sparkCPU.CpuMonitor.processLoad10SecAvg());
            System.out.println("System load: " + sparkCPU.CpuMonitor.systemLoad10SecAvg());
            System.out.println("Memory usage: " + heapUsage.getUsed() + "/" + heapUsage.getMax());
            System.out.println("Total System Memory: " + usedMemory + "/" + maxMemory);

            //dos.writeUTF("CPU: " + sparkCPU.CpuMonitor.processLoad10SecAvg());
            //dos.writeUTF("System Load: " + sparkCPU.CpuMonitor.systemLoad10SecAvg());
            //dos.writeUTF("Memory: " + ManagementFactory.getOperatingSystemMXBean());
        }
    }
}
