package api;

import java.util.ArrayList;
import java.util.Random;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ModelIterator;

public class SatMSAgent extends MSAgent {

  private Random rand; // random int generator in case bestCell == null

  private boolean displayActivated = false; // used to display only the first iteration
  private boolean firstDecision = true; // used to force the agent to pick (0,0) on his first step

  private FieldOfCells cells; // the inner representation of the field of cells

  /**
   * Constructor: Initialises the random int generator as well as the field of
   * cells.
   * 
   * @param field
   */
  public SatMSAgent(MSField field) {
    super(field);
    this.rand = new Random();
    cells = new FieldOfCells(this.field.getNumOfCols(), this.field.getNumOfRows());
  }

  @Override
  public boolean solve() {

    ArrayList<Cell> bestCells = new ArrayList<Cell>();

    int numOfRows = this.field.getNumOfRows();
    int numOfCols = this.field.getNumOfCols();
    int x, y, feedback;

    do {
      // start of a step
      long start = System.currentTimeMillis(); // time at the beginning of step
      if (displayActivated) {
        System.out.println(field);
      } 
      // uncover cell (0,0) on the first step
      if (firstDecision) {
        x = 0;
        y = 0;
        firstDecision = false;
      } else {
        Cell bestCell = null;

        // in case no safe cells are remaining in bestCells:
        if (bestCells.isEmpty()) {

          int[][] clauses = cnfGenerator(); // generate the clauses array

          // general idea:
          // For every cell which neighbours an uncovered cell, add a clause to the
          // generated clauses which is used to proof whether the cell is either
          // definitely a mine or definitely not. Use the gathered information to mark or
          // uncover the respective cells.
          // Cells for which neither can be proven are not modified.
          // In case no safe decision can be made, a random cell (which isn't marked as a
          // mine) is chosen an uncovered.

          long startCalculation = System.currentTimeMillis();
          bestCells = calculateBestCells(clauses);
          if (displayActivated) {
            System.out.println("The calculation of safe cells and mines took: "
                + (System.currentTimeMillis() - startCalculation) + "ms");
          }

          if (displayActivated) {
            if (bestCells.isEmpty()) {
              System.out.println("There aren't any safe cells.");
            } else {
              System.out.println("Safe cells:");
              for (Cell c : bestCells) {
                System.out.print("(" + c.getX() + "," + c.getY() + ") ");
              }
              System.out.println();
            }
          }

          if (bestCells.isEmpty()) {
            // if no safe cells could be found, choose a random cell which hasn't been
            // marked as a mine
            ArrayList<Cell> candidates = cells.getAllCoveredNotDefinitelyMines();
            if (displayActivated) {
              System.out.println(candidates.size() + " Random Candidates:");
              for (Cell c : candidates) {
                System.out.print("(" + c.getX() + "," + c.getY() + ") ");
              }
              System.out.println();
            }
            Random ran = new Random();
            int randIndex = ran.nextInt(candidates.size());
            bestCell = candidates.get(randIndex);
          } else if (bestCells.size() > 1) {
            // if multiple safe cells have been found, choose one of them at random
            int randIndex = (int) (Math.random() * bestCells.size());
            bestCell = bestCells.get(randIndex);
            bestCells.remove(randIndex);
          } else {
            // otherwise simply choose the one best cell
            bestCell = bestCells.get(0);
            bestCells.clear();
          }

        } else {
          // in case we skipped the selection process:
          // randomly choose one of the remaining bestCells
          if (displayActivated) {
            System.out.println("Selection process skipped, as there are still safe cells remaining");
          }
          Random ran = new Random();
          int randIndex = ran.nextInt(bestCells.size());
          bestCell = bestCells.get(randIndex);
          bestCells.remove(randIndex);
          if (displayActivated) {
            System.out.println("bestCells.size() remaining: " + bestCells.size());
          }
        }

        // get the coordinates of the chosen best cell
        if (bestCell != null) {
          x = bestCell.getX();
          y = bestCell.getY();
        } else {
          // this shouldn't happen
          System.out.println("Error: No suitable best cell found!\nChoosing completely random cell.");
          x = rand.nextInt(numOfCols);
          y = rand.nextInt(numOfRows);
        }
      }

      if (displayActivated) {
        System.out.println("Uncovering (" + x + "," + y + ")");
      }
      feedback = field.uncover(x, y);
      cells.uncoverCell(x, y, feedback);
      // end of a step
      if (displayActivated) {
        System.out.println("This step took " + (System.currentTimeMillis() - start) + "ms");
      }
    } while (feedback >= 0 && !field.solved());

    if (field.solved()) {
      if (displayActivated) {
        System.out.println("\nSolved the field");
      }
      return true;
    } else {
      if (displayActivated) {
        System.out.println("\nBOOM! because of (" + x + "," + y + ")");
      }
      return false;
    }
  }

  private ArrayList<Cell> calculateBestCells(int[][] clauses) {
    ArrayList<Cell> bestCells = new ArrayList<Cell>();

    for (Cell c : cells.getAllRelevantCells()) {
      for (int j = 0; j < 2; j++) {
        int index = c.getIndex();
        int[][] clausesResolution = new int[clauses.length + 1][];
        for (int i = 0; i < clauses.length; i++) {
          clausesResolution[i] = clauses[i];
        }
        if (j != 0) {
          index *= -1;
        }
        clausesResolution[clauses.length] = new int[] { index };
        // Set up the SATsolver
        ISolver solver = new ModelIterator(SolverFactory.newDefault());
        final int MAXVAR = cells.size(); // (max) number of variables
        final int NBCLAUSES = clausesResolution.length; // (max) number of clauses
        solver.newVar(MAXVAR);
        solver.setExpectedNumberOfClauses(NBCLAUSES);

        // iterate through the clauses and add them to the solver
        for (int i = 0; i < NBCLAUSES; i++) {
          int[] clause = clausesResolution[i];
          try {
            solver.addClause(new VecInt(clause));
          } catch (ContradictionException e) {
            // The exception occurs not only when adding a null clause or a clause which
            // itself is a contradiction, but also when the clause contains only falsified
            // literals after unit propagation.
            // We use this to our advantage, as that automatically means that both the
            // clause {c} as well as {¬c} exist in the formula, which therefore is
            // unsolvable.
            if (j == 0) {
              // KB ⊨ ¬c (c is definitely not a mine)
              bestCells.add(c);
            } else {
              // KB ⊨ c (c is definitely a mine)
              c.markMine();
            }
          }
        }
        try {
          // We use resolution inference:
          if (!solver.isSatisfiable()) {
            if (j == 0) {
              // KB ⊨ ¬c (c is definitely not a mine)
              bestCells.add(c);
            } else {
              // KB ⊨ c (c is definitely a mine)
              c.markMine();
            }
          }
        } catch (TimeoutException e) {
          System.out.println("Timeout occured while testing for satisfiability!");
          e.printStackTrace();
        }
      }
    }
    return bestCells;
  }

  private int[][] cnfGenerator() {
    ArrayList<int[]> clausesList = new ArrayList<int[]>();

    for (Cell c : cells.getClueCells()) {
      ArrayList<Cell> coveredNeighbourCellsNotMines = cells.getCoveredNeighbourCellsNotMines(c.getX(), c.getY());
      int n = coveredNeighbourCellsNotMines.size();

      boolean[][] truthTable = generateTruthTable(n); // Generate a table with number of rows = 2^n

      for (boolean[] truthRow : truthTable) {
        // Check if the condition is not reached (= 0 in truth table)
        int trueCount = 0;
        for (boolean b : truthRow) {
          if (b) {
            trueCount++;
          }
        }
        // For every truthRow which is not a propositional model for our cell
        if (trueCount != c.getClue() - cells.getNeighbourMineCount(c.getX(), c.getY())) {
          // Create a CNF clause with the respective indices as variables and add it to
          // the list
          int[] clause = new int[n];
          for (int i = 0; i < truthRow.length; i++) {
            int index = coveredNeighbourCellsNotMines.get(i).getIndex();
            clause[i] = truthRow[i] ? -index : index;
          }
          clausesList.add(clause);
        }
      }
    }
    // fill the 2D array with the clauses from the list
    int[][] clauses = new int[clausesList.size()][];
    for (int i = 0; i < clauses.length; i++) {
      clauses[i] = clausesList.get(i);
    }
    return clauses;
  }

  private boolean[][] generateTruthTable(int n) {
    boolean[][] truthTable = new boolean[(int) Math.pow(2.0, n)][n];
    boolean[] truthRow = new boolean[n];

    for (int i = 0; i < n; i++) {
      truthRow[i] = false;
    }
    int x = 1;
    for (int i = 0; i < n; i++) {
      if (truthRow[i]) {
        truthRow[i] = false;
      } else {
        truthRow[i] = true;
        i = -1;
        truthTable[x] = new boolean[n];
        for (int j = 0; j < n; j++) {
          truthTable[x][j] = truthRow[j];
        }
        x++;
        continue; // building of new row in truth table complete
      }
    }
    return truthTable;
  }

  public void activateDisplay() {
    this.displayActivated = true;
  }

  public void deactivateDisplay() {
    this.displayActivated = false;
  }
}
