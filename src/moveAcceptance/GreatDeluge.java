/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moveAcceptance;

import java.util.Random;
import util.Vars;

/**
 *
 * @author Alexandre
 */
public class GreatDeluge extends AcceptanceCriterion {

    private double beta;

    public GreatDeluge(Random r) {
        super(r);
    }

    @Override
    public boolean accept(double newFitness, double currentFitness, double bestFitness) {
        boolean acp = false;
        //accept better
        if (newFitness < currentFitness) {
            acp = true;
            if (newFitness < bestFitness) {
                numberOfIterationsStuck = 0;
                Vars.isAtStuck = false;
            }

        } //accept equal
        else if (newFitness == currentFitness) {
            acp = true;
            this.numberOfIterationsStuck++;
        } //worsen
        else if (newFitness < this.level) {
            acp = true;
            updateLevel();
        }
        return acp;
    }

    private void updateLevel() {
        this.level = this.level - this.beta;
    }

}
