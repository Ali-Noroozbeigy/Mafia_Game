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

    public God(int port)
    {
        this.port = port;

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

    }

    public void sendToAll(String message, Player player)
    {
        for (Player player1 : players)
        {
            if (!player1.isSleep() && !(player1==player))
                player1.receiveMessage(message);

        }
        for (Player player1 : spectators)
            player1.receiveMessage(message);
    }


    public void godMessage(String message)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[گاد]:");
        sb.append(message);

        for (Player player : players)
            player.receiveMessage(sb.toString());
        for (Player player : spectators)
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
        StringBuilder sb = new StringBuilder();
        String indexName;

        int i= 1;
        for (Player player : players)
        {
            indexName = (i++)+" : "+player.getUsername();
            sb.append(indexName + " ");
        }

        for (Player player : players )
            player.receiveMessage(sb.toString());

    }

    public void printPlayers(Player player)
    {
        StringBuilder sb = new StringBuilder();
        String indexName;

        int i= 1;
        for (Player player1 : players)
        {
            indexName = (i++)+" : "+player1.getUsername();
            sb.append(indexName + " ");
        }
        player.receiveMessage(sb.toString());
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
        }

        godMessage("روز تمام شد!");

    }

    public void printVotes()
    {
        StringBuilder sb = new StringBuilder();
        godMessage("نتایج رای گیری");
        for(Map.Entry<Player,Integer> entry : playerVotes.entrySet())
            sb.append(entry.getKey().getUsername() + " : " +entry.getValue());
        godMessage(sb.toString());
    }

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
        godMessage("رای گیری تمام شد!");
        printVotes();
    }

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
            }
            else if(entry.getValue() == maxVote)
                repeated = true;
        }
        return repeated?null:player;
    }

    public void setChosenPlayerIndex(int chosenPlayerIndex) {
        this.chosenPlayerIndex = chosenPlayerIndex;
    }

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
        Player godfather = findPlayer(Role.GODFATHER);
        godfather.setDoneOperation(false);
        godfather.setOperationTime(true);
        awakeRole(Role.GODFATHER);
        printPlayers(godfather);
        while (!godfather.isDoneOperation())
        {
            delay(1000);
        }
        godMessage("پدرخوانده میخوابد");
        sleepAll();
        return players.get(chosenPlayerIndex);
    }

    public Player lecterOperation()
    {
        Player lecter = findPlayer(Role.LECTER);
        boolean correctChoice = false;

        godMessage("دکتر لکتر بازیکن مورد نظر را انتخاب کند.");

        while (!correctChoice)
        {
            lecter.setDoneOperation(false);
            lecter.setOperationTime(true);
            awakeRole(Role.LECTER);
            printPlayers(lecter);
            while (!lecter.isDoneOperation())
            {
                delay(1000);
            }
            sleepAll();

            if(players.get(chosenPlayerIndex).getRole().equals(Role.LECTER)) {
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

    public Player doctorOperation()
    {
        Player doctor = findPlayer(Role.DOCTOR);
        boolean correctChoice = false;

        godMessage("دکتر شهر بازیکن مورد نظر را انتخاب کند.");

        while (!correctChoice)
        {
            doctor.setDoneOperation(false);
            doctor.setOperationTime(true);
            awakeRole(Role.DOCTOR);
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

    public void inspectorOperation()
    {
        godMessage("کارآگاه استعلام بگیرد.");
        Player inspector = findPlayer(Role.INSPECTOR);

        inspector.setDoneOperation(false);
        inspector.setOperationTime(true);
        awakeRole(Role.INSPECTOR);
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

    public Player sniperOperation()
    {
        godMessage("حرفه ای هدف خود را اعلام کند");
        Player sniper = findPlayer(Role.SNIPER);
        Player target;
        awakeRole(Role.SNIPER);
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

    public Player psyOperation()
    {
        Player psy = findPlayer(Role.PSYCHOLOGIST);
        Player target;
        godMessage("روانشناس بازیکن را اعلام کند.");
        awakeRole(Role.PSYCHOLOGIST);
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

    public boolean hardLifeOperation()
    {
        Player hard = findPlayer(Role.HARDLIFE);
        godMessage("جان سخت بیدار شود");
        hard.setDoneOperation(false);
        hard.setOperationTime(true);
        awakeRole(Role.HARDLIFE);

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

    public boolean mayorCanceled()
    {
        Player mayor = findPlayer(Role.MAYOR);
        godMessage("شهردار بیدار می شود");
        mayor.setDoneOperation(false);
        mayor.setOperationTime(true);
        awakeRole(Role.MAYOR);
        mayor.receiveMessage("اگر میخواهی رای گیری ملغی شود عدد 2 و اگر نه عدد 1 را وارد کن");
        while (!mayor.isDoneOperation())
            delay(1000);
        if(chosenPlayerIndex == 1)
        {
            godMessage("رای گیری ملغی شد! شهردار میخوابد");
            sleepAll();
            return true;
        }
        else
        {
            godMessage("رای گیری ملغی نشد! شهردار میخوابد");
            sleepAll();
            return false;
        }
    }

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

    public void nightStarts()
    {
        Player votedPlayer;
        Player mafiaTarget;
        Player lecterSaved;
        Player doctorSaved;
        Player sniperTarget;
        Player psyTarget;
        boolean hardCheck = false;

        godMessage("شب آغاز می شود. قابلیت صحبت کردن تا روز غیر فعال می شود.");
        sleepAll();

        votedPlayer = findMaxVote();
        if (votedPlayer != null)
        {
            godMessage(votedPlayer.getUsername()+" بیشترین رای را دارد.");
            if(!mayorCanceled())
                makeSpectator(votedPlayer);
        }
        else
            godMessage("کسی با رای گیری حذف نمی شود!");

        clearVotes();

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
            psyTarget.setSilent(true);
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

        godMessage(report.toString());
        godMessage("پایان شب !");
    }

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

            nightStarts();


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
