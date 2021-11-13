package api;

public class Cell {
  private int x;
  private int y;
  private int index;
  private boolean uncovered;
  private int clue;

  public Cell(int x, int y, int index) {
    this.x = x;
    this.y = y;
    this.index = index;
    this.uncovered = false;
  }

  public void uncover(int clue) {
    this.uncovered = true;
    this.clue = clue;
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

  public int getClue() {
    return this.clue;
  }
}
