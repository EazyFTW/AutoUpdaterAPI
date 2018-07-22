package com.gamerking195.dev.autoupdaterapi;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.maximvdw.spigotsite.SpigotSiteCore;
import be.maximvdw.spigotsite.api.SpigotSiteAPI;
import be.maximvdw.spigotsite.api.user.User;
import com.gamerking195.dev.autoupdaterapi.util.UtilDownloader;
import com.gamerking195.dev.autoupdaterapi.util.UtilSpigotCreds;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.logging.LogFactory;
import org.bstats.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


/*
 * AutoUpdaterAPI
 *
 * Author: flogic
 *
 * This resource is licensed under the Apache License Version 2.0.
 * Full license information in the LICENSE file.
 *
 * This resource uses code from resources developed by Ryan "rylinaux" Clancy (UtilPlugin.java) and Maxim Van de Wynckel (Maximvdw).
 * Any other dependencies used can be found in the pom.xml.
 *
 */


public final class AutoUpdaterAPI
        extends JavaPlugin {

    private static AutoUpdaterAPI instance;
    private Logger log;

    public static AutoUpdaterAPI getInstance() {
        return instance;
    }

    @Getter
    private SpigotSiteAPI api;

    @Getter
    @Setter
    private WebClient webClient;

    @Getter
    @Setter
    private User currentUser;

    @Getter
    @Setter
    private boolean debug = false;

    @Getter
    private static String fileSeperator;

    private Metrics metrics;

    public void onEnable() {
        /*
         * General setup.
         */

        instance = this;
        fileSeperator = FileSystems.getDefault().getSeparator();
        log = getLogger();

        /*
         * Setup local files
         */

        UtilSpigotCreds.getInstance().init();

        /*
         * HTML Unit (Using the OSGi classifier as to be compatible with MaximVdW's spigot site api)
         */

        UtilDownloader.downloadLib(UtilDownloader.Library.HTMMLUNIT);

        File htmlUnit = new File(getDataFolder().getParentFile().getPath()+fileSeperator+"MVdWPlugin"+fileSeperator+"lib"+fileSeperator+"htmlunit_2_15.jar");
        if (htmlUnit.exists()) {
            if (htmlUnit.length() < 11000000) {
                printPluginError("Error occurred while enabling the plugin.", "Error occurred while locating dependencies, it is likely that the dependency download has failed.\nTry deleting the MVdWPlugin directory (if it exists) and trying again.\nIf it fails again try contacting me via spigot @flogic, and/or contacting your hosting provider about internet speed/ram.");
                log.severe("DISABLING AUTOUPDATERAPI!!!");
                this.setEnabled(false);
                return;
            }
        } else {
            printPluginError("Error occurred while enabling the plugin.", "Error occurred while locating dependencies, it is likely that the dependency download has failed.\nTry deleting the MVdWPlugin directory (if it exists) and trying again.\nIf it fails again try contacting me via spigot @flogic, and/or contacting your hosting provider about internet speed/ram.");
            log.severe("DISABLING AUTOUPDATERAPI!!!");
            this.setEnabled(false);
            return;
        }

        try {
            LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log",
                    "org.apache.commons.logging.impl.NoOpLog");

            java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
        } catch (Exception ex) {
            printError(ex, "Unable to turn off HTML unit logging!.");
        }

        //Setup web client
        webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setTimeout(15000);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

        log.info("Initializing connection with spigot...");

        /*
         * Spigot Site API
         */

        new BukkitRunnable() {
            @Override
            public void run() {
                api = new SpigotSiteCore();

                UtilSpigotCreds.getInstance().updateKeys();

                try {
                    if (UtilSpigotCreds.getInstance().getUsername() != null && UtilSpigotCreds.getInstance().getPassword() != null) {
                        log.info("Stored credentials detected, attempting login.");
                        new PremiumUpdater(null, instance, 1, new UpdateLocale(), false, false).authenticate(false);
                    }
                } catch (Exception ex) {
                    printError(ex, "Error occurred while initializing the spigot site API.");
                }
            }
        }.runTaskAsynchronously(instance);

        /*
         * Statistics
         */

        metrics = new Metrics(instance);

        log.info("AutoUpdaterAPI V" + getDescription().getVersion() + " enabled!");
    }

    public void resetUser() {
        AutoUpdaterAPI.getInstance().setCurrentUser(null);
        UtilSpigotCreds.getInstance().reset();
    }

    public void printError(Exception ex) {
        this.log.severe("A severe error has occurred with AutoUpdaterAPI.");
        this.log.severe("If you cannot figure out this error on your own (e.g. a config error) please copy and paste everything from here to END ERROR and post it at https://github.com/fl0gic/AutoUpdaterAPI/issues.");
        this.log.severe("");
        this.log.severe("============== BEGIN ERROR ==============");
        this.log.severe("PLUGIN VERSION: AutoUpdaterAPI V" + getDescription().getVersion());
        this.log.severe("");
        this.log.severe("MESSAGE: " + ex.getMessage());
        this.log.severe("");
        this.log.severe("STACKTRACE: ");
        ex.printStackTrace();
        this.log.severe("");
        this.log.severe("============== END ERROR ==============");
    }

    public void printError(Exception ex, String extraInfo) {
        this.log.severe("A severe error has occurred with AutoUpdaterAPI.");
        this.log.severe("If you cannot figure out this error on your own (e.g. a config error) please copy and paste everything from here to END ERROR and post it at https://github.com/fl0gic/AutoUpdaterAPI/issues.");
        this.log.severe("");
        this.log.severe("============== BEGIN ERROR ==============");
        this.log.severe("PLUGIN VERSION: AutoUpdaterAPI V" + getDescription().getVersion());
        this.log.severe("");
        this.log.severe("PLUGIN MESSAGE: " + extraInfo);
        this.log.severe("");
        this.log.severe("MESSAGE: " + ex.getMessage());
        this.log.severe("");
        this.log.severe("STACKTRACE: ");
        ex.printStackTrace();
        this.log.severe("");
        this.log.severe("============== END ERROR ==============");
    }

    public void printPluginError(String header, String message) {
        this.log.severe("============== BEGIN ERROR ==============");
        this.log.severe(header);
        this.log.severe("");
        this.log.severe("PLUGIN VERSION: AutoUpdaterAPI V" + getDescription().getVersion());
        this.log.severe("");
        this.log.severe("PLUGIN MESSAGE: " + message);
        this.log.severe("");
        this.log.severe("============== END ERROR ==============");
    }
}