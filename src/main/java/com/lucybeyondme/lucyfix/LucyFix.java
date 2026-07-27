package com.lucybeyondme.lucyfix;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LucyFix implements ModInitializer {

    public static final String MOD_ID = "lucyfix";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Keep startup work minimal; gameplay changes are applied by Mixins.
        LOGGER.info("LucyFix 1.0.2 loaded.");
    }
}
