package api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ModelIterator;

public class SatMSAgent extends MSAgent {

  private Random rand;

  private boolean displayActivated = false;
  private boolean firstDecision = true;

  private FieldOfCells cells;

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
      // START OF STEP
      long start = System.currentTimeMillis();
      if (displayActivated) {
        System.out.println(field);
      }
      if (firstDecision) {
        x = 0;
        y = 0;
        firstDecision = false;
      } else {
        Cell bestCell = null;

        int[][] clauses = knfGenerator();

        ISolver solver = new ModelIterator(SolverFactory.newDefault());

        if (bestCells.isEmpty()) {

          final int MAXVAR = cells.size(); // (max) number of variables
          final int NBCLAUSES = clauses.length; // (max) number of clauses
          solver.newVar(MAXVAR);
          solver.setExpectedNumberOfClauses(NBCLAUSES);

          for (int i = 0; i < NBCLAUSES; i++) {
            int[] clause = clauses[i];
            //          System.out.println("Clause " + i + ": " );
            //          System.out.println(Arrays.toString(clause) );
            //          System.out.println( );
            try {
              solver.addClause(new VecInt(clause));
            } catch (ContradictionException e) {
              System.out.println("Contradiction occured!");
              e.printStackTrace();
            }
          }

          try {
            ArrayList<int[]> models = new ArrayList<int[]>();
            while (solver.isSatisfiable()) {
              models.add(solver.model());
            }
            // If a model could be found
            if (!models.isEmpty()) {
              boolean notPerfect = true;
              // Iterate through all models and sum up how many times a cell isn't assigned a mine
              int[] timesFalse = new int[models.get(0).length];
              for (int[] model : models) {
                for (int i = 0; i < timesFalse.length; i++) {
                  if (model[i] < 0) {
                    timesFalse[i]++;
                  }
                }
              }
              // Calculate the rate of a cell being false using these values
              double[] falseRate = new double[timesFalse.length];
              for (int i = 0; i < falseRate.length; i++) {
                falseRate[i] = timesFalse[i] / (double) models.size();
              }
              // And use the cell with the best rate
              double bestRate = 0.01;
              for (int i = 0; i < falseRate.length; i++) {
                if (falseRate[i] == bestRate) {
                  bestCells.add(cells.getCell(Math.abs(models.get(0)[i])));
                  if (displayActivated) {
                  System.out.println(
                      "Equal best cell found: "
                          + Math.abs(models.get(0)[i])
                          + (" (")
                          + cells.getCell(Math.abs(models.get(0)[i])).getX()
                          + ", "
                          + cells.getCell(Math.abs(models.get(0)[i])).getY()
                          + (") ")
                          + " with a falseRate of "
                          + falseRate[i]);
                  }
                } else if (falseRate[i] > bestRate) {
                  bestRate = falseRate[i];
                  bestCells.clear();
                  bestCells.add(cells.getCell(Math.abs(models.get(0)[i])));
                  if (displayActivated) {
                  System.out.println(
                      "New best cell found: "
                          + Math.abs(models.get(0)[i])
                          + (" (")
                          + cells.getCell(Math.abs(models.get(0)[i])).getX()
                          + ", "
                          + cells.getCell(Math.abs(models.get(0)[i])).getY()
                          + (") ")
                          + " with a falseRate of "
                          + falseRate[i]);
                  }
                  notPerfect = falseRate[i] != 1.0;
                }
              }
              if (bestCells.size() > 1) {
                int randIndex = (int) (Math.random() * bestCells.size());
                bestCell = bestCells.get(randIndex);
                bestCells.remove(randIndex);
              } else {
                bestCell = bestCells.get(0);
                bestCells.clear();
              }
              
              if (notPerfect) {
                bestCells.clear();
                // TODO: Test for random selection here
//                ArrayList<Cell> randomCells = cells.getAllCellsWithoutUncoveredNeighbour();
//                Random ran = new Random();
//                int randIndex = ran.nextInt(randomCells.size());
//                bestCell = randomCells.get(randIndex);
              }

              // System.out.println("Model count = "  + models.size());
            } else {
              System.out.println("Problem is not satisfiable.");
            }
          } catch (TimeoutException e) {
            System.out.println("Timeout occured!");
            e.printStackTrace();
          }

          if (bestCell != null) {
            x = bestCell.getX();
            y = bestCell.getY();
          } else {
            System.out.println("Error: No suitable best cell found!");
            x = rand.nextInt(numOfCols);
            y = rand.nextInt(numOfRows);
          }
          // in here
        } else {
          if (displayActivated) {
          System.out.println(
              "Selection process skipped, as there are still bestCells with falseRate = 1.0 remaining");
          System.out.println("bestCells.size() before: " + bestCells.size() ); 
          }
          //int randIndex = (int) (Math.random() * bestCells.size());
          Random ran = new Random();
          int randIndex = ran.nextInt(bestCells.size());
          bestCell = bestCells.get(randIndex);
          bestCells.remove(randIndex);
          if (bestCell != null) {
            x = bestCell.getX();
            y = bestCell.getY();
          } else {
            System.out.println("Error: No suitable best cell found!");
            x = rand.nextInt(numOfCols);
            y = rand.nextInt(numOfRows);
          }
        }
      }

      if (displayActivated) System.out.println("Uncovering (" + x + "," + y + ")");
      feedback = field.uncover(x, y);
      cells.uncoverCell(x, y, feedback);
      // END OF STEP
      //      System.out.println("Step: " + (System.currentTimeMillis() - start) + "ms" );
    } while (feedback >= 0 && !field.solved());

    if (field.solved()) {
      if (displayActivated) {
        System.out.println("Solved the field");
      }
      return true;
    } else {
      if (displayActivated) {
        System.out.println("BOOM!");
      }
      System.out.println("BOOM! because of (" + x + "," + y + ")"); 
      return false;
    }
  }

  private int[][] knfGenerator() {
    ArrayList<int[]> clausesList = new ArrayList<int[]>();

    for (Cell c : cells.getClueCells()) {
      ArrayList<Cell> coveredNeighbourCells = cells.getCoveredNeighbourCells(c.getX(), c.getY());
      int n = coveredNeighbourCells.size();

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
        if (trueCount != c.getClue()) {
          // Create a CNF clause with the respective indices as variables and add it to the list
          int[] clause = new int[n];
          for (int i = 0; i < truthRow.length; i++) {
            int index = coveredNeighbourCells.get(i).getIndex();
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
