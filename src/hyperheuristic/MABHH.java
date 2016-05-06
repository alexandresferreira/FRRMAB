package hyperheuristic;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;
import AbstractClasses.ProblemDomain.HeuristicType;
import analysis.PerformanceElements;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;
import moveAcceptance.AcceptanceCriterion;
import moveAcceptance.Adaptative;
import moveAcceptance.AllMoves;
import moveAcceptance.BetterEqual;
import moveAcceptance.ExponentialMonteCarlo;
import moveAcceptance.GreatDeluge;
import moveAcceptance.NaiveAcceptance;
import moveAcceptance.OnlyBetter;
import moveAcceptance.RecordToRecord;
import moveAcceptance.SimulatedAnnealing;
import selection.FRRMAB;
import selection.SelectionMethod;
import selection.MabSelection;
import selection.FRAMAB;
import selection.SLMAB;
import util.Vars;

public class MABHH extends HyperHeuristic {

    private int numberOfHeuristics;
    private long numberOfIterations;
    private int acceptanceType;
    private long lastRestartTimePoint;
    private long totalExecTime;
    private int lastCalledHeuristic;
    private int pastHeuristic;
    private double currentFitness;
    private double newFitness;
    private long lastIterationBest;
    private double bestFitness;
    private double scalingFactor;
    private int numberOfPhasesPassed;
    private int phaseIterCounter = 1;
    private boolean firstPhaseCheck = true;
    private AcceptanceCriterion acceptance;
    private SelectionMethod selection;
    private PerformanceElements currPerfomance;
    private PerformanceElements prevPerfomance;
    private long startTime;
    private long currentTime;
    private int totalNumOfNewBestFound;
    private int[] local_search_heuristics;
    private int[] mutation_heuristics;
    private double[] timesUsed;
    private long[] totalTime;
    private int[] crossover_heuristics;
    private int[] ruin_recreate_heuristics;
    private int[] auxC;
    private HeuristicType[] heuristicTypeList;
    private HeuristicClassType[] heuristicClassTypeList;
    public double[] learningRateMultiplierList;
    private int numberOfRestarts;
    private int numberOfRestartsWithoutNewBest;
    private long shortestRestartTime;

    public MABHH(long seed, int numberOfHeuristics, long totalExecTime, int acc) {
        super(seed);
        this.numberOfHeuristics = numberOfHeuristics;
        this.numberOfIterations = 0;
        this.totalExecTime = totalExecTime;
        Vars.totalExecutionTime = totalExecTime;
        this.lastIterationBest = -1;
        this.auxC = new int[numberOfHeuristics];
        this.currPerfomance = new PerformanceElements(numberOfHeuristics);
        this.prevPerfomance = new PerformanceElements(numberOfHeuristics);
        heuristicTypeList = new HeuristicType[numberOfHeuristics];
        timesUsed = new double[numberOfHeuristics];
        totalTime = new long[numberOfHeuristics];
        heuristicClassTypeList = new HeuristicClassType[numberOfHeuristics];
        learningRateMultiplierList = new double[numberOfHeuristics];
        numberOfRestarts = 0;
        numberOfRestartsWithoutNewBest = 0;
        shortestRestartTime = 0;
        this.acceptanceType = acc;

        for (int i = 0; i < numberOfHeuristics; i++) {
            learningRateMultiplierList[i] = 1.0;
        }
        initializeHH();
    }

    private void initializeHH() {
        initializeHeuristicSelection();
        initializeMoveAcceptance();
    }

    private void initializeHeuristicSelection() {
        switch(Vars.mabType){
            case 1:
                System.out.println("Selection: FRAMAB");
                selection = new FRAMAB(numberOfHeuristics, this.scalingFactor, rng);
                break;
            case 2:
                System.out.println("Selection: FRRMAB");
                selection = new FRRMAB(numberOfHeuristics, this.scalingFactor, rng);
                break;
            default:
                System.out.println("Selection: SLMAB");
                selection = new SLMAB(numberOfHeuristics, this.scalingFactor, rng);
        }

    }

    private void initializeMoveAcceptance() {
        switch (this.acceptanceType) {
            case 1:
                acceptance = new AllMoves(rng);
                System.out.println("Acceptance: All Moves");
                break;
            case 2:
                acceptance = new BetterEqual(rng);
                System.out.println("Acceptance: Better Equal");
                break;
            case 3:
                acceptance = new OnlyBetter(rng);
                System.out.println("Acceptance: Only Better");
                break;
            case 4:
                acceptance = new SimulatedAnnealing(rng);
                System.out.println("Acceptance: Simulated Annealing");
                break;
            case 5:
                acceptance = new ExponentialMonteCarlo(rng);
                System.out.println("Acceptance: Exponetional Monte Carlo");
                break;
            case 6:
                acceptance = new GreatDeluge(rng);
                System.out.println("Acceptance: Great Deluge");
                break;
            case 7:
                acceptance = new RecordToRecord(rng);
                System.out.println("Acceptance: Record to Record");
                break;
            case 8:
                acceptance = new Adaptative(rng);
                System.out.println("Acceptance: Adaptative");
                break;
            default:
                System.out.println("Acceptance: Naive Acceptance");
                acceptance = new NaiveAcceptance(rng);
        }
    }

    @Override
    protected void solve(ProblemDomain problem) {
        DecimalFormat fmt = new DecimalFormat("0.00");
        /*Set the problem */
        setHeuristicTypes(problem);
        /*Define the number of solutions on memory to 12 (default 2)*/
        problem.setMemorySize(12);
        long endTime;
        int auxNumberIt = 0;
        /*Start and end time of a low-level heuristic's application*/
        long startHeur, endHeur;
        /*Initialize Solution*/
        problem.initialiseSolution(0);
        /*Save the initial solution in the 10th position of the memory*/
        problem.copySolution(0, 10);
        System.out.println("Initial Solution: " + problem.getFunctionValue(0));
        /*Initialize the best, curr and new fitness as the initial solution*/
        bestFitness = currentFitness = newFitness = problem.getFunctionValue(0);
        acceptance.resetAcceptanceList(bestFitness);
        acceptance.setInitialLevel(problem.getFunctionValue(0));
        /*
         * Take a number of copies of the initial solution for using
         * the crossover operators (or any operator requiring two solutions)
         * */
        if (crossover_heuristics != null) {
            for (int mInx = 5; mInx < 10; mInx++) {
                problem.copySolution(0, mInx); //for crossovers
            }
        }
        /* Set the starting execution time in ms */
        lastRestartTimePoint = startTime;
        startTime = System.currentTimeMillis();
        /* Main loop */
        while (!hasTimeExpired()) {
            /*Select the low-level heuristic to be applied*/
            lastCalledHeuristic = selection.selectHeuristic();
            /*Set the parameters of HyFlex with the values of the selected heuristic*/
            problem.setIntensityOfMutation(selection.getLevelOfChangeList()[lastCalledHeuristic]);
            problem.setDepthOfSearch(selection.getLevelOfChangeList()[lastCalledHeuristic]);

            /*Applie the selected low-level heuristics, if it is a crossover the other solution is chosen from the memory*/
            startHeur = System.nanoTime();
            if (isCrossover(lastCalledHeuristic)) {
                int slnInxForCrossover = rng.nextInt(5) + 5;
                newFitness = problem.applyHeuristic(lastCalledHeuristic, 0, slnInxForCrossover, 1);
            } else {
                newFitness = problem.applyHeuristic(lastCalledHeuristic, 0, 1);
            }
            endHeur = System.nanoTime();

            /*Update the performance elements of the heuristics with the result of the application*/
            currPerfomance.updatePerformanceElements(lastCalledHeuristic, currentFitness, newFitness, bestFitness,
                    startHeur, endHeur);

            /*Calculate the delta value between the current and new solution*/
            double delta = 0;
            if (currentFitness == 0 && newFitness < 0) {
                delta = 0.5;
            }
            if (currentFitness < 0 && newFitness == 0) {
                delta = 0;
            }
            if (currentFitness < newFitness) {
                delta = 0;
            } else if (currentFitness > newFitness) {
                delta = Math.max(0, ((currentFitness - newFitness) / -currentFitness));
            }

            /*Update the rewards values using the methodology of the selection strategy based mainly in the delta value*/
            selection.updateHeuristicValue(delta, lastCalledHeuristic, (int) numberOfHeuristics);
            selection.updateSelectionElements(lastCalledHeuristic, heuristicClassTypeList[lastCalledHeuristic],
                    currentFitness, newFitness,
                    bestFitness, startHeur, endHeur,
                    learningRateMultiplierList, currPerfomance);
            auxC[lastCalledHeuristic]++;

            /*Accept the generated solution based on the mechanismn defined*/
            if (acceptance.accept(newFitness, currentFitness, bestFitness)) {
                problem.copySolution(1, 0);
                if (newFitness < bestFitness) {
                    totalNumOfNewBestFound++;
                    /* Update the best fitness value */
                    bestFitness = newFitness;
                    lastIterationBest = numberOfIterations;
                    /* Change randomly one of the solution used for crossovers (or any heuristic requiring two solutions) */
                    int randMemIndex = rng.nextInt(5) + 5;
                    problem.copySolution(1, randMemIndex);
                    /* Copy the new best solution to the solution memory */
                    problem.copySolution(0, 10);

                }
                currentFitness = newFitness;
            }

            /*Verify if the phase is over*/
            if (phaseIterCounter == Vars.phaseLength) {
                /* Check the performance changes of the heuristics and update the related elements */
                performanceCheckForDHS();
                /* Reset the phase iteration counter */
                phaseIterCounter = 0;
                /* Increment the number phases passed counter */
                numberOfPhasesPassed++;
            }
            /*Used for some acceptances*/
            if (numberOfIterations == 15) {
                endTime = System.currentTimeMillis() - startTime;
                //Vars.iterMax = Vars.totalExecutionTime / endTime;
                System.out.println("IterMAx: " + Vars.iterMax);
                acceptance.setBeta();
                Vars.reduceTemperature = true;
                acceptance.updateCooling();
            }
            /* Increment the number of iterations*/
            numberOfIterations++;
            auxNumberIt++;
            /* Increment the phase iteration counter */
            phaseIterCounter++;
            /* Increment the number of uses and time of the heuristic*/
            timesUsed[lastCalledHeuristic]++;
            totalTime[lastCalledHeuristic] += (endHeur - startHeur);
            pastHeuristic = lastCalledHeuristic;
            currentTime = System.currentTimeMillis() - startTime;

            /*First breakpoint print. To disable this set breakpoint1 or 2 variable to false in util/Vars.java*/
            if (currentTime >= Math.round(Vars.totalExecutionTime/3.0) && Vars.breakpoint1) {
                for (int i = 0; i < numberOfHeuristics; i++) {
                    System.out.println("Heuristic-bp1: " + i + " " + auxC[i] + " " + fmt.format(auxC[i] * 100.0 / auxNumberIt) + "%" );
                    auxC[i] = 0;
                }
                auxNumberIt = 0;
                Vars.breakpoint1 = false;
                selection.printWindow();
                System.out.println("Number of iterations without improvement: " + acceptance.numberOfIterationsStuck);
                System.out.println("");
            } //break point 2
            else if (currentTime >= Math.round(Vars.totalExecutionTime/2.0) && Vars.breakpoint2) {
                for (int i = 0; i < numberOfHeuristics; i++) {
                    System.out.println("Heuristic-bp2: " + i + " " + auxC[i] + " " + fmt.format(auxC[i] * 100.0 / auxNumberIt) + "%");
                    auxC[i] = 0;
                }
                selection.printWindow();
                System.out.println("");
                System.out.println("Number of iterations without improvement: " + acceptance.numberOfIterationsStuck);
                auxNumberIt = 0;
                Vars.breakpoint2 = false;
            }
        }

        System.out.println("Number of Iterations: " + numberOfIterations);
        System.out.println("Last iteration best value: " + lastIterationBest);

        /*Print for each low-level heuristic - id, numberofTimesused, time, number of worst, equal, improv and best movements generated*/
        for (int i = 0; i < numberOfHeuristics; i++) {
            System.out.println("Heuristic: " + i + "\t" + timesUsed[i] + "\t" + fmt.format(timesUsed[i] * 100.0 / numberOfIterations) + "%\tTempo "
                    + TimeUnit.NANOSECONDS.toMillis(totalTime[i]) + "ms\tworst: " + currPerfomance.getNumberOfWorseningMoves()[i] + " " +
                    fmt.format((currPerfomance.getNumberOfWorseningMoves()[i]*100)/timesUsed[i])        + "%\tequal: " + currPerfomance.getNumberOfEqualMoves()[i] +
                    " " + fmt.format((currPerfomance.getNumberOfEqualMoves()[i]*100)/timesUsed[i]) +
                    "%\timprov: " + currPerfomance.getNumberOfImprovingMoves()[i] +" "+ fmt.format((currPerfomance.getNumberOfImprovingMoves()[i]*100)/timesUsed[i])
                    +"%\tbest: " + currPerfomance.getNumberOfImprovingBestMoves()[i] + " " +
                    fmt.format((currPerfomance.getNumberOfImprovingBestMoves()[i]*100)/timesUsed[i]) + "%");
        }
    }

    private void performanceCheckForDHS() {
        updatePerformanceMetricForSelection();
    }

    private void setHeuristicClassType(PerformanceElements performance, int heuristicIndex) {
        //System.out.println("mudou!");
        if (performance.getTotalHeurImprovement()[heuristicIndex] > 0) {
            if (performance.getTotalHeurWorsening()[heuristicIndex] == 0) {
                heuristicClassTypeList[heuristicIndex] = HeuristicClassType.OnlyImproving;
            } else if (performance.getTotalHeurImprovement()[heuristicIndex] >= performance.getTotalHeurWorsening()[heuristicIndex]) {
                heuristicClassTypeList[heuristicIndex] = HeuristicClassType.ImprovingMoreOrEqual;
            } else if (performance.getTotalHeurImprovement()[heuristicIndex] < performance.getTotalHeurWorsening()[heuristicIndex]) {
                heuristicClassTypeList[heuristicIndex] = HeuristicClassType.WorseningMore;
            }
        } else {
            if (performance.getTotalHeurWorsening()[heuristicIndex] != 0) {
                heuristicClassTypeList[heuristicIndex] = HeuristicClassType.OnlyWorsening;
            } else {
                heuristicClassTypeList[heuristicIndex] = HeuristicClassType.OnlyEqual;
            }
        }
    }

    private void updatePerformanceMetricForSelection() {

        double tempTotalExecTimeForFirstPhase = 0.0;

        double[] performanceRate = new double[numberOfHeuristics];
        double[] worseningRate = new double[numberOfHeuristics];

        double tempExecTime;
        double tempSpentExecTime;
        String execTimeStr = "";

        double[] timeExecList = new double[numberOfHeuristics];

        /**
         * This is required to ignore the first part (about the new best
         * solutions) of the performance metric *
         */
        int tempNumOfNewBestSlnFoundInTheLastPhase = 0;
        for (int i = 0; i < numberOfHeuristics; i++) {
            if (currPerfomance.getNumberOfImprovingBestMoves()[i] != prevPerfomance.getNumberOfImprovingBestMoves()[i]) {
                tempNumOfNewBestSlnFoundInTheLastPhase += (currPerfomance.getNumberOfImprovingBestMoves()[i] - prevPerfomance.getNumberOfImprovingBestMoves()[i]);
            }
        }
        double timeRemaining = (totalExecTime - (System.currentTimeMillis() - this.startTime)) / 1000F;
        if (timeRemaining < 0) {
            timeRemaining = 0;
        }

        for (int i = 0; i < numberOfHeuristics; i++) {
            tempExecTime = (currPerfomance.getSpentExecutionTime()[i] - prevPerfomance.getSpentExecutionTime()[i]);
            tempSpentExecTime = currPerfomance.getSpentExecutionTime()[i];

            if (firstPhaseCheck) {
                tempTotalExecTimeForFirstPhase += tempExecTime;
            }

            if (tempExecTime != 0.0) {
                double nano = 1000000000.0;
                double tempTime = nano * Vars.execTimeSensitivity;
                /////////////////// For 1 Second - Sensitivity///////////////
                if (tempExecTime <= tempTime) {
                    tempExecTime = Vars.execTimeSensitivity;
                } else {
                    tempExecTime /= nano;
                }

                if (tempSpentExecTime <= tempTime) {
                    tempSpentExecTime = Vars.execTimeSensitivity;
                } else {
                    tempSpentExecTime /= nano;
                }

                performanceRate[i] = 0;

                /**
                 * This is required to ignore the first part (about the new best
                 * solutions) of the performance metric *
                 */
                if (tempNumOfNewBestSlnFoundInTheLastPhase > 0) {
                    performanceRate[i] = (Math.pow(1.0 + (currPerfomance.getNumberOfImprovingBestMoves()[i] - prevPerfomance.getNumberOfImprovingBestMoves()[i]), Vars.powerOfForNewBestSolutionsPerfM0) * (timeRemaining / tempExecTime) * 100000000);
                }

                performanceRate[i] += (((currPerfomance.getTotalHeurImprovement()[i] - prevPerfomance.getTotalHeurImprovement()[i]) / tempExecTime) * 100000)
                        - (((currPerfomance.getTotalHeurWorsening()[i] - prevPerfomance.getTotalHeurWorsening()[i]) / tempExecTime) * 0.0001)
                        + ((currPerfomance.getTotalHeurImprovement()[i] / tempSpentExecTime) * 0.000001)
                        - ((currPerfomance.getTotalHeurWorsening()[i] / tempSpentExecTime) * 0.000000001);

                worseningRate[i] = (((currPerfomance.getTotalHeurWorsening()[i] - prevPerfomance.getTotalHeurWorsening()[i]) / tempExecTime));

                timeExecList[i] = tempExecTime / (currPerfomance.getNumberOfMoves()[i] - prevPerfomance.getNumberOfMoves()[i]);
            } else {
                if (tempSpentExecTime != 0) {
                    worseningRate[i] += ((currPerfomance.getTotalHeurWorsening()[i] / tempSpentExecTime) * 0.000001);
                } else {
                    worseningRate[i] = 0.0;
                }

                performanceRate[i] = 0.0;
                timeExecList[i] = 0.0;
            }

            execTimeStr += "[" + i + "]=" + tempExecTime + ", ";

            setHeuristicClassType(currPerfomance, i);
        }

        firstPhaseCheck = false;

        // selection.update(performanceRate, worseningRate, timeExecList, currPerformance, prevPerformance, learningRateMultiplierList); //new method @12052011
        prevPerfomance.getCopyOf(currPerfomance);
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "MABHH";
    }

    private void setHeuristicTypes(ProblemDomain problem) {
        local_search_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH);
        mutation_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION);
        ruin_recreate_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.RUIN_RECREATE);
        crossover_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.CROSSOVER);

        for (int i = 0; i < local_search_heuristics.length; i++) {
            heuristicTypeList[local_search_heuristics[i]] = HeuristicType.LOCAL_SEARCH;
            heuristicClassTypeList[local_search_heuristics[i]] = HeuristicClassType.ImprovingMoreOrEqual;
        }
        for (int i = 0; i < mutation_heuristics.length; i++) {
            heuristicTypeList[mutation_heuristics[i]] = HeuristicType.MUTATION;
            heuristicClassTypeList[mutation_heuristics[i]] = HeuristicClassType.ImprovingMoreOrEqual;
        }
        for (int i = 0; i < ruin_recreate_heuristics.length; i++) {
            heuristicTypeList[ruin_recreate_heuristics[i]] = HeuristicType.RUIN_RECREATE;
            heuristicClassTypeList[ruin_recreate_heuristics[i]] = HeuristicClassType.ImprovingMoreOrEqual;
        }
        for (int i = 0; i < crossover_heuristics.length; i++) {
            heuristicTypeList[crossover_heuristics[i]] = HeuristicType.CROSSOVER;
            heuristicClassTypeList[crossover_heuristics[i]] = HeuristicClassType.ImprovingMoreOrEqual;
        }
    }

    private boolean isCrossover(int heurIndex) {
        boolean isCrossover = false;
        for (int cr = 0; cr < crossover_heuristics.length; cr++) {
            if (crossover_heuristics[cr] == heurIndex) {
                isCrossover = true;
                break;
            }
        }
        return isCrossover;
    }

}
