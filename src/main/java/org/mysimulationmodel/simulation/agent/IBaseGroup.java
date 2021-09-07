package org.mysimulationmodel.simulation.agent;

import org.mysimulationmodel.simulation.common.CForce;
import org.mysimulationmodel.simulation.common.CVector;
import org.mysimulationmodel.simulation.common.CWall;
import org.mysimulationmodel.simulation.environment.CEnvironment;

import javax.annotation.Nonnull;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * group abstract class
 */
public abstract class IBaseGroup implements IPedestrianGroup
{
    /**
     * serial version for serialization
     */
    private static final long serialVersionUID = - 8903187338768804103L;

    private Random rand = new Random();

    /**
     * group id
     */
    private final UUID m_uid = UUID.randomUUID();
    /**
     * threshold for coherence
     */
    private final double m_SOCIALDISTANCE;
    /**
     * group size
     */
    private final double m_size;

    private int m_pixelpermeter = CEnvironment.getpixelpermeter();

    /**
     * environment
     */
    final CEnvironment m_env;
    /**
     * group members
     */
    private final List<IBaseRoadUser> m_members = new CopyOnWriteArrayList<>();
    /**
     * FOV factor
     */
    private final double m_FOVfactor;


    private IBaseRoadUser m_clusterLeader;
    /**
     * group leader
     */
    private IBaseRoadUser m_leader;
    /**
     * group last member
     */
    private IBaseRoadUser m_lastmember;
    /**
     * current group mode
     */
    private EGroupMode m_mode = EGroupMode.WALKING;
    /**
     * group zone
     */
    private EZone m_zone = EZone.SAFE;
    /**
     * final goal for all members
     */
    private Vector2d m_goal = new Vector2d( 0, 0 );
    /**
     * speed flag, used to set speed only once (whether 0 for coordinating, or 0.55 for walking)
     */
    private boolean m_speedflag = false;

    /**
     * constructor
     *
     * @param p_size      group size
     * @param p_env       environment reference
     * @param p_FOVfactor FOV factor
     */
    IBaseGroup( double p_size, CEnvironment p_env, double p_FOVfactor )
    {
        this.m_size = p_size;
        this.m_env = p_env;
        m_FOVfactor = p_FOVfactor;
        m_SOCIALDISTANCE = /*0.5 * m_pixelpermeter * m_size;*/ 1.4f *m_pixelpermeter * m_size;
//        m_SOCIALDISTANCE = 7.5 * m_size;
    }

    @Override
    public boolean iscoherent( )
    {
        return CVector.distance( this.mainLeader().getPosition(), this.lastmember().getPosition() ) <= m_SOCIALDISTANCE;
    }

  @Override
  public  void chooseMainLeader()
  {
      //chooseleader according to the nearest goal

      IBaseRoadUser l_user = m_members.get( 0 );
      m_leader = l_user;
      double l_mindistance = CVector.distance( l_user.getGoalposition(), l_user.getPosition() );
      double l_distance = 0;
      for ( int i = 1; i < m_size; i++ )
      {
          l_user = m_members.get( i );

          l_distance = CVector.distance( l_user.getGoalposition(), l_user.getPosition() );
          if ( l_distance <= l_mindistance )
          {
              l_mindistance = l_distance;
              m_leader = l_user;
          }
      }
  }

    //@Override
    public void chooseMainLeader( CWall p_road )
    {
        double l_distancetoroad = CVector.distanceToWall( m_members.get( 0 ).getPosition(),
                                                          p_road.getPoint1(),
                                                          p_road.getPoint2() )
                                         .length();
        double l_mindistance = l_distancetoroad;

        for ( int i = 0; i < m_size; i++ )
        {
            l_distancetoroad = CVector.distanceToWall( m_members.get( i ).getPosition(),
                                                       p_road.getPoint1(),
                                                       p_road.getPoint2() ).length();

            if ( l_distancetoroad <= l_mindistance )
            {
                l_mindistance = l_distancetoroad;
                m_leader = m_members.get( i );
            }

        }
    }

    @Override
    public void findlastmember( )
    {
        IBaseRoadUser l_user = m_members.get( 0 );
        m_lastmember = l_user;
        Vector2d l_leaderposition = m_leader.getPosition();
        double l_maxdistance = CVector.distance( l_leaderposition, l_user.getPosition() );
        double l_distance = 0;
        for ( int i = 1; i < m_size; i++ )
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


    public void findlastmemberCluster( )
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

    @Override
    public double maxrotationangle( )
    {
        Vector2d l_direction = CVector.direction( m_leader.getGoalposition(), m_leader.getPosition() );
        double maxangle = 0;
        double l_viewangle;

        for ( int i = 0; i < m_size; i++ )
        {
            l_viewangle = CForce.getViewAngle(
                    l_direction.x,
                    l_direction.y,
                    m_members.get( i ).getPosition().x - m_leader.getPosition().x,
                    m_members.get( i ).getPosition().y - m_leader.getPosition().y
            );

            if ( ! inFOV( l_viewangle ) )
                continue;

            if ( minrotation( l_viewangle ) > maxangle )
                maxangle = minrotation( l_viewangle );
        }

        return maxangle;
    }

    /**
     * check if the given angle within FOV (Field Of View) FOV is attributed with +/- m_FOVfactor
     *
     * @param p_angle angle value
     * @return true if angle in FOV, otherwise false
     */
    private boolean inFOV( final double p_angle )
    {
        return ( p_angle <= m_FOVfactor || p_angle >= ( 360 - m_FOVfactor ) );
    }

    /**
     * calculate the min rotation angle to keep pedestrian in FOV
     *
     * @param p_angle angle value
     * @return min rotation angle
     */
    private double minrotation( final double p_angle )
    {
        if ( p_angle <= m_FOVfactor )
            return m_FOVfactor - p_angle;
        else
            return p_angle - ( 360 - m_FOVfactor );
    }

    @Override
    public Vector2d centroid( )
    {
        final Vector2d l_centroid = new Vector2d( 0, 0 );

        this.m_members.forEach( m -> l_centroid.add( m.getPosition() ) );

        return CVector.scale( 1 / this.m_size, l_centroid );
    }

    @Override
    public boolean ismember( IBaseRoadUser p_user )
    {

        for ( IBaseRoadUser u : m_members )
            if ( u.equals( p_user ) )
                return true;

        return false;
    }


    @Override
    public UUID id( )
    {
        return m_uid;
    }

    @Override
    public double size( )
    {
        return m_size;
    }

    @Override
    public List<IBaseRoadUser> members( )
    {
        return m_members;
    }

    @Override
    public IBaseRoadUser clusterLeader( )
    {
        return m_clusterLeader;
    }

    //@Override
    public IBaseRoadUser mainLeader( )
    {
        return m_leader;
    }

    @Override
    public IBaseRoadUser lastmember( )
    {
        return m_lastmember;
    }

    @Override
    public EGroupMode mode( )
    {
        return m_mode;
    }

    @Override
    public void updatemode( EGroupMode p_mode )
    {
        if ( this.m_mode == p_mode )
            return;

        this.m_mode = p_mode;
    }

    @Override
    public void draw( Graphics2D p_graphics2D )
    {
        Ellipse2D.Double l_shape;

        //draw leader
        p_graphics2D.setColor( Color.GREEN );
        l_shape = new Ellipse2D.Double( this.mainLeader().getPosition().getX(), this.mainLeader().getPosition().getY(),
                                        0.7 * m_pixelpermeter, 0.7 * m_pixelpermeter );
        p_graphics2D.fill( l_shape );

        //draw last member
        p_graphics2D.setColor( Color.RED );
        l_shape = new Ellipse2D.Double( this.lastmember().getPosition().getX(), this.lastmember().getPosition().getY(),
                                        0.7 * m_pixelpermeter, 0.7 * m_pixelpermeter );
        p_graphics2D.fill( l_shape );

        //draw other members
        p_graphics2D.setColor( Color.ORANGE );
        this.members()
            .stream()
            .filter( p -> !p.equals( this.mainLeader() ) && !p.equals( this.lastmember() ) )
            .forEach( m ->
                      {
                          Ellipse2D.Double shape = new Ellipse2D.Double(
                                  m.getPosition().getX(), m.getPosition().getY(), 0.7 * m_pixelpermeter,
                                  0.7 * m_pixelpermeter );//8
                          p_graphics2D.fill( shape );

                      } );
    }

    @Override
    public void coordinate( )
    {
        waitleader();
        updateleaderasgoal();
    }

    private void waitleader( )
    {
        this.mainLeader().setSpeed( 0 );
    }

    private void updateleaderasgoal( )
    {
        this.members()
            .parallelStream()
            .forEach( n ->
                      {
                          n.setGoalPedestrian( this.mainLeader().getPosition() );
                      } );
    }

  public void walk( )
  {
      moveleader();
      clusterspeed();
  }

  private void moveleader( )
    {
        this.mainLeader().setSpeed( 0.85 * m_pixelpermeter );//0.69
    }

  private void clusterspeed( )
  {
      this.members()
            .stream()
            .filter( m -> !m.equals( this.mainLeader() ) )
            .forEach( n -> n.setSpeed( 0.96 * m_pixelpermeter ) );//0.55
  }

  public void walkdangerzone( )
  {
        moveleader();
        clusterspeed();
  }


  void updatenormalgoals( )
  {
        // double l_leaderdistance = CVector.distance( this.mainLeader().getPosition(), this.goal() );
        this.mainLeader().setGoalPedestrian( this.goal() );
        this.members()
            .stream()
            .filter( m -> !m.equals( this.mainLeader() ) )
            .forEach( n -> n.setGoalPedestrian( this.mainLeader().getPosition() ) );
  }



    @Override
    public Vector2d goal( )
    {
        return m_goal;
    }

    @Override
    public void goal( Vector2d p_goal )
    {
        m_goal = p_goal;
    }


    public void deviate( final IBaseRoadUser p_self, final IBaseRoadUser p_other )
    {
        double l_temp = CVector.distance( p_other.getPosition(), p_self.getPosition() );
        if ( l_temp <= 20 * m_pixelpermeter )
        {
            rotate( 90, p_self.getVelocity() );
            rotate( 90, p_other.getVelocity() );
        }
    }

    /**
     * rotate a given vector by a given angle, using the following equation: x2 = x1*cos(a) - y1*sin(a) y2 = x1*sin(a) +
     * y1*cos(a)
     *
     * @param p_angle  angle value in degree
     * @param p_vector vector
     */
    public static void rotate( double p_angle, @Nonnull Vector2d p_vector )
    {
        p_vector.setX( p_vector.x * Math.cos( Math.toRadians( p_angle ) ) - p_vector.y * Math.sin(
                Math.toRadians( p_angle ) ) );
        p_vector.setY( p_vector.x * Math.sin( Math.toRadians( p_angle ) ) + p_vector.y * Math.cos(
                Math.toRadians( p_angle ) ) );
    }

    public void updatezone( EZone p_zone )
    {
        if ( this.m_zone == p_zone )
            return;

        this.m_zone = p_zone;
    }

    public EZone zone( )
    {
        return m_zone;
    }

}
