package com.fs.starfarer.api.campaign;

import java.awt.Color;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface StarSystemAPI extends LocationAPI {

	
	Vector2f getLocation();

	/**
	 * Will not automatically generate a hyperspace anchor for the star.
	 * Call autogenerateHyperspaceJumpPoints() to do that, or create the
	 * anchor manually using JumpPointAPI.
	 * @param id unique id for this star
	 * @param type
	 * @param color
	 * @param radius
	 * @return
	 */
	PlanetAPI initStar(String id, String type, float radius, float coronaSize, float windBurnLevel, float flareProbability, float crLossMult);
	
	PlanetAPI initStar(String id, String type, float radius, float coronaSize);
	
	/**
	 * Generates the hyperspace anchor for the star (and nothing else - no gas giant gravity
	 * wells or fringe jump point), unless one was already generated.
	 */
	void generateAnchorIfNeeded();
	
	/**
	 * Color argument is not used. Use PlanetAPI.getSpec() instead.
	 * 
	 */
	@Deprecated PlanetAPI initStar(String id, String type, Color color, float radius, float coronaSize);
	
	
	/**
	 * Also automatically creates a wormhole/jump point leading to the star from hyperspace. This
	 * wormhole can be accessed using getHyperspaceAnchor().
	 * @param id unique id for this star
	 * @param type
	 * @param color
	 * @param radius
	 * @param hyperspaceLocationX
	 * @param hyperspaceLocationY
	 * @return
	 */
	PlanetAPI initStar(String id, String type, float radius, float hyperspaceLocationX, float hyperspaceLocationY, float coronaSize);
	
	
	/**
	 * 	A location token corresponding to the center of the system in hyperspace.
	 */
	SectorEntityToken getHyperspaceAnchor();
	
	void setHyperspaceAnchor(SectorEntityToken hyperspaceAnchor);
	
	
	PlanetAPI getStar();
	
	
	/**
	 * Calls autogenerateHyperspaceJumpPoints(false, false)
	 */
	void autogenerateHyperspaceJumpPoints();
	
	
	/**
	 * Generates jump points into the system and adds them to hyperspace.
	 * 
	 * Jump points generated are based on the jump points within the system.
	 * 
	 * Also adds jump destinations from all in-system jump points to the associated,
	 * newly-generated hyperspace jump points.
	 * 
	 * Will also generate a wormhole for the star if one doesn't exist already.
	 * 
	 * @param generateEntrancesAtGasGiants whether one-way jump points into the system are generated at gas giants
	 * @param generateFringeJumpPoint whether an extra jump point (two-way) is generated on the fringes of the system
	 */
	void autogenerateHyperspaceJumpPoints(boolean generateEntrancesAtGasGiants, boolean generateFringeJumpPoint);
	
	
	Color getLightColor();
	/**
	 * Only applicable if this location has a light source (i.e. a star).
	 * @param lightColor
	 */
	void setLightColor(Color lightColor);

	/**
	 * Star name without "Star System" appended to it.
	 * @return
	 */
	String getBaseName();

	/**
	 * Only considers jump points into the system generated by autogenerateHyperspaceJumpPoints.
	 * 
	 * @return
	 */
	float getMaxRadiusInHyperspace();

	SectorEntityToken initNonStarCenter();

	SectorEntityToken getCenter();

	void setStar(PlanetAPI star);
	void setBaseName(String baseName);

	PlanetAPI getSecondary();
	void setSecondary(PlanetAPI secondary);
	PlanetAPI getTertiary();
	void setTertiary(PlanetAPI tertiary);

	List<JumpPointAPI> getAutogeneratedJumpPointsInHyper();

	StarSystemType getType();
	void setType(StarSystemType type);

	/**
	 * Can be null for non-procgen systems. Can be a one-system constellation for lone star systems.
	 * @return
	 */
	Constellation getConstellation();
	
	/**
	 * Returns false if the constellation is null or contains only one star system.
	 * @return
	 */
	boolean isInConstellation();
	
	void setConstellation(Constellation constellation);

	void setCenter(SectorEntityToken center);

	void autogenerateHyperspaceJumpPoints(boolean generateEntrancesAtGasGiants, boolean generateFringeJumpPoint, boolean generatePlanetConditions);

	void setProcgen(boolean isProcgen);
	boolean isProcgen();

	StarAge getAge();
	void setAge(StarAge age);
	Boolean hasSystemwideNebula();
	void setHasSystemwideNebula(Boolean hasSystemwideNebula);

	boolean isEnteredByPlayer();
	void setEnteredByPlayer(boolean enteredByPlayer);

	long getLastPlayerVisitTimestamp();

	float getDaysSinceLastPlayerVisit();

	boolean hasPulsar();

	Boolean getDoNotShowIntelFromThisLocationOnMap();
	void setDoNotShowIntelFromThisLocationOnMap(Boolean doNotShowIntelFromThisLocationOnMap);
}
