package org.mysimulationmodel.simulation.agent;

public enum EGroupMode
{
    WALKING,
    COORDINATING;

    @Override
    public final String toString()
    {
        return super.toString() + "_MODE";
    }
}