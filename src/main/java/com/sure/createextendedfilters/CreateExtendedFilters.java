package com.sure.createextendedfilters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sure.createextendedfilters.network.ModNetwork;
import com.sure.createextendedfilters.registry.ModDataComponents;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(CreateExtendedFilters.MOD_ID)
public class CreateExtendedFilters {
	public static final String MOD_ID = "createextendedfilters";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public CreateExtendedFilters(IEventBus modBus) {
		ModDataComponents.register(modBus);
		ModNetwork.register(modBus);
	}
}
