package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArrayUtils {

    public static boolean leq (List<Integer> list1, List<Integer> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }

        int i = 0;
        while (i < list1.size() && list1.get(i) <= list2.get(i)) {
            i++;
        }

        if (i < list1.size()) {
            return false;
        }

        return true;
    }

    public static boolean geq (List<Integer> list1, List<Integer> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }

        int i = 0;
        while (i < list1.size() && list1.get(i) >= list2.get(i)) {
            i++;
        }

        if (i < list1.size()) {
            return false;
        }

        return true;
    }

    public static ArrayList<Integer> min (ArrayList<Integer> gen1, ArrayList<Integer> gen2) {
        ArrayList<Integer> min = new ArrayList<>();

        for (int i = 0; i < gen1.size(); i++) {
            if (gen1.get(i) < gen2.get(i)) {
                min.add(gen1.get(i));
            } else {
                min.add(gen2.get(i));
            }
        }

        return min;
    }

    public static ArrayList<Integer> max (ArrayList<Integer> gen1, ArrayList<Integer> gen2) {
        ArrayList<Integer> max = new ArrayList<>();

        for (int i = 0; i < gen1.size(); i++) {
            if (gen1.get(i) > gen2.get(i)) {
                max.add(gen1.get(i));
            } else {
                max.add(gen2.get(i));
            }
        }

        return max;
    }

    private static List<List<Integer>> getAllCombinations (List<Integer> maxVector) {
        List<List<Integer>> allCombinations = new ArrayList<List<Integer>>();

        int actualPos = maxVector.size()-1;

        //Inizialize combination
        List<Integer> combination = new ArrayList<Integer>();
        for (int i = 0; i < maxVector.size(); i++) {
            combination.add(0);
        }

        allCombinations.add(new ArrayList<Integer>(combination));

        while (actualPos >= 0) {
            actualPos = maxVector.size()-1;
            while (actualPos >= 0 && combination.get(actualPos) == maxVector.get(actualPos)) {
                combination.set(actualPos, 0);
                actualPos--;
            }

            if (actualPos >= 0) {
                combination.set(actualPos, combination.get(actualPos)+1);
                allCombinations.add(new ArrayList<Integer>(combination));
            }
        }

        return allCombinations;
    }

}
