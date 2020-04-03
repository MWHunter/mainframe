package client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SyncListTest {
    static List<String> queueActions = new ArrayList<String>();

    static int count = 0;

    public static void main(String[] args) {
        startCount();
        addElementsToArray();
    }

    public static void addElementsToArray() {
        Runnable addStuff = () -> {
            for (int i = 0; i < 100; i++) {
                addToQueue("test");
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread thread = new Thread(addStuff);
        thread.start();
    }

    public static void startCount() {
        Runnable connect = () -> {
            while (true) {
                // Other threads add to this list, must be synchronized for reliability
                synchronized (queueActions) {
                    for (String queueAction : queueActions) {
                        count++;
                    }

                    queueActions.clear();
                }

                System.out.println(count + " is count");

                if (count == 100) {
                    break;
                }
            }
        };

        Thread thread = new Thread(connect);
        thread.start();
    }

    public static void addToQueue(String serverAddedInfo) {
        synchronized (queueActions) {
            queueActions.add(serverAddedInfo);
        }
    }
}
