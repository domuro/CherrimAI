import connectK.CKPlayer;
import connectK.BoardModel;
import java.awt.Point;

public class CherrimAI extends CKPlayer{
	private static boolean alphaBetaEnabled = true;
	
	private BoardModel state;
	private CherrimEvaluator evaluator;

	private CherrimBoard memory[];
	private Point possibleMoves[];
	private int alpha[];
	
	public CherrimAI(byte player, BoardModel state){
		super(player, state);
		teamName = "No Artificial Flavors";
		
		this.state = state;
		this.evaluator = new CherrimEvaluator(state);
		
		//allocate memory; don't exceed ~10 MB
		int spaceLimit = 10000000;
		int depth = state.width * state.height;
		int branchingFactor;
		if(state.gravity){
			branchingFactor =  (2 * state.kLength + 1);
		}
		else{
			branchingFactor = (2 * state.kLength + 1) * (2 * state.kLength + 1);
		}
		
		possibleMoves = new Point[branchingFactor];
		for(int i = 0; i < possibleMoves.length; i++){
			possibleMoves[i] = new Point();
		}
		
		alpha = new int[depth];
		for(int i = 0; i < depth; i++){
			alpha[i] = (i%2 == 0)?Integer.MIN_VALUE:Integer.MAX_VALUE;
		}

		int maxBoards = depth * branchingFactor;
		if(maxBoards*state.width*state.height > spaceLimit){
			memory = new CherrimBoard[spaceLimit/(state.width*state.height)];
		}
		else{
			memory = new CherrimBoard[maxBoards];
		}
		for(int i = 0; i < memory.length; i++){
			memory[i] = new CherrimBoard(state.width, state.height, this.player, state.kLength, state.gravity, evaluator);
		}
	}
	
	public Point getMove(BoardModel state){
		return getMove(state, 5000);
	}

	public Point getMove(BoardModel state, int deadline){
		int depth = 1;
		long end = System.currentTimeMillis() + deadline - 5;
		Point best = null;
		Point temp;
		memory[0].setRoot(state.pieces, state.lastMove);
		
		//respond to immediate threat
		if(!state.gravity){
			evaluator.evaluate(memory[0]);
			if(evaluator.threatFound == true){
				for(int i = 0; i < evaluator.immediateThreats.length; i++){
					if(memory[0].board[evaluator.immediateThreats[i].x][evaluator.immediateThreats[i].y] == 0){
						return new Point(evaluator.immediateThreats[i].x, evaluator.immediateThreats[i].y);
					}
				}
			}
		}
		
		//iterative deepening depth first search
		while(depth < state.width * state.height){
			temp = search(depth, end);
			if(temp != null){
				System.out.println("searched depth: " + depth);
				best = temp;
			}
			depth++;
		}
		
		if(best == null){
			System.out.println("[ERROR] Failed to find a move!");
			for(int i = 0; i < state.pieces.length; i++){
				for(int j = 0; j < state.pieces[0].length; j++){
					if(state.pieces[i][j] == 0){
						best = new Point(i, j);
					}
				}
			}
		}
		return best;
	}
	
	//depth-limited depth first search
	private Point search(int depthLimit, long end){
		CherrimBoard node;
		Point best = null;
		int sp = 0;
		int count = 0;
		int depth, value;
		
		//while stack not empty
		while(sp != -1){
			//check if time is left
			if(count++%100 == 0){
				if(System.currentTimeMillis() > end){
					//TODO: save work done
					//System.out.println("[WARNING] Out of time!");
					return null;
				}
			}
			//peek; push children; mark as visited
			while(memory[sp].depth != depthLimit && memory[sp].visited == false){
				node = memory[sp];
				node.getPossibleMoves(possibleMoves, state.kLength);
				for(int i = 0; possibleMoves[i] != null; i++){
					sp++;
					if(sp > memory.length){
						System.out.println("[WARNING] Out of memory!");
						return null;
					}
					memory[sp].set(node, possibleMoves[i]);
				}
				node.visited = true;
			}
			//evaluate and bookkeeping
			depth = memory[sp].depth;
			if(depth%2 == 0){
				if(depth != 0){
					value = (depth == depthLimit)?evaluator.evaluate(memory[sp]):alpha[depth];
					//should parent (min) take this value?
					if(alpha[depth-1] > value){
						alpha[depth-1] = value;
					}
				}
			}
			else{
				value = (depth == depthLimit)?evaluator.evaluate(memory[sp]):alpha[depth];
				//should parent (max) take this value?
				if(alpha[depth-1] < value){
					alpha[depth-1] = value;
					if(depth == 1){
						if(best == null){
							best = new Point(memory[sp].lastMove);
						}
						else{
							best.setLocation(memory[sp].lastMove);
						}
					}
				}
			}
			
			//alpha-beta pruning
			if(alphaBetaEnabled && depth > 1){
				if(depth%2==0 && alpha[depth-1] <= alpha[depth-2]){
					while(memory[sp].depth != depth-1){
						memory[sp].depth = 0;
						memory[sp].visited = false;
						sp--;
					}
				}
				else if(depth%2== 1 && alpha[depth-1] >= alpha[depth-2]){
					while(memory[sp].depth != depth-1){
						memory[sp].depth = 0;
						memory[sp].visited = false;
						sp--;
					}
				}
			}
			resetAlpha(memory[sp].depth, depthLimit);
			
			//pop
			memory[sp].depth = 0;
			memory[sp].visited = false;
			sp--;
		}
		return best;
	}
	
	private void resetAlpha(int start, int end){
		int i;
		if(start %2 == 0){
			for(i = start; i < end; i+=2){
				alpha[i] = Integer.MIN_VALUE;
			}
			for(i = start+1; i < end; i+=2){
				alpha[i] = Integer.MAX_VALUE;
			}
		}
		else{
			for(i = start; i < end; i+=2){
				alpha[i] = Integer.MAX_VALUE;
			}
			for(i = start+1; i < end; i+=2){
				alpha[i] = Integer.MIN_VALUE;
			}
		}
	}
}
