package api;

/**
 * Holds values of a cell of the field.
 * 
 * @author tthielen
 */
public class Cell {
  private int x;
  private int y;
  private int index;
  private boolean uncovered;
  private boolean mine;
  private int clue;

  /**
   * Constructor for the cell.
   * 
   * @param x     the x-coordinate of the cell
   * @param y     the y-coordinate of the cell
   * @param index the index of the cell in the ArrayList
   */
  public Cell(int x, int y, int index) {
    this.x = x;
    this.y = y;
    this.index = index;
    this.uncovered = false;
    this.mine = false;
  }

  /**
   * Uncovers the cell and thereby saves its clue value.
   * 
   * @param clue how many neighbours of the cell are mines
   */
  public void uncover(int clue) {
    this.uncovered = true;
    this.clue = clue;
  }

  /**
   * Permanently marks the cell as a mine.
   */
  public void markMine() {
    this.mine = true;
  }

  public int getX() {
    return this.x;
  }

  public int getY() {
    return this.y;
  }

  public int getIndex() {
    return this.index;
  }

  public boolean isUncovered() {
    return this.uncovered;
  }

  public boolean isMine() {
    return this.mine;
  }

  public int getClue() {
    return this.clue;
  }
}
