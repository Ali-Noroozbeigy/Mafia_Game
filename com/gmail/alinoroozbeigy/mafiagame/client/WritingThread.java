package com.gmail.alinoroozbeigy.mafiagame.client;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class WritingThread extends Thread{


    private PrintWriter writer;
    private Socket server;


    public WritingThread(Socket server)
    {
        this.server = server;

        boolean success = false;

        // to find weather connection was successful or not
        while (!success)
        {
            try {
                OutputStream output = server.getOutputStream();
                writer = new PrintWriter(output,true);
                success = true;
            }catch (IOException e)
            {
                System.out.println("جریان برقرار نشد، تلاش دوباره...");
            }
        }

    }

    @Override
    public void run() {

        // getting the user name
        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine();

        writer.println(username);

        String msg = "";

        // starting the chat
        while (!msg.equals("پایان"))
        {
            msg = scanner.nextLine();
            writer.println(msg);
        }

        try {
            server.close();
        }catch (IOException e)
        {
            System.out.println("ایراد در بستن سرور...");
        }

    }
}
