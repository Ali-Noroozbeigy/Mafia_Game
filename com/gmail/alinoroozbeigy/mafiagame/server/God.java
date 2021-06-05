package com.gmail.alinoroozbeigy.mafiagame.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class God {

    private final int MAX_PLAYER = 10;


    private ArrayList<Player> players;
    private ArrayList<Mafia> mafias;
    private ArrayList<Citizen> citizens;

    private ArrayList<Player> killedPlayers;
    private HashMap<Player, Integer> playerVotes;
    private HashSet<String> usernames;
    private ArrayList<Role> roles;

    private int port;
    private int numBullet;
    private boolean lecSaveLec;
    private boolean docSaveDoc;
    private int hardLifeCheck;

    public God(int port)
    {
        this.port = port;

        players = new ArrayList<>();
        mafias = new ArrayList<>();
        citizens = new ArrayList<>();
        killedPlayers = new ArrayList<>();
        playerVotes = new HashMap<>();
        usernames = new HashSet<>();
        roles = new ArrayList<>();

        numBullet = 2;
        lecSaveLec = false;
        docSaveDoc = false;
        hardLifeCheck = 2;

    }

    public void sendToAll(String message, Player player)
    {
        for (Player player1 : players)
        {
            if (!player.isSleep() && !(player1==player))
                player1.receiveMessage(message);

        }
    }


    public void godMessage(String message)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[گاد]:");
        sb.append(message);

        for (Player player : players)
            player.receiveMessage(sb.toString());
    }

    public Player findPlayer (Role role)
    {
        for(Player player : players)
            if (player.getRole().equals(role))
                return player;
        return null;
    }

    public void sleepAll ()
    {
        for (Player player : players)
            player.setSleep(true);
    }

    public void awakeAll ()
    {
        for (Player player : players)
            player.setSleep(false);
    }

    public void awakeMafias ()
    {
        for (Mafia mafia : mafias)
            mafia.setSleep(false);
    }

    public void awakeRole(Role role)
    {
        Player player = findPlayer(role);
        player.setSleep(false);
    }

    public void silentPlayer(Player player)
    {
        player.setSilent(true);
    }

    public boolean allReadyToVote ()
    {
        for (Player player : players)
            if (!player.isReadyForVote())
                return false;
        return true;
    }

    public void setVotingState(boolean state)
    {
        for (Player player : players)
            player.setVotingState(state);
    }


    public boolean allReadyToStart()
    {
        for (Player player : players)
            if (!player.isReadyToStart())
                return false;
        return true;
    }


    public void printPlayers ()
    {
        String indexName;
        for (Player player : players )
        {
            int i= 1;
            for (Player player1 : players)
            {
                indexName = (i++)+" : "+player1.getUsername();
                player.receiveMessage(indexName);
            }

        }

    }

    public synchronized void increaseVote(int i)
    {
        Player player = players.get(i);
        playerVotes.put(player,playerVotes.get(player)+1);
    }

    public void clearVotes ()
    {

        playerVotes.replaceAll((key,oldValue)->0);
    }

    public boolean allVoted()
    {
        for (Player player : players)
            if (!player.isVoted())
                return false;
        return true;
    }

    public HashSet<String> getUsernames() {
        return usernames;
    }

    public int getNumPlayers ()
    {
        return players.size();
    }

    public synchronized void addUsername (String username)
    {
        usernames.add(username);
    }

    public void generateRoles()
    {
        switch (MAX_PLAYER)
        {

            case 8:
                roles.add(Role.GODFATHER);
                roles.add(Role.LECTER);
                roles.add(Role.DOCTOR);
                roles.add(Role.INSPECTOR);
                roles.add(Role.SNIPER);
                roles.add(Role.CITIZEN);
                roles.add(Role.PSYCHOLOGIST);
                roles.add(Role.MAYOR);
                break;
            case 9:
                roles.add(Role.GODFATHER);
                roles.add(Role.LECTER);
                roles.add(Role.SIMPLEMAFIA);
                roles.add(Role.DOCTOR);
                roles.add(Role.INSPECTOR);
                roles.add(Role.SNIPER);
                roles.add(Role.CITIZEN);
                roles.add(Role.PSYCHOLOGIST);
                roles.add(Role.MAYOR);
                break;
            case 10:
                roles.add(Role.GODFATHER);
                roles.add(Role.LECTER);
                roles.add(Role.SIMPLEMAFIA);
                roles.add(Role.DOCTOR);
                roles.add(Role.INSPECTOR);
                roles.add(Role.SNIPER);
                roles.add(Role.CITIZEN);
                roles.add(Role.PSYCHOLOGIST);
                roles.add(Role.MAYOR);
                roles.add(Role.HARDLIFE);
                break;
        }

        Collections.shuffle(roles);
    }

    public void initPlayers(Socket client, ExecutorService service, int i)
    {
        System.out.println("بازیکن جدید متصل شد!");

        godMessage("بازیکن جدید متصل شد!");
        godMessage("[" + (i + 1) + "/" + MAX_PLAYER + "]");

        Player player;

        if (roles.get(i).getCategory().equals(Category.MAFIAS)) {
            player = new Mafia(this, client, roles.get(i));
            players.add(player);
            mafias.add((Mafia) player);
        } else {
            player = new Citizen(this, client, roles.get(i));
            players.add(player);
            citizens.add((Citizen) player);
        }
        playerVotes.put(player,0);
        service.execute(player);
    }

    public void waitForAll()
    {
        while (!allReadyToStart())
        {
            try {
                Thread.sleep(500);
            }catch (InterruptedException e){}
        }
    }

    public void delay(int millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
            System.out.println("خطا در تاخیر...");
        }
    }

    public void introductionNight()
    {
        godMessage("بازی آغاز می شود! همه در خواب هستید! شب معارفه...");
        delay(1500);
        // mafias to know each other
        godMessage("مافیا ها با هم آشنا می شوند!");
        delay(1500);
        for (Player player : mafias)
        {
            for (Player player1 : mafias)
                if (player != player1)
                    player.receiveMessage(player1.toString());
        }

        // introducing doctor to mayor
        godMessage("شهردار با دکتر شهر آشنا می شود.");
        delay(1500);
        Player player = findPlayer(Role.MAYOR);
        player.receiveMessage(findPlayer(Role.DOCTOR).toString());
        godMessage("پایان شب معارفه!");
        delay(1500);
    }

    public boolean gameOver ()
    {
        return (mafias.size() >= citizens.size()) ||
                mafias.size() == 0;
    }

    public void dayStarts()
    {
        godMessage("روز آغاز می شود، میتوانید پنج دقیقه با هم گفت و گو کنید !");
        godMessage("میتوانید عبارت آماده را برای شروع زودتر رای گیری تایپ کنید.");
        awakeAll();

        long end = System.currentTimeMillis() + (5 * 60 * 1000);

        while (!allReadyToVote() && System.currentTimeMillis() <= end)
        {
            delay(6000);
            /*try {
                Thread.sleep(6000);
            }catch (InterruptedException e){}*/
        }

        godMessage("روز تمام شد!");

    }

    public void getVotes()
    {
        setVotingState(true);
        godMessage("رای گیری آغاز شد. سی ثانیه فرصت دارید تا به شماره بازیکن مورد نظر رای بدهید.");
        printPlayers();
        long end = System.currentTimeMillis() + (2*60 *1000);
        while (!allVoted() && System.currentTimeMillis() <= end)
        {
            delay(6000);
            /*try {
                Thread.sleep(6000);
            }catch (InterruptedException e){}*/
        }
        setVotingState(false);
        godMessage("رای گیری تمام شد!");
    }

    public void gameplay()
    {
        try (ServerSocket server = new ServerSocket(port))
        {
            System.out.println("سرور ایجاد شد، منتظر برای اتصال بازیکنان...");

            generateRoles();

            ExecutorService service = Executors.newCachedThreadPool();

            int i=0;
            while (i != MAX_PLAYER)
            {
                Socket client = server.accept();
                initPlayers(client,service,i);
                i++;
            }


            waitForAll();
            service.shutdown(); //?

            printPlayers();
            introductionNight();

            dayStarts();
            getVotes();


        }
        catch (IOException e)
        {
            System.out.println("خطا در ساخت سرور...");
        }
    }

    public static void main (String[] arg)
    {

        int port;

        System.out.println("پورت که میخواهید بازی در آن آغاز شود را وارد کنید");
        Scanner sc = new Scanner(System.in);

        port = sc.nextInt();

        God god = new God(port);

        // game play
        god.gameplay();

    }

}
