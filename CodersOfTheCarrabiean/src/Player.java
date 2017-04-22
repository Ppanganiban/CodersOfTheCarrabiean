import java.util.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/

/* *****************************
 * CLASS
 * *****************************/
class ComparatorBlocMap implements Comparator<List<Barrel>> {

    Ship target;

    public ComparatorBlocMap(Ship tarShip) {
        this.target = tarShip;
    }

    @Override
    public int compare(List<Barrel> o1, List<Barrel> o2) {
        int size1 = o1.size();
        int size2 = o2.size();

        if (size1 != size2) {
            return size2 - size1;
        } else {
            if (size1 == 0) {
                return 0;
            } else {
                double distance1 = Positionable.computeShipDistance(o1.get(0),
                        target);
                double distance2 = Positionable.computeShipDistance(o2.get(0),
                        target);
                return distance1 < distance2 ? -1 : 0;
            }
        }
    }
}

class ComparatorMinPos implements Comparator<Positionable> {

    Positionable ref;

    public ComparatorMinPos(Positionable ref) {
        this.ref = ref;
    }

    @Override
    public int compare(Positionable arg0, Positionable arg1) {
        // TODO Auto-generated method stub
        double dist0 = Positionable.computeShipDistance(ref, arg0);
        double dist1 = Positionable.computeShipDistance(ref, arg1);
        return (int) (dist0 - dist1);
    }
}

class ComparatorMinShip implements Comparator<Ship> {

    Positionable ref;

    public ComparatorMinShip(Positionable ref) {
        this.ref = ref;
    }

    @Override
    public int compare(Ship arg0, Ship arg1) {
        // TODO Auto-generated method stub
        double dist0 = Positionable.computeShipDistance(ref, arg0);
        arg0.distanceToDest = dist0;

        double dist1 = Positionable.computeShipDistance(ref, arg1);
        arg1.distanceToDest = dist1;

        return (int) (dist0 - dist1);
    }
}

class Positionable {
    int x;
    int y;

    public Positionable(int x, int y) {
        this.x = x;
        this.y = y;
    }

    static public double computeShipDistance(Positionable start, Positionable dest) {

        if (start == null || dest == null) {
            return Double.MAX_VALUE;
        }

        double dx = start.x - dest.x;
        double dy = start.y - dest.y;
        double dz = dy - dx;
        return Math.max(Math.max(Math.abs(dx), Math.abs(dy)), dz);
    }

    static public boolean isBetween(Positionable start,
                                    Positionable dest,
                                    Positionable middle) {

        double distStartToDest = Positionable.computeShipDistance(start, dest);
        double distMiddleToDest = Positionable.computeShipDistance(middle, dest);
        double distMiddleToStart = Positionable.computeShipDistance(start, middle);

        return distMiddleToDest < distStartToDest
                && distMiddleToStart < distStartToDest;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return getClass().getName() + " (" + x + "," + y + ")";
    }

    public boolean isSamePos(Positionable pos) {
        return x == pos.x && y == pos.y;
    }

}

class Barrel extends Positionable {
    int id;
    int quantity;
    State state;

    enum State {
        TOOK,
        PROBABLY_LOSE,
        LOSE,
        STILL_OK,
    }

    public Barrel(int id, int x, int y, int quantity, State state) {
        super(x, y);
        this.quantity = quantity;
        this.state = state;
        this.id = id;
    }

    public Barrel(int id, int x, int y, int quantity) {
        this(id, x, y, quantity, State.STILL_OK);
    }
}

class Ship extends Positionable {
    int id;
    int orientation;
    Positionable front;
    Positionable back;
    int speed;
    int rhumLvl;
    boolean needRhum;
    Positionable destination;
    double distanceToDest;

    public Ship(int x, int y, int orientation) {
        this(-1, x, y, orientation, -1, -1);
    }

    public Ship(int id, int x, int y, int orientation, int speed, int rhumLvl) {
        super(x, y);
        this.id = id;
        this.orientation = orientation;
        this.speed = speed;
        this.rhumLvl = rhumLvl;
        this.needRhum = false;
        this.destination = null;
    }

    static public boolean isMyShip(int arg4) {
        return arg4 == 1;
    }

    public int invertedOrientation () { return invertedOrientation(orientation);}

    public static int invertedOrientation(int ori) {
        return (ori + 3) % 6;
    }
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString()
                +"\n Destination :" + destination +"(distance : " + distanceToDest+")"
                +"\n Orientation : " + orientation + "/ Speed :" + speed
                +"\n-------\n";
    }


}

class Trap extends Positionable {

    public Trap(int x, int y) {
        super(x, y);
    }

}

class Player {
    static final String ENTITY_SHIP = "SHIP";
    static final String ENTITY_BARREL = "BARREL";
    static final String ENTITY_CANONBALL = "CANNONBALL";
    static final String ENTITY_MINE = "MINE";
    static final int MAX_WIDTH = 23;
    static final int MAX_HEIGHT = 21;
    static final int LAST_X_CASE = MAX_WIDTH - 1;
    static final int LAST_Y_CASE = MAX_HEIGHT - 1;
    static final int NB_BLOCS = 8;
    static final String CMD_FIRE = "FIRE";
    static final String CMD_MINE = "MINE";
    static final String CMD_MOVE = "MOVE";
    static final String CMD_WAIT = "WAIT";
    static final String CMD_SLOWER = "SLOWER";
    static final int WARNING_RHUM_LVL = 85;
    static final int VISION = 5;

    static int MAX_NB_ROUND = 200;


    //Global data
    static List<Ship> myShips;
    static HashMap<Integer, Ship> myLastShipsPosition;

    static List<Ship> ennemyShips;
    static List<List<Barrel>> mappedBarrels;
    static Positionable blocsMapPositions[];
    static List<Trap> mappedTraps;
    static HashMap<Integer, Integer> timeOutFire;

    static List<Ship> randomDest;

    //Action data
    static String currAction;

    //*******************************
    //************************* DEBUG
    //*******************************
    static void debugEntry(String entityType,
                           int entityId,
                           int x, int y,
                           int arg1, int arg2, int arg3, int arg4) {
        System.err.print(String.format("%s %d %d %d %d %d %d %d",
                entityType,
                entityId,
                x, y,
                arg1, arg2, arg3, arg4));
    }


    static void printAllMappedBarrels() {
        System.err.println("Barrel trouvés :");
        for (List<Barrel> bloc : mappedBarrels) {
            for (Barrel entry : bloc) {
                System.err.println("Barrel " + entry.id + " : " +
                        entry.x +
                        "," + entry.y);
            }
            System.err.println("-----------------------");
        }
    }

    static void printAllMappedBarrels(List<List<Barrel>> barrels) {
        System.err.println("Barrel trouvés :");
        for (List<Barrel> bloc : barrels) {
            for (Barrel entry : bloc) {
                System.err.println("Barrel " + entry.id + " : " +
                        entry.x +
                        "," + entry.y);
            }
            System.err.println("-----------------------");
        }
    }

    static void printMappedTrap() {
        System.err.println("Traps trouvés :");
        for (Trap trap : mappedTraps) {
            System.err.println("Trap : " + trap.x + "," + trap.y);
            System.err.println("-----------------------");
        }
    }

    //*******************************
    //************************* TOOL
    //*******************************

    static private int indexMapBlocFromPos(int x, int y) {
        int index = 0;
        index = x / (MAX_WIDTH / 4);
        index = y > (MAX_HEIGHT / 2) ? index + 3 : index;
        return index;
    }

    /*
     * Ajoute les barrels et bateaux dans notre cartpographie
     */
    static public void initNewEntries(String entityType,
                                      int entityId,
                                      int x, int y,
                                      int arg1, int arg2, int arg3, int arg4) {

        if (0 == entityType.compareTo(ENTITY_BARREL)) {
            int indexBloc = indexMapBlocFromPos(x, y);
            List<Barrel> mapBloc;
            mapBloc = mappedBarrels.get(indexBloc);

            Barrel newBarrel = new Barrel(entityId, x, y, arg1);
            mapBloc.add(newBarrel);
        } else if (0 == entityType.compareTo(ENTITY_SHIP)) {
            Ship newShip = new Ship(entityId, x, y, arg1, arg2, arg3);
            if (Ship.isMyShip(arg4)) {
                myShips.add(newShip);
                if (false == timeOutFire.containsKey(entityId)) {
                    timeOutFire.put(entityId, 4);
                }
            } else {
                ennemyShips.add(newShip);
            }
        } else if (0 == entityType.compareTo(ENTITY_MINE)) {
            Trap newTrap = new Trap(x, y);
            mappedTraps.add(newTrap);
        }
    }


    public static String cmd_move(int x, int y) {
        return CMD_MOVE + " " + x + " " + y;
    }

    public static String cmd_fire(int x, int y) {
        return CMD_FIRE + " " + x + " " + y;
    }

    public static String cmd_slower() {
        return CMD_SLOWER;
    }

    public static String cmd_wait() {
        return CMD_WAIT;
    }

    public static String cmd_mine() {
        return CMD_MINE;
    }


    public static boolean isGoodOrientation(Ship ship) {
        Positionable projectedPosition = projectMove(ship,
                ship.orientation, 1);

        if (projectedPosition.x == ship.x
                && projectedPosition.y == ship.y) {
            return false;
        }

        double dist1 = Positionable.computeShipDistance(ship,
                ship.destination);
        System.err.println("Distance " + ship.toString() + " to destination : " + dist1);
        double dist2 = Positionable.computeShipDistance(projectedPosition,
                ship.destination);
        System.err.println("Distance " + projectedPosition.toString() + " to destination : " + dist2);
        return dist2 <= dist1;
    }

    public static Ship projectMove(Positionable ship,
                                   int orientation,
                                   int nbStep) {
        int x = ship.x;
        int y = ship.y;
        int xResult = x;
        int yResult = y;

        switch (orientation) {
            case 0:
                xResult += nbStep;
                break;
            case 1:
                for (int i = 0; i < nbStep; i++) {
                    xResult += (yResult % 2 == 0) ? 0 : 1;
                }
                yResult -= nbStep;
                break;
            case 2:
                for (int i = 0; i < nbStep; i++) {
                    xResult -= (yResult % 2 == 0) ? 1 : 0;
                }

                yResult -= nbStep;
                break;
            case 3:
                xResult -= nbStep;
                break;
            case 4:
                for (int i = 0; i < nbStep; i++) {
                    xResult -= (yResult % 2 == 0) ? 1 : 0;
                }

                yResult += nbStep;
                break;
            case 5:
                for (int i = 0; i < nbStep; i++) {
                    xResult += (yResult % 2 == 0) ? 0 : 1;
                }

                yResult += nbStep;
                break;
            default:
                xResult = x;
                yResult = y;
                break;
        }

        if (xResult < 0)
            xResult = 0;
        else if (xResult >= LAST_X_CASE)
            xResult = LAST_X_CASE;

        if (yResult < 0)
            yResult = 0;
        else if (yResult >= LAST_Y_CASE)
            yResult = LAST_Y_CASE;

        return new Ship(xResult, yResult, orientation);

    }

    public static boolean isSafePlace(Ship pos) {
        boolean isOk = true;
        Ship front = projectMove(pos, pos.orientation,1 );
        Ship back = projectMove(pos, pos.invertedOrientation(),1 );
        pos.front = front;
        pos.back = back;


        for (Trap t : mappedTraps) {
            int tx = t.x;
            int ty = t.y;
            if ((tx == pos.x && ty == pos.y)) {
                isOk = false;
                break;
            }
        }
        return isOk;
    }

    public static double closestTrap(Positionable pos) {
        double dist = -1;
        for (Trap t : mappedTraps) {
            double tmp = Positionable.computeShipDistance(pos, t);
            dist = dist < 0 || tmp < dist ? tmp : dist;
        }
        return dist;
    }

    public static Positionable frontBlockedByAShip(Ship ship) {
        Positionable frontShip = projectMove(ship, ship.orientation, 2);
        Positionable backShip = projectMove(ship, (ship.orientation + 3) % 6, 2);

        for (Ship blocShip : ennemyShips) {
            if (blocShip.isSamePos(frontShip) || blocShip.isSamePos(backShip)) {
                return blocShip;
            }
            //avant
            Positionable fronBlocShip = projectMove(blocShip, blocShip.orientation, 1);
            if (fronBlocShip.isSamePos(frontShip) || fronBlocShip.isSamePos(backShip)) {
                return fronBlocShip;
            }
            //arriere
            Positionable backBlocShip = projectMove(blocShip, blocShip.orientation + 3 % 6, 1);
            if (backBlocShip.isSamePos(frontShip) || backBlocShip.isSamePos(backShip)) {
                return backBlocShip;
            }
        }

        for (Ship blocShip : myShips) {
            if (blocShip.isSamePos(frontShip) || blocShip.isSamePos(backShip)) {
                return blocShip;
            }
            //avant
            Positionable fronBlocShip = projectMove(blocShip, blocShip.orientation, 1);
            if (fronBlocShip.isSamePos(frontShip) || fronBlocShip.isSamePos(backShip)) {
                return fronBlocShip;
            }
            //arriere
            Positionable backBlocShip = projectMove(blocShip, blocShip.orientation + 3 % 6, 1);
            if (backBlocShip.isSamePos(frontShip) || backBlocShip.isSamePos(backShip)) {
                return backBlocShip;
            }
        }
        return null;
    }

    public static Positionable sidesBlockedByAShip(Ship ship) {
        Positionable leftShip = projectMove(ship, (ship.orientation + 1) % 6, 1);
        Positionable rightShip = projectMove(ship, (ship.orientation - 1 + 6) % 6, 1);
        Positionable leftleftShip = projectMove(ship, (ship.orientation + 2) % 6, 1);
        Positionable rightrightShip = projectMove(ship, (ship.orientation - 2 + 6) % 6, 1);

        for (Ship blocShip : ennemyShips) {
            if (blocShip.isSamePos(leftShip)
                    || blocShip.isSamePos(rightShip)
                    || blocShip.isSamePos(leftleftShip)
                    || blocShip.isSamePos(rightrightShip)) {
                return blocShip;
            }
            //avant
            Positionable fronBlocShip = projectMove(blocShip, blocShip.orientation, 1);
            if (fronBlocShip.isSamePos(leftShip)
                    || fronBlocShip.isSamePos(rightShip)
                    || fronBlocShip.isSamePos(leftleftShip)
                    || fronBlocShip.isSamePos(rightrightShip)) {
                return fronBlocShip;
            }
            //arriere
            Positionable backBlocShip = projectMove(blocShip, blocShip.orientation + 3 % 6, 1);
            if (backBlocShip.isSamePos(leftShip)
                    || backBlocShip.isSamePos(rightShip)
                    || backBlocShip.isSamePos(leftleftShip)
                    || backBlocShip.isSamePos(rightrightShip)) {
                return backBlocShip;
            }
        }

        for (Ship blocShip : myShips) {
            if (blocShip.isSamePos(leftShip)
                    || blocShip.isSamePos(rightShip)
                    || blocShip.isSamePos(leftleftShip)
                    || blocShip.isSamePos(rightrightShip)) {
                return blocShip;
            }
            //avant
            Positionable fronBlocShip = projectMove(blocShip, blocShip.orientation, 1);
            if (fronBlocShip.isSamePos(leftShip)
                    || fronBlocShip.isSamePos(rightShip)
                    || fronBlocShip.isSamePos(leftleftShip)
                    || fronBlocShip.isSamePos(rightrightShip)) {
                return fronBlocShip;
            }
            //arriere
            Positionable backBlocShip = projectMove(blocShip, blocShip.orientation + 3 % 6, 1);
            if (backBlocShip.isSamePos(leftShip)
                    || backBlocShip.isSamePos(rightShip)
                    || backBlocShip.isSamePos(leftleftShip)
                    || backBlocShip.isSamePos(rightrightShip)) {
                return backBlocShip;
            }
        }
        return null;
    }


    public static Positionable computeBetterOrientation(Ship ship) {

        Positionable destination = ship.destination;
        Positionable result = destination;
        Ship lastPosition = myLastShipsPosition.get(ship.id);
        double currDistance = ship.distanceToDest;

        if (ship.isSamePos(destination) || ship.front.isSamePos(destination)) {
            return destination;
        }
        //On check toutes les positions (orientation possible) on va vers
        // celle la plus proche de la destination
        List<Ship> projectedPos = new ArrayList<Ship>();

        for (int i = 0; i < 6; i++) {
            if (i != ship.orientation && i != ship.invertedOrientation()) {
                projectedPos.add(projectMove(ship, i, 1));
            }
        }

        //TRIE ET INITIALISE LES DISTANCES TO DEST
        Collections.sort(projectedPos, new ComparatorMinShip(ship.destination));

        List<Integer> linkedOrientation = new ArrayList<Integer>();

        //On filtre les positions
        Iterator<Ship> iter = projectedPos.iterator();
        while (iter.hasNext()) {
            Ship tmpPos = iter.next();
            int reverseOri = Ship.invertedOrientation(tmpPos.orientation);

            //Empèche la possibilité de reculer
            if (tmpPos.distanceToDest > currDistance) {
                iter.remove();
            } else {
                //Empèche les va et viens
                if (lastPosition != null) {
                    if (tmpPos.isSamePos(lastPosition)
                            && tmpPos.orientation == lastPosition.orientation) {
                        iter.remove();
                    }
                    else {
                        //Check si il y a une bombe
                        if (false == isSafePlace(tmpPos)) {
                            linkedOrientation.add(reverseOri);
                            iter.remove();
                        }
                    }
                }
            }
        }

        //On supprime les orientations inverses
        Iterator<Ship> iter2 = projectedPos.iterator();
        while (iter2.hasNext()) {
            Ship tmpPos = (Ship) iter2.next();
            if (linkedOrientation.contains(tmpPos.orientation)) {
                System.err.println("Linked " + tmpPos.orientation);
                iter2.remove();
            }
        }

        for ( Ship s : projectedPos) {
            System.err.println(s);
            System.err.println("front : "+ s.front );
            System.err.println("back : "+ s.back );
        }
        System.err.println("*********");

        if (false == projectedPos.isEmpty()) {
            result = projectedPos.get(0);
        }
        return result;
    }
    public static Positionable subMove(Ship ship) {
        Positionable destination = ship.destination;
        Positionable result = destination;
        double currDistance = ship.distanceToDest;

        //1 - On vérifie si on est bloqué par un bateau
        //Si c'est le cas à traiter de suite car sinon on ne peut pas bouger
        Positionable blockShip = frontBlockedByAShip(ship);
        if (blockShip != null) {
            System.err.println("Bloqué devant");
            Positionable pos1 = projectMove(ship, (ship.orientation + 2) % 6, 3);
            Positionable pos2 = projectMove(ship, (ship.orientation - 2 + 6) % 6, 3);
            double dist1 = Positionable.computeShipDistance(pos1, destination);
            double dist2 = Positionable.computeShipDistance(pos2, destination);
            return dist1 < dist2 ? pos1 : pos2;
        }
        blockShip = sidesBlockedByAShip(ship);
        if (blockShip != null) {
            System.err.println("Bloqué cotés");
            return projectMove(ship.front, ship.orientation, 1);
        }

        //Si on est pas bloqué par un bateau on avance en évitant les mines
        printMappedTrap();
        if (mappedTraps.size() == 0) {
            return result;
        } else {

            //On essaye d'avancer le plus possible
            for (int i = 0; i < 2; i++) {
                Ship frontDep = projectMove(ship.front, ship.orientation, 1);
                double distAfterFrontDep = Positionable.computeShipDistance(frontDep,
                        destination);

                if(distAfterFrontDep <= currDistance && isSafePlace(frontDep)) {
                    ship.back.x = ship.x;
                    ship.back.y = ship.y;
                    ship.x = ship.front.x;
                    ship.y = ship.front.y;
                    ship.front.x = frontDep.x;
                    ship.front.y = frontDep.y;
                    ship.distanceToDest = Positionable.computeShipDistance(ship,
                            destination);
                }
            }
            System.err.println("AVANCE :"+ship);
            System.err.println("xxxxxxxxxxxxxxxx");
            result = computeBetterOrientation(ship);
            return result;
        }

    }

    public static void computeShipAction(int idShip, int round) {
        Ship ship = myShips.get(idShip);
        timeOutFire.put(ship.id, timeOutFire.get(ship.id) - 1);
        myLastShipsPosition.put(ship.id, new Ship(ship.x, ship.y, ship.orientation));

        //On regarde si on a un ennemy proche au cas ou on voudrait lui tirer dessus
        Ship posRealEnnemy = null;
        for (Ship ennemy : ennemyShips) {
            if (Positionable.computeShipDistance(ship, ennemy) < 4) {
                posRealEnnemy = ennemy;
                break;
            }
        }

        if (posRealEnnemy != null
                && timeOutFire.get(ship.id) <= 0
                && !posRealEnnemy.isSamePos(projectMove(ship.front, ship.orientation, 1))
                && !posRealEnnemy.isSamePos(projectMove(ship.front, ship.orientation, 2))
                && !posRealEnnemy.isSamePos(projectMove(ship.front, ship.orientation, 3))
                ) {
            //On tire sur l'ennemi
            Positionable target = projectMove(posRealEnnemy, posRealEnnemy.orientation, posRealEnnemy.speed);
            currAction = cmd_fire(target.x, target.y);
            timeOutFire.put(ship.id, 4);

        } else {
            //Fragmenter le déplacement
            Positionable subDest = subMove(ship);

            System.err.println(ship);
            System.err.println(subDest);
            if (subDest == null
                    || (subDest.x == ship.x && subDest.y == ship.y)) {
                currAction = cmd_move(ship.destination.x, ship.destination.y);
            } else {
                currAction = cmd_move(subDest.x, subDest.y);
            }

        }
    }


    public static Positionable nearestBarrel(Ship target, List<Barrel> currBlocMap) {
        if (currBlocMap.isEmpty()) {
            return null;
        } else {
            int size = currBlocMap.size();
            Positionable result = currBlocMap.get(0);
            double currDistance = Positionable.computeShipDistance(target, currBlocMap.get(0));
            for (int i = 1; i < size; i++) {
                double tmpDistance = Positionable.computeShipDistance(target, currBlocMap.get(i));
                if (tmpDistance < currDistance) {
                    currDistance = tmpDistance;
                    result = currBlocMap.get(i);
                }
            }
            return result;
        }
    }

    /**
     * Methode pour déterminer dans qu'elle zone doit aller un bateau
     */
    public static void updateShipDestination() {
        //Aller dans la zone où il y a le plus de barrels
        List<List<Barrel>> sortedMappedBarrels = new ArrayList<List<Barrel>>(8);
        sortedMappedBarrels.addAll(mappedBarrels);

        //Le bateu ayant besoin du plus de rhum sera prioritaire à un autre.
        List<Ship> sortedShips = new ArrayList<Ship>(3);
        sortedShips.addAll(myShips);
        Collections.sort(sortedShips, new Comparator<Ship>() {
            @Override
            public int compare(Ship o1, Ship o2) {
                return o1.rhumLvl - o2.rhumLvl;
            }
        });

        //Initialiser les destinations
        for (int i = 0; i < sortedShips.size(); i++) {
            Ship ship = sortedShips.get(i);
            //Calculer le plus proche
            Collections.sort(sortedMappedBarrels, new ComparatorBlocMap(ship));
            //printAllMappedBarrels(sortedMappedBarrels);
            List<Barrel> currBlocMap = sortedMappedBarrels.get(0);
            ship.destination = nearestBarrel(ship, currBlocMap);
            //Si il n' y a plus de barrel
            if (ship.destination == null) {
                //On va attaquer un ennemi s'il a plus de rhum
                for (Ship ennemy : ennemyShips) {
                    if (ennemy.rhumLvl >= ship.rhumLvl) {
                        ship.destination = ennemy;
                        break;
                    }
                }
                //Si on a le plus de rhum on va a un endroit random
                if (ship.destination == null) {
                    Ship rand = randomDest.get(i);
                    while (rand.x == -1
                            || !isSafePlace(rand)
                            || ship.isSamePos(rand)) {
                        rand.x = (int) (Math.random() * LAST_X_CASE);
                        rand.y = (int) (Math.random() * LAST_Y_CASE);
                    }
                    ship.destination = rand;
                }
            }
            ship.distanceToDest = Positionable.computeShipDistance(ship, ship.destination);
            ship.front = projectMove(ship, ship.orientation, 1);
            ship.back = projectMove(ship, ship.invertedOrientation(), 1);


        }


    }

    /* *****************************
     * MAIN
     * *****************************/
    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        int round = 0;
        timeOutFire = new HashMap<Integer, Integer>();
        myLastShipsPosition = new HashMap<Integer, Ship>();
        randomDest = new ArrayList<>();
        for (int i = 0; i < 3 ; i++)  {
            randomDest.add(new Ship(-1, -1, 0));
        }
        // game loop
        while (true) {
            int myShipCount = in.nextInt(); // the number of remaining ships
            int entityCount = in.nextInt(); // the number of entities (e.g. ships, mines or cannonballs)

            //Initialisation des variables globales
            myShips = new ArrayList<Ship>(3);
            ennemyShips = new ArrayList<Ship>(3);

            //Grille divisé en 8 pour simplifier la cartographie
            mappedBarrels = new ArrayList<List<Barrel>>(NB_BLOCS);
            for (int i = 0; i < NB_BLOCS; i++) {
                mappedBarrels.add(new ArrayList<Barrel>());
            }

            mappedTraps = new ArrayList<Trap>();

            for (int i = 0; i < entityCount; i++) {
                int entityId = in.nextInt();
                String entityType = in.next();
                int x = in.nextInt();
                int y = in.nextInt();
                int arg1 = in.nextInt();
                int arg2 = in.nextInt();
                int arg3 = in.nextInt();
                int arg4 = in.nextInt();

                //debugEntry(entityType, entityId, x, y, arg1, arg2, arg3, arg4);
                initNewEntries(entityType, entityId, x, y, arg1, arg2, arg3, arg4);

            }

            //On update la destination de chaque bateau
            updateShipDestination();

            for (int i = 0; i < myShipCount; i++) {
                // Write an action using System.out.println()
                // To debug: System.err.println("Debug messages...");

                computeShipAction(i, round);

                //On sauvegarde la dernière position du bateau
                //Pour éviter les va et viens
                myLastShipsPosition.put(myShips.get(i).id, myShips.get(i));
                System.err.println(myShips.get(i));
                System.out.println(currAction); // Any valid action, such as "WAIT" or "MOVE x y"
            }

            round++;
        }
    }
}