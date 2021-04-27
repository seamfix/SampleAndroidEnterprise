import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.androidmanagement.v1.AndroidManagement;
import com.google.api.services.androidmanagement.v1.model.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainApplication {

    private static AndroidManagement androidManagementClient  = null;

    /** The JSON credential file for the service account. */
    private static final String SERVICE_ACCOUNT_CREDENTIAL_FILE =
            "/Users/jeffemuveyan/Downloads/veyanjeffmdmproject-ac800f100ad3.json";

    /** The OAuth scope for the Android Management API. */
    private static final String OAUTH_SCOPE =
            "https://www.googleapis.com/auth/androidmanagement";

    /** The name of this app. */
    private static final String APP_NAME = "Android Management API sample app";


    public static void main(String []args) throws IOException, GeneralSecurityException {
        androidManagementClient = getAndroidManagementClient();

        /*List<Device> devices = listDevices("enterprises/LC04k2e918");
        for (Device device : devices) {
            System.out.println("Found device with name: " + device.getName());
        }

        // If there are any devices, reboot one.
        if (devices.isEmpty()) {
            System.out.println("No devices found.");
        } else {
            System.out.println("Rebooting a device...");
            rebootDevice(devices.get(0));
        }*/

        Policy p = new Policy()
                .setApplications(
                        Collections.singletonList(
                                new ApplicationPolicy()
                                        .setPackageName("com.seamfix.bioregistra")
                                        .setInstallType("FORCE_INSTALLED")
                                        .setDefaultPermissionPolicy("GRANT")
                                        .setLockTaskAllowed(true))).setFactoryResetDisabled(false)

                .setKeyguardDisabled(true)
                .setStatusBarDisabled(false);

        androidManagementClient
                .enterprises()
                .policies()
                .patch("enterprises/LC04k2e918"+ "/policies/" + "samplePolicy", p)
                .execute();
    }


    /** Builds an Android Management API client. */
    private static AndroidManagement getAndroidManagementClient()
            throws IOException, GeneralSecurityException {
        try (FileInputStream input = new FileInputStream(SERVICE_ACCOUNT_CREDENTIAL_FILE)) {
            GoogleCredential credential =
                    GoogleCredential.fromStream(input)
                            .createScoped(Collections.singleton(OAUTH_SCOPE));
            return new AndroidManagement.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential)
                    .setApplicationName(APP_NAME)
                    .build();
        }
    }

    /** Sets the policy of the given id to the given value. */
    private void setPolicy(String enterpriseName, String policyId, Policy policy)
            throws IOException {
        System.out.println("Setting policy...");
        String name = enterpriseName + "/policies/" + policyId;
        androidManagementClient
                .enterprises()
                .policies()
                .patch(name, policy)
                .execute();
    }


    /** Gets a Policy for a COSU device. */
    private Policy getCosuPolicy() {
        List<String> categories = new ArrayList<>();
        categories.add("android.intent.category.HOME");
        categories.add("android.intent.category.DEFAULT");

        return new Policy()
                .setApplications(
                        Collections.singletonList(
                                new ApplicationPolicy()
                                        .setPackageName("com.seamfix.bioregistra")
                                        .setInstallType("FORCE_INSTALLED")
                                        .setDefaultPermissionPolicy("GRANT")
                                        .setLockTaskAllowed(true)))
                .setPersistentPreferredActivities(
                        Collections.singletonList(
                                new PersistentPreferredActivity()
                                        .setReceiverActivity("com.seamfix.bioregistra")
                                        .setActions(
                                                Collections.singletonList("android.intent.action.MAIN"))
                                        .setCategories(categories)))
                .setKeyguardDisabled(true)
                .setStatusBarDisabled(true);
    }


    /** Lists the first page of devices for an enterprise. */
    private static List<Device> listDevices(String enterpriseName) throws IOException {
        System.out.println("Listing devices...");
        ListDevicesResponse response =
                androidManagementClient
                        .enterprises()
                        .devices()
                        .list(enterpriseName)
                        .execute();
        return response.getDevices() == null
                ? new ArrayList<Device>() : response.getDevices();
    }

    /** Reboots a device. Note that reboot only works on Android N+. */
    private static void rebootDevice(Device device) throws IOException {
        System.out.println(
                "Sending reboot command to " + device.getName() + "...");
        Command command = new Command().setType("REBOOT");
        androidManagementClient
                .enterprises()
                .devices()
                .issueCommand(device.getName(), command)
                .execute();
    }
}
