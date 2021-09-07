package org.mysimulationmodel.simulation.agent;

import java.util.List;

public interface IMainGroup extends IPedestrianGroup {

    List<ICluster> clusters();

    void cluster();
}
