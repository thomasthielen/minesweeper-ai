package api;

public class Cell {
  private int x;
  private int y;
  private int index;
  private boolean uncovered;
  private boolean mine;
  private int clue;

  public Cell(int x, int y, int index) {
    this.x = x;
    this.y = y;
    this.index = index;
    this.uncovered = false;
    this.mine = false;
  }

  public void uncover(int clue) {
    this.uncovered = true;
    this.clue = clue;
  }
  
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
