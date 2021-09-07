package org.mysimulationmodel.simulation.common;

import org.mysimulationmodel.simulation.agent.IBaseRoadUser;
import org.mysimulationmodel.simulation.agent.IPedestrianGroup;
import org.mysimulationmodel.simulation.environment.CEnvironment;

import javax.vecmath.Vector2d;

/**
 * calculate all forces
 * Created by Fatema on 10/22/2016.
 * @todo consider the physical properties also to calculate the force
 */
public class CForce
{

    private static final double m_pedlamda = 0.2;
    private static final double m_repulsefactorpedtoped = 1.4 ;//2.1//0.5
    private static final double m_repulsefactorpedtogroupmembers = 0.75 ;//2.1//0.5
    private static final double m_repulsefactorpedtocar = 7;//5//2.1
    private static final double m_pedtopedsigma = 0.4;//0.3
    private static final double m_pedtocaraccelarationdistance = 10;//10
    private static final double m_pedtocarsigma = 5;
    private static final double m_repulsefactorcartoped = 1.5;//6;
    private static final double m_cartopedsigma = 2;//5;
    private static final int m_pixelpermeter = CEnvironment.getpixelpermeter();
    private static final double m_visstrenghtparameter = 0.35;//0.35; //global parameter
    private static final double m_attstrenghtparameter = 0.25;//0.25;

    /**
     * calculate seek force towards goal position
     * @return force vector
     **/
    public static Vector2d drivingForce( final Vector2d p_desiredvelocity, final Vector2d p_current, final double p_relaxationtime )
    {
        return CVector.scale ( 1/(float)p_relaxationtime, CVector.subtract( p_desiredvelocity, p_current ) );
    }

    public static Vector2d drivingForce( final Vector2d p_desiredvelocity, final Vector2d p_current )
    {
        return CVector.subtract( p_desiredvelocity, p_current );
    }


    /**
     * calculate repulsive force towards other pedestrian (another way)
     * @return force vector
     **/
    public static Vector2d repulseotherPed( final IBaseRoadUser p_self, final IBaseRoadUser p_other, double p_pedlamda,
                                            double p_repulsefactorpedtoped, double p_pedtopedsigma, double p_pedtocarsigma,
                                            double p_pedtocarforce )
    {
        Vector2d l_normal = CVector.normalize(CVector.subtract(p_self.getPosition(), p_other.getPosition()));

        /*
         * in case of normal X/Y = 0, add a small value to deviate
         * this scenario happened when 2 pedestrians are fighting in an exactly straight line
         * (i.e. the normalized vector either 0 in x-axis or 0 in y-axis
         * this value acts as a rotation
         * ToDo: find a suitable algorithm to solve this problem
         */
        final double l_threshold = Math.pow( 10, -6 );
        if ( l_normal.x < l_threshold )
            l_normal.setX( l_normal.x + l_threshold );
        if ( l_normal.y < l_threshold )
            l_normal.setY( l_normal.y + l_threshold );

        if( p_other.getType() == 1 )
        {
            final double l_temp = p_self.getM_radius()/3 + p_other.getLengthradius()/2.5f - CVector.distance( p_self.getPosition(), p_other.getPosition() );
            if( p_self.getIsInGroup() == 1 && p_self.group().members().contains(p_other)) {
                return CVector.scale(p_repulsefactorpedtoped/ 2f * Math.exp((l_temp) / p_pedtopedsigma) * anisotropic_character(p_self.getPosition(),
                        p_other.getPosition(), p_pedlamda), l_normal);//1.5
            }
            else {
                return CVector.scale(p_repulsefactorpedtoped * Math.exp(l_temp / p_pedtopedsigma) * anisotropic_character(p_self.getPosition(),
                        p_other.getPosition(), p_pedlamda), CVector.normalize(CVector.subtract(p_self.getPosition(), p_other.getPosition())));
            }
        }
        else
        {
             double l_temp = p_self.getM_radius()/3 + p_other.getLengthradius()/1.5f - CVector.distance(p_self.getPosition(), p_other.getPosition());
             return CVector.scale(p_pedtocarforce * Math.exp(l_temp/ p_pedtocarsigma),
                        CVector.normalize(CVector.subtract(p_self.getPosition(), p_other.getPosition())));

        }
    }

    /**
     * 05.03.2018
     * calculate next car following force
     * @return position vector to follow
     **/
    public static Vector2d nextCarFollowing( final IBaseRoadUser p_self, final IBaseRoadUser p_other )
    {
        return CVector.add( p_self.getPosition(), CVector.scale ( 8*m_pixelpermeter, CVector.normalize( CVector.scale( 1*m_pixelpermeter, p_other.getVelocity() ) ) ) );
    }

    /**
     * check if wall/other pedestrians is under pedestrain's point of view or not?
     * @return double value
     **/
    public static double anisotropic_character(final Vector2d p_v1, final Vector2d p_v2, final double p_lamda )
    {
        return p_lamda + ( 1 - p_lamda )*( ( 1 + CVector.angle( p_v1, p_v2 ) ) * 0.5 );
    }

    /**
     * anticlockwise angle: left to right
     * @return double value
     **/
    public static double getViewAngle( final double p_x1, final double p_y1, final double p_x2, final double p_y2 )
    {
        double l_angle = Math.toDegrees( Math.atan2( p_y1, p_x1 ) - Math.atan2( p_y2, p_x2 ) );
        if( l_angle < 0 )
            l_angle += 360;

        return l_angle;
    }

    //if car is car following and distance between two car is <13m then no stopping
    // when ped is communicating with multiple car at a time then only stopping or going will be active
    // p_self = car, p_other = ped
    public static void helpingfunctionofaccelaration( IBaseRoadUser p_self, IBaseRoadUser p_other )//10
    {
        p_other.setBehavior(0);

        double l_scalingfactor = 6;//p_other.m_pedtocardistance;//
        Vector2d l_temp = CVector.add( p_self.getPosition(),CVector.scale( l_scalingfactor,
                CVector.direction( p_self.getGoalposition(), p_self.getPosition() ) ) );

        if (  CVector.segmentIntersect( CVector.add( p_self.getPosition(),CVector.scale( -5*m_pixelpermeter,
                CVector.direction( p_self.getGoalposition(), p_self.getPosition() ) )),
                l_temp, p_other.getPosition(), p_other.getGoalposition() ))
        {
            //if ( p_other.getRoute().size() == p_other.getnumberofgoalpoints() )
            p_other.getRoute().add(0, p_other.getGoalposition() );

            if ( Math.abs( p_self.getPosition().y - p_self.getGoalposition().y ) <= Math.abs( p_self.getPosition().x - p_self.getGoalposition().x ) )
            {
                if ( p_other.getPosition().y > p_other.getGoalposition().y )
                {
                    p_other.setGoalPedestrian( new Vector2d( l_temp.x, l_temp.y - p_self.getM_radius() * 2 ) );
                    return;
                }

                if ( p_other.getPosition().y < p_other.getGoalposition().y )
                {
                    p_other.setGoalPedestrian( new Vector2d( l_temp.x, l_temp.y + p_self.getM_radius() * 2 ) );
                    return;
                }
            }

            if( Math.abs( p_self.getPosition().y - p_self.getGoalposition().y ) > Math.abs( p_self.getPosition().x - p_self.getGoalposition().x ) )
            {   //need to check
                if ( p_other.getPosition().x > p_other.getGoalposition().x )
                {
                    p_other.setGoalPedestrian( new Vector2d( l_temp.x - p_self.getM_radius() * 2, l_temp.y ) );
                    return;
                }

                if ( p_other.getPosition().x < p_other.getGoalposition().x )
                {
                    p_other.setGoalPedestrian( new Vector2d( l_temp.x + p_self.getM_radius() * 2, l_temp.y ) );
                    return;
                }
            }


        }

    }



    // for deceleration only
    public static Boolean collisionCheckingfordeceleration( final IBaseRoadUser p_ped, final IBaseRoadUser p_car, final double p_scalefactor )
    {
        Vector2d l_radious = CVector.subtract( p_ped.getPosition(),
                CVector.scale( p_ped.getM_radius(),
                        CVector.direction( p_ped.getGoalposition(), p_ped.getPosition() )) );

        return CVector.segmentIntersect( l_radious, p_ped.getGoalposition(),
                p_car.getPosition(), p_car.getGoalposition());
    }

    public static Boolean collisionCheckingfordeviate( final IBaseRoadUser p_ped, final IBaseRoadUser p_car )
    {
        Vector2d l_goal =  p_ped.getRoute().isEmpty() ? p_ped.getGoalposition() : p_ped.getRoute().get(0);
        Vector2d l_radious = CVector.subtract( p_ped.getPosition(),
                CVector.scale( p_ped.getM_radius(),
                        CVector.direction( l_goal, p_ped.getPosition() )) );

        return CVector.segmentIntersect( l_radious, l_goal,
                p_car.getPosition(), p_car.getGoalposition());
    }

    //@note did not check for scaling
    public static Vector2d repulseotherCar(final IBaseRoadUser p_self, final IBaseRoadUser p_other )
    {
        // final double l_temp = 26 - CVector.distance( p_self.getPosition(), p_other.getPosition() );
        final double l_radious = p_self.getM_radius() + p_other.getM_radius() ;
        final double l_temp = l_radious - CVector.distance( p_self.getPosition(), p_other.getPosition() );
        if( p_other.getType() == 1 )
            return CVector.scale( m_repulsefactorcartoped * Math.exp( (l_temp+1) / m_cartopedsigma ) , CVector.normalize( CVector.subtract( p_self.getPosition(), p_other.getPosition() ) ) );
        else
            return new Vector2d(0,0);
    }

    // ------ group force functions ------

    public static Vector2d groupforce(final IPedestrianGroup p_group, final IBaseRoadUser p_self)
    {
        //  return attractivetootherPed(p_group,p_self);
        return CVector.add( visibiltytootherPed( p_group, p_self ), attractivetootherPed( p_group, p_self ) );
        //  return visibiltytootherPed(p_group, p_self);

    }


    private static Vector2d visibiltytootherPed(final IPedestrianGroup p_group, final IBaseRoadUser p_self)
    {
        double temp = m_visstrenghtparameter * p_group.maxrotationangle() / 20f;
        return CVector.scale( temp, CVector.direction( p_self.getGoalposition(), p_self.getPosition() ) );
        // return CVector.scale(temp, p_self.getVelocity());
    }

    private static Vector2d attractivetootherPed(final IPedestrianGroup p_group, final IBaseRoadUser p_self)
    {
        double m_centroiddistancethreshold = 0.5 * (p_group.size() - 1)*m_pixelpermeter;//0.5;//1*m_pixelpermeter;//0.5 * (p_group.size() - 1);

        if ( ( CVector.distance( p_self.getPosition(), p_group.centroid() ) >= m_centroiddistancethreshold )
                && !p_self.getVelocity().equals( new Vector2d( 0, 0 ) ) )
        {
            Vector2d pointingvector = CVector.direction( p_self.getPosition(), p_group.centroid() );
            return CVector.scale( m_attstrenghtparameter, pointingvector );
        }

        else
            return new Vector2d( 0, 0 );
    }
    //////-------------------till here--------------------------/////

    /*
     * the following methods are required to check for accuracy
     * not using currently
     * */

    //@note did not check for scaling
    public static Boolean collisionCheckingAnotherApproach( final IBaseRoadUser p_self, final IBaseRoadUser p_other, final double p_scalefactor )
    {
        double l_distance = CVector.distance( p_self.getPosition(), p_other.getPosition() ) - p_self.getM_radius() - p_other.getM_radius();

        if ( l_distance <= 15 ) return true;
        for ( int i = 1; i <= p_scalefactor; i++ )
        {
            double l_newdistance = CVector.distance( CVector.add( CVector.scale( i, p_self.getVelocity() ), p_self.getPosition() ),
                    CVector.add( CVector.scale( i, p_other.getVelocity() ), p_other.getPosition() ) );

            if ( l_newdistance < l_distance )
            {
                if ( l_newdistance <= 15 ) return true;
            }
            if( i == 3 && l_newdistance > l_distance )
                return false;
        }
        return false;
    }

    public static Boolean collisionChecking2( final IBaseRoadUser p_ped, final IBaseRoadUser p_car, final double p_scalefactor )
    {
        Vector2d l_radious = CVector.subtract( p_ped.getPosition(),
                CVector.scale( p_ped.getM_radius(), CVector.direction( p_ped.getGoalposition(), p_ped.getPosition() )) );

        System.out.println( p_ped.getSpeed() + " speed " + p_car.getSpeed());

        return CVector.segmentIntersect( p_car.getPosition(), CVector.add( p_car.getPosition(), CVector.scale( p_scalefactor
                        * p_car.getSpeed(), CVector.direction( p_car.getGoalposition(), p_car.getPosition() ) ) ), l_radious,
                CVector.add( p_ped.getPosition(), CVector.scale( p_scalefactor * p_ped.getSpeed(),
                        CVector.direction( p_ped.getGoalposition(), p_ped.getPosition() ) ) ) );
    }


    //test later if needed
    //deviate a car or avoiding colliding with a car without game playing: pedestrian's action, 1.5 = side, 3 = behind
    public static void avoidCollidingWithCar( final IBaseRoadUser p_ped, final IBaseRoadUser p_car )
    {
        Vector2d l_oppositeDirection = CVector.direction(p_car.getPosition(),p_car.getGoalposition());

        Vector2d l_behindPoint = CVector.add(p_car.getPosition(),CVector.scale(p_car.getLengthradius()+3,
                new Vector2d(Math.signum(l_oppositeDirection.x),Math.signum(l_oppositeDirection.y))));

        Vector2d l_cartopedirection = CVector.direction( p_ped.getPosition(), p_car.getPosition());

        Vector2d l_temp = CVector.add(p_car.getPosition(),CVector.scale(p_car.getM_radius()+1.5,
                new Vector2d(Math.signum(l_cartopedirection.x),Math.signum(l_cartopedirection.y))));

        Vector2d l_sidePoint = CVector.add(l_temp, CVector.scale( p_car.getLengthradius(),
                new Vector2d(Math.signum(l_oppositeDirection.x),Math.signum(l_oppositeDirection.y))));

        p_ped.setStrategyFollowDistance( p_car.getM_radius() + p_ped.getM_radius() + 3 + 1.5 );

        // get current number of goal point and save
        p_ped.setM_numberofgoalpoints( p_ped.getRoute().size() );

        // add l_sidePoint and l_behindPoint as intermediate goal points of p_ped respectively
        p_ped.setGoalPedestrian(l_sidePoint);
        p_ped.getRoute().add(0,l_behindPoint);
        if ( p_ped.getRoute().size() == 1 ) p_ped.getRoute().add(1, p_ped.getGoalposition() );

    }

    //test later if needed
    //delete newly added goal point if distance(p_car,p_ped)>=p_ped.getStrategyFollowDistance()
    public void deletegoalpoints( final IBaseRoadUser p_ped, final IBaseRoadUser p_car  )
    {
        if( p_ped.getnumberofgoalpoints() != 0 && CVector.distance( p_ped.getPosition(), p_car.getPosition() )
                > p_ped.getM_distanceStillFollowCurrentDeviateStrategy() )
            for( int i = p_ped.getRoute().size(); i < p_ped.getnumberofgoalpoints(); i-- )
            {
                p_ped.getRoute().remove(0);
                p_ped.setM_numberofgoalpoints(0);
            }
    }

    public static Boolean collisionChecking( final IBaseRoadUser p_self, final IBaseRoadUser p_other, final double p_scalefactor )
    {
        Vector2d l_radious = CVector.subtract( p_other.getPosition(),
                CVector.scale( p_self.getM_radius()*3,
                        CVector.direction( p_other.getGoalposition(), p_other.getPosition() )) );

        Vector2d l_seft_endpoint = CVector.add( p_self.getPosition(),
                CVector.scale( p_scalefactor * p_self.getMaxSpeed() + 3 * m_pixelpermeter,
                        CVector.direction( p_self.getGoalposition(), p_self.getPosition() ) ) );

        Vector2d l_other_endpoint = CVector.add( p_other.getPosition(),
                CVector.scale( p_scalefactor * p_other.getMaxSpeed() + 3 * m_pixelpermeter,
                        CVector.direction( p_other.getGoalposition(), p_other.getPosition() ) ) );

        return CVector.segmentIntersect( p_self.getPosition(), l_seft_endpoint, l_radious, l_other_endpoint );
    }



}
