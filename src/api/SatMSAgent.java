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

  private Random rand;

  private boolean displayActivated = false;
  private boolean firstDecision = true;

  private boolean secondDecision = false;

  private FieldOfCells cells;

  public SatMSAgent(MSField field) {
    super(field);
    this.rand = new Random();
    cells = new FieldOfCells(this.field.getNumOfCols(), this.field.getNumOfRows());
  }

  @Override
  public boolean solve() {

    int numOfRows = this.field.getNumOfRows();
    int numOfCols = this.field.getNumOfCols();
    int x, y, feedback;

    do {
      if (displayActivated) {
        System.out.println(field);
      }
      if (firstDecision) {
        x = 0;
        y = 0;
        firstDecision = false;
        secondDecision = true;
      } else {
        Cell bestCell = null;

        int[][] clauses = knfGenerator();

        ISolver solver = new ModelIterator(SolverFactory.newDefault());

        final int MAXVAR = cells.size(); // (max) number of variables
        final int NBCLAUSES = clauses.length; // (max) number of clauses
        solver.newVar(MAXVAR);
        solver.setExpectedNumberOfClauses(NBCLAUSES);

        for (int i = 0; i < NBCLAUSES; i++) {
          int[] clause = clauses[i];
//          if (secondDecision) {
//            System.out.println("Clause:");
//            for (int j : clause) {
//              System.out.print(j + " ");
//            }
//            System.out.println();
//          }
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
            // Iterate through all models and sum up how many times a cell isn't assigned a mine
            int[] timesFalse = new int[models.get(0).length];
            for (int[] model : models) {
//              if (secondDecision) {
//                System.out.println("Model: ");
//                for (int m : model) {
//                  System.out.print(m + " ");
//                }
//                System.out.println();
//              }
              for (int i = 0; i < timesFalse.length; i++) {
                if (model[i] < 0) {
                  timesFalse[i]++;
                }
              }
            }
            // Calculate the rate of a cell being false using these values
            double[] falseRate = new double[timesFalse.length];
//            System.out.println("falseRates:");
            for (int i = 0; i < falseRate.length; i++) {
              falseRate[i] = timesFalse[i] / (double) models.size();
//              System.out.print(falseRate[i] + " ");
            }
//            System.out.println();
            // And use the cell with the best rate
            double bestRate = -1.0;
            for (int i = 0; i < falseRate.length; i++) {
              if (falseRate[i] > bestRate) {
                bestRate = falseRate[i];
                bestCell = cells.getCell(Math.abs(models.get(0)[i]));
              }
            }
//            System.out.println("bestCell: " + bestCell.getX() + ", " + bestCell.getY());
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

        // secondDecision = false;
      }

      if (displayActivated) System.out.println("Uncovering (" + x + "," + y + ")");
      feedback = field.uncover(x, y);
      cells.uncoverCell(x, y, feedback);
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
      return false;
    }
  }

  private int[][] knfGenerator() {
    ArrayList<int[]> clausesList = new ArrayList<int[]>();

    for (Cell c : cells.getClueCells()) {
      //            System.out.println("ClueCell: " + c.getIndex() + " with clue = " + c.getClue());
      ArrayList<Cell> coveredNeighbourCells = cells.getCoveredNeighbourCells(c.getX(), c.getY());
      //            System.out.println("Its neighbours:" );
      //            for (Cell cn : coveredNeighbourCells) {
      //              System.out.print(cn.getIndex() + " " );
      //            }
      //            System.out.println( );
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
