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
		double dx = Math.abs(start.x-dest.x);
		double dy = Math.abs(start.y-dest.y);

		if (start.x == dest.x){
			return dy;
		} else if (start.y == dest.y) {
			return dx;
		} else {
			if(start.y < dest.y)
				return dx + dy - (int)(Math.ceil(dx / 2.0));
			else
				return dx + dy - (int)(Math.floor(dx / 2.0));
		}
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

class Player {
	static final String ENTITY_SHIP = "SHIP";
	static final String ENTITY_BARREL = "BARREL";
	static final int MAX_WIDTH = 23;
	static final int MAX_HEIGHT = 21;
	static final int LAST_X_CASE = MAX_WIDTH;
	static final int LAST_Y_CASE = MAX_HEIGHT;
	static final int NB_BLOCS = 8;
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
				System.err.println("Barrel "+entry.id+" "+
			entry.x +
			" " + entry.y + "State : " +entry.state);
			}
			System.err.println("-----------------------");
		}
	}
	
	static void printAllMappedBarrels(List<List<Barrel>> barrels){
		System.err.println("Barrel trouvés :");
		for (List<Barrel> bloc : barrels) {
			for (Barrel entry : bloc){
				System.err.println("Barrel "+entry.id+" "+
			entry.x +
			" " + entry.y + "State : " +entry.state);
			}
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
			} else {
				ennemyShips.add(newShip);
			}
		}
	}

	
	public static String cmd_move(int x, int y){
		return CMD_MOVE +" "+ x + " " + y;
	}
	public static String cmd_slower(){
		return CMD_SLOWER;
	}
	public static String cmd_wait(){
		return CMD_WAIT;
	}


	public static void computeShipAction(int idShip, int round){
		currShip = myShips.get(idShip);

		if (currShip.destination == null){
			currAction = cmd_wait();
		} else {
			currAction = cmd_move(currShip.destination.x, currShip.destination.y);			
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