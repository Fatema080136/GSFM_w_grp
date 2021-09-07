
package org.mysimulationmodel.simulation.agent;

import org.mysimulationmodel.simulation.environment.CEnvironment;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * main group classe, for non-clustered groups
 * it may divided into subgroups (clusters)
 */
public class CMainGroup extends IBaseGroup implements IMainGroup
{

    private final List<ICluster> m_clusters = new CopyOnWriteArrayList<>();

    /**
     * constructor
     *
     * @param p_size group size
     * @param p_env  environment //* @param p_FOVfactor FOV factor
     */
    public CMainGroup(double p_size, CEnvironment p_env, double p_FOVfactor )
    {
        super( p_size, p_env, p_FOVfactor );
    }

    @Override
    public List<ICluster> clusters( )
    {
        return m_clusters;
    }

    @Override
    public void cluster( )
    {
        int l_groupsize = m_env.groups().size();
        double l_clustersize = this.size();
        double l_numberofclusters = 0;
        double l_cindex = 0;
        double l_cmemberindex = 0;
        ICluster l_cluster;

        while ( l_cmemberindex < this.size() )
        {
            l_cluster = new CCluster( m_env, 360, this.mainLeader().getPosition() );

            while ( l_cindex < l_cluster.size() )
            {

                l_cluster.members().add( this.members().get( ( int ) l_cmemberindex ) );
                l_cindex++;
                l_cmemberindex++;

                if ( l_cmemberindex >= this.size() )
                    break;
            }

            if ( l_cluster.size() > l_groupsize )
            {
                l_numberofclusters = 1;
                l_clustersize = l_groupsize;
            }
            else
            {
                l_numberofclusters = Math.ceil( l_groupsize / l_cluster.size() );
                l_clustersize = l_cmemberindex;
            }
            for ( int i = 0; i < l_numberofclusters; i++ )
            {
                m_clusters.add( l_cluster );
                l_cindex = 0;
            }

        }

        m_clusters.forEach( ICluster::chooseMainLeader );
        m_clusters.forEach( ICluster::findlastmember );
        m_clusters.forEach( c -> c.goal( this.mainLeader().getPosition() ) );
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
        this.findlastmember(); //checked

        if ( this.zone().equals( EZone.DANGER ) )
        {
            if ( m_clusters.size() > 1 )
            {
                m_clusters.parallelStream()
                        .forEach( c ->
                        {
                            try
                            {
                                c.call();
                            }
                            catch ( Exception e )
                            {
                                e.printStackTrace();
                            }
                        } );
                return this;
            }

            this.updatenormalgoals();
            return this;
        }

        if ( !this.iscoherent() ) //checked
        {
            if ( this.mode() != EGroupMode.COORDINATING )
            {
                this.updatemode( EGroupMode.COORDINATING );
                this.coordinate();
            }

            if ( m_clusters.size() > 1 )
                m_clusters.parallelStream()
                        .forEach( c ->
                        {
                            try
                            {
                                c.call();
                            }
                            catch ( Exception e )
                            {
                                e.printStackTrace();
                            }
                        } );

            return this;
        }

        if ( this.mode() != EGroupMode.WALKING )
        {
            this.updatemode( EGroupMode.WALKING );
            this.updatenormalgoals();
        }
        this.walk();
        return this;
    }

}
