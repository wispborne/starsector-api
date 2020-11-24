package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RuleBasedDialog;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;


/**
 * SetActiveMission $missionEventHandle
 */
public class ReinitDialog extends BaseCommandPlugin {
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (!(dialog.getPlugin() instanceof RuleBasedDialog)) return false;
		
		((RuleBasedDialog) dialog.getPlugin()).reinit(true);
		
		return true;
	}

}




