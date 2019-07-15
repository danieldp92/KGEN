package main;

import jmetal.util.JMException;
import main.Experimentation.ExhaustiveRun;
import main.Experimentation.KGENRun;
import main.Experimentation.MultipleKGENRun;
import main.Experimentation.OLARun;

import java.io.*;
import java.sql.SQLException;

public class Main {
    private static final int NUMBER_OF_EXPERIMENTATION = 20;

    public static void main (String [] args) throws IOException, JMException, ClassNotFoundException, SQLException {
        //MultipleKGENRun.execute(NUMBER_OF_EXPERIMENTATION);
        //OLARun.execute();
        //ExhaustiveRun.execute();
        KGENRun.execute();
    }
}
