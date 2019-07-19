package main;

import gui.LatticeGui;
import jmetal.util.JMException;

import java.io.*;
import java.sql.SQLException;

public class Main {
    public static void main (String [] args) throws IOException, JMException, ClassNotFoundException, SQLException, InterruptedException {
        //Run GUI
        LatticeGui.run(args);
    }
}
