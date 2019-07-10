package main;

import anonymization.KAnonymity;
import dataset.beans.Attribute;
import dataset.beans.Dataset;
import dataset.beans.DatasetRow;
import approaches.geneticalgorithm.AnonymizationAlgorithm;
import approaches.geneticalgorithm.AnonymizationProblem;
import approaches.geneticalgorithm.AnonymizationSetting;
import dataset.database.DatasetMySQL;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;
import main.Experimentation.MultipleRun;
import results.CSVResultGenerator;
import utils.DatasetUtils;
import utils.FileUtils;
import utils.XlsUtils;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class Main {
    private static final int NUMBER_OF_EXPERIMENTATION = 20;

    public static void main (String [] args) throws IOException, JMException, ClassNotFoundException, SQLException {
        MultipleRun.execute(NUMBER_OF_EXPERIMENTATION);
    }
}
