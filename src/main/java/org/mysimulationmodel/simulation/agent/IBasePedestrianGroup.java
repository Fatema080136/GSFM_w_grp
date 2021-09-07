package org.mysimulationmodel.simulation.agent;

import org.mysimulationmodel.simulation.common.CForce;
import org.mysimulationmodel.simulation.common.CVector;
import org.mysimulationmodel.simulation.environment.CEnvironment;

import javax.annotation.Nonnull;
import javax.vecmath.Vector2d;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class IBasePedestrianGroup
{
    private static final long serialVersionUID = -8903187338768804103L;
    static int m_pixelpermeter = CEnvironment.getpixelpermeter();
    private static final double m_SOCIALDISTANCE = 10.0*m_pixelpermeter;

    private final double m_id;
    private final double m_size;

    private final CEnvironment m_env;
    private final List<IBaseRoadUser> m_members = new ArrayList<>(); //group members (pedestrians)

    private final int m_clustersize;
    private final List<IBasePedestrianGroup> m_clusters = new ArrayList<>();
    private IBaseRoadUser m_leader;
    private IBaseRoadUser m_lastmember;
    private final double FOVfactor = 181;


    private EGroupMode m_mode;
    private final boolean m_cluster;


    /**
     * constructor
     *
     * @param p_env environment reference
     * @param p_id group id
     * @param p_size group size
     * @param p_clustersize cluster size
     * @param p_cluster true if this group is cluster, false otherwise
     */
    public IBasePedestrianGroup(CEnvironment p_env, double p_id, double p_size, final int p_clustersize, boolean p_cluster )
    {
        m_id = p_id;
        m_env = p_env;
        m_size = p_size;
        m_clustersize = p_clustersize;
        m_cluster = p_cluster;
    }

    public boolean iscoherent()
    {
        double l_distance = 0;

        l_distance = CVector.distance( m_leader.getPosition(), m_lastmember.getPosition() );
        if ( l_distance <= m_SOCIALDISTANCE ) //+ m_leader.getM_radius())
            return true;

        return false;
    }

    public void findlastmember()
    {
        IBaseRoadUser l_user = m_members.get( 0 );
        m_lastmember = l_user;
        Vector2d l_leaderposition = m_leader.getPosition();
        double l_maxdistance = CVector.distance( l_leaderposition, l_user.getPosition() );
        double l_distance = 0;
        for ( int i = 0; i < m_size; i++ )
        {
            l_user = m_members.get( i );
            if ( l_user != m_leader )
            {
                l_distance = CVector.distance( l_leaderposition, l_user.getPosition() );
                if ( l_distance >= l_maxdistance )
                {
                    l_maxdistance = l_distance;
                    m_lastmember = l_user;

                }
            }
        }
    }


    public void chooseLeader() {
        Vector2d l_movingdirection = CVector.direction(members().get(0).getGoalposition(), members().get(0).getPosition());

        if (l_movingdirection.x > 0) {
            m_leader = helpingFunction(1);
        } else if (l_movingdirection.x < 0)
            m_leader = helpingFunction(2);
        else if (l_movingdirection.x == 0 && l_movingdirection.y > 0)
            m_leader = helpingFunction(3);
        else
            m_leader = helpingFunction(4);

    }



    public IBaseRoadUser helpingFunction(final int p_indicator)
    {
        if( p_indicator == 1 )
        {
            int id = 0;
            for (int i = 1; i < m_size; i++)
            {
                if(members().get(id).getPosition().x < members().get(i).getPosition().x)
                    id = i;
            }
            //System.out.println("check3 "+ members().get(id).getname());
            return members().get(id);
        }
        if( p_indicator == 2 )
        {
            int id = 0;
            for (int i = 1; i < m_size; i++)
            {
                if(members().get(id).getPosition().x > members().get(i).getPosition().x)
                    id = i;
            }
            return members().get(id);
        }
        if( p_indicator == 3 )
        {
            int id = 0;
            for (int i = 1; i < m_size; i++)
            {
                if(members().get(id).getPosition().y < members().get(i).getPosition().y)
                    id = i;
            }
            return members().get(id);
        }
        if( p_indicator == 4 )
        {
            int id = 0;
            for (int i = 1; i < m_size; i++)
            {
                if(members().get(id).getPosition().y > members().get(i).getPosition().y)
                    id = i;
            }
            return members().get(id);
        }
        return members().get((new Random().nextInt(members().size())));

    }

    public void chooselastmember() {
        //m_leader = members().get((new Random().nextInt(members().size())));

        Vector2d l_movingdirection = CVector.direction(members().get(0).getGoalposition(), members().get(0).getPosition());

        if (l_movingdirection.x > 0)
            m_lastmember = lastmemberhelpingFunction( 1);
        else if (l_movingdirection.x < 0)
            m_lastmember = lastmemberhelpingFunction( 2);
        else if (l_movingdirection.x == 0 && l_movingdirection.y > 0)
            m_lastmember = lastmemberhelpingFunction( 3);
        else
            m_lastmember= lastmemberhelpingFunction( 4);

    }


    public IBaseRoadUser lastmemberhelpingFunction(final int p_indicator)
    {
        if( p_indicator == 1 )
        {
            int id = 0;
            for (int i = 1; i < m_size; i++)
            {
                if(members().get(id).getPosition().x > members().get(i).getPosition().x)
                    id = i;

            }
            return members().get(id);
        }
        if( p_indicator == 2 )
        {
            int id = 0;
            for (int i = 1; i < m_size; i++)
            {
                if(members().get(id).getPosition().x < members().get(i).getPosition().x)
                    id = i;

            }
            return members().get(id);
        }
        if( p_indicator == 3 )
        {
            int id = 0;
            for (int i = 1; i < m_size; i++)
            {
                if(members().get(id).getPosition().y > members().get(i).getPosition().y)
                    id = i;

            }
            return members().get(id);
        }
        if( p_indicator == 4 )
        {
            int id = 0;
            for (int i = 1; i < m_size; i++)
            {
                if(members().get(id).getPosition().y < members().get(i).getPosition().y)
                    id = i;

            }
            return members().get(id);
        }
        return members().get((new Random().nextInt(members().size())));

    }

    /**
     * calculate the max of min rotation angles for all members w.r.t. leader
     *
     * @return max of min rotation angles
     */
    public double maxrotationangle()
    {
        Vector2d l_direction = CVector.direction( m_leader.getGoalposition(), m_leader.getPosition() );
        double maxangle = 0;
        double l_viewangle;

        for (int i = 0; i < m_size; i++) {
            l_viewangle = CForce.getViewAngle(
                    l_direction.x,
                    l_direction.y,
                    m_members.get(i).getPosition().x - m_leader.getPosition().x,
                    m_members.get(i).getPosition().y - m_leader.getPosition().y
            );

            if ( !inFOV( l_viewangle ) )
                continue;

            if ( minrotation(l_viewangle) > maxangle )
                maxangle = minrotation(l_viewangle);
        }

        return maxangle;
    }

    /**
     * check if the given angle within FOV (Field Of View)
     * FOV is attributed with +/- FOVfactor
     *
     * @param p_angle angle value
     * @return true if angle in FOV, otherwise false
     */
    private boolean inFOV(final double p_angle)
    {
        return ( p_angle <= FOVfactor || p_angle >= (360-FOVfactor) );
    }

    /**
     * calculate the min rotation angle to keep pedestrian in FOV
     *
     * @param p_angle angle value
     * @return min rotation angle
     */
    private double minrotation( final double p_angle )
    {
        if ( p_angle <= FOVfactor )
            return FOVfactor - p_angle;
        else
            return p_angle - (360-FOVfactor);
    }

    /**
     * calculates the centroid of the group
     *
     * @return centroid of the group
     */
    public Vector2d centroid()
    {
        final Vector2d l_centroid = new Vector2d(0,0);

        m_members.forEach( m -> l_centroid.add( m.getPosition() ) );

        return CVector.scale( 1/m_size, l_centroid );
    }


    public void cluster()
    {
        int l_cindex = 0;
        int l_cmemberindex = 0;
        double l_id = 0;
        IBasePedestrianGroup l_group;

        while( l_cmemberindex < m_size )
        {
            l_group = new IBasePedestrianGroup( m_env, l_id, m_clustersize, 0, true );

            while ( l_cindex < m_clustersize )
            {
                l_group.members().add( m_members.get( l_cmemberindex ) );
                l_cindex++;
                l_cmemberindex++;

                if ( l_cmemberindex >= m_size )
                    break;
            }

            m_clusters.add( l_group );
            l_cindex = 0;
            l_id++;
        }

        m_clusters.forEach( IBasePedestrianGroup::chooseLeader );
        m_clusters.forEach( IBasePedestrianGroup::findlastmember);

    }


    /**
     * check if user is member of this group
     *
     * @param p_user member reference
     * @return true if user is member of this group, else return false
     */
    boolean ismember(IBaseRoadUser p_user)
    {
        for (IBaseRoadUser u : m_members)
            if (u.equals(p_user))
                return true;

        return false;
    }

    public void setM_mode(EGroupMode m_mode) {
        this.m_mode = m_mode;
    }

    public EGroupMode mode()
    {
        return m_mode;
    }

    public double size() {
        return m_size;
    }

    public double id() {
        return m_id;
    }

    public List<IBaseRoadUser> members() {
        return m_members;
    }

    public IBaseRoadUser leader() {
        return m_leader;
    }

    public IBaseRoadUser lastmember() { return m_lastmember; }

    public boolean isCluster()
    {
        return m_cluster;
    }

    public List<IBasePedestrianGroup> clusters()
    {
        return m_clusters;
    }

    public int getM_clustersize() {
        return m_clustersize;
    }
}
















