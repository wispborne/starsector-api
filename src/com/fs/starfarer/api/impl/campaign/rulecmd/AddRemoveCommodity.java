package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.loading.AbilitySpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.MutableValue;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	AddRemoveCommodity <commodity id> <quantity> <withText>
 */
public class AddRemoveCommodity extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		String commodityId = params.get(0).getString(memoryMap);
		float quantity = 0;
		int next = 2;
		if (params.get(1).isOperator()) {
			quantity = -1 * params.get(2).getFloat(memoryMap);
			next = 3;
		} else {
			quantity = params.get(1).getFloat(memoryMap);
		}
		boolean withText = dialog != null && params.size() >= next + 1 && params.get(next).getBoolean(memoryMap) && Math.abs(quantity) >= 1;
		
		if (commodityId.equals("credits")) {
			MutableValue credits = Global.getSector().getPlayerFleet().getCargo().getCredits();
			if (quantity > 0) {
				credits.add(quantity);
				if (withText) {
					addCreditsGainText((int) quantity, dialog.getTextPanel());
				}
			} else {
				credits.subtract(Math.abs(quantity));
				if (credits.get() < 0) credits.set(0);
				if (withText) {
					addCreditsLossText((int) Math.abs(quantity), dialog.getTextPanel());
				}
			}
		} else {
			if (quantity > 0) {
				Global.getSector().getPlayerFleet().getCargo().addCommodity(commodityId, quantity);
				if (withText) {
					addCommodityGainText(commodityId, (int) quantity, dialog.getTextPanel());
				}
			} else {
				Global.getSector().getPlayerFleet().getCargo().removeCommodity(commodityId, Math.abs(quantity));
				if (withText) {
					addCommodityLossText(commodityId, (int) Math.abs(quantity), dialog.getTextPanel());
				}
			}
		}
		
		
		// update $supplies, $fuel, etc if relevant
		updatePlayerMemoryQuantity(commodityId);
		
		return true;
	}
	
	public static void updatePlayerMemoryQuantity(String commodityId) {
		MemoryAPI memory = Global.getSector().getCharacterData().getMemory();
		String key ="$" + commodityId;
		if (memory.contains(key)) {
			if (memory.get(key) instanceof Integer || memory.get(key) instanceof Float) {
				memory.set(key, (int)Global.getSector().getPlayerFleet().getCargo().getCommodityQuantity(commodityId), 0);
			}
		}
	}

	
	public static void addStackGainText(CargoStackAPI stack, TextPanelAPI text) {
		addStackGainText(stack, text, false);
	}
	public static void addStackGainText(CargoStackAPI stack, TextPanelAPI text, boolean lowerCase) {
		if (stack.getSize() < 1) return;
		text.setFontSmallInsignia();
		String name = stack.getDisplayName();
		if (lowerCase) {
			name = name.toLowerCase();
		}
		int quantity = (int) stack.getSize();
		text.addParagraph("Gained " + quantity + Strings.X + " " + name + "", Misc.getPositiveHighlightColor());
		text.highlightInLastPara(Misc.getHighlightColor(), "" + (int) quantity + Strings.X);
		text.setFontInsignia();
	}
	
	public static void addCommodityGainText(String commodityId, int quantity, TextPanelAPI text) {
		CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(commodityId);
		text.setFontSmallInsignia();
//		String units = quantity == 1 ? "unit" : "units";
//		text.addParagraph("Gained " + (int) quantity + " " + units + " of " + spec.getName().toLowerCase() + "", Misc.getPositiveHighlightColor());
		String name = spec.getLowerCaseName();
		//boolean special = Commodities.SURVEY_DATA.equals(spec.getDemandClass());
		//if (!special) name = name.toLowerCase();
		text.addParagraph("Gained " + (int) quantity + Strings.X + " " + name + "", Misc.getPositiveHighlightColor());
		text.highlightInLastPara(Misc.getHighlightColor(), "" + (int) quantity + Strings.X);
		text.setFontInsignia();
	}
	
	public static void addCommodityLossText(String commodityId, int quantity, TextPanelAPI text) {
		CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(commodityId);
		text.setFontSmallInsignia();
//		String units = quantity == 1 ? "unit" : "units";
//		text.addParagraph("Lost " + (int) quantity + " " + units + " of " + spec.getName().toLowerCase() + "", Misc.getNegativeHighlightColor());
		String name = spec.getLowerCaseName();
		//boolean special = Commodities.SURVEY_DATA.equals(spec.getDemandClass());
		//if (!special) name = name.toLowerCase();
		text.addParagraph("Lost " + (int) quantity + Strings.X + " " + name + "", Misc.getNegativeHighlightColor());
		text.highlightInLastPara(Misc.getHighlightColor(), "" + (int) quantity + Strings.X);
		text.setFontInsignia();
	}
	
	public static void addCreditsGainText(int credits, TextPanelAPI text) {
		text.setFontSmallInsignia();
		String str = Misc.getWithDGS(credits) + Strings.C;
		text.addParagraph("Gained " + str + "", Misc.getPositiveHighlightColor());
		text.highlightInLastPara(Misc.getHighlightColor(), str);
		text.setFontInsignia();
		
		if (Global.getCurrentState() == GameState.CAMPAIGN) {
			CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
			MemoryAPI memory = Global.getSector().getCharacterData().getMemoryWithoutUpdate();
			memory.set("$credits", (int)fleet.getCargo().getCredits().get(), 0);
			memory.set("$creditsStr", Misc.getWithDGS((int)fleet.getCargo().getCredits().get()), 0);
			memory.set("$creditsStrC", Misc.getWithDGS((int)fleet.getCargo().getCredits().get()) + Strings.C, 0);
		}
	}
	
	public static void addCreditsLossText(int credits, TextPanelAPI text) {
		text.setFontSmallInsignia();
		String str = Misc.getWithDGS(credits) + Strings.C;
		text.addParagraph("Lost " + str + "", Misc.getNegativeHighlightColor());
		text.highlightInLastPara(Misc.getHighlightColor(), str);
		text.setFontInsignia();
		
		if (Global.getCurrentState() == GameState.CAMPAIGN) {
			CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
			MemoryAPI memory = Global.getSector().getCharacterData().getMemoryWithoutUpdate();
			memory.set("$credits", (int)fleet.getCargo().getCredits().get(), 0);
			memory.set("$creditsStr", Misc.getWithDGS((int)fleet.getCargo().getCredits().get()), 0);
			memory.set("$creditsStrC", Misc.getWithDGS((int)fleet.getCargo().getCredits().get()) + Strings.C, 0);
		}
	}
	
	
	public static void addAbilityGainText(String abilityId, TextPanelAPI text) {
		AbilitySpecAPI ability = Global.getSettings().getAbilitySpec(abilityId);
		text.setFontSmallInsignia();
		String str = "\"" + ability.getName() + "\"";
		text.addParagraph("Gained ability: " + str + "", Misc.getPositiveHighlightColor());
		text.highlightInLastPara(Misc.getHighlightColor(), str);
		text.setFontInsignia();
	}
	
	
	public static void addOfficerGainText(PersonAPI officer, TextPanelAPI text) {
		text.setFontSmallInsignia();
		String rank = officer.getRank();
		if (rank != null) {
			rank = Misc.ucFirst(rank);
		}
		String str = officer.getName().getFullName();
		if (rank != null) str = rank + " " + str;
		text.addParagraph(str + " has joined your fleet", Misc.getPositiveHighlightColor());
		text.highlightInLastPara(Misc.getHighlightColor(), str);
		text.setFontInsignia();
	}
	
	public static void addAdminGainText(PersonAPI admin, TextPanelAPI text) {
		text.setFontSmallInsignia();
//		String rank = admin.getRank();
//		if (rank != null) {
//			rank = Misc.ucFirst(rank);
//		}
		String rank = "Administrator";
		String str = admin.getName().getFullName();
		if (rank != null) str = rank + " " + str;
		text.addParagraph(str + " has entered your service", Misc.getPositiveHighlightColor());
		text.highlightInLastPara(Misc.getHighlightColor(), str);
		text.setFontInsignia();
	}
	
	
	public static void addFleetMemberGainText(FleetMemberAPI member, TextPanelAPI text) {
		text.setFontSmallInsignia();
		String str = member.getShipName() + ", " + member.getVariant().getHullSpec().getHullNameWithDashClass() + " " + member.getVariant().getHullSpec().getDesignation(); 
		text.addParagraph("Acquired " + str + "", Misc.getPositiveHighlightColor());
		text.highlightInLastPara(Misc.getHighlightColor(), str);
		text.setFontInsignia();
	}
	
	public static void addFleetMemberGainText(ShipVariantAPI variant, TextPanelAPI text) {
		text.setFontSmallInsignia();
		String str = variant.getHullSpec().getHullNameWithDashClass() + " " + variant.getHullSpec().getDesignation(); 
		text.addParagraph("Acquired " + str + "", Misc.getPositiveHighlightColor());
		text.highlightInLastPara(Misc.getHighlightColor(), str);
		text.setFontInsignia();
	}
}



