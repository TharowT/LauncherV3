/*
 * This file is part of The Technic Launcher Version 3.
 * Copyright ©2015 Syndicate, LLC
 *
 * The Technic Launcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Technic Launcher  is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Technic Launcher.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.tharow.tantalum.launcher;

import com.beust.jcommander.JCommander;
import net.tharow.tantalum.autoupdate.IBuildNumber;
import net.tharow.tantalum.autoupdate.Relauncher;
import net.tharow.tantalum.autoupdate.http.HttpUpdateStream;
import net.tharow.tantalum.launcher.autoupdate.CommandLineBuildNumber;
import net.tharow.tantalum.launcher.autoupdate.TechnicRelauncher;
import net.tharow.tantalum.launcher.autoupdate.VersionFileBuildNumber;
import net.tharow.tantalum.launcher.io.*;
import net.tharow.tantalum.launcher.launch.Installer;
import net.tharow.tantalum.launcher.settings.SettingsFactory;
import net.tharow.tantalum.launcher.settings.StartupParameters;
import net.tharow.tantalum.launcher.settings.TantalumSettings;
import net.tharow.tantalum.launcher.settings.migration.IMigrator;
import net.tharow.tantalum.launcher.settings.migration.InitialV3Migrator;
import net.tharow.tantalum.launcher.ui.InstallerFrame;
import net.tharow.tantalum.launcher.ui.LauncherFrame;
import net.tharow.tantalum.launcher.ui.LoginFrame;
import net.tharow.tantalum.launcher.ui.components.discover.DiscoverInfoPanel;
import net.tharow.tantalum.launcher.ui.components.modpacks.ModpackSelector;
import net.tharow.tantalum.launchercore.TantalumConstants;
import net.tharow.tantalum.launchercore.auth.IUserType;
import net.tharow.tantalum.launchercore.auth.UserModel;
import net.tharow.tantalum.launchercore.exception.DownloadException;
import net.tharow.tantalum.launchercore.image.ImageRepository;
import net.tharow.tantalum.launchercore.image.face.MinotarFaceImageStore;
import net.tharow.tantalum.launchercore.image.face.WebAvatarImageStore;
import net.tharow.tantalum.launchercore.install.LauncherDirectories;
import net.tharow.tantalum.launchercore.install.ModpackInstaller;
import net.tharow.tantalum.launchercore.launch.java.JavaVersionRepository;
import net.tharow.tantalum.launchercore.launch.java.source.FileJavaSource;
import net.tharow.tantalum.launchercore.launch.java.source.InstalledJavaSource;
import net.tharow.tantalum.launchercore.logging.BuildLogFormatter;
import net.tharow.tantalum.launchercore.logging.RotatingFileHandler;
import net.tharow.tantalum.launchercore.modpacks.ModpackModel;
import net.tharow.tantalum.launchercore.modpacks.PackLoader;
import net.tharow.tantalum.launchercore.modpacks.resources.PackImageStore;
import net.tharow.tantalum.launchercore.modpacks.resources.PackResourceMapper;
import net.tharow.tantalum.launchercore.modpacks.resources.resourcetype.BackgroundResourceType;
import net.tharow.tantalum.launchercore.modpacks.resources.resourcetype.IModpackResourceType;
import net.tharow.tantalum.launchercore.modpacks.resources.resourcetype.IconResourceType;
import net.tharow.tantalum.launchercore.modpacks.resources.resourcetype.LogoResourceType;
import net.tharow.tantalum.launchercore.modpacks.sources.IAuthoritativePackSource;
import net.tharow.tantalum.launchercore.modpacks.sources.IInstalledPackRepository;
import net.tharow.tantalum.minecraftcore.launch.MinecraftLauncher;
import net.tharow.tantalum.launchercore.auth.TantalumAuthenticator;
import net.tharow.tantalum.platform.IPlatformApi;
import net.tharow.tantalum.platform.IPlatformSearchApi;
import net.tharow.tantalum.platform.PlatformPackInfoRepository;
import net.tharow.tantalum.platform.cache.ModpackCachePlatformApi;
import net.tharow.tantalum.platform.http.HttpPlatformApi;
import net.tharow.tantalum.platform.http.HttpPlatformSearchApi;
import net.tharow.tantalum.platform.io.AuthorshipInfo;
import net.tharow.tantalum.solder.ISolderApi;
import net.tharow.tantalum.solder.SolderPackSource;
import net.tharow.tantalum.solder.cache.CachedSolderApi;
import net.tharow.tantalum.solder.http.HttpSolderApi;
import net.tharow.tantalum.ui.components.Console;
import net.tharow.tantalum.ui.components.ConsoleFrame;
import net.tharow.tantalum.ui.components.ConsoleHandler;
import net.tharow.tantalum.ui.components.LoggerOutputStream;
import net.tharow.tantalum.ui.controls.installation.SplashScreen;
import net.tharow.tantalum.ui.lang.ResourceLoader;
import net.tharow.tantalum.utilslib.JavaUtils;
import net.tharow.tantalum.utilslib.OperatingSystem;
import net.tharow.tantalum.utilslib.Utils;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LauncherMain {

    public static ConsoleFrame consoleFrame;

    public static final Locale[] supportedLanguages = new Locale[] {
            Locale.ENGLISH,
            new Locale("pt","BR"),
            new Locale("pt","PT"),
            new Locale("cs"),
            Locale.GERMAN,
            Locale.FRENCH,
            Locale.ITALIAN,
            new Locale("hu"),
            new Locale("pl"),
            Locale.CHINA,
            Locale.TAIWAN,
            new Locale("nl", "NL"),
            new Locale("sk"),
    };

    private static IBuildNumber buildNumber;

    public static void main(String[] argv) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            Utils.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        }

        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

        StartupParameters params = new StartupParameters(argv);
        try {
            JCommander.newBuilder()
                    .addObject(params)
                    .build()
                    .parse(argv);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        TantalumSettings settings = null;

        try {
            settings = SettingsFactory.buildSettingsObject(Relauncher.getRunningPath(LauncherMain.class), params.isMover());
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }

        if (settings == null) {
            ResourceLoader installerResources = new ResourceLoader(null, "net","tharow","tantalum","launcher","resources");
            installerResources.setSupportedLanguages(supportedLanguages);
            installerResources.setLocale(ResourceLoader.DEFAULT_LOCALE);
            InstallerFrame dialog = new InstallerFrame(installerResources, params);
            dialog.setVisible(true);
            return;
        }

        LauncherDirectories directories = new TechnicLauncherDirectories(settings.getTechnicRoot());
        ResourceLoader resources = new ResourceLoader(directories, "net","tharow","tantalum","launcher","resources");
        resources.setSupportedLanguages(supportedLanguages);
        resources.setLocale(settings.getLanguageCode());

        // Sanity check
        checkIfRunningInsideOneDrive(directories.getLauncherDirectory());

        if (params.getBuildNumber() != null && !params.getBuildNumber().isEmpty()) {
            buildNumber = new CommandLineBuildNumber(params);
        } else {
            buildNumber = new VersionFileBuildNumber(resources);
        }

        TantalumConstants.setBuildNumber(buildNumber);

        setupLogging(directories, resources);

        //Currently Unused
        //String launcherBuild = buildNumber.getBuildNumber();
        int build = -1;

        try {
            build = Integer.parseInt((new VersionFileBuildNumber(resources)).getBuildNumber());
        } catch (NumberFormatException ex) {
            //This is probably a debug build or something, build number is invalid
        }
        Utils.getLogger().info("Current Build Number is " + build);
        // These 2 need to happen *before* the launcher or the updater run, so we have valuable debug information, and so
        // we can properly use websites that use Let's Encrypt (and other current certs not supported by old Java versions)
        runStartupDebug();
        injectNewRootCerts();
        //startLauncher(settings, params, directories, resources);
        Relauncher launcher = new TechnicRelauncher(new HttpUpdateStream("https://tantalum-auth.azurewebsites.net/platform/"), settings.getBuildStream()+"4", build, directories, resources, params);
        try {
            if (launcher.runAutoUpdater())
                startLauncher(settings, params, directories, resources);
        } catch (InterruptedException e) {
            //Canceled by user
        } catch (DownloadException e) {
            //JOptionPane.showMessageDialog(null, resources.getString("launcher.updateerror.download", pack.getDisplayName(), e.getMessage()), resources.getString("launcher.installerror.title"), JOptionPane.WARNING_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static void checkIfRunningInsideOneDrive(File launcherRoot) {
        if (OperatingSystem.getOperatingSystem() != OperatingSystem.WINDOWS) {
            return;
        }

        Path launcherRootPath = launcherRoot.toPath();

        for (String varName : new String[]{"OneDrive", "OneDriveConsumer"}) {
            String varValue = System.getenv(varName);
            if (varValue == null || varValue.isEmpty()) {
                continue;
            }

            Path oneDrivePath = new File(varValue).toPath();

            if (launcherRootPath.startsWith(oneDrivePath)) {
                JOptionPane.showMessageDialog(null, "Technic Launcher cannot run inside OneDrive. Please move it out of OneDrive, in the launcher settings.", "Cannot run inside OneDrive", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void setupLogging(LauncherDirectories directories, ResourceLoader resources) {
        System.out.println("Setting up logging");
        final Logger logger = Utils.getLogger();
        File logDirectory = new File(directories.getLauncherDirectory(), "logs");
        if (!logDirectory.exists()) {
            try{
                if(logDirectory.mkdir()){
                    System.out.println("Logging dir made");
                } else {
                    System.out.println("Logging dir Couldn't be made");
                }

            } catch (SecurityException e){
                throw new SecurityException("Security manager is Active and prevented log dir from being made" + e);
            }

        }
        File logs = new File(logDirectory, "techniclauncher_%D.log");
        RotatingFileHandler fileHandler = new RotatingFileHandler(logs.getPath());

        fileHandler.setFormatter(new BuildLogFormatter(buildNumber.getBuildNumber()));

        for (Handler h : logger.getHandlers()) {
            logger.removeHandler(h);
        }
        logger.addHandler(fileHandler);
        logger.setUseParentHandlers(false);

        LauncherMain.consoleFrame = new ConsoleFrame(2500, resources.getImage("icon.png"));
        Console console = new Console(LauncherMain.consoleFrame, buildNumber.getBuildNumber());

        logger.addHandler(new ConsoleHandler(console));

        System.setOut(new PrintStream(new LoggerOutputStream(console, Level.INFO, logger), true));
        System.setErr(new PrintStream(new LoggerOutputStream(console, Level.SEVERE, logger), true));

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Unhandled Exception in " + t, e);
        });
    }

    private static void runStartupDebug() {
        // Startup debug messages
        Utils.getLogger().info("OS: " + System.getProperty("os.name").toLowerCase(Locale.ENGLISH));
        Utils.getLogger().info("Identified as "+ OperatingSystem.getOperatingSystem().getName());
        Utils.getLogger().info("Java: " + System.getProperty("java.version") + " " + JavaUtils.getJavaBitness() + "-bit (" + System.getProperty("os.arch") + ")");
        final String[] domains = {"minecraft.net", "session.minecraft.net", "textures.minecraft.net", "libraries.minecraft.net", "authserver.mojang.com", "account.mojang.com", "technicpack.net", "launcher.technicpack.net", "api.technicpack.net", "mirror.technicpack.net", "solder.technicpack.net", "files.minecraftforge.net"};
        for (String domain : domains) {
            try {
                Collection<InetAddress> inetAddresses = Arrays.asList(InetAddress.getAllByName(domain));
                String ips = inetAddresses.stream().map(InetAddress::getHostAddress).collect(Collectors.joining(", "));
                Utils.getLogger().info(domain + " resolves to [" + ips + "]");
            } catch (UnknownHostException ex) {
                Utils.getLogger().log(Level.SEVERE, "Failed to resolve " + domain + ": " + ex);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static void injectNewRootCerts() {
        // Adapted from Forge installer
        final String javaVersion = System.getProperty("java.version");
        if (javaVersion == null || !javaVersion.startsWith("1.8.0_")) {
            Utils.getLogger().log(Level.INFO, "Don't need to inject new root certificates: Java is newer than 8 (" + javaVersion + ")");
            return;
        }

        try {
            if (Integer.parseInt(javaVersion.substring("1.8.0_".length())) >= 101) {
                Utils.getLogger().log(Level.INFO, "Don't need to inject new root certificates: Java 8 is 101+ (" + javaVersion + ")");
                return;
            }
        } catch (final NumberFormatException e) {
            Utils.getLogger().log(Level.WARNING,
                    "Couldn't parse Java version, can't inject new root certs",
                    e);
            return;
        }

        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            final Path ksPath = Paths.get(System.getProperty("java.home"),"lib", "security", "cacerts");
            keyStore.load(Files.newInputStream(ksPath), "changeit".toCharArray());
            Map<String, Certificate> jdkTrustStore = Collections.list(keyStore.aliases()).stream().collect(Collectors.toMap(a -> a, (String alias) -> {
                try {
                    return keyStore.getCertificate(alias);
                } catch (KeyStoreException e) {
                    Utils.getLogger().log(Level.WARNING, "Failed to get certificate", e);
                    return null;
                }
            }));

            KeyStore leKS = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream leKSFile = LauncherMain.class.getResourceAsStream("/net/tharow/tantalum/launcher/resources/technickeystore.jks");
            leKS.load(leKSFile, "technicrootca".toCharArray());
            Map<String, Certificate> leTrustStore = Collections.list(leKS.aliases()).stream().collect(Collectors.toMap(a -> a, (String alias) -> {
                try {
                    return leKS.getCertificate(alias);
                } catch (KeyStoreException e) {
                    Utils.getLogger().log(Level.WARNING, "Failed to get certificate", e);
                    return null;
                }
            }));

            KeyStore mergedTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            mergedTrustStore.load(null, new char[0]);
            for (final Map.Entry<String, Certificate> entry : jdkTrustStore.entrySet()) {
                mergedTrustStore.setCertificateEntry(entry.getKey(), entry.getValue());
            }
            for (final Map.Entry<String , Certificate> entry : leTrustStore.entrySet()) {
                mergedTrustStore.setCertificateEntry(entry.getKey(), entry.getValue());
            }

            TrustManagerFactory instance = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            instance.init(mergedTrustStore);
            SSLContext tls = SSLContext.getInstance("TLS");
            tls.init(null, instance.getTrustManagers(), null);
            HttpsURLConnection.setDefaultSSLSocketFactory(tls.getSocketFactory());
            Utils.getLogger().log(Level.INFO, "Injected new root certificates");
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | KeyManagementException e) {
            Utils.getLogger().log(Level.WARNING, "Failed to inject new root certificates. Problems might happen");
            e.printStackTrace();
        }
    }

    private static void startLauncher(final TantalumSettings settings, StartupParameters startupParameters, final LauncherDirectories directories, ResourceLoader resources) {
        UIManager.put( "ComboBox.disabledBackground", LauncherFrame.COLOR_FORMELEMENT_INTERNAL );
        UIManager.put( "ComboBox.disabledForeground", LauncherFrame.COLOR_GREY_TEXT );
        System.setProperty("xr.load.xml-reader", "org.ccil.cowan.tagsoup.Parser");

        //Remove all log files older than a week
        new Thread(() -> {
            Iterator<File> files = FileUtils.iterateFiles(new File(directories.getLauncherDirectory(), "logs"), new String[] {"log"}, false);
            while (files.hasNext()) {
                File logFile = files.next();
                if (logFile.exists() && (new DateTime(logFile.lastModified())).isBefore(DateTime.now().minusWeeks(1)))
                    //noinspection ResultOfMethodCallIgnored
                    logFile.delete();
            }
        }).start();

        final SplashScreen splash = new SplashScreen(resources.getImage("launch_splash.png"), 0);
        Color bg = LauncherFrame.COLOR_FORMELEMENT_INTERNAL;
        splash.getContentPane().setBackground(new Color (bg.getRed(),bg.getGreen(),bg.getBlue(),255));
        splash.pack();
        splash.setLocationRelativeTo(null);
        splash.setVisible(true);

        JavaVersionRepository javaVersions = new JavaVersionRepository();
        (new InstalledJavaSource()).enumerateVersions(javaVersions);
        FileJavaSource javaVersionFile = FileJavaSource.load(new File(settings.getTechnicRoot(), "javaVersions.json"));
        javaVersionFile.enumerateVersions(javaVersions);
        javaVersions.selectVersion(settings.getJavaVersion(), settings.getJavaBitness());

        TantalumUserStore users = TantalumUserStore.load(new File(directories.getLauncherDirectory(),"users.json"));
        TantalumAuthenticator tantalumAuthenticator = new TantalumAuthenticator(users.getClientToken());
        UserModel userModel = new UserModel(users, tantalumAuthenticator);

        IModpackResourceType iconType = new IconResourceType();
        IModpackResourceType logoType = new LogoResourceType();
        IModpackResourceType backgroundType = new BackgroundResourceType();

        PackResourceMapper iconMapper = new PackResourceMapper(directories, resources.getImage("icon.png"), iconType);
        ImageRepository<ModpackModel> iconRepo = new ImageRepository<>(iconMapper, new PackImageStore(iconType));
        ImageRepository<ModpackModel> logoRepo = new ImageRepository<>(new PackResourceMapper(directories, resources.getImage("modpack/ModImageFiller.png"), logoType), new PackImageStore(logoType));
        ImageRepository<ModpackModel> backgroundRepo = new ImageRepository<>(new PackResourceMapper(directories, null, backgroundType), new PackImageStore(backgroundType));

        ImageRepository<IUserType> skinRepo = new ImageRepository<>(new TechnicFaceMapper(directories, resources), new MinotarFaceImageStore("https://minotar.net/"));

        ImageRepository<AuthorshipInfo> avatarRepo = new ImageRepository<>(new TantalumAvatarMapper(directories, resources), new WebAvatarImageStore());

        HttpSolderApi httpSolder = new HttpSolderApi(settings.getClientId());
        ISolderApi solder = new CachedSolderApi(directories, httpSolder, 60 * 60);

        HttpPlatformApi httpPlatform = new HttpPlatformApi("https://tantalum-auth.azurewebsites.net/platform/", buildNumber.getBuildNumber());
        Utils.getLogger().log(Level.INFO, buildNumber.getBuildNumber());
        IPlatformApi platform = new ModpackCachePlatformApi(httpPlatform, 60 * 60, directories);
        IPlatformSearchApi platformSearch = new HttpPlatformSearchApi("https://tantalum-auth.azurewebsites.net/platform/", buildNumber.getBuildNumber());

        IInstalledPackRepository packStore = TechnicInstalledPackStore.load(new File(directories.getLauncherDirectory(), "installedPacks"));
        IAuthoritativePackSource packInfoRepository = new PlatformPackInfoRepository(platform, solder);


        ArrayList<IMigrator> migrators = new ArrayList<>(1);
        migrators.add(new InitialV3Migrator(platform));
        SettingsFactory.migrateSettings(settings, packStore, directories, users, migrators);

        PackLoader packList = new PackLoader(directories, packStore, packInfoRepository);
        ModpackSelector selector = new ModpackSelector(resources, packList, new SolderPackSource("https://example.com/", solder), solder, platform, platformSearch, iconRepo);
        selector.setBorder(BorderFactory.createEmptyBorder());
        userModel.addAuthListener(selector);

        resources.registerResource(selector);

        DiscoverInfoPanel discoverInfoPanel = new DiscoverInfoPanel(resources, startupParameters.getDiscoverUrl(), platform, directories, selector);

        MinecraftLauncher launcher = new MinecraftLauncher(platform, directories, userModel, javaVersions, buildNumber);
        //noinspection rawtypes
        ModpackInstaller modpackInstaller = new ModpackInstaller(platform, settings.getClientId());
        Installer installer = new Installer(startupParameters, directories, modpackInstaller, launcher, settings, iconMapper);

        final LauncherFrame frame = new LauncherFrame(resources, skinRepo, userModel, settings, selector, iconRepo, logoRepo, backgroundRepo, installer, avatarRepo, platform, directories, packStore, startupParameters, discoverInfoPanel, javaVersions, javaVersionFile, buildNumber);
        userModel.addAuthListener(frame);

        ActionListener listener = e -> {
            splash.dispose();
            if (settings.getLaunchToModpacks())
                frame.selectTab("modpacks");
        };

        discoverInfoPanel.setLoadListener(listener);

        LoginFrame login = new LoginFrame(resources, settings, userModel, skinRepo);
        userModel.addAuthListener(login);
        userModel.addAuthListener(user -> {
            if (user == null)
                splash.dispose();
        });

        userModel.startupAuth();

        Utils.sendTracking("runLauncher", "run", buildNumber.getBuildNumber(), settings.getClientId());
    }
}
