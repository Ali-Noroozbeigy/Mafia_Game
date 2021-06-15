package com.gmail.alinoroozbeigy.mafiagame.client;


import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


/**
 * User class which allows player to send and receive messages from god
 * @author Ali Noroozbeigy
 * @version 1
 */
public class User {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main (String[] args)
    {
        Socket server ;
        int port;

        Scanner scanner = new Scanner(System.in);
        boolean success = false;

        // to find weather connection was successful or not
        while (!success)
        {
            try {

                System.out.println("پورت موردنظر را وارد کنید :");
                port = scanner.nextInt();
                server = new Socket("127.0.0.1",port);
                System.out.println("به سرور وصل شدید!");
                // starting threads
                ReadingThread readingThread = new ReadingThread(server);
                WritingThread writingThread = new WritingThread(server);
                readingThread.start();
                writingThread.start();
                success = true;
            }
            catch (UnknownHostException e)
            {
                System.out.println("سرور یافت نشد، تلاش دوباره...");
            }
            catch (IOException e)
            {
                System.out.println("خطا در ارتباط با سرور");
            }
        }


    }

}
