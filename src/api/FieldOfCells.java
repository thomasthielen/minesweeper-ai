package api;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * An inner representation of the field holding all cells.
 * 
 * @author tthielen
 */
public class FieldOfCells {
  private ArrayList<Cell> cells;
  private int numOfCols;
  private int numOfRows;

  /**
   * Constructor which builds the cells array.
   *
   * @param numOfCols the amount of columns
   * @param numOfRows the amount of rows
   */
  public FieldOfCells(int numOfCols, int numOfRows) {
    cells = new ArrayList<Cell>();
    int i = 0;
    for (int y = 0; y < numOfRows; y++) {
      for (int x = 0; x < numOfCols; x++) {
        cells.add(new Cell(x, y, i));
        i++;
      }
    }
    this.numOfCols = numOfCols;
    this.numOfRows = numOfRows;
  }

  /**
   * Returns all uncovered cells which still have covered neighbours.
   *
   * @return all clue cells
   */
  public ArrayList<Cell> getClueCells() {
    ArrayList<Cell> clueCells = new ArrayList<Cell>();
    for (Cell c : cells) {
      ArrayList<Cell> coveredNeighbours = getCoveredNeighbourCells(c.getX(), c.getY());
      ArrayList<Cell> coveredNeighboursNotMine = new ArrayList<Cell>();
      for (Cell cn : coveredNeighbours) {
        if (!cn.isMine()) {
          coveredNeighboursNotMine.add(cn);
        }
      }
      if (coveredNeighbours.size() > 0 && c.isUncovered()) {
        clueCells.add(c);
      }
    }
    return clueCells;
  }

  /**
   * Returns all covered neighbours of all clue cells.
   *
   * @return all relevant cells
   */
  public ArrayList<Cell> getAllRelevantCells() {
    HashSet<Cell> allRelevantCellsSet = new HashSet<Cell>();
    for (Cell c : getClueCells()) {
      allRelevantCellsSet.addAll(getCoveredNeighbourCellsNotMines(c.getX(), c.getY()));
    }
    ArrayList<Cell> allRelevantCells = new ArrayList<Cell>();
    allRelevantCells.addAll(allRelevantCellsSet);
    return allRelevantCells;
  }

  /**
   * Uncovers the cell at the given coordinates and saves its clue.
   *
   * @param x    the x-coordinate of the cell
   * @param y    the y-coordinate of the cell
   * @param clue how many neighbours of the cell are mines
   */
  public void uncoverCell(int x, int y, int clue) {
    getCell(x, y).uncover(clue);
  }

  /**
   * Marks a mine at the given coordinates.
   *
   * @param x the x-coordinate of the cell
   * @param y the y-coordinate of the cell
   */
  public void markMine(int x, int y) {
    getCell(x, y).markMine();
  }

  /**
   * Returns all covered neighbours of the given cell.
   *
   * @param x the x-coordinate of the cell
   * @param y the y-coordinate of the cell
   * @return all covered neighbours of the cell
   */
  public ArrayList<Cell> getCoveredNeighbourCells(int x, int y) {
    ArrayList<Cell> neighbourCells = getNeighbourCells(x, y);
    ArrayList<Cell> coveredNeighbourCells = new ArrayList<Cell>();
    for (Cell c : neighbourCells) {
      if (!c.isUncovered()) {
        coveredNeighbourCells.add(c);
      }
    }
    return coveredNeighbourCells;
  }

  /**
   * Returns all covered neighbours (which are not marked as mines) of the given
   * cell.
   *
   * @param x the x-coordinate of the cell
   * @param y the y-coordinate of the cell
   * @return all covered neighbours (which are not marked as mines) of the cell
   */
  public ArrayList<Cell> getCoveredNeighbourCellsNotMines(int x, int y) {
    ArrayList<Cell> coveredNeighbourCells = getCoveredNeighbourCells(x, y);
    ArrayList<Cell> coveredNeighbourCellsNotMines = new ArrayList<Cell>();
    for (Cell c : coveredNeighbourCells) {
      if (!c.isMine()) {
        coveredNeighbourCellsNotMines.add(c);
      }
    }
    return coveredNeighbourCellsNotMines;
  }

  /**
   * Returns the amount of marked mines among the neighbours of the given cell.
   *
   * @param x the x-coordinate of the cell
   * @param y the y-coordinate of the cell
   * @return the amount of marked mines among the neighbours of the cell
   */
  public int getNeighbourMineCount(int x, int y) {
    ArrayList<Cell> coveredNeighbourCells = getCoveredNeighbourCells(x, y);
    ArrayList<Cell> coveredNeighbourCellsNotMines = getCoveredNeighbourCellsNotMines(x, y);
    return (coveredNeighbourCells.size() - coveredNeighbourCellsNotMines.size());

  }

  /**
   * Returns all covered mines which haven't been marked as mines (yet).
   * 
   * @return all cells which are covered and haven't been marked
   */
  public ArrayList<Cell> getAllCoveredNotDefinitelyMines() {
    ArrayList<Cell> coveredNotDefinitelyMines = new ArrayList<Cell>();
    for (Cell c : cells) {
      if (!c.isUncovered() && !c.isMine()) {
        coveredNotDefinitelyMines.add(c);
      }
    }
    return coveredNotDefinitelyMines;
  }

  /**
   * Returns all neighbours of the given cell.
   *
   * @param x the x-coordinate of the cell
   * @param y the y-coordinate of the cell
   * @return all neighbours of the cell
   */
  public ArrayList<Cell> getNeighbourCells(int x, int y) {
    ArrayList<Cell> neighbourCells = new ArrayList<Cell>();
    // Upper Neighbour
    if (y > 0) {
      neighbourCells.add(getCell(x, y - 1));
    }
    // Upper Right Neighbour
    if (y > 0 && x < numOfCols - 1) {
      neighbourCells.add(getCell(x + 1, y - 1));
    }
    // Right Neighbour
    if (x < numOfCols - 1) {
      neighbourCells.add(getCell(x + 1, y));
    }
    // Lower Right Neighbour
    if (y < numOfRows - 1 && x < numOfCols - 1) {
      neighbourCells.add(getCell(x + 1, y + 1));
    }
    // Lower Neighbour
    if (y < numOfRows - 1) {
      neighbourCells.add(getCell(x, y + 1));
    }
    // Lower Left Neighbour
    if (y < numOfRows - 1 && x > 0) {
      neighbourCells.add(getCell(x - 1, y + 1));
    }
    // Left Neighbour
    if (x > 0) {
      neighbourCells.add(getCell(x - 1, y));
    }
    // Upper Left Neighbour
    if (y > 0 && x > 0) {
      neighbourCells.add(getCell(x - 1, y - 1));
    }
    return neighbourCells;
  }

  /**
   * Returns the size of the cells ArrayList.
   * 
   * @return the size of the field
   */
  public int size() {
    return this.cells.size();
  }

  /**
   * Returns the cell at the given coordinates.
   * 
   * @param x the x-coordinate of the cell
   * @param y the y-coordinate of the cell
   * @return the cell at the given coordinates
   */
  public Cell getCell(int x, int y) {
    return cells.get(x + y * numOfCols);
  }
}
