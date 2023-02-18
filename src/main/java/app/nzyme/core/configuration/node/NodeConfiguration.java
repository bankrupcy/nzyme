package app.nzyme.core.configuration.node;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import app.nzyme.core.Role;
import app.nzyme.core.alerts.Alert;
import app.nzyme.core.alerts.service.callbacks.AlertCallback;
import app.nzyme.core.configuration.*;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Path;


@AutoValue
public abstract class NodeConfiguration {

    public abstract boolean versionchecksEnabled();
    public abstract boolean fetchOuis();

    public abstract Role role();

    public abstract String adminPasswordHash();

    public abstract String databasePath();

    public abstract String pythonExecutable();
    public abstract String pythonScriptDirectory();
    public abstract String pythonScriptPrefix();

    public abstract URI restListenUri();
    public abstract URI httpExternalUri();

    public abstract String pluginDirectory();

    public abstract String cryptoDirectory();

    public abstract String ntpServer();

    @Nullable
    public abstract InetSocketAddress remoteInputAddress();

    public abstract ImmutableList<UplinkDefinition> uplinks();

    public abstract ImmutableList<Dot11MonitorDefinition> dot11Monitors();
    public abstract ImmutableList<Dot11NetworkDefinition> dot11Networks();
    public abstract ImmutableList<Dot11TrapDeviceDefinition> dot11TrapDevices();

    public abstract ImmutableList<Alert.TYPE_WIDE> dot11Alerts();
    public abstract int alertingTrainingPeriodSeconds();

    public abstract ImmutableList<AlertCallback> alertCallbacks();

    public abstract ImmutableList<ForwarderDefinition> forwarders();

    @Nullable
    public abstract UplinkDeviceConfiguration groundstationDevice();

    @Nullable
    public abstract ReportingConfiguration reporting();

    @Nullable
    public abstract DeauthenticationMonitorConfiguration deauth();

    public ImmutableList<String> ourSSIDs() {
        ImmutableList.Builder<String> ssids = new ImmutableList.Builder<>();
        dot11Networks().forEach(n -> ssids.add(n.ssid()));
        return ssids.build();
    }

    public static NodeConfiguration create(boolean versionchecksEnabled, boolean fetchOuis, Role role, String adminPasswordHash, String databasePath, String pythonExecutable, String pythonScriptDirectory, String pythonScriptPrefix, URI restListenUri, URI httpExternalUri, String pluginDirectory, String cryptoDirectory, String ntpServer, InetSocketAddress remoteInputAddress, ImmutableList<UplinkDefinition> uplinks, ImmutableList<Dot11MonitorDefinition> dot11Monitors, ImmutableList<Dot11NetworkDefinition> dot11Networks, ImmutableList<Dot11TrapDeviceDefinition> dot11TrapDevices, ImmutableList<Alert.TYPE_WIDE> dot11Alerts, int alertingTrainingPeriodSeconds, ImmutableList<AlertCallback> alertCallbacks, ImmutableList<ForwarderDefinition> forwarders, UplinkDeviceConfiguration groundstationDevice, ReportingConfiguration reporting, DeauthenticationMonitorConfiguration deauth) {
        return builder()
                .versionchecksEnabled(versionchecksEnabled)
                .fetchOuis(fetchOuis)
                .role(role)
                .adminPasswordHash(adminPasswordHash)
                .databasePath(databasePath)
                .pythonExecutable(pythonExecutable)
                .pythonScriptDirectory(pythonScriptDirectory)
                .pythonScriptPrefix(pythonScriptPrefix)
                .restListenUri(restListenUri)
                .httpExternalUri(httpExternalUri)
                .pluginDirectory(pluginDirectory)
                .cryptoDirectory(cryptoDirectory)
                .ntpServer(ntpServer)
                .remoteInputAddress(remoteInputAddress)
                .uplinks(uplinks)
                .dot11Monitors(dot11Monitors)
                .dot11Networks(dot11Networks)
                .dot11TrapDevices(dot11TrapDevices)
                .dot11Alerts(dot11Alerts)
                .alertingTrainingPeriodSeconds(alertingTrainingPeriodSeconds)
                .alertCallbacks(alertCallbacks)
                .forwarders(forwarders)
                .groundstationDevice(groundstationDevice)
                .reporting(reporting)
                .deauth(deauth)
                .build();
    }

    @Nullable
    public Dot11NetworkDefinition findNetworkDefinition(String bssid, String ssid) {
        for (Dot11NetworkDefinition network : dot11Networks()) {
            if (network.allBSSIDAddresses().contains(bssid) && network.ssid().equals(ssid)) {
                return network;
            }
        }

        return null;
    }

    @Nullable
    public Dot11BSSIDDefinition findBSSIDDefinition(String bssid, String ssid) {
        Dot11NetworkDefinition networkDefinition = findNetworkDefinition(bssid, ssid);
        if (networkDefinition != null) {
            for (Dot11BSSIDDefinition dot11BSSIDDefinition : networkDefinition.bssids()) {
                if (dot11BSSIDDefinition.address().equals(bssid)) {
                    return dot11BSSIDDefinition;
                }
            }
        }

        return null;
    }

    public static Builder builder() {
        return new AutoValue_NodeConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder versionchecksEnabled(boolean versionchecksEnabled);

        public abstract Builder fetchOuis(boolean fetchOuis);

        public abstract Builder role(Role role);

        public abstract Builder adminPasswordHash(String adminPasswordHash);

        public abstract Builder databasePath(String databasePath);

        public abstract Builder pythonExecutable(String pythonExecutable);

        public abstract Builder pythonScriptDirectory(String pythonScriptDirectory);

        public abstract Builder pythonScriptPrefix(String pythonScriptPrefix);

        public abstract Builder restListenUri(URI restListenUri);

        public abstract Builder httpExternalUri(URI httpExternalUri);

        public abstract Builder pluginDirectory(String pluginDirectory);

        public abstract Builder cryptoDirectory(String cryptoDirectory);

        public abstract Builder ntpServer(String ntpServer);

        public abstract Builder remoteInputAddress(InetSocketAddress remoteInputAddress);

        public abstract Builder uplinks(ImmutableList<UplinkDefinition> uplinks);

        public abstract Builder dot11Monitors(ImmutableList<Dot11MonitorDefinition> dot11Monitors);

        public abstract Builder dot11Networks(ImmutableList<Dot11NetworkDefinition> dot11Networks);

        public abstract Builder dot11TrapDevices(ImmutableList<Dot11TrapDeviceDefinition> dot11TrapDevices);

        public abstract Builder dot11Alerts(ImmutableList<Alert.TYPE_WIDE> dot11Alerts);

        public abstract Builder alertingTrainingPeriodSeconds(int alertingTrainingPeriodSeconds);

        public abstract Builder alertCallbacks(ImmutableList<AlertCallback> alertCallbacks);

        public abstract Builder forwarders(ImmutableList<ForwarderDefinition> forwarders);

        public abstract Builder groundstationDevice(UplinkDeviceConfiguration groundstationDevice);

        public abstract Builder reporting(ReportingConfiguration reporting);

        public abstract Builder deauth(DeauthenticationMonitorConfiguration deauth);

        public abstract NodeConfiguration build();
    }

}