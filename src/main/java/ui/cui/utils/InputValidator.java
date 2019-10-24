package ui.cui.utils;

import runner.experimentation.type.AlgorithmType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class InputValidator {

    // Menu section

    public static int getChoice(int minValue, int maxValue, String exitCondition) {
        List<Integer> choices = new ArrayList<>();
        for (int i = minValue; i <= maxValue; i++)
            choices.add(i);

        return getChoice(choices, exitCondition);
    }

    public static int getChoice(List<Integer> choiceList, String exitCondition) {
        return getMultipleChoices(choiceList, exitCondition, null).get(0);
    }

    public static List<Integer> getMultipleChoices (int minValue, int maxValue, String exitCondition, String multiChoiceSeparatorTag) {
        List<Integer> possibleChoices = new ArrayList<>();
        for (int i = minValue; i <= maxValue; i++) {
            possibleChoices.add(i);
        }

        return getMultipleChoices(possibleChoices, exitCondition, multiChoiceSeparatorTag);
    }

    public static List<Integer> getMultipleChoices (List<Integer> choiceList, String exitCondition, String multiChoiceSeparatorTag) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        List<Integer> choices = new ArrayList<>();
        String input = null;
        int choice = -1;

        boolean invalidInput;

        do {
            invalidInput = false;
            System.out.print("Choice (" + exitCondition + " for quit): ");

            try {
                input = br.readLine();
                if (input.toLowerCase().trim().equals(exitCondition.toLowerCase())) {
                    System.exit(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }

            String [] split = null;
            if (multiChoiceSeparatorTag != null) {
                split = input.split(multiChoiceSeparatorTag);
            } else {
                split = new String[1];
                split[0] = input;
            }

            for (String s : split) {
                try {
                    choice = Integer.parseInt(s);

                    if (choiceList.contains(choice)) {
                        choices.add(choice);
                    } else {
                        System.out.println("\nYou must insert a numeric value between all values in the menu\n");
                        invalidInput = true;
                        break;
                    }

                } catch (NumberFormatException ex) {
                    System.out.println("\nYou must insert a numeric value\n");
                    invalidInput = true;
                }
            }
        } while (invalidInput);

        return choices;
    }

    // Input section

    public Integer getInteger(String message) {
        Integer inputInt = -1;
        String input = null;

        boolean invalidInput = false;
        do {
            invalidInput = false;
            input = getString(message);

            try {
                inputInt = Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                System.out.println("The value must be an Integer value");
                invalidInput = true;
            }
        } while (invalidInput);

        return inputInt;
    }

    public Double getDouble(String message) {
        Double inputDb = -1.0;
        String input = null;

        boolean invalidInput = false;
        do {
            invalidInput = false;
            input = getString(message);

            try {
                inputDb = Double.parseDouble(input);
            } catch (NumberFormatException ex) {
                System.out.println("The value must be a Double value");
                invalidInput = true;
            }
        } while (invalidInput);

        return inputDb;
    }

    public String getString(String message) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String input = null;

        System.out.print(message + ": ");
        try {
            input = br.readLine();
        } catch (IOException e) {
            System.exit(0);
        }

        return input;
    }
}
