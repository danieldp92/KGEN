package results;

import dataset.beans.Attribute;
import dataset.beans.DatasetRow;
import dataset.type.QuasiIdentifier;
import jmetal.core.SolutionSet;
import jmetal.core.Variable;
import jmetal.util.JMException;

import java.util.ArrayList;

public class CSVResultGenerator {

    public static ArrayList<String> csvResultGenerator (SolutionSet bestSolutions, DatasetRow header) throws JMException {
        ArrayList<String> csvTextFile = new ArrayList<String>();

        //Header
        String actualString = "";
        actualString += "Index;";
        for (Object attributeObj : header) {
            Attribute attribute = (Attribute) attributeObj;
            if (attribute.getType() instanceof QuasiIdentifier) {
                actualString += attribute.getName() + ";";
            }
        }

        for (int i = 0; i < bestSolutions.get(0).getNumberOfObjectives(); i++) {
            actualString += "Objective " + (i+1) + ";";
        }

        csvTextFile.add(actualString);


        //Data
        for (int i = 0; i < bestSolutions.size(); i++) {
            //Index
            actualString = "";
            actualString += (i+1) + ";";

            //Level of generalization
            for (Variable var : bestSolutions.get(i).getDecisionVariables()) {
                actualString += (int)var.getValue() + ";";
            }

            //Objectives
            for (int j = 0; j < bestSolutions.get(i).getNumberOfObjectives(); j++) {
                if (j == 0) {
                    actualString += (1 - bestSolutions.get(i).getObjective(j)) + ";";
                } else {
                    actualString += bestSolutions.get(i).getObjective(j) + ";";
                }

            }

            csvTextFile.add(actualString);
        }

        return csvTextFile;
    }
}
