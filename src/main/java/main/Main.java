package main;

import gui.LatticeGui;
import jmetal.util.JMException;

import java.io.*;
import java.sql.SQLException;

public class Main {
    private static final int OLA_ALGORITHM = 1;
    private static final int EXHAUSTIVE_ALGORITHM = 2;
    private static final int KGEN_ALGORITHM = 3;

    private static final int NUMBER_OF_EXPERIMENTATION = 20;

    public static void main (String [] args) throws IOException, JMException, ClassNotFoundException, SQLException, InterruptedException {
        int approach = OLA_ALGORITHM;
        //MultipleKGENExperimentation.execute(NUMBER_OF_EXPERIMENTATION);
        //OLAExperimentation.execute();
        //ExhaustiveExperimentation.execute();
        //KGENExperimentation.execute();
        //OLARunGui.execute(args);

        //Run GUI
        LatticeGui.run(args);
    }
}
