package com.fs.starfarer.api.impl.campaign.missions.hub;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class TriggerFleetAssignmentAI implements EveryFrameScript {

	protected CampaignFleetAPI fleet;
	protected LocationAPI system;
	
	protected List<SectorEntityToken> patrolPoints = new ArrayList<SectorEntityToken>();
	protected float elapsedWaitingForDespawn = 0f;
	protected HubMission mission;
	protected String patrolText;
	protected String travelText;
	
	public TriggerFleetAssignmentAI(String travelText, String patrolText, HubMission mission, LocationAPI system, boolean randomLocation, CampaignFleetAPI fleet, SectorEntityToken ... patrolPoints) {
		this.travelText = travelText;
		this.patrolText = patrolText;
		this.mission = mission;
		this.fleet = fleet;
		this.system = system;
		
		if (patrolPoints != null) {
			for (SectorEntityToken curr : patrolPoints) {
				if (curr == null) continue;
				if (curr == fleet) continue;
				this.patrolPoints.add(curr);
			}
		}
		
		if (!fleet.hasScriptOfClass(MissionFleetAutoDespawn.class)) {
			fleet.addScript(new MissionFleetAutoDespawn(mission, fleet));
		}
		
		// moving this to CreateFleetAction, since most mission fleets are likely to want to ignore WarSimScript
		//fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_BUSY, true);
		
		giveInitialAssignments(randomLocation);
	}
	
	protected void giveInitialAssignments(boolean randomLocation) {
		if (randomLocation) {
			// start at random location
			SectorEntityToken target = pickPatrolPoint();
			if (target != null) {
				Vector2f loc = Misc.getPointAtRadius(target.getLocation(), target.getRadius() + 100f);
				fleet.setLocation(loc.x, loc.y);
			} else {
				Vector2f loc = Misc.getPointAtRadius(new Vector2f(), 5000f);
				fleet.setLocation(loc.x, loc.y);
			}
		}
		pickNext();
	}

	protected SectorEntityToken pickPatrolPoint() {
		if (patrolPoints != null) {
			Random random = null;
			if (mission instanceof BaseHubMission) random = ((BaseHubMission)mission).getGenRandom();
			WeightedRandomPicker<SectorEntityToken> picker = new WeightedRandomPicker<SectorEntityToken>(random);
			for (SectorEntityToken curr : patrolPoints) {
				if (!curr.isAlive()) continue;
				picker.addAll(patrolPoints);
			}
			return picker.pick();
		}
		return null;
	}
	
	protected SectorEntityToken currTarget;
	protected void pickNext() {
		currTarget = pickPatrolPoint();
		if (currTarget != null) {
			float speed = Misc.getSpeedForBurnLevel(8);
			float dist = Misc.getDistance(fleet.getLocation(), currTarget.getLocation());
			float seconds = dist / speed;
			float days = seconds / Global.getSector().getClock().getSecondsPerDay();
			days += 5f + 5f * (float) Math.random();
			fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, currTarget, days, patrolText == null ? "patrolling" : patrolText);
			return;
		} else if (system instanceof StarSystemAPI) {
			float days = 5f + 5f * (float) Math.random();
			fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, ((StarSystemAPI)system).getCenter(), days, patrolText == null ? "patrolling" : patrolText);
		}
	}

	public void advance(float amount) {
		if (fleet.getCurrentAssignment() == null) {
			pickNext();
		} else {
			String travel = travelText;
			if (travel == null) {
				if (Misc.isPatrol(fleet)) {
					travel = "patrolling";
				} else {
					travel = "traveling";
				}
			}
			if (fleet.getAI() != null && 
					travel != null && currTarget != null && fleet.getCurrentAssignment().getTarget() == currTarget &&
					fleet.getCurrentAssignment().getAssignment() == FleetAssignment.PATROL_SYSTEM) {
				float dist = Misc.getDistance(fleet, currTarget);
				if (dist > 1500 || fleet.getContainingLocation() != currTarget.getContainingLocation()) {
					boolean standingDown = fleet.getAI() instanceof ModularFleetAIAPI &&
					 			((ModularFleetAIAPI) fleet.getAI()).getTacticalModule() != null &&
					 			((ModularFleetAIAPI) fleet.getAI()).getTacticalModule().isStandingDown();
					if (standingDown) {
						fleet.getAI().setActionTextOverride(null);
					} else {
						fleet.getAI().setActionTextOverride(travel);
					}
				} else {
					fleet.getAI().setActionTextOverride(null);
				}
			} else if (fleet.getAI() != null) {
				fleet.getAI().setActionTextOverride(null);
			}
		}
		
		// replaced with separate MissionFleetAutoDespawn script
		//despawnIfNeeded(amount);
	}
	
//	protected void despawnIfNeeded(float amount) {
//		if (isMissionEnded()) {
//			if (!fleet.isInCurrentLocation()) {
//				elapsedWaitingForDespawn += Global.getSector().getClock().convertToDays(amount);
//				if (elapsedWaitingForDespawn > 30f && fleet.getBattle() == null) {
//					fleet.despawn(FleetDespawnReason.PLAYER_FAR_AWAY, null);
//					elapsedWaitingForDespawn = 0f;
//				}
//			} else {
//				elapsedWaitingForDespawn = 0f;
//			}
//		}
//	}
	
	public boolean isMissionEnded() {
		return mission instanceof IntelInfoPlugin && ((IntelInfoPlugin)mission).isEnded();
	}

	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}
	
	

}










