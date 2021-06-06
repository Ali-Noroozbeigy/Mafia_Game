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
    private boolean spectator;
    private boolean sleep;
    private boolean gameover;

    // saved by doctor
    private boolean saved;

    private boolean readyToStart;
    private boolean startingState;
    private boolean readyForVote;
    private boolean votingState;
    private boolean voted;
    private boolean operationTime;
    private boolean doneOperation;


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
        spectator = false;
        gameover = false;
        saved = false;
        readyToStart = false;
        startingState = true;
        readyForVote = false;
        votingState = false;
        voted = false;
        operationTime = false;
        doneOperation = false;
    }


    public void gettingUsername () throws IOException
    {
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

    @Override
    public void run() {

        // getting username
        try {

            gettingUsername();

            String msg = "";
            do {
                // voting state
                if (isStartingState())
                {
                    do
                    {
                        receiveMessage("برای شروع عبارت «آماده» را تایپ کن");
                        msg = getMessage();
                    }while (!msg.equals("آماده"));
                    setReadyToStart(true);
                    receiveMessage("ثبت شد! منتظر برای بقیه بازیکنان");
                    setStartingState(false);
                }
                else
                {
                    msg = getMessage();

                    if(isVotingState())
                    {
                        boolean success = false;
                        int num = -1;
                        do {
                            try {
                                num = Integer.parseInt(msg);
                                if (num > god.getNumPlayers() || num<1)
                                    throw new IndexOutOfBoundsException();
                                god.increaseVote(num-1);
                                receiveMessage("ثبت شد!");
                                success = true;
                            }
                            catch (NumberFormatException e)
                            {
                                receiveMessage("برای رای دادن، لطفا عدد وارد کنید.");
                                msg = getMessage();
                            }
                            catch (IndexOutOfBoundsException e)
                            {
                                receiveMessage("عدد وارد شده غیر مجاز است. دوباره وارد کنید.");
                                msg = getMessage();
                            }
                        }while (!success  && isVotingState());
                        setVoted(true);
                        setReadyForVote(false);
                        setVotingState(false);
                        msg = "رای به بازیکن شماره "+num ;
                    }

                    else if (operationTime)
                    {
                        boolean success = false;
                        int num = -1;
                        do {
                            try {
                                num = Integer.parseInt(msg);
                                if (num > god.getNumPlayers() || num<1)
                                    throw new IndexOutOfBoundsException();
                                god.setChosenPlayerIndex(num - 1);
                                receiveMessage("دریافت شد!");
                                success = true;
                            }
                            catch (NumberFormatException e)
                            {
                                receiveMessage("لطفا عدد بازیکن مورد نظر را وارد کنید.");
                                msg = getMessage();
                            }
                            catch (IndexOutOfBoundsException e)
                            {
                                receiveMessage("عدد وارد شده غیر مجاز است. دوباره وارد کنید.");
                            }
                        }while (!success);
                        doneOperation = true;
                        operationTime = false;
                    }

                    if(msg.equals("آماده"))
                    {
                        setReadyForVote(true);
                        receiveMessage("ثبت شد، منتظر باقی بازیکنان بمانید!");
                    }

                    // if not silent and sleep
                    if(!isSleep() && !isSilent())
                        sendToServer("["+username+"] : "+msg);
                }

            }while (!msg.equals("پایان"));
        }
        catch (IOException e)
        {
            System.out.println("خطا در خواندن از بازیکن");
        }


        // removing operations
        try {
            mySocket.close();
            // remove from god
        }
        catch (IOException e)
        {
            System.out.println("خطا در بستن ارتباط");
        }

        // message for others

    }

    public String getUsername() {
        return username;
    }

    public String getMessage ()
    {
        StringBuilder sb = new StringBuilder();

        try {
            sb.append(reader.readLine());
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
        return spectator;
    }

    public void setSpectator(boolean spectator) {
        spectator = spectator;
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

    public boolean isReadyToStart() {
        return readyToStart;
    }

    public void setReadyToStart(boolean readyToStart) {
        this.readyToStart = readyToStart;
    }

    public boolean isStartingState() {
        return startingState;
    }

    public void setStartingState(boolean startingState) {
        this.startingState = startingState;
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

    public boolean isVoted() {
        return voted;
    }

    public void setVoted(boolean voted) {
        this.voted = voted;
    }

    public void setOperationTime(boolean operationTime) {
        this.operationTime = operationTime;
    }

    public void setDoneOperation(boolean doneOperation) {
        this.doneOperation = doneOperation;
    }

    public boolean isDoneOperation() {
        return doneOperation;
    }

    @Override
    public String toString() {
        return "["+username+"] : " + role;
    }
}
