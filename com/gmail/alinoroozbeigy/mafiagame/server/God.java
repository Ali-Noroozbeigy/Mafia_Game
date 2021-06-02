package com.gmail.alinoroozbeigy.mafiagame.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class God {

    private ArrayList<Player> players;
    private ArrayList<Mafia> mafias;
    private ArrayList<Citizen> citizens;

    private ArrayList<Player> killedPlayers;
    private HashMap<Player, Integer> playerVotes;
    private HashSet<String> usernames;

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
        for (Player player : players)
            System.out.println(player);
    }

    public synchronized void increaseVote(Player player)
    {

        playerVotes.put(player,playerVotes.get(player)+1);
    }

    public void clearVotes ()
    {

        playerVotes.replaceAll((key,oldValue)->0);
    }


}
