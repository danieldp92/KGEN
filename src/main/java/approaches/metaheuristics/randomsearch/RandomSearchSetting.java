package approaches.metaheuristics.randomsearch;

import jmetal.core.Algorithm;
import jmetal.core.Problem;
import jmetal.experiments.Settings;
import jmetal.experiments.settings.RandomSearch_Settings;
import jmetal.metaheuristics.randomSearch.RandomSearch;
import jmetal.util.JMException;

public class RandomSearchSetting extends Settings {
    // Default experiments.settings
    public int maxEvaluations_;

    public RandomSearchSetting(Problem problem) {
        this.problem_ = problem;

        this.maxEvaluations_ = 5000;
    }

    @Override
    public Algorithm configure() throws JMException {
        Algorithm algorithm;

        // Creating the problem
        algorithm = new RandomSearch(problem_);

        // Algorithm parameters
        algorithm.setInputParameter("maxEvaluations", maxEvaluations_);

        return algorithm;
    }
}
