package runner;

import runner.experimentation.ArgumentException;
import ui.cui.AnonymizationCLI;
import utils.FileUtils;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static final boolean SHOW_LOG_MESSAGE = true;
    public static final boolean EXACT_METAHEURISTIC_VERIFICATION = false;

    public static void main (String [] args) throws IOException, ArgumentException {
        AnonymizationCLI anonymizationCLI = new AnonymizationCLI();
        anonymizationCLI.run(args);

        //Run GUI
        //LatticeGui.run(args);
    }
}
