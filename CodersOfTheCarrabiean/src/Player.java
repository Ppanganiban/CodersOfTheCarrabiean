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

abstract class Positionable {
	int x;
	int y;

	public Positionable(int x, int y) {
		this.x = x;
		this.y = y;
	}
}

class Barrel extends Positionable {
	int quantity;
	State state;
	enum State{
		TOOK,
		PROBABLY_LOSE,
		STILL_OK,
	}
	public Barrel (int x, int y, int quantity, State state){
		super(x,y);
		this.quantity = quantity;
		this.state = state;
	}
	public Barrel (int x, int y, int quantity) {
		this(x, y, quantity, State.STILL_OK);
	}
}

class Ship extends Positionable {
	int orientation;
	int speed; 
	int rhumLvl;
	
	
	public Ship(int x, int y, int orientation, int speed, int rhumLvl) {
		super(x,y);
		this.orientation = orientation;
		this.speed = speed;
		this.rhumLvl = rhumLvl;
	}

	static public boolean isMyShip(int arg4) {
		return arg4 == 1;
	}
}

enum CheckStep {
	TOP_LEFT(0),
	BOT_LEFT(1),
	BOT_RIGHT(2),
	TOP_RIGHT(3);
	
	public int value;
	CheckStep(int value){
		this.value = value;
	}
	int getValue(){
		return value;
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
	static int VISION = 5;

	static int MAX_NB_ROUND = 200;
	
	
	//Check phase data
	static int destY = -1;
	static int destX = -1;
	static int toX, toY;
	static int nbVisitedSides = 0;
	static boolean inCheckPhase = true;
	static int checkPhaseStep = -1;

	static int directionCheck = 0;
	static final int L_TO_R = 1;
	static final int R_TO_L = -1;

	
	
	//Global data 
	static HashMap< Integer, Ship> myShips;
	static HashMap< Integer, Ship> ennemyShips;
	static List<HashMap< Integer, Barrel>> mappedBarrels;

	
	//Action data
	static Ship currShip;
	static String currAction;

	/* *****************************
	 * Tool
	 * *****************************/

	
	static private int indexMapBlocFromPos(int x, int y){
		int index = 0;
		index = x / (MAX_WIDTH/4);
		index = y > (MAX_HEIGHT/2) ? index + 3 : index;
		return index;
	}

	static public void initNewEntries(String entityType,
			int entityId,
			int x, int y,
			int arg1, int arg2, int arg3, int arg4){
		
		if (0 == entityType.compareTo(ENTITY_BARREL)){
			int indexBloc = indexMapBlocFromPos(x,y);
			HashMap<Integer, Barrel> mapBloc;
			mapBloc = mappedBarrels.get(indexBloc);
			mapBloc.put(entityId, new Barrel(x, y, arg1));
		
		}
		else if (0 == entityType.compareTo(ENTITY_SHIP)){
			Ship newShip = new Ship(x,y, arg1, arg2, arg3);

			if (Ship.isMyShip(arg4)){
				myShips.put(entityId, newShip);
			} else {
				ennemyShips.put(entityId, newShip);
			}
		}
	}

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

		if(inCheckPhase)
			checkPhase();
	}
	

	
	//******************** CHECK PHASE
	//On check d'abord toute la zone horizontalement
	static public void checkPhase(){

		if (checkPhaseStep == -1) {
			destX = currShip.x < LAST_X_CASE - currShip.x ?
					LAST_X_CASE - VISION
					: VISION;
	
			if (destX == VISION) {
				directionCheck = R_TO_L;
			} else {
				directionCheck = L_TO_R;
			}
			
			destY = currShip.y < LAST_Y_CASE/2 ?
					LAST_Y_CASE/4
					: 3*LAST_Y_CASE/4;
			
			checkPhaseStep = destX == VISION ? 0 : 1;
			checkPhaseStep = destY == LAST_Y_CASE/2 ?
					checkPhaseStep
					: checkPhaseStep + 2;
			
			toX = destX;
			toY = destY;
		}

		currAction = cmd_move(toX, toY);

		prepareNextPhase();


		

	}

	static public void prepareNextPhase(){

		if(destX == currShip.x){
			destY = currShip.y > LAST_Y_CASE/2 ?
					LAST_Y_CASE/4
					: 3*LAST_Y_CASE/4;
			
			toY = destY;
		} else if (destY == currShip.y) {
			destX = currShip.x < LAST_X_CASE - currShip.x ?
					LAST_X_CASE - VISION
					: VISION;
			toX = destX;
		}
		
		if (nbVisitedSides == 4) {
			inCheckPhase = false;
		}
	}
	
	
	/* *****************************
	 * MAIN
	 * *****************************/
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);

		myShips = new HashMap<Integer, Ship>();
		ennemyShips = new HashMap<Integer, Ship>();

		//Grille divisé en 8 pour simplifier la cartographie
		mappedBarrels = new ArrayList<HashMap<Integer,Barrel>>(NB_BLOCS);
		for(int i = 0; i < NB_BLOCS; i++){
			mappedBarrels.add(new HashMap<Integer, Barrel>());
		}
		
		int round = 0;
		// game loop
		while (true) {
			int myShipCount = in.nextInt(); // the number of remaining ships
			int entityCount = in.nextInt(); // the number of entities (e.g. ships, mines or cannonballs)

			
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