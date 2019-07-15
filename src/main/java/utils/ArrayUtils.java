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
