package org.mysimulationmodel.simulation.agent;

import org.mysimulationmodel.simulation.environment.CEnvironment;

import javax.vecmath.Vector2d;

/**
 * cluster class (subgroup)
 */
public class CCluster extends IBaseGroup implements ICluster
{
    private Vector2d cMainGoal;

    /**
     * constructor
     *
     * @param p_env       environment reference
     * @param p_FOVfactor FOV factor
     */
    CCluster( CEnvironment p_env, double p_FOVfactor, Vector2d p_cMainGoal )
    {
        super( 8, p_env, p_FOVfactor );
        cMainGoal = p_cMainGoal;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Object call( ) throws Exception
    {
        this.findlastmember();

        if ( this.zone().equals( EZone.DANGER ) )
        {
            this.members().forEach( m -> m.setGoalPedestrian( cMainGoal ) );
            return this;
        }

        if ( !this.iscoherent() )
        {
            this.updatemode( EGroupMode.COORDINATING );
            this.coordinate();
            return this;
        }

        if ( !this.mode().equals( EGroupMode.WALKING ) )
        {
            this.updatemode(EGroupMode.WALKING);
            this.updatenormalgoals();
        }
        this.walk();

        return this;
    }

}


