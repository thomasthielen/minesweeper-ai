package api;

import java.text.DecimalFormat;

/**
 * An example of how to use a MSAgent to solve the game. You can do whatever you want with this
 * class.
 */
public class UsageExample {

  /**
   * Array containing the names of all fields. If you want to iterate over all of them, this might
   * help
   */
  
  private static final double[] expectedRate = {
    100, 83.7, 100, 100, 100, 100, 100, 100, 50.6, 75.3, 69, 100, 100, 100, 100, 51.2, 67.8, 61.7,
    16.3, 6, 74, 66, 14, 100
  };

  private static final double[] expectedAvgDuration = {
    0.001, 0.004, 0.005, 0.005, 0.008, 0.009, 0.01, 0.012, 0.023, 0.082, 0.073, 0.036, 0.048, 0.031,
    0.281, 0.451, 0.332, 0.439, 0.169, 1.876, 1.178, 1.769, 2.451, 1.548
  };
  
  // File Name | Index | Iterations | Success Rate | Average Duration on Success
  public static final String[] fields = {
    "baby1-3x3-0.txt", // 0 | 1000 | 100% | 0,001s
    "baby2-3x3-1.txt", // 1 | 1000 | 83,7% | 0,004s
    "baby3-5x5-1.txt", // 2 | 1000 | 100% | 0,005s
    "baby4-5x5-3.txt", // 3 | 1000 | 100% | 0,005s
    "baby5-5x5-5.txt", // 4 | 1000 | 100% | 0,008s
    "baby6-7x7-1.txt", // 5 | 1000 | 100% | 0,009s
    "baby7-7x7-3.txt", // 6 | 1000 | 100% | 0,01s
    "baby8-7x7-5.txt", // 7 | 1000 | 100% | 0,012s
    "baby9-7x7-10.txt", // 8 | 1000 | 50,6% | 0,023s
    "anfaenger1-9x9-10.txt", // 9 | 1000 | 75,3% | 0,082s
    "anfaenger2-9x9-10.txt", // 10 | 1000 | 69% | 0,073s
    "anfaenger3-9x9-10.txt", // 11 | 1000 | 100% | 0,036s
    "anfaenger4-9x9-10.txt", // 12 | 1000 | 100% | 0,048s
    "anfaenger5-9x9-10.txt", // 13 | 1000 | 100% | 0,031s
    "fortgeschrittene1-16x16-40.txt", // 14 | 1000 | 100% | 0,281s
    "fortgeschrittene2-16x16-40.txt", // 15 | 1000 | 51,2% | 0,451s
    "fortgeschrittene3-16x16-40.txt", // 16 | 1000 | 67,8% | 0,332s
    "fortgeschrittene4-16x16-40.txt", // 17 | 1000 | 61,7% | 0,439s
    "fortgeschrittene5-16x16-40.txt", // 18 | 1000 | 16,3% | 0,169s
    "profi1-30x16-99.txt", // 19 | 100 | 6% | 1,876s
    "profi2-30x16-99.txt", // 20 | 100 | 74% | 1,178s
    "profi3-30x16-99.txt", // 21 | 100 | 66% | 1,769s
    "profi4-30x16-99.txt", // 22 | 100 | 14% | 2,451s
    "profi5-30x16-99.txt" // 23 | 100 | 100% | 1,548s
  };

  public static void main(String[] args) {

    // CHANGE VALUES HERE
    int iterations = 100;
    int field = 19;
    
    System.out.println("Solving field " + fields[field]);

    int success = 0;
    long duration = 0;
    for (int i = 0; i < iterations; i++) {
      long start = System.currentTimeMillis();

      MSField f = new MSField("fields/" + fields[field]);
      MSAgent agent = new SatMSAgent(f);

      // to see what happens in the first iteration
      if (i == 0) {
        agent.activateDisplay();
      } else {
        agent.deactivateDisplay();
      }

      System.out.println("\nIteration " + i + ": ");
      boolean solved = agent.solve();
      if (solved) {
        System.out.println("Success!");
        success++;
        duration += (System.currentTimeMillis() - start);
      }
      System.out.println("Duration: " + (System.currentTimeMillis() - start) + "ms"); 
    }
    
    System.out.println("\nStatistics for " + iterations + " iterations of field " + fields[field] + ":");
    
    double rate = 100 * (double) success / (double) iterations;
    double avgDuration = ((double) duration / (double) success) / 1000;
    DecimalFormat dfr = new DecimalFormat("#.#");
    DecimalFormat dfa = new DecimalFormat("#.###");
    
    System.out.println("\nSuccess rate: " + dfr.format(rate) + "%");
    if (rate - expectedRate[field] > 0) {
      System.out.println(
          "Difference to expected rate = +"
              + (dfr.format(rate - expectedRate[field]))
              + "% (Expected: "
              + dfr.format(expectedRate[field])
              + "%)");
    } else {
      System.out.println(
          "Difference to expected rate = "
              + (dfr.format(rate - expectedRate[field]))
              + "% (Expected: "
              + dfr.format(expectedRate[field])
              + "%)");
    }

    System.out.println("\nAverage duration (on success): " + dfa.format(avgDuration) + "s");
    System.out.println(
        "Difference to expected average duration = "
            + (dfa.format(avgDuration - expectedAvgDuration[field]))
            + "s (Expected: "
            + dfa.format(expectedAvgDuration[field])
            + "s)");
  }
}
