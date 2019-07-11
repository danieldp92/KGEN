package utils;

import java.util.ArrayList;
import java.util.List;

public class ArrayUtils {

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