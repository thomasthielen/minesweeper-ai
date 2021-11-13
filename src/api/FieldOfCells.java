package api;

import java.util.ArrayList;
import java.util.HashSet;

public class FieldOfCells {
  private ArrayList<Cell> cells;
  private int numOfCols;
  private int numOfRows;

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

  public ArrayList<Cell> getUncoveredCells() {
    ArrayList<Cell> uncoveredCells = new ArrayList<Cell>();
    for (Cell c : cells) {
      if (c.isUncovered()) {
        uncoveredCells.add(c);
      }
    }
    return uncoveredCells;
  }

  public ArrayList<Cell> getAllRelevantCells() {
    HashSet<Cell> allRelevantCellsSet = new HashSet<Cell>();
    for (Cell c : getClueCells()) {
      allRelevantCellsSet.addAll(getCoveredNeighbourCells(c.getX(), c.getY()));
    }
    ArrayList<Cell> allRelevantCells = new ArrayList<Cell>();
    allRelevantCells.addAll(allRelevantCellsSet);
    return allRelevantCells;
  }

  //  public ArrayList<Integer> getAllRelevantIndeces() {
  //    ArrayList<Integer> allRelevantIndices = new ArrayList<Integer>();
  //    for (Cell c : getAllRelevantCells()) {
  //      allRelevantIndices.add(Integer.valueOf(c.getIndex()));
  //    }
  //    return allRelevantIndices;
  //  }

  public ArrayList<Cell> getClueCells() {
    ArrayList<Cell> clueCells = new ArrayList<Cell>();
    for (Cell c : cells) {
      ArrayList<Cell> coveredNeighbours = getCoveredNeighbourCells(c.getX(), c.getY());
      if (coveredNeighbours.size() > 0 && c.isUncovered()) {
        clueCells.add(c);
      }
    }
    return clueCells;
  }

  public void uncoverCell(int x, int y, int clue) {
    getCell(x, y).uncover(clue);
  }

  public Cell getCell(int x, int y) {
    return cells.get(x + y * numOfCols);
  }

  public Cell getCell(int index) {
    return cells.get(index);
  }

  public boolean hasCoveredNeighbour(int x, int y) {
    for (Cell c : getNeighbourCells(x, y)) {
      if (!c.isUncovered()) {
        return true;
      }
    }
    return false;
  }

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

  public int size() {
    return this.cells.size();
  }
}
