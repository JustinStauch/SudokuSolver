import java.util.*;
import java.io.*;


class Sudoku {
    /* SIZE is the size parameter of the Sudoku puzzle, and N is the square of the size.  For 
     * a standard Sudoku puzzle, SIZE is 3 and N is 9. */
    int SIZE, N;

    /* The grid contains all the numbers in the Sudoku puzzle.  Numbers which have
     * not yet been revealed are stored as 0. */
    int Grid[][];

    /**
     * Store previous copies of the grids, so that they can be brought up when backtracking.
     */
    Stack<int[][]> grids;
    
    public Sudoku(int[][] Grid) {
    	this.Grid = Grid;
    	SIZE = Grid.length;
    	N = SIZE * SIZE;
    	
    	for( int i = 0; i < N; i++ ) 
            for( int j = 0; j < N; j++ ) 
                Grid[i][j] = 0;
    }
    
    /*
     * Solves a Sudoku puzzle.
     * 
     * Author: Justin Stauch
     * ID: 260618573
     * Date: 7 December 2015
     */
    
    /* The solve() method should remove all the unknown characters ('x') in the Grid
     * and replace them with the numbers from 1-9 that satisfy the Sudoku puzzle. */
    public void solve() {
    	//Convert the grid to having data about the spaces encoded in binary.
        initializeGrid();
        
        //Check if solution was deduced already.
        //If it hasn't been deduced, run a backtracking algorithm.
        if (!isFinished()) {
        	grids = new Stack<int[][]>();
        	startBacktrack();
        }
        
        //Convert the grid info back to just numbers.
        restoreGrid();
    }
    
    /**
     * Converts the grid back to readable format.
     * 
     * To run the algorithm each space is represented by a binary number.
     * Each bit represents if the number of the sport is a possible choice.
     * The zero bit indicates if the values have been adjust for it.
     */
    public void restoreGrid() {
    	int j;
    	for (int i = 0; i < N; i++) {
    		for (j = 0; j < N; j++) {
    			Grid[i][j] = (int) (Math.log(Grid[i][j]) / Math.log(2));
    		}
    	}
    }
    
    /**
     * Run a backtracking algorithm to solve the Sudoku.
     * 
     * This is used if the solution cannot be deduced.
     * It picks the space with the fewest possibilities and tries each one.
     * It runs recursively if the guess results in an incomplete grid without a contradiction.
     * 
     * @return If it resulted in a correct solution.
     */
    public boolean startBacktrack() {
    	
    	//Start: Find the best space to guess.
    	//The best one is the one with the fewest choices.
    	//This gives the best odds of guessing right on the first try.
    	
    	int bestX = 0;
    	int bestY = 0;
    	int bestCount = N + 1;
    	
    	int count;
    	int j;
    	for(int i = 0; i < N && bestCount != 2; i++) {
    		for (j = 0; j < N && bestCount!= 2; j++) {
    			count = countChoices(Grid[i][j]);
    			
    			if (count > 1 && count < bestCount) {
    				bestX = i;
    				bestY = j;
    				bestCount = count;
    			}
    		}
    	}
    	
    	//End: Find the best space to guess.
    	
    	//Copy the grid into a new array.
    	int[][] newGrid = new int[N][N];
    	for (int i = 0; i < N; i++) {
    		System.arraycopy(Grid[i], 0, newGrid[i], 0, N);
    	}
    	
    	//Save the old grid on the stack.
    	grids.push(Grid);
    	
    	//Star using the new grid.
    	Grid = newGrid;
    	
    	//If no choices work, reset to the last grid, and return until it has the next choice to try.
    	//The recursion happens here as the backtrack will be called within this method if necessary.
    	if (!tryEachChoice(bestX, bestY)) {
    		//Move to the last grid.
    		Grid = grids.pop();
    		return false;
    	}
    	
    	//Must be correct.
    	return true;
    }
    
    /**
     * Tries each possible choice for the given space, and moves forward with finding the solution.
     * 
     * @param x The x coordinate of the space.
     * @param y The y coordinate of the space.
     * @return If a solution could be found from a guess.
     */
    public boolean tryEachChoice(int x, int y) {
    	//Get the binary number representing this space in the grid.
    	int val = Grid[x][y];
    	
    	while (val > 0) {
    		//Isolates the rightmost 1 bit,
    		// i.e. setting it to the lowest possible value in the space.
    		Grid[x][y] = val & -val;
    		
    		//Sees if the value fits.
    		if (adjustForValue(x, y)) {
    			//If there was no contradiction, but it isn't finished, backtrack again.
    			if (!isFinished()) {
    				//If backtracking works return true.
    				if (startBacktrack()) {
    					return true;
    				}
    			}
    			//It worked, and its done, so return true.
    			else {
    			   return true;
    			}
    		}
    		//Undo the changes to the grid.
    		Grid = grids.peek();
    		
    		//Eliminate the rightmost bit from the possible values.
    		val &= val - 1;
    	}
    	
    	//None of the choices work, head back.
    	return false;
    }
    
    /**
     * Convert the values in the grid to binary numbers.
     * 
     * In any given space, the nth bit represents if n is a possible choice for the space.
     * If it is a power of two, it only has one choice.
     * A 1 in the zero bit means that the it was a power of 2 that was used to eliminate possibilities in other spaces.
     */
    public void initializeGrid() {
    	//Converts all values.
    	for (int i = 0; i < N; i++) {
    		for (int j = 0; j < N; j++) {
    			//Raises 2 to the power of any set value.
    			Grid[i][j] = 1 << Grid[i][j];
    			
    			//If there is no set value there, set bits 1 to N to 1.
    			if (Grid[i][j] < 2) {
    				Grid[i][j] = (1 << N | ((1 << N) - 1)) - 1;
    			}
    		}
    	}
    	
    	//Use any power of 2 values to eliminate choices in other values.
    	for (int i = 0; i < N; i++) {
    		for (int j = 0; j < N; j++) {
    			if (isPowerOfTwo(Grid[i][j])) {
    				adjustForValue(i, j);
    			}
    		}
    	}
    }
    
    /**
     * Eliminates choices in other spaces based on a set space.
     * 
     * @param x The x value of the space.
     * @param y The y value of the space.
     * @return If the value could be set.
     */
    public boolean adjustForValue(int x, int y) {
        //Hold the value.
    	int value = Grid[x][y];
    	
    	//Mark the zero bit as 1 to mark that it was checked.
    	Grid[x][y]++;
    	
    	//Hold if it found contradictions.
    	boolean flag = true;
    	for (int i = 0; i < N && flag; i++) {
    		//Checking to make sure not to check with the current space.
    		if (i != y) {
    			//Eliminates the bit for the value in the space.
    			Grid[x][i] &= ~value;
    			
    			//Nothing works for this space, so it does not work.
    		    if (Grid[x][i] < 2) {
    			    return false;
    		    }
    		    
    		    //If it's a power of two, here is only one choice, so make adjustments for this.
    		    //It makes the changes depth first.
    		    if (isPowerOfTwo(Grid[x][i])) {
    		    	//If this doesn't work, won't work.
    		    	flag = adjustForValue(x, i);
    		    }
    		}
    		
    		//Same as above.
    		if (i != x) {
    		    Grid[i][y] &= ~value;
    		    if (Grid[i][y] < 2) {
    		    	return false;
    		    }
    		    if (isPowerOfTwo(Grid[i][y])) {
    		    	flag = adjustForValue(i, y);
    		    }
    		}
    	}
    	
    	//Adjust to check the big box.
    	int cornerX = x - (x % SIZE);
    	int cornerY = y - (y % SIZE);
    	
    	//Optimization so it isn't reinitialized.
        int j;
    	
    	//Check the big box.
        //Same as checking the rows and coloumns.
    	for (int i = cornerX; i < cornerX + SIZE && flag; i++) {
    		for (j = cornerY; j < cornerY + SIZE && flag; j++) {
    			if (i == x && j == y) {
    				continue;
    			}
    			
    			Grid[i][j] &= ~value;
    			if (Grid[i][j] < 2) {
    				return false;
    			}
    			if (isPowerOfTwo(Grid[i][j])) {
    				flag = adjustForValue(i, j);
    		    }
    		}
    	}
    	
    	//Return what results it got.
    	return flag;
    }

    /**
     * Checks if the number is a power of 2.
     * 
     * @param x The number to check.
     * @return If x is a power of 2.
     */
    public boolean isPowerOfTwo(int x) {
    	return ((x != 0) && ((x & (~x + 1)) == x));
    }
    
    /**
     * Counts the 1 bits in x.
     * 
     * @param x The number to count 1 bits for.
     * @return The number of 1 bits in x.
     */
    public int countChoices(int x) {
    	int count = 0;
    	
    	x &= ~1;
    	
    	while (x != 0) {
    		x &= x - 1;
    		count++;
    	}
    	
    	return count;
    }
    
    /**
     * Checks if every space holds a power of two.
     * 
     * A solution is found if the whole thing has been broken down to just one 1 bit in each.
     * It subtracts 1, because all solved spaces will have a 1 in its zero bit.
     * 
     * @return
     */
    public boolean isFinished() {
    	for (int[] row : Grid) {
    		for (int space : row) {
    			if (!isPowerOfTwo(space - 1)) {
    				return false;
    			}
    		}
    	}
    	return true;
    }
    
    //Bellow this point is the Skeleton code given for the assignment.
    //It just reads in the puzzles, and displays them.
    //It was included, so that testing of the programs could be uniform.
    
    /*****************************************************************************/
    /* NOTE: YOU SHOULD NOT HAVE TO MODIFY ANY OF THE FUNCTIONS BELOW THIS LINE. */
    /*****************************************************************************/
 
    /* Default constructor.  This will initialize all positions to the default 0
     * value.  Use the read() function to load the Sudoku puzzle from a file or
     * the standard input. */
    public Sudoku( int size )
    {
        SIZE = size;
        N = size*size;

        Grid = new int[N][N];
        for( int i = 0; i < N; i++ ) 
            for( int j = 0; j < N; j++ ) 
                Grid[i][j] = 0;
    }


    /* readInteger is a helper function for the reading of the input file.  It reads
     * words until it finds one that represents an integer. For convenience, it will also
     * recognize the string "x" as equivalent to "0". */
    static int readInteger( InputStream in ) throws Exception
    {
        int result = 0;
        boolean success = false;

        while( !success ) {
            String word = readWord( in );

            try {
                result = Integer.parseInt( word );
                success = true;
            } catch( Exception e ) {
                // Convert 'x' words into 0's
                if( word.compareTo("x") == 0 ) {
                    result = 0;
                    success = true;
                }
                // Ignore all other words that are not integers
            }
        }

        return result;
    }


    /* readWord is a helper function that reads a word separated by white space. */
    static String readWord( InputStream in ) throws Exception
    {
        StringBuffer result = new StringBuffer();
        int currentChar = in.read();
	String whiteSpace = " \t\r\n";
        // Ignore any leading white space
        while( whiteSpace.indexOf(currentChar) > -1 ) {
            currentChar = in.read();
        }

        // Read all characters until you reach white space
        while( whiteSpace.indexOf(currentChar) == -1 ) {
            result.append( (char) currentChar );
            currentChar = in.read();
        }
        return result.toString();
    }


    /* This function reads a Sudoku puzzle from the input stream in.  The Sudoku
     * grid is filled in one row at at time, from left to right.  All non-valid
     * characters are ignored by this function and may be used in the Sudoku file
     * to increase its legibility. */
    public void read( InputStream in ) throws Exception
    {
        for( int i = 0; i < N; i++ ) {
            for( int j = 0; j < N; j++ ) {
                Grid[i][j] = readInteger( in );
            }
        }
    }


    /* Helper function for the printing of Sudoku puzzle.  This function will print
     * out text, preceded by enough ' ' characters to make sure that the printint out
     * takes at least width characters.  */
    void printFixedWidth( String text, int width )
    {
        for( int i = 0; i < width - text.length(); i++ )
            System.out.print( " " );
        System.out.print( text );
    }


    /* The print() function outputs the Sudoku grid to the standard output, using
     * a bit of extra formatting to make the result clearly readable. */
    public void print()
    {
        // Compute the number of digits necessary to print out each number in the Sudoku puzzle
        int digits = (int) Math.floor(Math.log(N) / Math.log(10)) + 1;

        // Create a dashed line to separate the boxes 
        int lineLength = (digits + 1) * N + 2 * SIZE - 3;
        StringBuffer line = new StringBuffer();
        for( int lineInit = 0; lineInit < lineLength; lineInit++ )
            line.append('-');

        // Go through the Grid, printing out its values separated by spaces
        for( int i = 0; i < N; i++ ) {
            for( int j = 0; j < N; j++ ) {
                printFixedWidth( String.valueOf( Grid[i][j] ), digits );
            	// Print the vertical lines between boxes 
                if( (j < N-1) && ((j+1) % SIZE == 0) )
                    System.out.print( " |" );
                System.out.print( " " );
            }
            System.out.println();

            // Print the horizontal line between boxes
            if( (i < N-1) && ((i+1) % SIZE == 0) )
                System.out.println( line.toString() );
        }
    }


    /* The main function reads in a Sudoku puzzle from the standard input, 
     * unless a file name is provided as a run-time argument, in which case the
     * Sudoku puzzle is loaded from that file.  It then solves the puzzle, and
     * outputs the completed puzzle to the standard output. */
    public static void main( String args[] ) throws Exception
    {
        InputStream in;
        if( args.length > 0 ) 
            in = new FileInputStream( args[0] );
        else
            in = System.in;

        // The first number in all Sudoku files must represent the size of the puzzle.  See
        // the example files for the file format.
        int puzzleSize = readInteger( in );
        if( puzzleSize > 100 || puzzleSize < 1 ) {
            System.out.println("Error: The Sudoku puzzle size must be between 1 and 100.");
            System.exit(-1);
        }

        Sudoku s = new Sudoku( puzzleSize );

        // read the rest of the Sudoku puzzle
        s.read( in );
        
        long startTime = System.nanoTime();
        
        // Solve the puzzle.  We don't currently check to verify that the puzzle can be
        // successfully completed.  You may add that check if you want to, but it is not
        // necessary.
        s.solve();
        
        long endTime = System.nanoTime();

        // Print out the (hopefully completed!) puzzle
        s.print();
        
        //Check the solution, and print the time.
        if (s.checkSolution()) {
        	System.out.println("Found solution in " + ((endTime - startTime) / 1000) + " microseconds");
        }
        else {
        	System.out.println("Incorrect");
        }
    }
    
    /**
     * I was using this to check if it was solved.
     * 
     * @return If it is solved.
     */
    public boolean checkSolution() {
    	Set<Integer> foundX;
    	Set<Integer> foundY;
    	
    	int sumX;
    	int sumY;
    	
    	for(int i = 0; i < N; i++) {
    		foundX = new HashSet<Integer>();
        	foundY = new HashSet<Integer>();
        	sumX = 0;
        	sumY = 0;
    		for(int j = 0; j < N; j++) {
    			sumX += Grid[i][j];
    			
    			if (foundX.contains(Grid[i][j])) {
    				System.out.println(i + ", " + j + " = " + Grid[i][j]);
    				return false;
    			}
    			
    			foundX.add(Grid[i][j]);
    			
    			
    			sumY += Grid[j][i];
    			
    			if (foundY.contains(Grid[j][i])) {
    				return false;
    			}
    			
    			foundY.add(Grid[j][i]);
    		}
    		
    		if (sumX != (N * (N + 1)) / 2) {
    			return false;
    		}
    		
    		if (sumY != (N * (N + 1)) / 2) {
    			return false;
    		}
    		
    		
    	}
    	int sum;
    	for (int i = 0; i < SIZE; i++) {
    		for (int j = 0; j < SIZE; j++) {
    			int conX = i * SIZE;
    			int conY = j * SIZE;
    		    
    			foundX = new HashSet<Integer>();
    			sum = 0;
    			for (int k = 0; k < SIZE; k++) {
    				for (int l = 0; l < SIZE; l++) {
    					sum += Grid[conX + k][conY + l];
    					
    	    			if (foundX.contains(Grid[conX + k][conY + l])) {
    	    				System.out.println("(" + i + ", " + j + ") = " + Grid[conX + k][conY + l]);
    	    				return false;
    	    			}
    	    			
    	    			foundX.add(Grid[conX + k][conY + l]);
    				}
    			}
    			
    			if (sum != (N * (N + 1)) / 2) {
        			return false;
        		}
    		}
    	}
    	
    	return true;
    }
    
    public void m(int x) {
    	System.out.println(x);
    }
}
