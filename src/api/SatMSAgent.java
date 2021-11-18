package api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
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

    boolean modelInterrupt = false;

    do {
      // START OF STEP
      long start = System.currentTimeMillis(); // time at the beginning of step
      if (displayActivated) {
        System.out.println(field);
      }
      if (firstDecision) {
        x = 0;
        y = 0;
        firstDecision = false;
      } else {
        Cell bestCell = null;
        // in case no safe cells are remaining in bestCells:
        if (bestCells.isEmpty()) {
          int[][] clauses = cnfGenerator(); // generate the clauses array

          // TEST 1

//          if (displayActivated) {
//            System.out.println("CLAUSES SEPARATION TEST");
//            System.out.println("clauses.length = " + clauses.length);
//          }
//          ArrayList<int[][]> separateClauses = cnfSeparator(clauses);
//          if (displayActivated) {
//            System.out.println("separateClauses.size() = " + separateClauses.size());
//          }

          // TEST 2: Try adding a clause to the clauseList which requires that
          // the neighbourCell must be true (holds a mine).
          // If not a single model can be found for the resulting formula, the
          // neighbourCell must be false => safe bet

          ArrayList<Cell> testBestCells = calculateBestCells(clauses);

          System.out.println("testBestCells:");
          for (Cell c : testBestCells) {
            System.out.print("(" + c.getX() + "," + c.getY() + ") ");
          }
          System.out.println();
          // END TEST

          // Set up the SATsolver
          ISolver solver = new ModelIterator(SolverFactory.newDefault());
          final int MAXVAR = cells.size(); // (max) number of variables
          final int NBCLAUSES = clauses.length; // (max) number of clauses
          solver.newVar(MAXVAR);
          solver.setExpectedNumberOfClauses(NBCLAUSES);

          // iterate through the clauses and add them to the solver
          for (int i = 0; i < NBCLAUSES; i++) {
            int[] clause = clauses[i];
//            if (displayActivated) {
//               System.out.println("Clause " + i + ": " );
//               System.out.println(Arrays.toString(clause) );
//               System.out.println( );
//            }
            try {
              solver.addClause(new VecInt(clause));
            } catch (ContradictionException e) {
              System.out.println("Contradiction occured!");
              e.printStackTrace();
            }
          }

          if (displayActivated) {
            System.out.println("It took " + (System.currentTimeMillis() - start) + "ms to calculate the clauses");
          }

          try {
            ArrayList<int[]> models = new ArrayList<int[]>();
            long startLoop = System.currentTimeMillis();
            // Calculate all models via the solver and add them to the arraylist models
            while (solver.isSatisfiable() && (System.currentTimeMillis() - startLoop) < 50) {
              models.add(solver.model());

              // TODO: TEST
//              long startTest = System.currentTimeMillis();
//              int[] timesFalse = new int[models.get(0).length];
//              for (int[] model : models) {
//                for (int i = 0; i < timesFalse.length; i++) {
//                  if (model[i] < 0) {
//                    timesFalse[i]++;
//                  }
//                }
//              }
//              double[] falseRate = new double[timesFalse.length];
//              for (int i = 0; i < falseRate.length; i++) {
//                falseRate[i] = timesFalse[i] / (double) models.size();
//              }
//              boolean testBool = true;
//              for (double d : falseRate) {
//                if (d == 1.0) {
//                  if (displayActivated) {
//                    System.out.println("Test successful, continue generating models");
//                    System.out.println("The test took " + (System.currentTimeMillis() - startTest) + "ms");
//                  }
//                  testBool = false;
//                  break;
//                }
//              }
//              if (testBool) {
//                if (displayActivated) {
//                  System.out.println("Test failed, break model generation");
//                  System.out.println("The test took " + (System.currentTimeMillis() - startTest) + "ms");
//                }
//                break;
//              }
            }
            if ((System.currentTimeMillis() - startLoop) >= 50) {
              if (displayActivated) {
                System.out.println("MODEL CALCULATION TIMEOUT");
              }
              modelInterrupt = true;
            }
            if (displayActivated) {
              System.out.println("It took " + (System.currentTimeMillis() - start) + "ms to calculate the models");
              System.out.println("models.size = " + models.size());
            }
            // If a model could be found
            if (!models.isEmpty()) {
              boolean notPerfect = true;
              // Iterate through all models and sum up how often a cell isn't assigned a mine
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
                    System.out.println("Equal best cell found: " + Math.abs(models.get(0)[i]) + (" (")
                        + cells.getCell(Math.abs(models.get(0)[i])).getX() + ", "
                        + cells.getCell(Math.abs(models.get(0)[i])).getY() + (") ") + " with a falseRate of "
                        + falseRate[i]);
                  }
                } else if (falseRate[i] > bestRate) {
                  bestRate = falseRate[i];
                  bestCells.clear();
                  bestCells.add(cells.getCell(Math.abs(models.get(0)[i])));
                  if (displayActivated) {
                    System.out.println("New best cell found: " + Math.abs(models.get(0)[i]) + (" (")
                        + cells.getCell(Math.abs(models.get(0)[i])).getX() + ", "
                        + cells.getCell(Math.abs(models.get(0)[i])).getY() + (") ") + " with a falseRate of "
                        + falseRate[i]);
                  }
                  notPerfect = falseRate[i] != 1.0;
                } else if (falseRate[i] == 0) {
                  cells.getCell(Math.abs(models.get(0)[i])).markMine();
                }
              }
              // if there are multiple best cells, choose one of them at random
              if (bestCells.size() > 1) {
                int randIndex = (int) (Math.random() * bestCells.size());
                bestCell = bestCells.get(randIndex);
                bestCells.remove(randIndex);
              } else {
                // otherwise simply choose the one best cell
                bestCell = bestCells.get(0);
                bestCells.clear();
              }

              // If no perfect cell was found
              // OR we cannot be sure the "safe" cell we found is actually safe
              // because our solver took too long and couldn't generate every model
              if (notPerfect || modelInterrupt) {
                bestCells.clear();
                // for testing purposes, we (might) tolerate cells with a false rate of >90%
                // (currently not the case, we only accept perfect cells)
                if (bestRate < 1) {
                  // Choose a random cell from all cells which haven't been marked as mines yet
                  ArrayList<Cell> candidates = cells.getAllCoveredNotDefinitelyMines();
                  if (displayActivated) {
                    System.out.println("Random selection due to notPerfect = true:");
                    System.out.println("Candidates: (" + candidates.size() + ")");
                    for (Cell c : candidates) {
                      System.out.print("(" + c.getX() + "," + c.getY() + ") ");
                    }
                    System.out.println();
                  }
                  Random ran = new Random();
                  int randIndex = ran.nextInt(candidates.size());
                  bestCell = candidates.get(randIndex);
                }
              }

            } else {
              System.out.println("Problem is not satisfiable.");
            }
          } catch (TimeoutException e) {
            System.out.println("Timeout occured!");
            e.printStackTrace();
          }

          // get the coordinates of the chosen best cell
          if (bestCell != null) {
            x = bestCell.getX();
            y = bestCell.getY();
          } else {
            System.out.println("Error: No suitable best cell found!");
            x = rand.nextInt(numOfCols);
            y = rand.nextInt(numOfRows);
          }
        } else {
          // should we have skipped the selection process: choose one of the remaining
          // bestCells
          if (displayActivated) {
            System.out
                .println("Selection process skipped, as there are still bestCells with falseRate = 1.0 remaining");
          }
          Random ran = new Random();
          int randIndex = ran.nextInt(bestCells.size());
          bestCell = bestCells.get(randIndex);
          bestCells.remove(randIndex);
          if (displayActivated) {
            System.out.println("bestCells.size() remaining: " + bestCells.size());
          }
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

      if (displayActivated)
        System.out.println("Uncovering (" + x + "," + y + ")");
      feedback = field.uncover(x, y);
      cells.uncoverCell(x, y, feedback);
      // END OF STEP
      if (displayActivated) {
        System.out.println("This step took " + (System.currentTimeMillis() - start) + "ms");
      }
    } while (feedback >= 0 && !field.solved());

    if (modelInterrupt) {
      System.out.println("\nModel solving was interrupted in at least one step!");
    }

    if (field.solved()) {
      if (displayActivated) {
        System.out.println("\nSolved the field");
      }
      return true;
    } else {
      if (displayActivated) {
        System.out.println("\nBOOM! because of (" + x + "," + y + ")");
      }
//      System.out.println("BOOM! because of (" + x + "," + y + ")");
      return false;
    }
  }

  private ArrayList<Cell> calculateBestCells(int[][] clauses) {
    ArrayList<Cell> bestCells = new ArrayList<Cell>();

    for (Cell c : cells.getAllRelevantCells()) {
      int index = c.getIndex();
      int[][] clausesResolution = new int[clauses.length + 1][];
      for (int i = 0; i < clauses.length; i++) {
        clausesResolution[i] = clauses[i];
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
          // In that case, solver would not be satisfiable and therefore c is a bestCell.
          bestCells.add(c);
        }
      }
      try {
        // We use resolution inference:
        // If the formula is not satisfiable, KB ⊨ ¬c and c is definitely not a mine
        if (!solver.isSatisfiable()) {
          bestCells.add(c);
        }
      } catch (TimeoutException e) {
        System.out.println("Timeout occured!");
        e.printStackTrace();
      }
    }
    return bestCells;
  }

  private ArrayList<int[][]> cnfSeparator(int[][] clauses) {
    ArrayList<int[]> clausesList = new ArrayList<>(Arrays.asList(clauses));
    ArrayList<ArrayList<int[]>> separateClauses = new ArrayList<ArrayList<int[]>>();

    Iterator<int[]> it = clausesList.iterator();
    int i = 0;
    // Iterate through all clauses
    while (it.hasNext()) {
      // Create a new list a new clause island
      separateClauses.add(new ArrayList<int[]>());
      // add the first (root) clause
      separateClauses.get(i).add(it.next());
      i++;
      // and remove it from the list
      it.remove();
      // iterate through all remaining clauses in the original clauses list:
      Iterator<int[]> innerIt = clausesList.iterator();
      while (innerIt.hasNext()) {
        int[] clause = innerIt.next();
        // check every "island" list
        for (ArrayList<int[]> island : separateClauses) {
          // check if any clause of the island shares a literal with the current clause
          for (int[] clauseIsland : island) {
            boolean found = false;
            HashSet<Integer> set = new HashSet<Integer>();
            for (int el : clause) {
              set.add(Math.abs(el));
            }
            for (int el : clauseIsland) {
              if (set.contains(Math.abs(el))) {
                // then add the clause to the island
                island.add(clause);
                // and remove it from the original clauses list
                innerIt.remove();
                found = true;
                break;
              }
            }
            if (found) {
              break;
            }
          }
        }
      }
    }

    ArrayList<int[][]> separateClausesArray = new ArrayList<int[][]>();
    for (ArrayList<int[]> clause : separateClauses) {
      separateClausesArray.add(clause.toArray(new int[0][]));
    }
    return separateClausesArray;
  }

  private int[][] cnfGenerator() {
    ArrayList<int[]> clausesList = new ArrayList<int[]>();

    for (Cell c : cells.getClueCells()) {
      // ArrayList<Cell> coveredNeighbourCells =
      // cells.getCoveredNeighbourCells(c.getX(),
      // c.getY());
      // int n = coveredNeighbourCells.size();
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
