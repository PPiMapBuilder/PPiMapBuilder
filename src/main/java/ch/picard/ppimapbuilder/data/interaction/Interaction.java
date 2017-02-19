/*
 * This file is part of PPiMapBuilder.
 *
 * PPiMapBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PPiMapBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PPiMapBuilder.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2015 Echeverria P.C., Dupuis P., Cornut G., Gravouil K., Kieffer A., Picard D.
 *
 */

package ch.picard.ppimapbuilder.data.interaction;


import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;

import java.util.List;

public class Interaction implements IInteraction {
    private final Protein interactorA;
    private final Protein interactorB;
    private final List<String> sourceDatabases;
    private final Organism sourceOrganism;

    public Interaction(Protein interactorA, Protein interactorB, List<String> sourceDatabases, Organism sourceOrganism) {
        this.interactorA = interactorA;
        this.interactorB = interactorB;
        this.sourceDatabases = sourceDatabases;
        this.sourceOrganism = sourceOrganism;
    }

    @Override
    public Protein getInteractorA() {
        return interactorA;
    }

    @Override
    public Protein getInteractorB() {
        return interactorB;
    }

    @Override
    public List<String> getSourceDatabases() {
        return sourceDatabases;
    }

    @Override
    public Organism getSourceOrganism() {
        return sourceOrganism;
    }
}
