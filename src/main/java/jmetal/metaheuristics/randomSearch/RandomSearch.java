//  RandomSearch.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jmetal.metaheuristics.randomSearch;

import approaches.metaheuristics.randomsearch.RandomAlgorithm;
import jmetal.core.Algorithm;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;
import jmetal.util.NonDominatedSolutionList;

/**
 * This class implements a simple random search algorithm.
 */
public class RandomSearch extends Algorithm {
  private RandomAlgorithm randomAlgorithm;

  /**
  * Constructor
  * @param problem Problem to solve
  */
  public RandomSearch(Problem problem){
    super (problem) ;
  } // RandomSearch

  public void setRandomAlgorithm(RandomAlgorithm randomAlgorithm) {
    this.randomAlgorithm = randomAlgorithm;
  }

  /**
  * Runs the RandomSearch algorithm.
  * @return a <code>SolutionSet</code> that is a set of solutions
  * as a result of the algorithm execution
   * @throws JMException
  */
  public SolutionSet execute() throws JMException, ClassNotFoundException {
    int maxEvaluations ;
    int evaluations    ;

    maxEvaluations    = ((Integer)getInputParameter("maxEvaluations")).intValue();

    //Initialize the variables
    evaluations = 0;

    //NonDominatedSolutionList ndl = new NonDominatedSolutionList();
    SolutionSet ndl = new SolutionSet(maxEvaluations);

    // Create the initial solutionSet
    Solution newSolution;
    for (int i = 0; i < maxEvaluations; i++) {
      newSolution = new Solution(problem_);
      problem_.evaluate(newSolution);
      problem_.evaluateConstraints(newSolution);
      evaluations++;
      ndl.add(newSolution);

      this.randomAlgorithm.setChanged();
      this.randomAlgorithm.notifyObservers(evaluations);
    } //for

    return ndl;
  } // execute
} // RandomSearch
