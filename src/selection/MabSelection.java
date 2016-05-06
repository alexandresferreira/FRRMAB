/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package selection;

import analysis.PerformanceElements;
import creditAssingment.CreditAssignment;
import hyperheuristic.HeuristicClassType;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import util.Vars;

/**
 *
 * @author asferreira
 */
public abstract class MabSelection extends SelectionMethod {

    public enum LevelOfChangeType {

        NewBest,
        Improving,
        Equal,
        Worsening,
    }

    private double[] levelOfChangeList;
    private final static double incrementLOCNewBest = 0.01;
    private final static double incrementLOCImproving = 0.001;
    private final static double decrementLOCWorsening = 0.0005;
    private final static double decrementLOCEqual = 0.0001;
    private final static double initialLOCValue = 0.2;
    private final static double LOCLowerBound = 0.2;
    private final static double LOCUpperBound = 1.0;

    private int numberOfIterationsAtStuck;
    private boolean directionChangeForLowLOC = false;
    private boolean directionChangeForHighLOC = false;

    private double W;
    private LinkedList<Operator> slidingWindow;
    private CreditAssignment creditAssignment;
    private double scalingFactor;
    private double nHeutrials[];
    private double qH[];
    private double reward;
    private int countH[];
    private double last[];
    private boolean hSelected[];
    private double calc[];
    private Random r;

    @Override
    public abstract void updateHeuristicValue(double delta, int heuristic, int k);

    public abstract void addToSlidingWindow(Operator op);

    public MabSelection(int numberOfHeuristics, double scalingFactor, Random r) {
        super(numberOfHeuristics, r);
        nHeutrials = new double[numberOfHeuristics];
        qH = new double[numberOfHeuristics];
        countH = new int[numberOfHeuristics];
        //this.scalingFactor = scalingFactor;
        last = new double[numberOfHeuristics];
        calc = new double[numberOfHeuristics];
        hSelected = new boolean[numberOfHeuristics];
        creditAssignment = new CreditAssignment();
        slidingWindow = new LinkedList();
        this.r = r;
        this.W = Vars.windowSize;
        for (int i = 0; i < numberOfHeuristics; i++) {
            last[i] = 0;
            hSelected[i] = false;
            //qH[i] = 0.0;
        }

        levelOfChangeList = new double[numberOfHeuristics];
        for (int i = 0; i < numberOfHeuristics; i++) {
            levelOfChangeList[i] = initialLOCValue;
        }
    }

    @Override
    public int selectHeuristic() {
        int heurIndex;
        //System.out.println("Scaling Factor: " + Vars.scalingFactor);
        if (thereIsUnusedOperator(hSelected)) {
            heurIndex = getUnusedOperator(hSelected);
            hSelected[heurIndex] = true;
            //nHeutrials[heurIndex] = 1.0;
        } else {
            for (int j = 0; j < numberOfHeuristics; j++) {
                //calc[j] = 0;
                if(nHeutrials[j] <= 0){
                    nHeutrials[j] = 1;
                }
                calc[j] = qH[j] + (Vars.scalingFactor * Math.sqrt(2 * Math.log(sumOfTrials(nHeutrials)) / nHeutrials[j]));
                //System.out.println("Operador " + j + " :" + qH[j] + " " + (Vars.scalingFactor * Math.sqrt(2 * Math.log10(sumOfTrials(nHeutrials) / nHeutrials[j]))));
            }
            heurIndex = argMax(calc);
            //System.out.println("Selecionou: " + op);
        }
        return heurIndex;
    }

    @Override
    public void updateSelectionElements(int heuristicIndex, HeuristicClassType heuristicClassType,
            double fitnessBefore, double fitnessAfter, double bestFitness,
            long heursiticStartTime, long heursiticEndTime, double[] learningMultRateList, PerformanceElements currPerformance) {

        if (fitnessAfter > fitnessBefore) { /* If the fitness value of the new solution is worse than the earlier solution */

            updateLevelOfChange(LevelOfChangeType.Worsening, heuristicClassType, heuristicIndex);
            //System.out.println("\nWorsening...");
        } else if (fitnessAfter < fitnessBefore) { /* If the fitness value of the new solution is better than the earlier solution */

            if (fitnessAfter < bestFitness) { /* If the fitness value of the new solution is better than the current best solution */

                //  System.out.println("\nnewBest...");

                updateLevelOfChange(LevelOfChangeType.NewBest, heuristicClassType, heuristicIndex);
            } else { /* If the fitness value of the new solution is not better than the current best solution */

                // System.out.println("\nImproving...");

                updateLevelOfChange(LevelOfChangeType.Improving, heuristicClassType, heuristicIndex);
            }
        } else { /* If the fitness value of the new solution is equal to the earlier solution's fitness */

            //System.out.println("\nEqual...");

            updateLevelOfChange(LevelOfChangeType.Equal, heuristicClassType, heuristicIndex);
        }
    }

    @Override
    public void updateLevelOfChange(LevelOfChangeType lcType, HeuristicClassType hClassType, int heurIndex) {
        double learningRateLevel = 1.0;
        if (Vars.oscilateLOCValues && Vars.isAtStuck) {

            if (hClassType == HeuristicClassType.OnlyImproving) { // >= 0.5
                if (!directionChangeForHighLOC) {
                    levelOfChangeList[heurIndex] = 0.5 + ((LOCUpperBound - 0.5) * (numberOfIterationsAtStuck / 5000.0));

                    if (levelOfChangeList[heurIndex] == LOCUpperBound) {
                        directionChangeForHighLOC = true;
                    }
                } else {
                    levelOfChangeList[heurIndex] = LOCUpperBound - ((LOCUpperBound - 0.5) * (numberOfIterationsAtStuck / 5000.0));

                    if (levelOfChangeList[heurIndex] == 0.5) {
                        directionChangeForHighLOC = false;
                    }
                }
            } else { // if(levelOfChangeList[heurIndex] < 0.5){
                if (!directionChangeForLowLOC) {
                    levelOfChangeList[heurIndex] = LOCLowerBound + ((0.499999999 - LOCLowerBound) * (numberOfIterationsAtStuck / 5000.0));

                    if (levelOfChangeList[heurIndex] == 0.499999999) {
                        directionChangeForLowLOC = true;
                    }
                } else {
                    levelOfChangeList[heurIndex] = 0.5 - ((0.5 - LOCLowerBound) * (numberOfIterationsAtStuck / 5000.0));

                    if (levelOfChangeList[heurIndex] == LOCLowerBound) {
                        directionChangeForLowLOC = false;
                    }
                }
            }

            numberOfIterationsAtStuck++;

            if (numberOfIterationsAtStuck == 5000) {
                numberOfIterationsAtStuck = 1;
            }

        } else {
            double plusMinusZero = 1.0;

            if (lcType == LevelOfChangeType.NewBest) {

                numberOfIterationsAtStuck = 0;

                if (hClassType == HeuristicClassType.OnlyImproving) {
                    double tempProb = r.nextDouble();
                    if (tempProb <= 0.5) {
                        plusMinusZero = 1.0; //increase
                    } else {
                        plusMinusZero = 0.0; //no change
                    }
                } else if (hClassType == HeuristicClassType.ImprovingMoreOrEqual) {
                    double tempProb = r.nextDouble();
                    if (tempProb <= 0.5) {
                        plusMinusZero = 1.0;   //increase
                    } else if (tempProb <= 0.75) {
                        plusMinusZero = -1.0;  //decrease
                    } else {
                        plusMinusZero = 0.0;   //no change
                    }
                } else if (hClassType == HeuristicClassType.WorseningMore) {
                    double tempProb = r.nextDouble();
                    if (tempProb <= 0.5) {
                        plusMinusZero = 1.0;
                    } else {
                        plusMinusZero = -1.0;
                    }
                } //@16052011 gecici kapattim
                //System.out.println("Entou1");
                levelOfChangeList[heurIndex] += incrementLOCNewBest * learningRateLevel * plusMinusZero;

            } else if (lcType == LevelOfChangeType.Improving) {
                if (hClassType == HeuristicClassType.OnlyImproving) {
                    double tempProb = r.nextDouble();
                    if (tempProb <= 0.5) {
                        plusMinusZero = 1.0; //increase
                    } else {
                        plusMinusZero = 0.0; //no change
                    }
                } else if (hClassType == HeuristicClassType.ImprovingMoreOrEqual) {
                    double tempProb = r.nextDouble();
                    if (tempProb <= 0.5) {
                        plusMinusZero = 1.0;  //increase
                    } else if (tempProb <= 0.75) {
                        plusMinusZero = -1.0; //decrease
                    } else {
                        plusMinusZero = 0.0; //no change
                    }
                } else if (hClassType == HeuristicClassType.WorseningMore) {
                    double tempProb = r.nextDouble();
                    if (tempProb <= 0.5) {
                        plusMinusZero = 1.0;
                    } else {
                        plusMinusZero = -1.0;
                    }
                }
                //System.out.println("Entou2");
                levelOfChangeList[heurIndex] += incrementLOCImproving * learningRateLevel * plusMinusZero;

            } else if (lcType == LevelOfChangeType.Worsening) { //This is not possible for local search

                if (hClassType == HeuristicClassType.ImprovingMoreOrEqual) {
                    double tempProb = r.nextDouble();
                    if (tempProb <= 0.5) {
                        plusMinusZero = 1.0; //decrease
                    } else {
                        plusMinusZero = 0.0; //no change
                    }
                } else { //WorseningMore or OnlyWorsening
                    plusMinusZero = 1.0;
                }
                //System.out.println("Entou3");
                levelOfChangeList[heurIndex] -= decrementLOCWorsening * learningRateLevel * plusMinusZero;

            } else if (lcType == LevelOfChangeType.Equal) {

                if (hClassType == HeuristicClassType.OnlyImproving) {
                    double tempProb = r.nextDouble();
                    if (tempProb <= 0.5) {
                        plusMinusZero = 1.0; //decrease
                    } else if (tempProb <= 0.75) {
                        plusMinusZero = -1.0; //increase
                    } else {
                        plusMinusZero = 0.0; //no change
                    }
                } else if (hClassType == HeuristicClassType.ImprovingMoreOrEqual) {
                    double tempProb = r.nextDouble();
                    if (tempProb <= 0.5) {
                        plusMinusZero = 1.0; //decrease
                    } else {
                        plusMinusZero = 0.0; //no change
                    }
                } else {//WorseningMore or OnlyWorsening
                    plusMinusZero = -1.0;
                }
                //System.out.println("Entou4");
                levelOfChangeList[heurIndex] -= decrementLOCEqual * learningRateLevel * plusMinusZero;

            } else {
                System.out.println("@updateLevelOfChange => Uncrecognised LevelOfChangeType : " + lcType.toString());
                System.exit(1);
            }
        }

        //Keep the values within their predetermined bounds
        if (levelOfChangeList[heurIndex] < LOCLowerBound) {
            levelOfChangeList[heurIndex] = LOCLowerBound;
        } else if (levelOfChangeList[heurIndex] > LOCUpperBound) {
            levelOfChangeList[heurIndex] = LOCUpperBound;
        }
    }

    private int argMax(double[] calc) {
        int max = 0;
        double maxvalue = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < calc.length; i++) {
            if (calc[i] > maxvalue) {
                max = i;
                maxvalue = calc[i];
            }
        }
        return max;
    }

    private boolean thereIsUnusedOperator(boolean[] v) {
        boolean result = false;
        for (int i = 0; i < v.length; i++) {
            if (v[i] == false) {
                result = true;
            }
        }
        return result;
    }

    private int getUnusedOperator(boolean[] v) {
        int sum = 0;
        int chosen = 0;
        ArrayList<Integer> operators = new ArrayList<Integer>();
        for (int i = 0; i < v.length; i++) {
            if (v[i] == false) {
                sum++;
                operators.add(i);
            }
        }
        double prob[] = new double[sum];

        prob[0] = (double) 1 / sum;
        for (int i = 1; i < prob.length; i++) {
            prob[i] = prob[i - 1] + (double) 1 / sum;
        }

        double randomNumber = r.nextDouble();
        boolean x = true;
        for (int i = 0; i < prob.length; i++) {
            if (randomNumber <= prob[i]) {
                chosen = i;
                break;
            }
        }
        return operators.get(chosen);
    }

    private int sumOfTrials(double v[]) {
        int sum = 0;
        for (int i = 0; i < v.length; i++) {
            sum += v[i];
        }
        return sum;
    }

    @Override
    public void printWindow() {
        System.out.println("XValores heuristicas");
        for (int i = 0; i < numberOfHeuristics; i++) {
            System.out.println("H" + i + " q: " + qH[i] + " n:" + nHeutrials[i]);
        }
        System.out.println("Window:");
        for (Operator o : slidingWindow) {
            System.out.print(o.getOperatorId() + " ");
        }
        System.out.println("");
    }

    public double[] getLast() {
        return last;
    }

    public void setLast(double[] last) {
        this.last = last;
    }



    @Override
    public double[] getLevelOfChangeList() {
        return levelOfChangeList;
    }

    public LinkedList<Operator> getSlidingWindow() {
        return slidingWindow;
    }

    public void setSlidingWindow(LinkedList<Operator> slidingWindow) {
        this.slidingWindow = slidingWindow;
    }

    public CreditAssignment getCreditAssignment() {
        return creditAssignment;
    }

    public void setCreditAssignment(CreditAssignment creditAssignment) {
        this.creditAssignment = creditAssignment;
    }

    public double getReward() {
        return reward;
    }

    public void setReward(double reward) {
        this.reward = reward;
    }

    public double getW() {
        return W;
    }

    public void setW(double W) {
        this.W = W;
    }

    public double[] getnHeutrials() {
        return nHeutrials;
    }

    public void setnHeutrials(double[] nHeutrials) {
        this.nHeutrials = nHeutrials;
    }

    public double[] getqH() {
        return qH;
    }

    public void setqH(double[] qH) {
        this.qH = qH;
    }

    public Random getR() {
        return r;
    }

    public void setR(Random r) {
        this.r = r;
    }

}
