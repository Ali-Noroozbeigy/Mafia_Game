package com.gmail.alinoroozbeigy.mafiagame.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;


/**
 * ReadingThread class which makes player to be able to always receive message
 * @author Ali Norozbeigy
 * @version 1
 */
public class ReadingThread extends Thread {

    private Socket server;
    private BufferedReader reader;

    /**
     * Instantiates a new Reading thread.
     *
     * @param server the server
     */
    public ReadingThread(Socket server)
    {
        this.server = server;

        boolean success = false;

        // to check weather connection was successful or not
        while (!success)
        {
            try
            {
                InputStream input = this.server.getInputStream();
                reader = new BufferedReader(new InputStreamReader(input));
                success = true;
            }catch (IOException e)
            {
                System.out.println("جریان برقرار نشد، تلاش دوباره...");
            }
        }

    }

    /**
     * run class which starts receiving message
     */
    @Override
    public void run() {

        // for saving received messages
        String receivedMessage = "";

        // stating the chat
        try
        {
            while (!receivedMessage.equals("خروج\n")){

                // reading messages from server
                receivedMessage = reader.readLine()+"\n";
                System.out.println(receivedMessage);

            }
            System.exit(1);
        }
        catch (IOException e)
        {
            System.out.println("خطا در خواندن از سرور");
        }

    }
}
