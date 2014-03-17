import java.awt.Point;

public class CherrimBoard{
	public static final byte MY_PIECE = 1;
	public static final byte OP_PIECE = -1;
	
	public byte[][] board;
	public boolean visited;
	public int depth;
	public Point lastMove;
	public byte player;
	public int kLength;
	public boolean gravity;
	private CherrimEvaluator evaluator;

	public CherrimBoard(int height, int width, byte player, int kLength, boolean gravity, CherrimEvaluator evaluator){
		this.board = new byte[height][width];
		this.lastMove = new Point();
		
		this.visited = false;
		this.depth = 0;
		
		this.kLength = kLength;
		this.gravity = gravity;
		this.evaluator = evaluator;
		
		this.player = player;
	}
	
	//set root board
	public void setRoot(byte[][] source, Point move){
		for(int i = 0; i < board.length; i++){
			for(int j = 0; j < board[0].length; j++){
				if(source[i][j] == 0){
					board[i][j] = 0;
				}
				else{
					board[i][j] = (byte)((source[i][j] == player)?MY_PIECE:OP_PIECE);
				}
			}
		}
		
		if(move != null){
			this.lastMove.setLocation(move);
		}
		else{
			this.lastMove = new Point(source.length/2, source[0].length/2);
		}
		
		this.depth = 0;
		this.visited = false;
	}
	
	//set this board to a child of source board
	public void set(CherrimBoard source, Point move){
		for(int i = 0; i < board.length; i++){
			for(int j = 0; j < board[0].length; j++){
				board[i][j] = source.board[i][j];
			}
		}
		
		this.depth = source.depth+1;
		this.visited = false;
		
		this.board[move.x][move.y] = (depth%2 == 1)?MY_PIECE:OP_PIECE;
		this.lastMove.setLocation(move);
	}
	
	//find all possible moves in a certain radius
	public void getPossibleMoves(Point possibleMoves[], int radius){
		if(gravity){
			int i, j, count = 0;
			for(i = 0; i < board.length; i++){
				for(j = 0; j < board[0].length; j++){
					if(board[i][j] == 0){
						if(possibleMoves[count] == null){
							possibleMoves[count++] = new Point(i, j);
						}
						else{
							possibleMoves[count++].setLocation(i, j);
						}
						break;
					}
				}
			}
			possibleMoves[count] = null;
		}
		else{
			int i, j, x, y, count = 0;
			
			//analyze forced moves
			evaluator.evaluate(this);
			if(evaluator.threatFound == true){
				for(i = 0; i < evaluator.immediateThreats.length; i++){
					if(board[evaluator.immediateThreats[i].x][evaluator.immediateThreats[i].y] == 0){
						if(possibleMoves[count] == null){
							possibleMoves[count++] = new Point(evaluator.immediateThreats[i].x, evaluator.immediateThreats[i].y);
						}
						else{
							possibleMoves[count++].setLocation(evaluator.immediateThreats[i].x, evaluator.immediateThreats[i].y);
						}
					}
				}
			}
			if(count == 0){
				for(i = -1 * radius; i < radius+1; i++){
					for(j = -1 * radius; j < radius+1; j++){
						x = lastMove.x + i;
						y = lastMove.y + j;
						if(x >= 0 && x < board.length && y >= 0 && y < board[0].length){
							if(board[x][y] == 0){
								if(possibleMoves[count] == null){
									possibleMoves[count++] = new Point(x, y);
								}
								else{
									possibleMoves[count++].setLocation(x, y);
								}
							}
						}
					}
				}
				
				//no moves found in radius; search entire board for possible moves
				if(count == 0){
					for(i = 0; i < board.length; i++){
						for(j = 0; j < board[0].length; j++){
							if(board[i][j] == 0){
								if(possibleMoves[count] == null){
									possibleMoves[count++] = new Point(i, j);
								}
								else{
									possibleMoves[count++].setLocation(i, j);
								}
							}
						}
					}
				}
				possibleMoves[count] = null;
			}
		}
	}
}
