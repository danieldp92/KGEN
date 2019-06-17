package approaches.geneticalgorithm.encoding;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.IntSolutionType;

public class GeneralizationSolution extends Solution {
    private int penalty;

    public GeneralizationSolution() {
        this.penalty = 0;
    }

    public GeneralizationSolution(Solution solution) {
        super(solution);
        this.penalty = 0;
    }

    public GeneralizationSolution(Problem problem) throws ClassNotFoundException {
        super(problem);
        this.penalty = 0;
    }

    public int getPenalty() {
        return penalty;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }

    public void increasePenalty() {
        this.penalty = this.penalty + 1;
    }
}
