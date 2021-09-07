package org.mysimulationmodel.simulation.agent;

import org.mysimulationmodel.simulation.common.CWall;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

public interface IPedestrianGroup extends Callable {

    /**
     * function checks the group coherence
     *
     * @return true if group is not coherent; otherwise false
     */
    boolean iscoherent();


    /**
     * function chooses the leader nearest to the car
     */
    void chooseMainLeader();

    void chooseMainLeader( CWall p_road );

    void findlastmemberCluster();

    /**
     * function finds the last (farthest) member in the group
     */
    void findlastmember();

    /**
     * calculates the maximum rotation angle, used for group force
     *
     * @return maximum rotation angle
     */
    double maxrotationangle();

    /**
     * calculates the centroid of the group
     *
     * @return centroid of the group
     */
    Vector2d centroid();

    /**
     * checks whether a user is a member of this group
     *
     * @param p_user user
     * @return true if p_user is member of this group; otherwise false
     */
    boolean ismember(IBaseRoadUser p_user);

    /**
     * returns group id
     *
     * @return group id
     */
    UUID id();

    /**
     * returns group size
     *
     * @return group size
     */
    double size();

    /**
     * returns the list of members of this group
     *
     * @return list of members
     */
    List<IBaseRoadUser> members();


    IBaseRoadUser mainLeader();
    /**
     * returns group leader
     *
     * @return leader
     */
    IBaseRoadUser clusterLeader();

    /**
     * returns the last (farthest) member in the group
     *
     * @return last member
     */
    IBaseRoadUser lastmember();

    /**
     * returns the group mode; COORDINATING or WALKING
     *
     * @return current mode
     */
    EGroupMode mode();

    /**
     * function updates the current mode to p_mode value
     *
     * @param p_mode current group mode
     */
    void updatemode(EGroupMode p_mode);


    EZone zone();

    void updatezone(EZone p_zone);

    /**
     * draw group, used for visualization
     *
     * @param p_graphics2D graphics reference
     */
    void draw(Graphics2D p_graphics2D);

    /**
     * coordinate group, used to re-group members
     */
    void coordinate();

    /**
     * make the whole group walk to the goal
     */
    void walk();

    /**
     * returns the main goal of the group
     *
     * @return goal point
     */
    Vector2d goal();

    /**
     * change the goal to p_goal
     *
     * @param p_goal goal value
     */
    void goal(Vector2d p_goal);

}
