package main;

import javafx.application.Application;
import javafx.stage.Stage;
import jmetal.util.JMException;
import main.experimentation.KGENRun;
import main.gui_experimentation.OLARunGui;

import java.io.*;
import java.sql.SQLException;

public class Main {
    private static final int NUMBER_OF_EXPERIMENTATION = 20;

    public static void main (String [] args) throws IOException, JMException, ClassNotFoundException, SQLException {
        //MultipleKGENRun.execute(NUMBER_OF_EXPERIMENTATION);
        //OLARun.execute();
        //ExhaustiveRun.execute();
        //KGENRun.execute();
        OLARunGui.execute(args);
    }
}
