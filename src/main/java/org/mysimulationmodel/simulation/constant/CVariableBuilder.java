package org.mysimulationmodel.simulation.constant;

import org.lightjason.agentspeak.agent.IAgent;
import org.lightjason.agentspeak.language.execution.IVariableBuilder;
import org.lightjason.agentspeak.language.instantiable.IInstantiable;
import org.lightjason.agentspeak.language.variable.CConstant;
import org.lightjason.agentspeak.language.variable.IVariable;
import org.mysimulationmodel.simulation.agent.IBaseRoadUser;


import java.util.stream.Stream;


/**
 * Code adapted from
 * https://github.com/SocialCars/LightVoting/blob/master/src/main/java/org/lightvoting/simulation/constants/CVariableBuilder.java
 */
public final class CVariableBuilder implements IVariableBuilder
{

    public final Stream<IVariable<?>> apply( final IAgent<?> p_agent, final IInstantiable p_context )
    {
        return Stream.of(
            new CConstant<>( "AgentName", p_agent.<IBaseRoadUser>raw().getname() )
        );
    }

}
