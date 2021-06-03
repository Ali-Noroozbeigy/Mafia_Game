package com.gmail.alinoroozbeigy.mafiagame.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ReadingThread extends Thread {

    private Socket server;
    private BufferedReader reader;

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

    @Override
    public void run() {

        // for saving received messages
        String receivedMessage;

        // stating the chat
        try
        {
            while (true){

                // reading messages from server
                receivedMessage = reader.readLine()+"\n";
                System.out.println(receivedMessage);

            }
        }
        catch (IOException e)
        {
            System.out.println("خطا در خواندن پیام");
        }

    }
}
