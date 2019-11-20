package runner;

import runner.experimentation.bean.Result;
import runner.experimentation.exceptions.ArgumentException;
import runner.experimentation.util.ResultUtils;
import ui.cui.AnonymizationCLI;
import utils.ArrayUtils;
import utils.FileUtils;
import utils.ObjectUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static final boolean SHOW_LOG_MESSAGE = false;

    public static void main (String [] args) throws ArgumentException {
        AnonymizationCLI anonymizationCLI = new AnonymizationCLI();
        anonymizationCLI.run(args);
    }
}
