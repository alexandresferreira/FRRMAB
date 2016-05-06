/*
 Copyright (c) 2012  Mustafa MISIR, KU Leuven - KAHO Sint-Lieven, Belgium

 This file is part of GIHH v1.0.

 GIHH is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

adapted for MAB-HH

 */
package util;

import java.io.Serializable;

/**
 * This class consists of a number of parameter used within MABHH.
 */
public class Vars implements Serializable {

    /**
     * The total execution time allowed (stopping condition) *
     */
    public static long totalExecutionTime = 480000;
    /**
     * The MAB algorithm used as the selection strategy 1 - FRAMAB 2- FRRMAB default: SLMAB
     */
    public static int mabType = 1;

    /**
     * Wheter the breakpoints will be used to print information about the selection or not. First break point totalExecTime/3, Second totalExecTime/2
     */
    public static boolean breakpoint1 = true;
    public static boolean breakpoint2 = true;

    /**
     * MAX number of iterations withtout a improvement before accept an worse solution
     */
    public static long iterMax = 120;

    /**
     * The value of the scaling factor parameter
     */
    public static double scalingFactor = 0.05;

    /**
     * Size of the sliding window structure
     */
    public static double windowSize = 1500;

    /**
     * Simulated Annealing Accep Parameters
     */
    public static double temperature = 1;
    public static boolean reduceTemperature = false;
    /**
     * Whether the heuristics' parameters are oscillated in case of getting
     * stuck*
     */
    public final static boolean oscilateLOCValues = false;
    /**
     * Whether a search process is at stuck *
     */
    public static boolean isAtStuck = false;
    /**
     * Whether a search process is at stuck *
     */
    public static int numberOfRestarts = 0;

     /**
     * Phase length (pl) *
     */
    public static int phaseLength = 2000;
    /**
     * A constant value used to calculate the phase length *
     */
    public static int PLFactor = 500;
    /**
     * The default tabu duration (d) value *
     */
    public static int tabuDuration;
    /**
     * A multiplier showing the speed of a heuristic with respect to the fastest
     * non-tabu heuristic *
     */
    public static int learningRateMultiplier = 1;
    /**
      * Sensitivity level considered while using time information *
      */
     public static double execTimeSensitivity = 0.001;
    /**
     * A constant value used to calculate the performance of the heuristic at
     * the end of each phase *
     */
    public static double powerOfForNewBestSolutionsPerfM0 = 2;
    /**
     * Upper bound for the learning rates *
     */
    public final static double learningRateLimit = 0.5;

}
