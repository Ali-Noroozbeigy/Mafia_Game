package com.gmail.alinoroozbeigy.mafiagame.server;

import java.io.*;
import java.net.Socket;

public abstract class Player implements Runnable {

    private God god;
    private Socket mySocket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Role role;
    private String username;
    private boolean isMafia;
    private boolean isSilent;
    private boolean Spectator;
    private boolean sleep;
    private boolean gameover;

    // saved by doctor
    private boolean saved;

    private boolean readyForVote;
    private boolean votingState;


    public Player (God god, Socket mySocket, Role role)
    {
        this.god = god;
        this.mySocket = mySocket;
        this.role = role;

        boolean success = false;

        while (!success)
        {
            try {
                InputStream input = this.mySocket.getInputStream();
                reader = new BufferedReader(new InputStreamReader(input));

                OutputStream output = this.mySocket.getOutputStream();
                writer = new PrintWriter(output,true);
                success = true;
            }
            catch (IOException e)
            {
                System.out.println("خطا در برقراری جریان، تلاش دوباره...");
                try {
                    reader.close();
                    writer.close();
                }catch (IOException ex)
                {
                    System.out.println("خطا در بستن جریان...");
                }
            }
        }

        if (role.getCategory().equals(Category.CITIZENS))
            isMafia = false;
        else
            isMafia = !(role.equals(Role.GODFATHER));

        isSilent = false;
        sleep = true;
        gameover = false;
        saved = false;
        readyForVote = false;
        votingState = false;
    }


    @Override
    public void run() {

        // getting username
        try {
            writer.println("یک نام کابری وارد کنید");
            this.username = reader.readLine();
            while (god.getUsernames().contains(username)) {

                writer.println("این نام کاربری وجود دارد. نامی دیگر انتخاب کنید");
                this.username = reader.readLine();
            }
            writer.println("با موفقیت اضافه شدید! منتظر برای اتصال بقیه بازیکنان...");
            writer.println("تا زمانی که گاد اعلام نکرده، اگر پیامی بفرستی، به کسی ارسال نمیشه.");
            god.addUsername(username);
            writer.println("نقش شما در بازی : "+role);
        }
        catch (IOException e)
        {
            System.out.println("خطا در خواندن از بازیکن");
        }

        String msg = "";
        do {
            // voting state
            msg = getMessage();
            // if not silent and sleep
            sendToServer(msg);

        }while (!msg.equals("پایان"));

        // removing operations
        try {
            mySocket.close();
        }
        catch (IOException e)
        {
            System.out.println("خطا در بستن ارتباط");
        }

        // message for others

    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage ()
    {
        StringBuilder sb = new StringBuilder();

        try {
            sb.append("[");
            sb.append(username);
            sb.append("] : ");
            sb.append(reader.readLine());
            sb.append("\n");
        }
        catch (IOException e)
        {
            System.out.println("خطا در برقراری ارتباط...");
        }

        return sb.toString();
    }


    public void sendToServer (String message)
    {
        god.sendToAll(message,this);
    }

    public void receiveMessage(String message)
    {
        writer.println(message);
    }

    public boolean isSleep() {
        return sleep;
    }

    public void setSleep(boolean sleep) {
        this.sleep = sleep;
    }

    public boolean isSilent() {
        return isSilent;
    }

    public void setSilent(boolean silent) {
        isSilent = silent;
    }

    public boolean isSpectator() {
        return Spectator;
    }

    public void setSpectator(boolean spectator) {
        Spectator = spectator;
    }

    public Role getRole() {
        return role;
    }

    public boolean isMafia() {
        return isMafia;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public boolean isReadyForVote() {
        return readyForVote;
    }

    public void setReadyForVote(boolean readyForVote) {
        this.readyForVote = readyForVote;
    }

    public boolean isVotingState() {
        return votingState;
    }

    public void setVotingState(boolean votingState) {
        this.votingState = votingState;
    }

    @Override
    public String toString() {
        return username;
    }
}
