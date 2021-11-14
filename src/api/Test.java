package api;

import java.util.Arrays;
import java.util.HashSet;

public class Test {
  public static void main(String[] args) {
    HashSet<int[]> set = new HashSet<int[]>();
    int[] arr1 = {0, 1};
    int[] arr2 = {1, 0};
    int[] arr3 = {0, 1};
    set.add(arr1);
    System.out.println("first" ); 
    for (int[] arr : set) {
      System.out.println(Arrays.toString(arr));
    }
    set.add(arr2);
    System.out.println("second" ); 
    for (int[] arr : set) {
      System.out.println(Arrays.toString(arr));
    }
    set.add(arr3);
    System.out.println("third" ); 
    for (int[] arr : set) {
      System.out.println(Arrays.toString(arr));
    }
    
  }
}
