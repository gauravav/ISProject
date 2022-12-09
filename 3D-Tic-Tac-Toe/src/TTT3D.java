import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class TTT3D extends JFrame implements ActionListener
{

	//Global variables that will be accessed or changed by the game
	private JButton newGameBtn;
	private JPanel boardPanel, textPanel, buttonPanel;
	private JLabel status, score;
	private JRadioButton oRadButton, xRadButton, cpuFirstButton, humanFirstButton, easyButton, mediumButton, hardButton;
	private boolean humanFirst = true;
	private int difficulty = 2;				//Variable that changes the amount of looks ahead and the intelligence used if computer goes first
	private int totalLooksAhead = 2;		//Variable that contains the amount of looks ahead the minimax algorithm will do
	private int lookAheadCounter = 0;		//Variable that keeps track of the looks ahead that have been done through recursion

	private int humanScore = 0;				//Total score for human
	private int computerScore = 0;			//Total score for CPU
	int[] finalWin = new int[4];			//Final winning combination
	TicTacToeButton[] finalWinButton = new TicTacToeButton[4];	//Final winning combination

	public boolean win = false;				//Variable to tell if a winning move has been achieved

	char humanPiece = 'X';
	char computerPiece = 'O';

	private char config[][][];				//Configuration of the board that is manipulated in the minimax algorithm
	private TicTacToeButton[][][] boardConfig;	//Button array allows direct access to all buttons on the GUI itself

	private int[][] wins = {
			//Rows on single board 16
			{0,1,2,3}, {4,5,6,7}, {8,9,10,11}, {12,13,14,15}, {16,17,18,19} , {20,21,22,23},
			{24,25,26,27}, {28,29,30,31}, {32,33,34,35}, {36,37,38,39}, {40,41,42,43}, {44,45,46,47},
			{48,49,50,51} , {52,53,54,55}, {56,57,58,59}, {60,61,62,63},

			//Columns on single board 16
			{0,4,8,12}, {1,5,9,13}, {2,6,10,14}, {3,7,11,15}, {16,20,24,28}, {17,21,25,29}, {18,22,26,30},
			{19,23,27,31}, {32,36,40,44}, {33,37,41,45}, {34,38,42,46}, {35,39,43,47}, {48,52,56,60},
			{49,53,57,61}, {50,54,58,62}, {51,55,59,63},

			//Diagonals on single board 8
			{0,5,10,15}, {3,6,9,12}, {16,21,26,31}, {19,22,25,28}, {32,37,42,47}, {35,38,41,44},
			{48, 53, 58, 63}, {51,54,57,60},

			//Straight down through boards 16
			{0,16,32,48}, {1,17,33,49}, {2,18,34,50}, {3,19,35,51}, {4,20,36,52}, {5,21,37,53}, {6,22,38,54},
			{7,23,39,55}, {8,24,40,56}, {9,25,41,57}, {10,26,42,58}, {11,27,43,59}, {12,28,44,60},
			{13,29,45,61}, {14,30,46,62}, {15,31,47,63},

			//Diagonals through boards 20
			{0,20,40,60}, {1,21,41,61}, {2,22,42,62}, {3,23,43,63}, {12,24,36,48}, {13,25,37,49}, {14,26,38,50},
			{15,27,39,51}, {0,17,34,51}, {4,21,38,55}, {8,25,42,59}, {12,29,46,63}, {3,18,33,48}, {7,22,37,52},
			{11,26,41,56}, {15,30,45,60}, {0,21,42,63}, {3,22,41,60}, {12,25,38,51}, {15,26,37,48}
	};


	public static void main(String a[])
	{
		new TTT3D();
	}

	/*
	 * TicTacToeButton is a private inner class that extends JButton and adds information vital to determine the location
	 * to send back to the main array
	 */
	private class TicTacToeButton extends JButton
	{
		public int boxRow;
		public int boxColumn;
		public int boxBoard;
	}

	/*
	 * OneMove is a class that holds information for one potential move. This is used to check if a certain
	 * move is a win
	 */
	public class OneMove
	{
		int board;
		int row;
		int column;
	}

	/*
	 * constructor
	 */
	public TTT3D()
	{
		super("IS Project");
		setSize(700,1000);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setupBoard();
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}


    /*
     * BoardPanel extends JPanel and allows the game board to be drawn behind the TicTacToeButtons without
     * interference.
     */
	public class BoardPanel extends JPanel
	{
		protected void paintComponent(Graphics graphic)
		{
			super.paintComponent(graphic);

			Graphics2D line = (Graphics2D) graphic;
			line.setStroke(new BasicStroke(2));

			//Board 0
			line.drawLine(60, 80, 300, 80);
			line.drawLine(40, 130, 280, 130);
			line.drawLine(20, 180, 260, 180);
			line.drawLine(140, 30, 60, 230);
			line.drawLine(200, 30, 120, 230);
			line.drawLine(260, 30, 180, 230);


			//Board 1
			line.drawLine(60, 300, 300, 300);
			line.drawLine(40, 350, 280, 350);
			line.drawLine(20, 400, 260, 400);
			line.drawLine(140, 250, 60, 450);
			line.drawLine(200, 250, 120, 450);
			line.drawLine(260, 250, 180, 450);

			//Board 2
			line.drawLine(60, 520, 300, 520);
			line.drawLine(40, 570, 280, 570);
			line.drawLine(20, 620, 260, 620);
			line.drawLine(140, 470, 60, 670);
			line.drawLine(200, 470, 120, 670);
			line.drawLine(260, 470, 180, 670);

			//Board 3
			line.drawLine(60, 740, 300, 740);
			line.drawLine(40, 790, 280, 790);
			line.drawLine(20, 840, 260, 840);
			line.drawLine(140, 690, 60, 890);
			line.drawLine(200, 690, 120, 890);
			line.drawLine(260, 690, 180, 890);

			//Draws red line through the first and last winning position, always going through the second, indicating the location
			//of the win
			//Needs work on this
			if(win)
			{
				line.setColor(Color.RED);
				line.drawLine(finalWinButton[0].getBounds().x + 27, finalWinButton[0].getBounds().y + 20,
				finalWinButton[3].getBounds().x + 27, finalWinButton[3].getBounds().y + 20);
			}

		}
	}

	/*
	* setupBoard is the method that builds the GUI
	*/
	public void setupBoard()
	{
		//Creating the 2 arrays to represent the game
		config = new char[4][4][4];
		boardConfig = new TicTacToeButton[4][4][4];

		boardPanel = new BoardPanel();
		buttonPanel = new JPanel();
		textPanel = new JPanel();

		//New Game Button
		newGameBtn = new JButton("New Game");
		newGameBtn.setBounds(400, 370, 120, 30);
		newGameBtn.addActionListener(new NewButtonListener());
		newGameBtn.setName("newGameBtn");

		//X/O Radio Button
		xRadButton = new JRadioButton("X", true);
		oRadButton = new JRadioButton("O");
		xRadButton.setBounds(400, 320, 50, 50);
		oRadButton.setBounds(450, 320, 50, 50);

		ButtonGroup xoSelect = new ButtonGroup();
		xoSelect.add(xRadButton);
		xoSelect.add(oRadButton);

		PieceListener xoListener = new PieceListener();
		xRadButton.addActionListener(xoListener);
		oRadButton.addActionListener(xoListener);

		//First move radio buttons
		humanFirstButton = new JRadioButton("Human First", true);
		cpuFirstButton = new JRadioButton("CPU First");
		humanFirstButton.setBounds(400, 110, 150, 40);
		cpuFirstButton.setBounds(400, 80, 150, 40);

		ButtonGroup firstSelect = new ButtonGroup();
		firstSelect.add(cpuFirstButton);
		firstSelect.add(humanFirstButton);

		FirstListener firstListener = new FirstListener();
		cpuFirstButton.addActionListener(firstListener);
		humanFirstButton.addActionListener(firstListener);

		//Difficulty radio buttons
		easyButton = new JRadioButton("Easy");
		mediumButton = new JRadioButton("Difficult", true);
		hardButton = new JRadioButton("Insane");
		easyButton.setBounds(400, 190, 150, 40);
		mediumButton.setBounds(400, 220, 150, 40);
		hardButton.setBounds(400, 250, 150, 40);

		ButtonGroup difficultyGroup = new ButtonGroup();
		difficultyGroup.add(easyButton);
		difficultyGroup.add(mediumButton);
		difficultyGroup.add(hardButton);

		DifficultyListener difficultyListener = new DifficultyListener();
		easyButton.addActionListener(difficultyListener);
		mediumButton.addActionListener(difficultyListener);
		hardButton.addActionListener(difficultyListener);


		//Welcome title
		status = new JLabel("       Welcome to 3D Tic-Tac-Toe!");
		status.setFont(new Font("Tahoma", Font.PLAIN, 12));

		//Current score panel
//		score = new JLabel("    Human: " + humanScore + "   Computer: " + computerScore);
//		score.setFont(new Font("Tahoma", Font.BOLD, 15));

		//Variables that determine the locations of the TicTacToeButtons as they are placed within loops
		int rowShift = 20;
		int rowStart = 70;

		int xPos = 70;
		int yPos = 30;
		int width = 60;
		int height = 50;

		//Variables to keep track of the current button being placed
		int boardNum = 0;
		int rowNum = 0;
		int columnNum = 0;

		int boxCounter = 0;

		//Board loop
		for (int i = 0; i <= 3; i++)
		{
			//Row loop
			for (int j = 0; j <= 3; j++)
			{
				//Column loop
				for(int k = 0; k <= 3; k++)
				{
					//Creating the new button, setting it to be empty in both arrays
					config[i][j][k] = '-';
					boardConfig[i][j][k] = new TicTacToeButton();
					boardConfig[i][j][k].setFont(new Font("Arial Bold", Font.ITALIC, 20));
					boardConfig[i][j][k].setText("");

					//Making it transparent and add
					boardConfig[i][j][k].setContentAreaFilled(false);
					boardConfig[i][j][k].setBorderPainted(false);
					boardConfig[i][j][k].setFocusPainted(false);

					//Placing the button
					boardConfig[i][j][k].setBounds(xPos, yPos, width, height);
					//Setting information variables
					boardConfig[i][j][k].setName(Integer.toString(boxCounter));
					boardConfig[i][j][k].boxBoard = boardNum;
					boardConfig[i][j][k].boxRow = rowNum;
					boardConfig[i][j][k].boxColumn = columnNum;
					//Adding action listener
					boardConfig[i][j][k].addActionListener(this);

					//Bump the column number 1, move the position that the next button will be placed to the right, and add the current button to the panel
					columnNum++;
					boxCounter++;
					xPos += 60;
					getContentPane().add(boardConfig[i][j][k]);
				}

				//Reset the column number, bump the row number one, move the position that the next button will be placed down and skew it so it matches with the game board
				columnNum = 0;
				rowNum++;
				rowStart = rowStart - rowShift;
				xPos = rowStart;
				yPos += 50;
			}

			//Reset row numbers and row shifts
			rowNum = 0;
			rowShift = 20;
			rowStart = 70;
			boardNum++;
			xPos = rowStart;
			yPos += 20;
		}
		//Panel setup
		boardPanel.setVisible(true);
		textPanel.setVisible(true);
		buttonPanel.setVisible(true);
		status.setVisible(true);

		textPanel.setLayout(new GridLayout(2,1));
		textPanel.add(status);
//		textPanel.add(score);
		textPanel.setBounds(80, 0, 380, 30);

		add(xRadButton);
		add(oRadButton);
		add(humanFirstButton);
		add(cpuFirstButton);
		add(easyButton);
		add(mediumButton);
		add(hardButton);
		add(newGameBtn);
		add(textPanel);
		add(boardPanel);
		setVisible(true);
	}

	/*
	* FirstListener is an ActionListener that sets the starting player based on the players input. If clicked it simply clears the board, sets the
	* starting player, sets the title text, and checks the current difficulty. If the computer is selected to go first and the difficulty is not hard, the computer will
	* play in a random spot to allow for a more competitive game
	*/
	class FirstListener implements ActionListener
	{

		public void actionPerformed(ActionEvent arg0)
		{
			clearBoard();
			status.setForeground(Color.BLACK);
			status.setText("                      Good luck!");

			if(cpuFirstButton.isSelected())
			{
				humanFirst = false;

				if(!hardButton.isSelected())
					computerPlayRandom(); //Easy And Medium Difficulty scenario
				else
					computerPlays(); // Hard Scenario
			}
			else
			{
				humanFirst = true;
			}
		}
	}

	/*
	* NewButtonListener is an ActionListener that clears the board, sets the text, and starts a new game, and will go first if the radio button for the CPU
	* to go first is selected
	*/
	class NewButtonListener implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0)
		{
			clearBoard();
			status.setForeground(Color.BLACK);
			status.setText("                      Good luck!");

			if(!humanFirst)
			{
				if(difficulty == 3)
					computerPlays();
				else
					computerPlayRandom();
			}
		}
	}
	/*
	* PieceListener is an ActionListener that changes the human and computer piece variables based on input from the user. It then clears the board
	* and starts a new game
	*/
	class PieceListener implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			clearBoard();
			status.setForeground(Color.BLACK);
			status.setText("                      Good luck!");

			if(xRadButton.isSelected())
			{
				humanPiece = 'X';
				computerPiece = 'O';
			}
			else
			{
				humanPiece = 'O';
				computerPiece = 'X';
			}

			if(!humanFirst)
			{
				if(difficulty == 3)
					computerPlays();
				else
					computerPlayRandom();
			}
		}
	}

	/*
	* DifficultyListener is an ActionListener that manipulates the difficulty variable and allows the user to change how aggressive or smart the
	* computer will play. The class itself just changes the global difficulty variable, and then starts a new game
	*/
	class DifficultyListener implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			clearBoard();
			status.setForeground(Color.BLACK);
			status.setText("                      Good luck!");

			if(easyButton.isSelected())
			{
				difficulty = 1;
				totalLooksAhead = 2;
			}
			else if(mediumButton.isSelected())
			{
				difficulty = 2;
				totalLooksAhead = 4;
			}
			else
			{
				difficulty = 3;
				totalLooksAhead = 6;
			}

			if(!humanFirst)
			{
				if(difficulty == 3)
					computerPlays();
				else
					computerPlayRandom();
			}
		}
	}

	/*
	* actionPerformed is the listener for all the buttons within the GUI. It takes in the input from the user if he/she clicks on a space, writes that information
	* to both the internal board and the GUI board, and simply checks if that move was a win. If it was, end the game and display the winning move/winning message.
	* If it wasn't, call computerPlays()
	*/
	public void actionPerformed(ActionEvent e)
	{

		//Getting the button clicked's information and setting the arrays accordingly
		TicTacToeButton button = (TicTacToeButton)e.getSource();
		config[button.boxBoard][button.boxRow][button.boxColumn] = humanPiece;
		boardConfig[button.boxBoard][button.boxRow][button.boxColumn].setText(Character.toString(humanPiece));
		boardConfig[button.boxBoard][button.boxRow][button.boxColumn].setEnabled(false);

		OneMove newMove = new OneMove();
		newMove.board = button.boxBoard;
		newMove.row = button.boxRow;
		newMove.column = button.boxColumn;

		if(checkWin(humanPiece, newMove))
		{
			status.setText("Human Won! Please press New Game button to play again.");
			status.setForeground(Color.RED);
			humanScore++;
			win = true;
			disableBoard();
			updateScore();
		}
		else
		{
			computerPlays();
		}
	}

	/*
	 * updateScore() is used to update the score panel with the correct score when a win has occurred
	 */
	public void updateScore()
	{
//		score.setText("               You: " + humanScore + "   Me: " + computerScore);
	}

	/*
	 * clearBoard() is used to reset the board when the new game button has been pressed. It also
	 * repaints the board, clearing the winning move's line
	 */
	public void clearBoard()
	{
		repaint();
		win = false;
		lookAheadCounter = 0;

		for (int i = 0; i <= 3; i++)
		{
			for (int j = 0; j <= 3; j++)
			{
				for(int k = 0; k <= 3; k++)
				{
	    		config[i][j][k] = '-';
	    		boardConfig[i][j][k].setText("");
	    		boardConfig[i][j][k].setEnabled(true);
				}
			}
		}

		finalWin = new int[4];
	}

	/*
	 * disableBoard() is used to disable the board when a win has occurred, not allowing for any unintended
	 * clicks in the game space
	 */

	public void disableBoard()
	{
		int index = 0;
		for (int i = 0; i <= 3; i++)
		{
			for (int j = 0; j <= 3; j++)
			{
				for(int k = 0; k <= 3; k++)
				{
					if(contains(finalWin, Integer.parseInt(boardConfig[i][j][k].getName())))
					{
						boardConfig[i][j][k].setEnabled(true);
						boardConfig[i][j][k].setForeground(Color.RED);
						finalWinButton[index] = boardConfig[i][j][k];
						System.out.println();
						index++;
					}
					else
					{
						boardConfig[i][j][k].setEnabled(false);
					}
				}
			}
		}
		repaint();
	}

	/*
	 * Private method contains() is used in the process of checking the contents of the finalWin int array and
	 * changing the appropriate boxes to show the winning combination
	 */
	private boolean contains(int[] a, int k)
	{
		//Step through array
		for(int i : a)
		{	//Compare elements
			if(k == i)
				return true;
		}
		return false;
	}

	/*
	 * The method computerPlayRandom() is used when the difficulty setting is easy or medium and the computer is selected to go first.
	 * This is implemented because if the computer is allowed to move first using the minimax method, it is almost impossible for a
	 * human to win. Since the setting is on easy or medium, showing that the player might actually want to win, putting the first
	 * move in a random spot allows the game to be more competitive and fun. This method is not called when the setting is hard
	 * allowing for very aggressive play as the difficulty setting would suggest.
	 */
	private void computerPlayRandom()
	{
		Random random = new Random();
		int row = random.nextInt(4);
		int column = random.nextInt(4);
		int board = random.nextInt(4);
		config[board][row][column] = computerPiece;
		boardConfig[board][row][column].setText(Character.toString(computerPiece));
		boardConfig[board][row][column].setEnabled(false);
	}

	/*
	 * computerPlays() is the main method used in the A.I. implementation of this game. It walks through each available move in the
	 * current game board, then creates branches off of those moves using lookAhead(), judging what the player will do in response to
	 * that potential move, and makes a move that is most promising in response to the possible humans most promising move.
	 */
	private void computerPlays()
	{
		int bestScore;
		int hValue;
		OneMove nextMove;
		int bestScoreBoard = -1;
		int bestScoreRow = -1;
		int bestScoreColumn = -1;

		//Low number so the first bestScore will be the starting bestScore
		bestScore = -1000;
		//Walk through the entire game board
		check:
		for (int i = 0; i <= 3; i++)
		{
			for (int j = 0; j <= 3; j++)
			{
				for(int k = 0; k <= 3; k++)
				{
					if(config[i][j][k] == '-')
					{
						//Creating a new move on every empty position
						nextMove = new OneMove();
						nextMove.board = i;
						nextMove.row = j;
						nextMove.column = k;

						if(checkWin(computerPiece, nextMove))
						{
							//Leave the piece there if it is a win and end the game
							config[i][j][k] = computerPiece;
							boardConfig[i][j][k].setText(Character.toString(computerPiece));
							status.setText("   AI won! Please press New Game button to play again.");
							status.setForeground(Color.BLUE);
							win = true;
							computerScore++;
							disableBoard();
							updateScore();
							break check;
						}
						else
						{
							//This is where the method generates all the possible counter moves potentially made
							//by the human player
							if(difficulty != 1)
							{
								hValue = lookAhead(humanPiece, -1000, 1000);
							}
							else
							{
								//If the player is on easy, just calculate the heuristic value for every current possible move, no looking ahead
								hValue = heuristic();
							}

							lookAheadCounter = 0;

							//CPU chooses the best hValue out of every move
							if(hValue >= bestScore)
							{
								bestScore = hValue;
								bestScoreBoard = i;
								bestScoreRow = j;
								bestScoreColumn = k;
								config[i][j][k] = '-';
							}
							else
							{
								config[i][j][k] = '-';
							}
						}
					}
				}
			}
		}

		//If there is no possible winning move, make the move in the calculated best position.
		if(!win)
		{
			config[bestScoreBoard][bestScoreRow][bestScoreColumn] = computerPiece;
			boardConfig[bestScoreBoard][bestScoreRow][bestScoreColumn].setText(Character.toString(computerPiece));

			boardConfig[bestScoreBoard][bestScoreRow][bestScoreColumn].setEnabled(false);
		}
	}

	/*
	 * lookAhead() generates all the possible moves in the available spaces based on the current board in response
	 * to the possible move made by the computer in computerPlays(). This method returns a heuristic value that is calculated
	 * using the heuristic() function. This function also implements the alpha beta pruning technique since the search
	 * tree can become quite large when playing on hard difficulty
	 */
	private int lookAhead(char c, int a, int b)
	{
		//Alpha and beta values that get passed in
		int alpha = a;
		int beta = b;

		//If you still want to look ahead
		if(lookAheadCounter <= totalLooksAhead)
		{

			lookAheadCounter++;
			//If you are going to be placing the computer's piece this time
			if(c == computerPiece)
			{
				int hValue;
				OneMove nextMove;

				for (int i = 0; i <= 3; i++)
				{
					for (int j = 0; j <= 3; j++)
					{
						for(int k = 0; k <= 3; k++)
						{
							if(config[i][j][k] == '-')
							{
								nextMove = new OneMove();
								nextMove.board = i;
								nextMove.row = j;
								nextMove.column = k;

								if(checkWin(computerPiece, nextMove))
								{
									config[i][j][k] = '-';
									return 1000;
								}
								else
								{
									//Recursive look ahead, placing human pieces next
									hValue = lookAhead(humanPiece, alpha, beta);
									if(hValue > alpha)
									{
										alpha = hValue;
										config[i][j][k] = '-';
									}
									else
									{
										config[i][j][k] = '-';
									}
								}

								//Break out of the look if the alpha value is larger than the beta value, going down no further
								if (alpha >= beta)
									break;
							}
						}
					}
				}

				return alpha;
			}

			//If you are going to be placing the human's piece this time
			else
			{
				int hValue;
				OneMove nextMove;

				for (int i = 0; i <= 3; i++)
				{
					for (int j = 0; j <= 3; j++)
					{
						for(int k = 0; k <= 3; k++)
						{

							if(config[i][j][k] == '-')
							{

								nextMove = new OneMove();
								nextMove.board = i;
								nextMove.row = j;
								nextMove.column = k;

								if(checkWin(humanPiece, nextMove))
								{
									config[i][j][k] = '-';
									return -1000;
								}
								else
								{
									//Recursive look ahead, placing computer pieces next
									hValue = lookAhead(computerPiece, alpha, beta);
									if(hValue < beta)
									{
										beta = hValue;
										config[i][j][k] = '-';
									}
									else
									{
										config[i][j][k] = '-';
									}
								}

								//Break out of the look if the alpha value is larger than the beta value, going down no further
								if (alpha >= beta)
									break;
							}
						}
					}
				}

				return beta;
			}
		}
		//If you are at the last level of nodes you want to check
		else
		{
			return heuristic();
		}
	}

	/*
	 * heuristic() simply uses the checkAvailable method for both the computer and human on the current board, and subtracts them
	 * making a higher value more promising for the computer.
	 */
	private int heuristic()
	{
		return (checkAvailable(computerPiece) - checkAvailable(humanPiece));
	}

	/*
	 * checkWin() takes in a character that will be checked for a win and a move that checks if that creates a win. It uses
	 * a 2-dimensional array that holds every possible winning combination and a 1-dimensional array that represents all the
	 * spaces on the game board.
	 */
	private boolean checkWin(char c, OneMove pos)
	{
		config[pos.board][pos.row][pos.column] = c;

		//Array that indicates all the spaces on the game board
		int[] gameBoard = new int[64];

		//Counter from 0 to 49, one for each win combo
		int counter = 0;

		//If the space on the board is the same as the input char, set the corresponding location
		//in gameBoard to 1.
		for (int i = 0; i <= 3; i++)
		{
			for (int j = 0; j <= 3; j++)
			{
				for(int k = 0; k <= 3; k++)
				{
					if(config[i][j][k] == c)
					{
						gameBoard[counter] = 1;
					}
					else
					{
						gameBoard[counter] = 0;
					}
					counter++;
				}
			}
		}

		//For each possible win combination
		for (int i = 0; i <= 75; i++)
		{
			//Resetting counter to see if all 3 locations have been used
			counter = 0;
			for (int j = 0; j <= 3; j++)
			{
				//For each individual winning space in the current combination
				if(gameBoard[wins[i][j]] == 1)
				{
					counter++;

					finalWin[j] = wins[i][j];
					//If all 3 moves of the current winning combination are occupied by char c
					if(counter == 4)
					{
						return true;
					}
				}
			}
		}

		return false;
	}

	/*
	 * checkAvailable is very similar to checkWin(), however instead of returning a boolean if the input
	 * move is a win or not, this method returns an int corresponding to the amount of possible wins available
	 * on the current board for input char c ('X' or 'O')
	 */
	private int checkAvailable(char c)
	{
		int winCounter = 0;

		//Array that indicates all the spaces on the game board
		int[] gameBoard = new int[64];

		//Counter from 0 to 49, one for each win combo
		int counter = 0;

		//If the space on the board is the same as the input char, set the corresponding location
		//in gameBoard to 1.
		for (int i = 0; i <= 3; i++)
		{
			for (int j = 0; j <= 3; j++)
			{
				for(int k = 0; k <= 3; k++)
				{
					if(config[i][j][k] == c || config[i][j][k] == '-')
						gameBoard[counter] = 1;
					else
						gameBoard[counter] = 0;

					counter++;
				}
			}
		}

		//For each possible win combination
		for (int i = 0; i <= 75; i++)
		{
			//Resetting counter to see if all 4 locations have been used
			counter = 0;
			for (int j = 0; j <= 3; j++)
			{
				//For each individual winning space in the current combination
				if(gameBoard[wins[i][j]] == 1)
				{
					counter++;

					finalWin[j] = wins[i][j];
					//If all 4 moves of the current winning combination are occupied by char c
					if(counter == 4)
						winCounter++;
				}
			}
		}

		return winCounter;
	}
}
