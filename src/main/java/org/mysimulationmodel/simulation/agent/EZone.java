package org.mysimulationmodel.simulation.agent;

public enum EZone {
    SAFE,
    DANGER;

    @Override
    public final String toString()
    {
        return super.toString() + "_ZONE";
    }
}

