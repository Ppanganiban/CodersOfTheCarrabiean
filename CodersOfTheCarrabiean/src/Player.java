import java.util.*;
import java.io.*;
import java.math.*;

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
				return  distance1 < distance2 ? -1 : 0;
			}
		}
	}
	
}
class Positionable {
	int x;
	int y;

	public Positionable(int x, int y) {
		this.x = x;
		this.y = y;
	}

	static public double computeShipDistance(Positionable start, Positionable dest){
		//http://stackoverflow.com/questions/14491444/calculating-distance-on-a-hexagon-grid
		double dx = start.x-dest.x;
		double dy = start.y-dest.y;
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
		return getClass().getName() + " ("+x+","+y+")";
	}
	
	
}

class Barrel extends Positionable {
	int id;
	int quantity;
	State state;
	enum State{
		TOOK,
		PROBABLY_LOSE,
		LOSE,
		STILL_OK,
	}
	public Barrel (int id, int x, int y, int quantity, State state){
		super(x,y);
		this.quantity = quantity;
		this.state = state;
		this.id = id;
	}

	public Barrel (int id, int x, int y, int quantity) {
		this(id, x, y, quantity, State.STILL_OK);
	}
}

class Ship extends Positionable {
	int id;
	int orientation;
	int speed; 
	int rhumLvl;
	boolean needRhum;
	Positionable destination;
	
	public Ship(int id, int x, int y, int orientation, int speed, int rhumLvl) {
		super(x,y);
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
	static List<Ship> ennemyShips;
	static List<List<Barrel>> mappedBarrels;
	static Positionable blocsMapPositions[];
	static List<Trap> mappedTraps;
	static HashMap<Integer, Integer> timeOutFire;

	//Action data
	static Ship currShip;
	static String currAction;
	
	//*******************************
	//************************* DEBUG
	//*******************************
	static void debugEntry(String entityType,
			int entityId,
			int x, int y,
			int arg1, int arg2, int arg3, int arg4){
		System.err.print(String.format("%s %d %d %d %d %d %d %d",
				entityType,
				entityId,
				x, y,
				arg1, arg2, arg3, arg4));
	}
	

	static void printAllMappedBarrels(){
		System.err.println("Barrel trouvés :");
		for (List<Barrel> bloc : mappedBarrels) {
			for (Barrel entry : bloc){
				System.err.println("Barrel "+entry.id+" : "+
			entry.x +
			"," + entry.y);
			}
			System.err.println("-----------------------");
		}
	}
	
	static void printAllMappedBarrels(List<List<Barrel>> barrels){
		System.err.println("Barrel trouvés :");
		for (List<Barrel> bloc : barrels) {
			for (Barrel entry : bloc){
				System.err.println("Barrel "+entry.id+" : "+
						entry.x +
						"," + entry.y);
			}
			System.err.println("-----------------------");
		}
	}

	static void printMappedTrap(){
		System.err.println("Traps trouvés :");
		for (Trap trap : mappedTraps) {		
				System.err.println("Trap : " + trap.x + "," + trap.y);
			System.err.println("-----------------------");
		}
	}

	//*******************************
	//************************* TOOL
	//*******************************
	
	static private int indexMapBlocFromPos(int x, int y){
		int index = 0;
		index = x / (MAX_WIDTH/4);
		index = y > (MAX_HEIGHT/2) ? index + 3 : index;
		return index;
	}

	/*
	 * Ajoute les barrels et bateaux dans notre cartpographie
	 */
	static public void initNewEntries(String entityType,
			int entityId,
			int x, int y,
			int arg1, int arg2, int arg3, int arg4){
		
		if (0 == entityType.compareTo(ENTITY_BARREL)){
			int indexBloc = indexMapBlocFromPos(x,y);
			List<Barrel> mapBloc;
			mapBloc = mappedBarrels.get(indexBloc);

			Barrel newBarrel = new Barrel(entityId, x, y, arg1);
			mapBloc.add(newBarrel);
		}
		else if (0 == entityType.compareTo(ENTITY_SHIP)){
			Ship newShip = new Ship(entityId, x,y, arg1, arg2, arg3);
			if (Ship.isMyShip(arg4)){
				myShips.add(newShip);
				if(false == timeOutFire.containsKey(entityId)){
					timeOutFire.put(entityId, 4);
				}
			} else {
				ennemyShips.add(newShip);
			}
		}
		else if (0 == entityType.compareTo(ENTITY_MINE)){
			Trap newTrap = new Trap(x, y);
			mappedTraps.add(newTrap);
		}
	}

	
	public static String cmd_move(int x, int y){
		return CMD_MOVE +" "+ x + " " + y;
	}
	public static String cmd_fire(int x, int y){
		return CMD_FIRE +" "+ x + " " + y;
	}
	public static String cmd_slower(){
		return CMD_SLOWER;
	}
	public static String cmd_wait(){
		return CMD_WAIT;
	}
	public static String cmd_mine(){
		return CMD_MINE;
	}


	public static boolean isGoodOrientation (Ship ship) {
		Positionable projectedPosition = projectMove(ship,
				ship.orientation, 1);

		if (projectedPosition.x == ship.x 
				&& projectedPosition.y == ship.y) {
			return false;
		}

		double dist1 = Positionable.computeShipDistance(ship,
				ship.destination);
		System.err.println("Distance "+ship.toString()+" to destination : " + dist1);
		double dist2 = Positionable.computeShipDistance(projectedPosition,
				ship.destination);
		System.err.println("Distance "+projectedPosition.toString()+" to destination : " + dist2);		
		return dist2 <= dist1;
	}
	public static Positionable projectMove(Positionable ship,
			int orientation,
			int nbStep){
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
				xResult += (xResult % 2 == 0) ? 0 : 1;				
			}
			yResult -= nbStep;
			break;
		case 2:
			for (int i = 0; i < nbStep; i++) {
				xResult -= (xResult % 2 == 0) ? 1 : 0;
			}

			yResult -= nbStep;
			break;
		case 3:
			xResult -= nbStep;
			break;
		case 4:
			for (int i = 0; i < nbStep; i++) {
				xResult -= (xResult % 2 == 0) ? 1 : 0;
			}

			yResult += nbStep;
			break;
		case 5:
			for (int i = 0; i < nbStep; i++) {
				xResult += (xResult % 2 == 0) ? 0 : 1;
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

		return new Positionable(xResult, yResult);
		
	}
	
	public static boolean isSafePlace(Positionable pos) {
		boolean isOk = true;
		for (Trap t : mappedTraps) {
			if (t.x == pos.x && t.y == pos.y) {
				isOk = false;
				break;
			}
		}
		return isOk;
	}
	public static Positionable subMove(Ship ship) {
		Positionable destination = ship.destination;
		Positionable result = destination;

		if(mappedTraps.size() == 0){
			return result;
		} else {
			//Check si c'est dans notre direction
			//On projette notre bateau de deux cases et on voit si on tombe sur une mine
			boolean isOk = false;
			if(isGoodOrientation(ship)){
				Positionable proj2 = projectMove(ship, ship.orientation, 1);
				isOk = isSafePlace(proj2);
		
				if(isOk) {
					Positionable proj3 = projectMove(proj2, ship.orientation, 2);
					return isSafePlace(proj3) ? proj3 : proj2;
				}				
			}


			Positionable newProj = null;			
			int orientation = ship.orientation;

			newProj = projectMove(ship,
					(orientation + 1) % 6,
					1);
			isOk = isSafePlace(newProj);
			if(isOk) {
				Positionable proj3 = projectMove(newProj, ship.orientation, 2);
				return isSafePlace(proj3) ? proj3 : newProj;
			}

			newProj = projectMove(ship,
					(orientation - 1 + 6) % 6,
					1);
			isOk = isSafePlace(newProj);				
			if(isOk) {
				Positionable proj3 = projectMove(newProj, ship.orientation, 2);
				return isSafePlace(proj3) ? proj3 : newProj;
			}
			return newProj;
			
		}
		
	}
	public static void computeShipAction(int idShip, int round){
		currShip = myShips.get(idShip);
		timeOutFire.put(currShip.id, timeOutFire.get(currShip.id) - 1);

		if (currShip.destination == null){
			currShip.destination = new Positionable((int)(Math.random() * LAST_X_CASE), (int)(Math.random() * LAST_Y_CASE));
		}
		System.err.println("ID "+currShip.id+" Destination : "+ currShip.destination.x + ","+currShip.destination.y);
		boolean isNearEnnemy = false;
		Ship posRealEnnemy = null;
		for (Ship ennemy : ennemyShips) {
			if (Positionable.computeShipDistance(currShip, ennemy) < 4) {
				posRealEnnemy = ennemy;
				break;
			}
		}

		if (posRealEnnemy != null && timeOutFire.get(currShip.id)<= 0){
			Positionable target = projectMove(posRealEnnemy, posRealEnnemy.orientation, 2);
			currAction = cmd_fire(target.x, target.y);
			timeOutFire.put(currShip.id, 4);
		} else {
			//Fragmenter le déplacement
			Positionable subDest = subMove(currShip);
			
			if(subDest == null
					|| (subDest.x == currShip.x && subDest.y == currShip.y)) {
				currAction = cmd_move(currShip.destination.x, currShip.destination.y);				
			} else {
				currAction = cmd_move(subDest.x, subDest.y);
			}


			
		}
	}



	public static Positionable nearestBarrel(Ship target, List<Barrel> currBlocMap){
		if(currBlocMap.isEmpty()){
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
		
		//Initialiser le barrels
		for (int i = 0; i < sortedShips.size(); i++) {
			Ship currShip = sortedShips.get(i);
			//Calculer le plus proche
			Collections.sort(sortedMappedBarrels, new ComparatorBlocMap(currShip));
			//printAllMappedBarrels(sortedMappedBarrels);
			List<Barrel> currBlocMap = sortedMappedBarrels.get(0);
			currShip.destination = nearestBarrel(currShip, currBlocMap);
		}
	}
	
	/* *****************************
	 * MAIN
	 * *****************************/
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);

		int round = 0;
		timeOutFire = new HashMap<Integer, Integer>();

		// game loop
		while (true) {
			int myShipCount = in.nextInt(); // the number of remaining ships
			int entityCount = in.nextInt(); // the number of entities (e.g. ships, mines or cannonballs)

			//Initialisation des variables globales
			myShips = new ArrayList<Ship>(3);
			ennemyShips = new ArrayList<Ship>(3);

			//Grille divisé en 8 pour simplifier la cartographie
			mappedBarrels = new ArrayList<List<Barrel>>(NB_BLOCS);
			for(int i = 0; i < NB_BLOCS; i++){
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
				System.out.println(currAction); // Any valid action, such as "WAIT" or "MOVE x y"
			}
			
			round++;
		}
	}
}