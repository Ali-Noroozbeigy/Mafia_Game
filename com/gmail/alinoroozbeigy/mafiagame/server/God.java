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

    public void sleppAll ()
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

    public boolean allReady ()
    {
        for (Player player : players)
            if (!player.isReadyForVote())
                return false;
        return true;
    }

    public void printPlayers ()
    {
        for (Player player : players )
            player.receiveMessage(players.toString());

    }

    public synchronized void increaseVote(Player player)
    {

        playerVotes.put(player,playerVotes.get(player)+1);
    }

    public void clearVotes ()
    {

        playerVotes.replaceAll((key,oldValue)->0);
    }


    public HashSet<String> getUsernames() {
        return usernames;
    }

    public void addUsername (String username)
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
                service.execute(player);
                i++;
            }
            printPlayers();
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
