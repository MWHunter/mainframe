package client;

import defineoutside.network.DataStatistics;
import defineoutside.network.NetworkInfo;
import defineoutside.network.ServerCommand;

import java.io.*;
import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;

public class client {
    static NetworkInfo connectToMainframe(String host, Integer port, String name) throws IOException {

        Socket s = new Socket(host, port);
        DataInputStream dis = new DataInputStream(s.getInputStream());

        String msg = dis.readUTF();

        if (msg.equals("CRva7SfCPaiBrS7cZh6bNXuupO0qnfOYrgOCZceQFWcFjbiksI1mgcUyhO31AZtz10k6Kj8Ji5XQ0pMObC2BXEKg2XptcVjFdGf")) {
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            dos.writeUTF("4v2NZ8RTar54k4PoYEsnjxpL0IObNMgediJQP65QwUwmm9hBw1hQCJvxcSo6tIDwiHY2RkYzmVMWIpN8Oe4rrmPxVum2PBwBnL6");
            dos.writeUTF(name);

            return new NetworkInfo(dis, dos);
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println("Running");

        ConnectToMainframe("192.168.1.196");
        /*try {
            Runnable myRunnable = new Runnable() {
                public void run() {
                    try {
                        Socket s = new Socket("69.245.169.213", 27469);
                        DataInputStream dis = new DataInputStream(s.getInputStream());

                        System.out.println("We have connected to port 27469 on host");

                        String msg = dis.readUTF();

                        System.out.println("Reply was: " + msg);

                        if (msg.equals("CRva7SfCPaiBrS7cZh6bNXuupO0qnfOYrgOCZceQFWcFjbiksI1mgcUyhO31AZtz10k6Kj8Ji5XQ0pMObC2BXEKg2XptcVjFdGf")) {
                            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                            dos.writeUTF("4v2NZ8RTar54k4PoYEsnjxpL0IObNMgediJQP65QwUwmm9hBw1hQCJvxcSo6tIDwiHY2RkYzmVMWIpN8Oe4rrmPxVum2PBwBnL6");
                            dos.writeUTF("god");
                            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
                            MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
                            long usedMemory;
                            long maxMemory;
                            // All good
                            while (true) {
                                ObjectOutputStream objectOutputStreams = new ObjectOutputStream(s.getOutputStream());

                                java.util.concurrent.TimeUnit.SECONDS.sleep(1);

                                usedMemory = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getFreePhysicalMemorySize();
                                maxMemory = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();

                                //System.out.println(sparkCPU.CpuMonitor.processLoad10SecAvg());

                                DataStatistics dataStatistics = new DataStatistics(sparkCPU.CpuMonitor.processLoad10SecAvg() * 100,
                                        sparkCPU.CpuMonitor.systemLoad10SecAvg() * 100, heapUsage.getUsed(), heapUsage.getMax(),
                                        usedMemory, maxMemory, null);

                                objectOutputStreams.writeObject(dataStatistics);

                                //dos.writeUTF("CPU: " + sparkCPU.CpuMonitor.processLoad10SecAvg());
                                //dos.writeUTF("System Load: " + sparkCPU.CpuMonitor.systemLoad10SecAvg());
                                //dos.writeUTF("Memory: " + ManagementFactory.getOperatingSystemM);
                                //dos.writeUTF("Application load: " + sparkCPU.CpuMonitor.processLoad10SecAvg());
                                //dos.writeUTF("System load: " + sparkCPU.CpuMonitor.systemLoad10SecAvg());
                                //dos.writeUTF("Memory usage: " + heapUsage.getUsed() + "/" + heapUsage.getMax());
                                //dos.writeUTF("Total System Memory: " + usedMemory + "/" + maxMemory);
                            }

                        } else {
                            System.out.println("We connected to something that isn't right!");

                        }
                    } catch (SocketException e) {
                        System.out.println("Unable to connect to the game manager with host and port 27469");
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

            Thread thread = new Thread(myRunnable);
            thread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    static NetworkInfo networkInfo;

    public static void ConnectToMainframe(String host) {
        Runnable connect = () -> {
            while (true) {
                ObjectInputStream objectInputStream = null;
                try {
                    networkInfo = connectToMainframe(host, 27469, "Generic#Input");
                    System.out.println("Connected to the central server #Input");
                    objectInputStream = new ObjectInputStream(networkInfo.getDataInputStream());
                } catch (IOException e) {
                    // it restarts later
                }

                while (true) {
                    try {
                        // Now listen for players being sent
                        try {
                            Object received = objectInputStream.readObject();

                            if (received instanceof ServerCommand) {
                                ServerCommand serverCommand = (ServerCommand) received;
                                System.out.println(serverCommand.getCommand() + " has been received");

                                //MainAPI.getPlugin().getServer().getScheduler().runTask(MainAPI.getPlugin(), () -> MainAPI.getPlugin().getServer().dispatchCommand(Bukkit.getConsoleSender(), serverCommand.getCommand())
                                //);

                                System.out.println("We got " + serverCommand.getCommand());
                            }
                        } catch (EOFException e) {
                            System.out.println("EOFException thrown receivePlayerTransferAndCommands:43");
                            Thread.sleep(50);
                        }

                    } catch (Exception e) {

                        e.printStackTrace();
                        System.out.println("Disconnected from the central server");

                        // Loop again!
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException ex) {
                            // nothing
                        }

                        break;
                    }
                }
            }

        };

        Thread thread = new Thread(connect);
        thread.start();
    }
}
