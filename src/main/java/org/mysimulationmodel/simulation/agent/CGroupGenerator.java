package org.mysimulationmodel.simulation.agent;

import org.lightjason.agentspeak.common.CCommon;
import org.lightjason.agentspeak.generator.IBaseAgentGenerator;
import org.mysimulationmodel.simulation.common.CInputFormat_grp;
import org.mysimulationmodel.simulation.constant.CVariableBuilder;
import org.mysimulationmodel.simulation.environment.CEnvironment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Vector2d;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class CGroupGenerator extends IBaseAgentGenerator<IBaseRoadUser>
{
    /**
     * for fixed start and goal position
     */
    private CopyOnWriteArrayList<Vector2d> m_positions = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Vector2d> m_goalpositions = new CopyOnWriteArrayList<>();
    private static final int m_pixelpermeter = CEnvironment.getpixelpermeter();
    private static final double m_GaussianMean = 1.41;//0.67;// meter per half second
    private static final double m_GaussianStandardDeviation = 0.44;//0.13;// meter per half second
    private Random rand = new Random();
    private static final double m_GaussianMeanMaxSpeed = 1.8*m_pixelpermeter;//2.36*m_pixelpermeter;// 3.535: meter per half second
    private static final double m_GaussianStandardDeviationMaxSpeed = 0.21*m_pixelpermeter;//0.435: meter per half second
    private final AtomicLong m_counter = new AtomicLong();


    /**
     * environment
     */
    private final CEnvironment m_environment;
    private final List<CInputFormat_grp> m_inputdata;


    /**
     * constructor of the generator
     * @param p_stream ASL code as any stream e.g. FileInputStream
     * @throws Exception Thrown if something goes wrong while generating agents.
     */


    public CGroupGenerator(@Nonnull final InputStream p_stream, final CEnvironment p_environment, final List<CInputFormat_grp> p_inputdata  ) throws Exception
    {
        super(
                // input ASL stream
                p_stream,
                // a set with all possible actions for the agent
                Stream.concat(
                        // we use all build-in actions of LightJason
                        CCommon.actionsFromPackage(),
                        // use the actions which are defined inside the agent class
                        CCommon.actionsFromAgentClass( IBaseRoadUser.class )

                        // build the set with a collector
                ).collect( Collectors.toSet() ),
                // variable builder
                new CVariableBuilder()
        );

        m_environment = p_environment;
        m_inputdata = Collections.synchronizedList(p_inputdata);
    }


    /**
     * generator method of the agent
     * @param p_data any data which can be put from outside to the generator method
     * @return returns an agent
     */
    @Override
    public final IBaseRoadUser generatesingle( @Nullable final Object... p_data )
    {
        // create agent with a reference to the environment
        final IBaseRoadUser l_pedestrian = new IBaseRoadUser( m_configuration, m_environment,1.2*m_pixelpermeter );//0.083
        CInputFormat_grp l_ped = m_inputdata.remove(0);
        // initialize pedestrian's state

        l_pedestrian.setPosition( l_ped.m_startx_axis*m_pixelpermeter, l_ped.m_starty_axis*m_pixelpermeter);
        l_pedestrian.setGoalPedestrian( l_ped.m_endx_axis*m_pixelpermeter, l_ped.m_endy_axis*m_pixelpermeter);

        l_pedestrian.setradius( 0.75*m_pixelpermeter );// 1.25//.25
        l_pedestrian.setLengthradius(0.75*m_pixelpermeter ); //1.25//.25

        l_pedestrian.setname( l_ped.m_roaduser_id );

        l_pedestrian.settype( 1 );
        l_pedestrian.setmaxforce( 2*m_pixelpermeter* m_environment.getTimestep() );//1//0.09
        l_pedestrian.updateParameter(0.25*m_pixelpermeter, 0.91*m_pixelpermeter,6*m_pixelpermeter, 0.35,
                0.1*m_pixelpermeter, 11.7*m_pixelpermeter,6*m_pixelpermeter,8*m_pixelpermeter);
        l_pedestrian.setSpeed( (1.8*m_pixelpermeter*m_environment.getTimestep())  );
        l_pedestrian.setMaxSpeed( (2*m_pixelpermeter*m_environment.getTimestep()));//l_ped.m_max_speed
        l_pedestrian.setVelocity( l_pedestrian.getSpeed(), l_pedestrian.getGoalposition(), l_pedestrian.getPosition() );

        m_environment.initialset(l_pedestrian);
        // add car to the pedestrian's list
        m_environment.initialPedestrian(l_pedestrian);

        m_environment.addPedGroups().computeIfAbsent( l_ped.m_groupid, k -> new ArrayList<>()).add(l_pedestrian);

        System.out.println( l_pedestrian.getname()+"id"+l_pedestrian.getPosition() +"ped_start_end"+ l_pedestrian.getGoalposition());

        return l_pedestrian;

    }
}
