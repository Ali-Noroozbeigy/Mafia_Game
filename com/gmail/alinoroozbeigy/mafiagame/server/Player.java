package com.gmail.alinoroozbeigy.mafiagame.server;

import java.io.*;
import java.net.Socket;


/**
 * Player class that runs and does operations during the game .
 * @author Ali Noroozbeigy
 * @version 1
 */
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


    private boolean readyToStart;
    private boolean startingState;
    private boolean readyForVote;
    private boolean votingState;
    private boolean voted;
    private boolean operationTime;
    private boolean doneOperation;


    /**
     * Instantiates a new Player.
     *
     * @param god      the god
     * @param mySocket the my socket
     * @param role     the role
     */
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
        readyToStart = false;
        startingState = true;
        readyForVote = false;
        votingState = false;
        voted = false;
        operationTime = false;
        doneOperation = false;
    }


    /**
     * Gets username.
     *
     * @throws IOException the io exception
     */
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
        System.out.println(this);
    }

    /**
     * run method plays the game for client
     */
    @Override
    public void run() {

        // getting username
        String msg = "";
        try {

            gettingUsername();

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
                                receiveMessage("لطفا عدد بازیکن مورد نظر وارد کنید.");
                                msg = getMessage();
                            }
                            catch (IndexOutOfBoundsException e)
                            {
                                receiveMessage("عدد وارد شده غیر مجاز است. دوباره وارد کنید.");
                                msg = getMessage();
                            }
                        }while (!success);
                        doneOperation = true;
                        operationTime = false;
                    }

                    if(msg.equals("آماده") && !isSpectator())
                    {
                        setReadyForVote(true);
                        receiveMessage("ثبت شد، منتظر باقی بازیکنان بمانید!");
                    }

                    // if not silent and sleep
                    if(!isSleep() && !isSilent() && !isSpectator())
                        sendToServer("["+username+"] : "+msg);
                }
            }while (!msg.equals("پایان") && !isSpectator());
        }
        catch (IOException e)
        {
            System.out.println("خطا در خواندن از بازیکن");
        }

        while (!msg.equals("پایان"))
        {
            msg = getMessage();
        }

        // removing operations
        try {
            god.removePlayer(this,isSpectator());
            receiveMessage("خروج");
            mySocket.close();
            // remove from god
        }
        catch (IOException e)
        {
            System.out.println("خطا در بستن ارتباط");
        }

        // message for others

    }

    /**
     * Gets username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets message from client.
     *
     * @return the message
     */
    public String getMessage ()
    {
        StringBuilder sb = new StringBuilder();

        try {
            sb.append(reader.readLine());
        }
        catch (IOException e)
        {
            System.out.println("خطا در برقراری ارتباط...");
            try {
                writer.close();
                reader.close();
                mySocket.close();
                System.out.println("بازیکن خارج شد.");
            }
            catch (IOException ex){
            }
        }

        return sb.toString();
    }


    /**
     * Sends a message to server.
     *
     * @param message the message
     */
    public void sendToServer (String message)
    {
        god.sendToAll(message,this);
    }

    /**
     * Receive message.
     *
     * @param message the message
     */
    public void receiveMessage(String message)
    {
        writer.println(message);
    }

    /**
     * Is sleep.
     *
     * @return the sleep of player
     */
    public boolean isSleep() {
        return sleep;
    }

    /**
     * Sets sleep.
     *
     * @param sleep the sleep
     */
    public void setSleep(boolean sleep) {
        this.sleep = sleep;
    }

    /**
     * Is silent.
     *
     * @return the silence of player
     */
    public boolean isSilent() {
        return isSilent;
    }

    /**
     * Sets silent.
     *
     * @param silent the silent
     */
    public void setSilent(boolean silent) {
        isSilent = silent;
    }

    /**
     * Is spectator.
     *
     * @return the spectator
     */
    public boolean isSpectator() {
        return spectator;
    }

    /**
     * Sets spectator.
     *
     * @param spectator the spectator
     */
    public void setSpectator(boolean spectator) {
        this.spectator = spectator;
    }

    /**
     * Gets role.
     *
     * @return the role
     */
    public Role getRole() {
        return role;
    }

    /**
     * Is mafia.
     *
     * @return the if mafia
     */
    public boolean isMafia() {
        return isMafia;
    }

    /**
     * Is ready to start boolean.
     *
     * @return ready to start
     */
    public boolean isReadyToStart() {
        return readyToStart;
    }

    /**
     * Sets ready to start.
     *
     * @param readyToStart the ready to start
     */
    public void setReadyToStart(boolean readyToStart) {
        this.readyToStart = readyToStart;
    }

    /**
     * Is starting state boolean.
     *
     * @return starting state
     */
    public boolean isStartingState() {
        return startingState;
    }

    /**
     * Sets starting state.
     *
     * @param startingState the starting state
     */
    public void setStartingState(boolean startingState) {
        this.startingState = startingState;
    }

    /**
     * Is ready for vote.
     *
     * @return ready for vote
     */
    public boolean isReadyForVote() {
        return readyForVote;
    }

    /**
     * Sets ready for vote.
     *
     * @param readyForVote the ready for vote
     */
    public void setReadyForVote(boolean readyForVote) {
        this.readyForVote = readyForVote;
    }

    /**
     * Is voting state.
     *
     * @return voting state
     */
    public boolean isVotingState() {
        return votingState;
    }

    /**
     * Sets voting state.
     *
     * @param votingState the voting state
     */
    public void setVotingState(boolean votingState) {
        this.votingState = votingState;
    }

    /**
     * Is voted.
     *
     * @return if player voted
     */
    public boolean isVoted() {
        return voted;
    }

    /**
     * Sets voted.
     *
     * @param voted the voted
     */
    public void setVoted(boolean voted) {
        this.voted = voted;
    }

    /**
     * Sets operation time.
     *
     * @param operationTime the operation time
     */
    public void setOperationTime(boolean operationTime) {
        this.operationTime = operationTime;
    }

    /**
     * Sets done operation.
     *
     * @param doneOperation the done operation
     */
    public void setDoneOperation(boolean doneOperation) {
        this.doneOperation = doneOperation;
    }

    /**
     * Is done operation
     *
     * @return if operation is done
     */
    public boolean isDoneOperation() {
        return doneOperation;
    }

    /**
     * Gets my socket.
     *
     * @return the my socket
     */
    public Socket getMySocket() {
        return mySocket;
    }

    /**
     * to string method
     * @return name plus role
     */
    @Override
    public String toString() {
        return "["+username+"] : " + role;
    }
}
