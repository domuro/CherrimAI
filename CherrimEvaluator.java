import connectK.BoardModel;
import java.awt.Point;

public class CherrimEvaluator{ 
	private int[] kVectors;
	private BoardModel state;
	public Point immediateThreats[];
	public boolean threatFound;
	
	
	public CherrimEvaluator(BoardModel state){
		kVectors = new int[state.kLength];
		this.state = state;
		immediateThreats = new Point[state.kLength];
		for(int i = 0; i < immediateThreats.length; i++){
			immediateThreats[i] = new Point();
		}
		threatFound = false;
	}
	
	public int evaluate(CherrimBoard board){
		int value = 0;
		int partialKPoints;
		int i, j;
		byte currentPiece;
		threatFound = false;
		
		//pass through board
		for(j = 0; j < state.height; j++){
			for(i = 0; i < state.width; i++){
				currentPiece = board.board[i][j];
				if(currentPiece != 0){
					//points for partial-k vectors
					partialKPoints = evaluatePartialK(board, i, j);
					if(partialKPoints == Integer.MAX_VALUE || partialKPoints == Integer.MIN_VALUE){
						return partialKPoints;
					}
					
					if(board.lastMove.x == i && board.lastMove.y == j){
						//weigh last move more
						partialKPoints *= 1.4;
					}
					
					value += partialKPoints;
				}
			}
		}
		return value;
	}
	
	private int evaluatePartialK(CherrimBoard board, int i, int j){
		int n;
		boolean h = true;
		boolean v = true;
		boolean r = true;
		boolean f = true;

		//find all partial k involving this point, ignoring duplicates
		for(n = 0; n < state.kLength; n++){
			if(i-n >= 0 && i-n+state.kLength-1 < state.width){
				//update horizontal
				if(board.board[i-n][j] != 0 && n != 0){
					h = false;
				}
				else if(h){
					updateKVectors(board, i-n, j, 0);
				}
				if(j-n >= 0 && j-n+state.kLength-1 < state.height){
					//update vertical
					if(board.board[i][j-n] != 0 && n != 0){
						v = false;
					}
					else if(v){
						updateKVectors(board, i, j-n, 1);
					}
					
					//update rising
					if(board.board[i-n][j-n] != 0 && n != 0){
						r = false;
					}
					else if(r){
						updateKVectors(board, i-n, j-n, 2);
					}
				}
				if(j+n < state.height && j+n-state.kLength+1 >= 0){
					//update falling
					if(board.board[i-n][j+n] != 0 && n != 0){
						f = false;
					}
					else if(f){
						updateKVectors(board, i-n, j+n, 3);
					}
				}	
			}
			else if(j-n >= 0 && j-n+state.kLength-1 < state.height){
				//update vertical
				if(board.board[i][j-n] != 0 && n != 0){
					v = false;
				}
				else if(v){
					updateKVectors(board, i, j-n, 1);
				}
			}
		}
		
		//assign points for partial-k
		int value = 0;
		int multiplier = 1;
		if(kVectors[kVectors.length-1] != 0){
			value = (kVectors[kVectors.length-1]>0)?Integer.MAX_VALUE:Integer.MIN_VALUE;
			for(n = 0; n < kVectors.length; n++){
				kVectors[n] = 0;
			}
			return value;
		}
		
		for(n = 0; n < kVectors.length-1; n++){
			value += kVectors[n] * multiplier;
			multiplier *= 5;
			kVectors[n] = 0;
		}
		return value;
	}
	
	private void updateKVectors(CherrimBoard board, int i, int j, int dir){
		int n;
		int consecutive = 0;
		switch(dir){
			case 0:
				for(n = 0; n < state.kLength; n++){
					if(!threatFound){
						immediateThreats[n].setLocation(i+n, j);
					}
					if(consecutive == 0){
						consecutive = board.board[i+n][j];
					}
					else{
						if(board.board[i+n][j] == 0 || (consecutive > 0) == (board.board[i+n][j] > 0)){
							consecutive += board.board[i+n][j];
						}
						else{
							consecutive = 0;
							n = state.kLength;
						}
					}
				}
				break;
			case 1:
				for(n = 0; n < state.kLength; n++){
					if(!threatFound){
						immediateThreats[n].setLocation(i, j+n);
					}
					if(consecutive == 0){
						consecutive = board.board[i][j+n];
					}
					else{
						if(board.board[i][j+n] == 0 || (consecutive > 0) == (board.board[i][j+n] > 0)){
							consecutive += board.board[i][j+n];
						}
						else{
							consecutive = 0;
							n = state.kLength;
						}
					}
				}
				break;
			case 2:
				for(n = 0; n < state.kLength; n++){
					if(!threatFound){
						immediateThreats[n].setLocation(i+n, j+n);
					}
					if(consecutive == 0){
						consecutive = board.board[i+n][j+n];
					}
					else{
						if(board.board[i+n][j+n] == 0 || (consecutive > 0) == (board.board[i+n][j+n] > 0)){
							consecutive += board.board[i+n][j+n];
						}
						else{
							consecutive = 0;
							n = state.kLength;
						}
					}
				}
				break;
			case 3:
				for(n = 0; n < state.kLength; n++){
					if(!threatFound){
						immediateThreats[n].setLocation(i+n, j-n);
					}
					if(consecutive == 0){
						consecutive = board.board[i+n][j-n];
					}
					else{
						if(board.board[i+n][j-n] == 0 || (consecutive > 0) == (board.board[i+n][j-n] > 0)){
							consecutive += board.board[i+n][j-n];
						}
						else{
							consecutive = 0;
							n = state.kLength;
						}
					}
				}
				break;
			default:
				System.out.println("[ERROR] Invalid direction!");
				break;
		}
		if(kVectors[state.kLength-1] != 0){
			return;
		}
		if(consecutive > 0){
			kVectors[consecutive-1]++;
		}
		else if(consecutive < 0){
			kVectors[-1*consecutive-1]--;
		}
		if(consecutive == state.kLength -1){
			threatFound = true;
		}
		
	}
}