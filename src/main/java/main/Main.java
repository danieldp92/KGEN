package main;

import gui.LatticeGui;
import jmetal.util.JMException;
import main.experimentation.bean.Result;
import utils.CsvUtils;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main (String [] args) throws IOException, JMException, ClassNotFoundException, SQLException, InterruptedException {
        //Run GUI
        LatticeGui.run(args);
    }
}
