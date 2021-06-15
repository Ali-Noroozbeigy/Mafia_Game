package com.gmail.alinoroozbeigy.mafiagame.server;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * God class which manages the game.
 * @author Ali Noroozbeigy
 * @version 1
 */
public class God {

    private final int MAX_PLAYER;


    private ArrayList<Player> players;
    private ArrayList<Mafia> mafias;
    private ArrayList<Citizen> citizens;

    private ArrayList<Player> killedPlayers;
    private ArrayList<Player> spectators;
    private HashMap<Player, Integer> playerVotes;
    private HashSet<String> usernames;
    private ArrayList<Role> roles;

    private int port;
    private int numBullet;
    private boolean lecSaveLec;
    private boolean docSaveDoc;
    private boolean hardLifeShot;
    private int hardLifeCheck;
    private int chosenPlayerIndex;

    private FileWriter fileWriter;
    private PrintWriter writer;

    /**
     * Instantiates a new God.
     *
     * @param port       the port
     * @param MAX_PLAYER the max player
     */
    public God(int port, int MAX_PLAYER)
    {
        this.port = port;

        this.MAX_PLAYER = MAX_PLAYER;

        players = new ArrayList<>();
        mafias = new ArrayList<>();
        citizens = new ArrayList<>();
        killedPlayers = new ArrayList<>();
        spectators = new ArrayList<>();
        playerVotes = new HashMap<>();
        usernames = new HashSet<>();
        roles = new ArrayList<>();

        numBullet = 2;
        lecSaveLec = false;
        docSaveDoc = false;
        hardLifeCheck = 2;
        hardLifeShot = false;
        chosenPlayerIndex = MAX_PLAYER;

        try {

            fileWriter = new FileWriter("Chats.txt");
            writer = new PrintWriter(fileWriter);
        }
        catch(IOException e)
        {
            System.out.println("خطا در اتصال به فایل");
        }

    }

    /**
     * Send a message to all if they won't sleep.
     *
     * @param message the message
     * @param player  the player
     */
    public void sendToAll(String message, Player player)
    {
        for (Player player1 : players)
        {
            if (!player1.isSleep() && !(player1==player))
                player1.receiveMessage(message);

        }
        for (Player player1 : spectators)
            player1.receiveMessage(message);

        writer.println(message);
    }


    /**
     * God message which everyone receive that.
     *
     * @param message the message
     */
    public void godMessage(String message)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[گاد]:");
        sb.append(message);

        for (Player player : players)
            player.receiveMessage(sb.toString());
        for (Player player : spectators)
            player.receiveMessage(sb.toString());
        writer.println(message);
    }

    /**
     * Finds player according to its role
     *
     * @param role the role
     * @return the player
     */
    public Player findPlayer (Role role)
    {
        for(Player player : players)
            if (player.getRole().equals(role))
                return player;
        return null;
    }

    /**
     * Sleeps all.
     */
    public void sleepAll ()
    {
        for (Player player : players)
            player.setSleep(true);
    }

    /**
     * Awakes all.
     */
    public void awakeAll ()
    {
        for (Player player : players)
            player.setSleep(false);
    }

    /**
     * Awakes mafias.
     */
    public void awakeMafias ()
    {
        for (Mafia mafia : mafias)
            mafia.setSleep(false);
    }

    /**
     * Awakes a role.
     *
     * @param player the player
     */
    public void awakeRole(Player player)
    {
        player.setSleep(false);
    }

    /**
     * Silents a player.
     *
     * @param player the player
     */
    public void silentPlayer(Player player)
    {
        player.setSilent(true);
    }

    /**
     * checks if All ready to vote.
     *
     * @return true if all ready to vote
     */
    public boolean allReadyToVote ()
    {
        for (Player player : players)
            if (!player.isReadyForVote())
                return false;
        return true;
    }

    /**
     * Sets voting state.
     *
     * @param state the state
     */
    public void setVotingState(boolean state)
    {
        for (Player player : players)
            player.setVotingState(state);
    }


    /**
     * checks All ready to start
     *
     * @return if all ready to start
     */
    public boolean allReadyToStart()
    {
        for (Player player : players)
            if (!player.isReadyToStart())
                return false;
        return true;
    }


    /**
     * Prints players.
     */
    public void printPlayers ()
    {
        StringBuilder sb = new StringBuilder();
        String indexName;

        int i= 1;
        for (Player player : players)
        {
            indexName = (i++)+" : "+player.getUsername();
            sb.append("[ "+indexName + " ] ");
        }

        for (Player player : players )
            player.receiveMessage(sb.toString());

    }

    /**
     * Prints players for a specific role.
     *
     * @param player the player
     */
    public void printPlayers(Player player)
    {
        StringBuilder sb = new StringBuilder();
        String indexName;

        int i= 1;
        for (Player player1 : players)
        {
            indexName = (i++)+" : "+player1.getUsername();
            sb.append("[ "+indexName + " ] ");
        }
        player.receiveMessage(sb.toString());
    }

    /**
     * Increase votes of players.
     *
     * @param i the
     */
    public synchronized void increaseVote(int i)
    {
        Player player = players.get(i);
        playerVotes.put(player,playerVotes.get(player)+1);
    }

    /**
     * Clear votes.
     */
    public void clearVotes ()
    {
        playerVotes.replaceAll((key,oldValue)->0);
    }

    /**
     * checks if All voted.
     *
     * @return if all voted
     */
    public boolean allVoted()
    {
        for (Player player : players)
            if (!player.isVoted())
                return false;
        return true;
    }

    /**
     * Gets usernames.
     *
     * @return the usernames
     */
    public HashSet<String> getUsernames() {
        return usernames;
    }

    /**
     * Gets numbers of players.
     *
     * @return the numbers of players
     */
    public int getNumPlayers ()
    {
        return players.size();
    }

    /**
     * Add username.
     *
     * @param username the username
     */
    public synchronized void addUsername (String username)
    {
        usernames.add(username);
    }

    /**
     * Generate roles.
     */
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

    /**
     * Init players with their roles.
     *
     * @param client  the client
     * @param service the service
     * @param i       the
     */
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

    /**
     * Wait for all to become ready.
     */
    public void waitForAll()
    {
        while (!allReadyToStart())
        {
            try {
                Thread.sleep(500);
            }catch (InterruptedException e){}
        }
    }

    /**
     * Delays seconds.
     *
     * @param millis milliseconds of delays
     */
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

    /**
     * Introduction night.
     */
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

    /**
     * checks if game is over.
     *
     * @return the boolean
     */
    public boolean gameOver ()
    {
        return (mafias.size() >= citizens.size()) ||
                mafias.size() == 0;
    }

    /**
     * Day starts.
     */
    public void dayStarts()
    {
        godMessage("روز آغاز می شود، میتوانید پنج دقیقه با هم گفت و گو کنید !");
        godMessage("میتوانید عبارت آماده را برای شروع زودتر رای گیری تایپ کنید.");
        awakeAll();

        printPlayers();

        long end = System.currentTimeMillis() + (5 * 60 * 1000);

        while (!allReadyToVote() && System.currentTimeMillis() <= end)
        {
            delay(6000);
        }

        godMessage("روز تمام شد!");

    }

    /**
     * Print votes.
     */
    public void printVotes()
    {
        StringBuilder sb = new StringBuilder();
        godMessage("نتایج رای گیری");
        for(Map.Entry<Player,Integer> entry : playerVotes.entrySet())
            sb.append("["+entry.getKey().getUsername() + " : " +entry.getValue()+"]");
        godMessage(sb.toString());
    }

    /**
     * Gets votes.
     */
    public void getVotes()
    {
        for (Player player : players)
            player.setVoted(false);
        setVotingState(true);
        godMessage("رای گیری آغاز شد. سی ثانیه فرصت دارید تا به شماره بازیکن مورد نظر رای بدهید.");
        printPlayers();
        long end = System.currentTimeMillis() + (30 *1000);
        while (!allVoted() && System.currentTimeMillis() <= end)
        {
            delay(6000);
        }
        setVotingState(false);
        for(Player player : players)
            player.setReadyForVote(false);
        godMessage("رای گیری تمام شد!");
        printVotes();
    }

    /**
     * Find max vote of a player.
     *
     * @return the player
     */
    public Player findMaxVote()
    {
        int maxVote = -1;
        Player player = null;
        boolean repeated = false;
        for(Map.Entry<Player, Integer> entry : playerVotes.entrySet())
        {
            if(entry.getValue() > maxVote)
            {
                player = entry.getKey();
                repeated = false;
                maxVote = entry.getValue();
            }
            else if(entry.getValue() == maxVote)
                repeated = true;
        }
        return repeated?null:player;
    }

    /**
     * sets the player that a player chosen in operation.
     *
     * @param chosenPlayerIndex the chosen player index
     */
    public void setChosenPlayerIndex(int chosenPlayerIndex) {
        this.chosenPlayerIndex = chosenPlayerIndex;
    }

    /**
     * checks if a Player is dead.
     *
     * @param role the role
     * @return true if dead
     */
    public boolean playerDead(Role role)
    {
        for(Player player : players)
        if(player.getRole().equals(role))
            return false;
        return true;
    }

    /**
     * Godfather operation.
     *
     * @return target of god father
     */
    public Player godfatherOperation()
    {
        godMessage("مافیاها به مدت ده ثانیه با هم مشورت کنند!");
        awakeMafias();
        long end = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() <= end)
        {
            delay(2000);
        }
        godMessage("زمان مشورت تمام شد. مافیا ها میخوابند");
        sleepAll();

        godMessage("پدرخوانده نظر نهایی را اعلام کند.");
        Player godfather;
        if(playerDead(Role.GODFATHER))
        {
            if (playerDead(Role.LECTER))
                godfather = findPlayer(Role.SIMPLEMAFIA);
            else
                godfather = findPlayer(Role.LECTER);
            godfather.receiveMessage("به جای پدرخوانده شما هدف را انتخاب میکنید.");
        }
        else
            godfather = findPlayer(Role.GODFATHER);
        godfather.setDoneOperation(false);
        godfather.setOperationTime(true);
        awakeRole(godfather);
        printPlayers(godfather);
        while (!godfather.isDoneOperation())
        {
            delay(1000);
        }
        godMessage("پدرخوانده میخوابد");
        sleepAll();
        return players.get(chosenPlayerIndex);
    }

    /**
     * Lecter operation.
     *
     * @return target
     */
    public Player lecterOperation()
    {
        godMessage("دکتر لکتر بازیکن مورد نظر را انتخاب کند.");

        if(playerDead(Role.LECTER))
        {
            delay(3000);
            godMessage("دکتر لکتر میخوابد");
            return null;
        }

        Player lecter = findPlayer(Role.LECTER);
        boolean correctChoice = false;

        while (!correctChoice)
        {
            lecter.setDoneOperation(false);
            lecter.setOperationTime(true);
            awakeRole(lecter);
            printPlayers(lecter);
            while (!lecter.isDoneOperation())
            {
                delay(1000);
            }
            sleepAll();

            if(players.get(chosenPlayerIndex).getRole().equals(Role.LECTER) &&
                    mafias.size() !=1)
            {
                if (lecSaveLec)
                    lecter.receiveMessage("قبلا خودت را سیو کردی، فردی دیگر انتخاب کن");
                else
                {
                    correctChoice = true;
                    lecSaveLec = true;
                }
            }
            else if (!players.get(chosenPlayerIndex).getRole().getCategory().equals(Category.MAFIAS))
                lecter.receiveMessage("باید مافیا انتخاب کنی!دوباره انتخاب کن");
            else
                correctChoice = true;
        }
        godMessage("دکتر لکتر میخوابد");
        return players.get(chosenPlayerIndex);
    }

    /**
     * Doctor operation.
     *
     * @return target
     */
    public Player doctorOperation()
    {
        godMessage("دکتر شهر بازیکن مورد نظر را انتخاب کند.");

        if (playerDead(Role.DOCTOR))
        {
            delay(3000);
            godMessage("دکتر شهر میخوابد");
            return null;
        }
        Player doctor = findPlayer(Role.DOCTOR);
        boolean correctChoice = false;


        while (!correctChoice)
        {
            doctor.setDoneOperation(false);
            doctor.setOperationTime(true);
            awakeRole(doctor);
            printPlayers(doctor);
            while (!doctor.isDoneOperation())
            {
                delay(1000);
            }
            sleepAll();

            if(players.get(chosenPlayerIndex).getRole().equals(Role.DOCTOR))
            {
                if (docSaveDoc)
                    doctor.receiveMessage("قبلا خودت را سیو کردی، فردی دیگر انتخاب کن.");
                else
                {
                    correctChoice = true;
                    docSaveDoc = true;
                }
            }
            else
                correctChoice = true;
        }
        godMessage("دکتر شهر میخوابد");
        return players.get(chosenPlayerIndex);
    }

    /**
     * Inspector operation.
     */
    public void inspectorOperation()
    {
        godMessage("کارآگاه استعلام بگیرد.");

        if (playerDead(Role.INSPECTOR))
        {
            delay(3000);
            godMessage("کارآگاه میخوابد");
            return;
        }

        Player inspector = findPlayer(Role.INSPECTOR);

        inspector.setDoneOperation(false);
        inspector.setOperationTime(true);
        awakeRole(inspector);
        printPlayers(inspector);
        while (!inspector.isDoneOperation())
        {
            delay(1000);
        }

        if(players.get(chosenPlayerIndex).isMafia())
            inspector.receiveMessage(players.get(chosenPlayerIndex).getUsername() + " مافیاست!");
        else
            inspector.receiveMessage(players.get(chosenPlayerIndex).getUsername() + " مافیا نیست!");
        godMessage("کارآگاه میخوابد");
        sleepAll();
    }

    /**
     * Sniper operation
     *
     * @return target
     */
    public Player sniperOperation()
    {
        godMessage("حرفه ای هدف خود را اعلام کند");

        if(playerDead(Role.SNIPER))
        {
            delay(3000);
            godMessage("حرفه ای میخوابد");
            return null;
        }

        Player sniper = findPlayer(Role.SNIPER);
        Player target;
        awakeRole(sniper);
        sniper.setDoneOperation(false);
        sniper.setOperationTime(true);
        if(numBullet>0) {
            sniper.receiveMessage(numBullet + "تا تیر داری، اگر مایل به شلیک هستی عدد 2 رو وارد کن.");
            sniper.receiveMessage("و اگر نه عدد 1 را وارد کن");
            while (!sniper.isDoneOperation()) {
                delay(1000);
            }
            if (chosenPlayerIndex == 1)
            {
                printPlayers(sniper);
                sniper.receiveMessage("شماره بازیکن را وارد کن");
                sniper.setDoneOperation(false);
                sniper.setOperationTime(true);
                while (!sniper.isDoneOperation())
                    delay(1000);
                target = players.get(chosenPlayerIndex);
                numBullet--;
            }
            else
            {
                sniper.receiveMessage("هدفی انتخاب نکردی");
                target = null;
            }

            if(target != null && target.getRole().getCategory().equals(Category.CITIZENS))
                sniper.receiveMessage("به شهروندان شلیک کردی، از بازی خارج میشی.");
            godMessage("حرفه ای میخوابد");
            sleepAll();
            return target;
        }
        else
        {
            sniper.receiveMessage("گلوله هات تموم شده، انتخابی نداری");
            godMessage("حرفه ای میخوابد");
            sleepAll();
            return null;
        }
    }

    /**
     * Psy operation.
     *
     * @return target
     */
    public Player psyOperation()
    {
        godMessage("روانشناس بازیکن را اعلام کند.");

        if(playerDead(Role.PSYCHOLOGIST))
        {
            delay(3000);
            godMessage("روانشناس میخوابد");
            return null;
        }

        Player psy = findPlayer(Role.PSYCHOLOGIST);
        Player target;
        awakeRole(psy);
        psy.setDoneOperation(false);
        psy.setOperationTime(true);
        psy.receiveMessage("اگر مایل به ساکت کردن کسی هستی عدد 2 و اگر نه عدد 1 رو وارد کن");
        while (!psy.isDoneOperation())
            delay(1000);
        if(chosenPlayerIndex == 1)
        {
            psy.setDoneOperation(false);
            psy.setOperationTime(true);
            psy.receiveMessage("عدد بازیکن مورد نظر را وارد کن");
            printPlayers(psy);
            while (!psy.isDoneOperation())
                delay(1000);
            target = players.get(chosenPlayerIndex);
            godMessage("روانشناس میخوابد");
            sleepAll();
            return target;
        }
        else
        {
            psy.receiveMessage("هدفی انتخاب نکردی");
            godMessage("روانشناس میخوابد");
            sleepAll();
            return null;
        }

    }

    /**
     * Hard life operation.
     *
     * @return if checked
     */
    public boolean hardLifeOperation()
    {
        godMessage("جان سخت بیدار شود");

        if(playerDead(Role.HARDLIFE))
        {
            delay(3000);
            godMessage("جان سخت میخوابد");
            return false;
        }

        Player hard = findPlayer(Role.HARDLIFE);
        hard.setDoneOperation(false);
        hard.setOperationTime(true);
        awakeRole(hard);

        if(hardLifeCheck>0) {
            hard.receiveMessage(hardLifeCheck + " بار میتونی استعلام بگیری");
            hard.receiveMessage("اگر مایل به استعلام گرفتن هستی عدد 2 و اگر نه عدد 1 رو وارد کن.");
            while (!hard.isDoneOperation())
                delay(1000);
            if (chosenPlayerIndex == 1)
            {
                hard.receiveMessage("استعلام اعلام خواهد شد.");
                hardLifeCheck--;
                godMessage("جان سخت میخوابد");
                sleepAll();
                return true;
            }
            else
            {
                hard.receiveMessage("استعلام نگرفتی");
                godMessage("جان سخت میخوابد");
                sleepAll();
                return false;
            }
        }
        else
        {
            hard.receiveMessage("تعداد استعلام ها تمام شده!");
            godMessage("جان سخت میخوابد");
            sleepAll();
            return false;
        }
    }

    /**
     * Mayor operation.
     */
    public void mayorOperation()
    {
        sleepAll();
        Player votedPlayer = findMaxVote();
        if (votedPlayer != null)
        {
            godMessage(votedPlayer.getUsername()+" بیشترین رای را دارد.");
            godMessage("شهردار نظر خود را اعلام کند.");

            if(playerDead(Role.MAYOR))
            {
                delay(3000);
                godMessage("رای گیری ملغی نشد! شهردار میخوابد");
                godMessage(votedPlayer.getUsername() + " بازی را ترک میکند");
                makeSpectator(votedPlayer);
                sleepAll();
                clearVotes();
                return;
            }

            Player mayor = findPlayer(Role.MAYOR);
            mayor.setDoneOperation(false);
            mayor.setOperationTime(true);
            awakeRole(mayor);
            mayor.receiveMessage("اگر میخواهی رای گیری ملغی شود عدد 2 و اگر نه عدد 1 را وارد کن");
            while (!mayor.isDoneOperation())
                delay(1000);
            if(chosenPlayerIndex == 1)
                godMessage("رای گیری ملغی شد! شهردار میخوابد");
            else
            {
                godMessage("رای گیری ملغی نشد! شهردار میخوابد");
                godMessage(votedPlayer.getUsername() + " بازی را ترک میکند");
                makeSpectator(votedPlayer);
            }
        }

        else
            godMessage("کسی با رای گیری حذف نمی شود! شهردار میخوابد");
        sleepAll();
        clearVotes();
    }

    /**
     * Makes a player spectator.
     *
     * @param player  the player want to be spectator
     */
    public void makeSpectator(Player player)
    {
        player.receiveMessage("شما کشته شدید و از این پس به عنوان تماشاچی بازی را خواهید دید");
        player.receiveMessage("در صورت عدم تمایل، با نوشتن پایان از بازی خارج شوید.");
        player.setSpectator(true);
        players.remove(player);
        if(player.getRole().getCategory().equals(Category.MAFIAS))
            mafias.remove((Mafia) player);
        else
            citizens.remove((Citizen) player);
        playerVotes.remove(player);
        usernames.remove(player.getUsername());

        spectators.add(player);
        killedPlayers.add(player);

    }

    /**
     * Night starts.
     */
    public void nightStarts()
    {
        Player mafiaTarget;
        Player lecterSaved;
        Player doctorSaved;
        Player sniperTarget;
        Player psyTarget;
        boolean hardCheck = false;

        godMessage("شب آغاز می شود. قابلیت صحبت کردن تا روز غیر فعال می شود.");
        sleepAll();


        mafiaTarget = godfatherOperation();
        lecterSaved = lecterOperation();
        doctorSaved = doctorOperation();
        inspectorOperation();
        sniperTarget = sniperOperation();
        psyTarget = psyOperation();
        if (MAX_PLAYER == 10)
            hardCheck = hardLifeOperation();

        StringBuilder report = new StringBuilder();
        if(mafiaTarget==doctorSaved)
            report.append("هدف مافیا توسط دکتر شهر نجات یافت!\n");
        else
        {
            if (mafiaTarget.getRole().equals(Role.HARDLIFE) && !hardLifeShot)
            {
                hardLifeShot = true;
                report.append("جان سخت برای اولین بار تیر خورد!");
            }
            else
            {
                report.append(mafiaTarget.getUsername());
                report.append(" توسط مافیا کشته شد.\n");
                makeSpectator(mafiaTarget);
            }
        }

        if(sniperTarget != null)
        {
            if(sniperTarget == lecterSaved)
                report.append("هدف حرفه ای توسط دکتر لکتر نجات یافت!\n");
            else if (sniperTarget.getRole().getCategory().equals(Category.CITIZENS))
            {
                report.append("حرفه ای به شهروندان شلیک کرد و خودش از بازی حذف می شود.");
                makeSpectator(findPlayer(Role.SNIPER));
            }
            else
            {
                report.append(sniperTarget.getUsername());
                report.append(" توسط حرفه ای کشته شد.\n");
                makeSpectator(sniperTarget);
            }
        }
        else
            report.append("حرفه ای شلیکی نداشت.\n");
        if(psyTarget != null)
        {
            report.append(psyTarget.getUsername());
            report.append(" توسط روانشناس در روز بعد ساکت شد.\n");
            silentPlayer(psyTarget);
        }
        else
            report.append("روانشناس کسی را ساکت نکرد.\n");
        if(hardCheck)
        {
            report.append("جان سخت استعلام گرفت : ");
            for(Player player : killedPlayers)
                report.append(player.getRole() +",");
        }
        else
        {
            if (MAX_PLAYER == 10)
                report.append("جان سخت استعلام نگرفت");
        }

        godMessage("پایان شب !");
        godMessage(report.toString());
    }

    /**
     * Removes player.
     *
     * @param player    the player
     * @param spectator the spectator
     */
    public void removePlayer(Player player, boolean spectator)
    {
        if(spectator)
            spectators.remove(player);
        else
        {
            players.remove(player);
            if(player.getRole().getCategory().equals(Category.MAFIAS))
                mafias.remove((Mafia)player);
            else
                citizens.remove((Citizen)player);
            playerVotes.remove(player);
            usernames.remove(player.getUsername());
        }
        godMessage(player.getUsername()+" بازی را ترک کرد");
    }

    /**
     * clears a player silence.
     */
    public void unSilentPlayer()
    {
        for (Player player : players)
            if(player.isSilent())
            {
                player.receiveMessage("از حالت سکوت خارج می شوید!");
                player.setSilent(false);
            }
    }

    /**
     * Close players.
     */
    public void closePlayers()
    {
        try {
            for (Player player : players)
            {
                player.receiveMessage("خروج");
                player.getMySocket().close();
            }
            for (Player player : spectators)
            {
                player.receiveMessage("خروج");
                player.getMySocket().close();
            }
            System.exit(1);
        }
        catch(IOException e)
        {
            System.out.println("خطا در بستن بازیکنان...");
        }
    }

    /**
     * Gameplay.
     */
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

            while (!gameOver())
            {
                dayStarts();
                unSilentPlayer();
                getVotes();
                mayorOperation();
                if(!gameOver())
                    nightStarts();
            }
            godMessage("بازی تمام شد!");
            if(mafias.size()==0)
                godMessage("شهروندان برنده شدند!");
            else
                godMessage("مافیاها برنده شدند!");

            closePlayers();

        }
        catch (IOException e)
        {
            System.out.println("خطا در ساخت سرور...");
        }
    }

    /**
     * The entry point of application.
     *
     * @param arg the input arguments
     */
    public static void main (String[] arg)
    {

        int port;

        System.out.println("پورت که میخواهید بازی در آن آغاز شود را وارد کنید");
        Scanner sc = new Scanner(System.in);

        port = sc.nextInt();

        System.out.println("تعداد بازیکنان را وارد کنید (8 تا 10 نفر)");
        int n = sc.nextInt();
        while (!(n>=8 && n<=10))
        {
            System.out.println("عددی بین 8 و 10 وارد کنید");
            n = sc.nextInt();
        }

        God god = new God(port,n);

        // game play
        god.gameplay();

    }

}
