package connectx.elenaUltimate;
import connectx.*;
import java.util.*;
import connectx.CXPlayer;
import connectx.CXBoard;
import connectx.CXGameState;
import connectx.CXCell;
import java.util.Random;


public class elenaUltimate implements CXPlayer {
    private int M, N, X, timeout_in_secs;
    boolean first;
    private Random rand;
    private CXGameState myWin;
    private CXGameState yourWin;
    private int TIMEOUT;
    private long START;
    boolean primogioc;



    public elenaUltimate() {
    }

    public void initPlayer(int M, int N, int X, boolean first, int timeout_in_secs) {
        rand    = new Random(System.currentTimeMillis());
        myWin   = first ? CXGameState.WINP1 : CXGameState.WINP2;
        yourWin = first ? CXGameState.WINP2 : CXGameState.WINP1;
        TIMEOUT = timeout_in_secs;
        primogioc = first;

    }

    public int selectColumn(CXBoard board) {
        START = System.currentTimeMillis(); // Save starting time
        int bestMove = -1;
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        int depth = 0;

        //if the board is empty or it has one tile I put the tile on the centre of the board or
        //in a position I consider optimal for the game result
        if(board.numOfMarkedCells()==0 && first && board.M==6 && board.N==7){
            return(3);
        } else if(board.numOfMarkedCells()<2 && !first && board.M==6 && board.N==7){
            return(1);
        } else if(board.numOfMarkedCells()==0){
            return(board.getAvailableColumns()[board.getAvailableColumns().length / 2]);
        } else if(board.numOfMarkedCells()==1){
            //lm : last move
            CXCell lm = board.getLastMove();
            return(lm.j);
        }

        Integer[] columns = board.getAvailableColumns();

        //for every column : if time's up, break, else if the column is not empty I empirically
        //set a depth, mark the column and call the alphabeta. Since i always want to maximize
        //my score i call alphabeta with maximizePlayer set as false, and i choose the max value
        //between bestScore and score
        //I finally return the column associated with the bestValue
        for(int col : columns){
            if((System.currentTimeMillis()-START)/1000.0 > TIMEOUT*(99.0/100.0)) {
                break;
            } else if(!board.fullColumn(col)){
                if(board.M>7 && board.N>7){
                    depth = 3;
                } else {
                    if(board.numOfFreeCells()<=15){
                        depth=20;
                    } else if(board.numOfFreeCells()>15 && board.numOfFreeCells()<=20){
                        depth=11;
                    } else if(board.numOfFreeCells()>20 && board.numOfFreeCells()<=30){
                        depth = 10;
                    }  else if(board.numOfFreeCells()>30 && board.numOfFreeCells()<=35){
                        depth=10;
                    }else if(board.numOfFreeCells()>35 && board.numOfFreeCells()<=40){
                        depth=9;
                    }  else if(board.numOfFreeCells()>40 && board.numOfFreeCells()<=50){
                        depth=8;
                    } else if(board.numOfFreeCells()>50 && board.numOfFreeCells()<70){
                        depth=6;
                    } else if(board.numOfFreeCells()>=70 && board.numOfFreeCells()< 90){
                        depth = 5;
                    } else if(board.numOfFreeCells()>=90 && board.numOfFreeCells()>=130){
                        depth = 4;
                    } else {
                        depth = 3;
                    }
                }

               board.markColumn(col);
               int score = alphabeta(board, depth-1, alpha, beta, false);
               board.unmarkColumn();
               if(score > bestScore){
                   bestScore = score;
                   bestMove = col;
               }
            }
        }

        if(bestMove <0 || bestMove>board.N || ((System.currentTimeMillis()-START)/1000.0 > TIMEOUT*(99.0/100.0))){
            bestMove = board.getAvailableColumns()[board.getAvailableColumns().length / 2];
        }
        return bestMove;

    }

    //the alphabeta function has as parameters the board, the depth (decremented), alpha and beta values and maximizingPlayer
    //(set at false at the beginning); if maximizingPlayer is set as true, it takes the max value between the
    //eval and the result of the recursive call of alphabeta with maximizingPlayer set as false; otherwise the opposite
    //it returns the eval int
    private int alphabeta(CXBoard board, int depth, int alpha, int beta, boolean maximizingPlayer){
        int eval =0;
        Integer[] columns = board.getAvailableColumns();

        if(depth<=0 || board.gameState()!=CXGameState.OPEN || (System.currentTimeMillis()-START)/1000.0 > TIMEOUT*(99.0/100.0)){
            eval = evaluateGame(board);
            return eval;
        }

        if(maximizingPlayer){
            eval = Integer.MIN_VALUE;
            for(int col : columns){
                if(!board.fullColumn(col)){
                    board.markColumn(col);
                    eval = Math.max(eval, alphabeta(board, depth-1, alpha, beta, false));
                    board.unmarkColumn();
                    alpha = Math.max(alpha, eval);
                    if(alpha >= beta){
                        break;
                    }
                }

            }
        } else {
            eval = Integer.MAX_VALUE;
            for (int col : columns) {
                if (!board.fullColumn(col)) {
                    board.markColumn(col);
                    eval = Math.min(eval, alphabeta(board, depth - 1, alpha, beta, true));
                    board.unmarkColumn();
                    beta = Math.min(eval, beta);
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
        }
        return eval;
    }

    //function that evaluate the game state: if my program wins the game, it returns 200, else if the opponents wins
    //it returns -200; else if it's draw it returns 0, else if the game is open it evaluates the board score based on
    //the consecutive X rows, cols, diagonals and inverted diagonal with evalBoard(board);
    //it returns the score (int)
    private int evaluateGame(CXBoard board){
        int eval=0;
        if(board.gameState() == myWin){
            eval += 200;
            //più è grande la profondità e più diminuisco eval
        } else if (board.gameState()==yourWin){
            eval -= 200;
        } else if(board.gameState()==CXGameState.DRAW){
            eval = 0;
        } else {
            eval = evalBoard(board) ;
        }
        return eval;
    }

    //function that, given the current board as a parameter, evaluate if there are consecutives tiles
    //checking horizontally, vertically, diagonally and with inverted diagonal;
    //if there are between X-1 and X-3 consecutive tiles it adds or decrease (if it opponents') a score "gradually";
    //if X is more than 4 it considers also X-4 consecutive tiles
    //it returns the sum of the score of every sequence of tiles
    private int evalBoard(CXBoard board){
        CXCellState player, opponent;

        if(primogioc){
            player=CXCellState.P1;
            opponent = CXCellState.P2;
        } else {
            player=CXCellState.P2;
            opponent = CXCellState.P1;
        }
        int pieceCount = 0;
        int oppPieceCount = 0;
        int score = 0;

        //horizontal check
        for(int row=0; row<board.M; row++){
            for(int col=0; col<board.N-board.X-1; col++){
                pieceCount=0;
                oppPieceCount=0;
                for(int i=0; i<board.X; i++){
                    if(board.cellState(row, col+i)==player){
                        pieceCount++;
                    } else if(board.cellState(row, col+i)==opponent){
                        oppPieceCount++;
                    }
                }
                if(pieceCount== board.X -1){
                    score +=80;
                } else if(pieceCount== board.X -2){
                    score +=20;
                } else if(pieceCount== board.X -3){
                    score +=15;
                }
                if(oppPieceCount==board.X -1){
                    score -=80;
                }else if(oppPieceCount== board.X -2){
                    score -=20;
                }else if(oppPieceCount== board.X -3){
                    score -=15;
                }
                if(board.X!=4){
                    if(pieceCount==board.X-4){
                        score +=10;
                    }
                    if(oppPieceCount==board.X -4){
                        score -=10;
                    }
                }
            }
        }

        //vertical check
        for(int row=0; row<board.M-board.X-1; row++){
            for(int col=0; col<board.N; col++){
                pieceCount=0;
                oppPieceCount=0;
                for(int i=0; i<board.X; i++){
                    if(board.cellState(row+i, col)==player){
                        pieceCount++;
                    } else if(board.cellState(row+i, col)==opponent){
                        oppPieceCount++;
                    }
                }

                if(pieceCount== board.X -1){
                    score +=80;
                } else if(pieceCount== board.X -2){
                    score +=20;
                } else if(pieceCount== board.X -3){
                    score +=15;
                }
                if(oppPieceCount==board.X -1){
                    score -=80;
                }else if(oppPieceCount== board.X -2){
                    score -=20;
                }else if(oppPieceCount== board.X -3){
                    score -=15;
                }
                if(board.X!=4){
                    if(pieceCount==board.X-4){
                        score +=10;
                    }
                    if(oppPieceCount==board.X -4){
                        score -=10;
                    }
                }


            }
        }

        //diagonal check
        for(int row=0; row<board.M-board.X-1; row++){
            for(int col=0; col<board.N-board.X-1; col++){
                pieceCount=0;
                oppPieceCount=0;
                for(int i=0; i<board.X; i++){
                    if(board.cellState(row+i, col+i)==player){
                        pieceCount++;
                    } else if(board.cellState(row+i, col+i)==opponent){
                        oppPieceCount++;
                    }
                }
                if(pieceCount== board.X -1){
                    score +=80;
                } else if(pieceCount== board.X -2){
                    score +=20;
                } else if(pieceCount== board.X -3){
                    score +=15;
                }
                if(oppPieceCount==board.X -1){
                    score -=80;
                }else if(oppPieceCount== board.X -2){
                    score -=20;
                }else if(oppPieceCount== board.X -3){
                    score -=15;
                }
                if(board.X!=4){
                    if(pieceCount==board.X-4){
                        score +=10;
                    }
                    if(oppPieceCount==board.X -4){
                        score -=10;
                    }
                }
            }
        }


        //inverted diagonal check
        for(int row=0; row<board.M-board.X-1; row++){
            for(int col=board.N; col<board.X-1; col++){
                pieceCount=0;
                oppPieceCount=0;
                for(int i=0; i<board.X; i++){
                    if(board.cellState(row+i, col-i)==player){
                        pieceCount++;
                    } else if(board.cellState(row+i, col-i)==opponent){
                        oppPieceCount++;
                    }
                }
                if(pieceCount== board.X -1){
                    score +=80;
                } else if(pieceCount== board.X -2){
                    score +=20;
                } else if(pieceCount== board.X -3){
                    score +=15;
                }
                if(oppPieceCount==board.X -1){
                    score -=80;
                }else if(oppPieceCount== board.X -2){
                    score -=20;
                }else if(oppPieceCount== board.X -3){
                    score -=15;
                }
                if(board.X!=4){
                    if(pieceCount==board.X-4){
                        score +=10;
                    }
                    if(oppPieceCount==board.X -4){
                        score -=10;
                    }
                }
            }
        }

        return score;
    }


    public String playerName() {
        return "elena";
    }
}



