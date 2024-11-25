package aws;

/*
 * Cloud Computing
 *
 * Dynamic Resource Management Tool
 * using AWS Java SDK Library
 *
 */

import com.amazonaws.services.ec2.model.MonitorInstancesRequest;
import com.amazonaws.services.ec2.model.UnmonitorInstancesRequest;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Scanner;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeRegionsResult;
import com.amazonaws.services.ec2.model.Region;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.DryRunSupportedRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Filter;

public class awsTest {

    static AmazonEC2 ec2;

    private static void init() throws Exception {

        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        ec2 = AmazonEC2ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("ap-southeast-2")    /* check the region at AWS console */
                .build();
    }

    private static String getPublicIpAddress(String instance_id) {
        DescribeInstancesRequest request = new DescribeInstancesRequest()
                .withInstanceIds(instance_id);
        DescribeInstancesResult response = ec2.describeInstances(request);

        String public_ip = response.getReservations().get(0).getInstances().get(0).getPublicIpAddress();
        return public_ip;
    }

    public static void main(String[] args) throws Exception {

        init();

        Scanner menu = new Scanner(System.in);
        Scanner id_string = new Scanner(System.in);
        int number = 0;

        while (true) {
            System.out.println("                                                            ");
            System.out.println("                                                            ");
            System.out.println("------------------------------------------------------------");
            System.out.println("           Amazon AWS Control Panel using SDK               ");
            System.out.println("------------------------------------------------------------");
            System.out.println("  1. list instance                2. available zones        ");
            System.out.println("  3. start instance               4. available regions      ");
            System.out.println("  5. stop instance                6. create instance        ");
            System.out.println("  7. reboot instance              8. list images            ");
            System.out.println("  9. monitor instance            10. unmonitor instance     ");
            System.out.println(" 11. condor status                                          ");
            System.out.println("                                 99. quit                   ");
            System.out.println("------------------------------------------------------------");

            System.out.print("Enter an integer: ");

            if (menu.hasNextInt()) {
                number = menu.nextInt();
            } else {
                System.out.println("concentration!");
                break;
            }

            String instance_id = "";
            String private_key = "";

            switch (number) {
                case 1:
                    listInstances();
                    break;

                case 2:
                    availableZones();
                    break;

                case 3:
                    System.out.print("Enter instance id: ");
                    if (id_string.hasNext()) {
                        instance_id = id_string.nextLine();
                    }

                    if (!instance_id.trim().isEmpty()) {
                        startInstance(instance_id);
                    }
                    break;

                case 4:
                    availableRegions();
                    break;

                case 5:
                    System.out.print("Enter instance id: ");
                    if (id_string.hasNext()) {
                        instance_id = id_string.nextLine();
                    }

                    if (!instance_id.trim().isEmpty()) {
                        stopInstance(instance_id);
                    }
                    break;

                case 6:
                    System.out.print("Enter ami id: ");
                    String ami_id = "";
                    if (id_string.hasNext()) {
                        ami_id = id_string.nextLine();
                    }

                    if (!ami_id.trim().isEmpty()) {
                        createInstance(ami_id);
                    }
                    break;

                case 7:
                    System.out.print("Enter instance id: ");
                    if (id_string.hasNext()) {
                        instance_id = id_string.nextLine();
                    }

                    if (!instance_id.trim().isEmpty()) {
                        rebootInstance(instance_id);
                    }
                    break;

                case 8:
                    listImages();
                    break;

                case 9:
                    System.out.print("Enter instance id: ");
                    if (id_string.hasNext()) {
                        instance_id = id_string.nextLine();
                    }

                    if (!instance_id.trim().isEmpty()) {
                        monitorInstance(instance_id);
                    }
                    break;

                case 10:
                    System.out.print("Enter instance id: ");
                    if (id_string.hasNext()) {
                        instance_id = id_string.nextLine();
                    }

                    if (!instance_id.trim().isEmpty()) {
                        unmonitorInstance(instance_id);
                    }
                    break;

                case 11:
                    System.out.print("Enter instance id: ");
                    if (id_string.hasNext()) {
                        instance_id = id_string.nextLine();
                    }
                    System.out.print("Enter private key path: ");
                    if (id_string.hasNext()) {
                        private_key = id_string.nextLine();
                    }

                    if (!instance_id.trim().isEmpty() && !private_key.trim().isEmpty()) {
                        executeCondorStatus(instance_id, private_key);
                    }
                    break;

                case 99:
                    System.out.println("bye!");
                    menu.close();
                    id_string.close();
                    return;

                default:
                    System.out.println("concentration!");
            }

        }

    }

    public static void listInstances() {

        System.out.println("Listing instances....");
        boolean done = false;

        DescribeInstancesRequest request = new DescribeInstancesRequest();

        while (!done) {
            DescribeInstancesResult response = ec2.describeInstances(request);

            for (Reservation reservation : response.getReservations()) {
                for (Instance instance : reservation.getInstances()) {
                    System.out.printf(
                            "[id] %s, " +
                                    "[AMI] %s, " +
                                    "[type] %s, " +
                                    "[state] %10s, " +
                                    "[monitoring state] %s",
                            instance.getInstanceId(),
                            instance.getImageId(),
                            instance.getInstanceType(),
                            instance.getState().getName(),
                            instance.getMonitoring().getState());
                }
                System.out.println();
            }

            request.setNextToken(response.getNextToken());

            if (response.getNextToken() == null) {
                done = true;
            }
        }
    }

    public static void availableZones() {

        System.out.println("Available zones....");
        try {
            DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();
            Iterator<AvailabilityZone> iterator = availabilityZonesResult.getAvailabilityZones().iterator();

            AvailabilityZone zone;
            while (iterator.hasNext()) {
                zone = iterator.next();
                System.out.printf("[id] %s,  [region] %15s, [zone] %15s\n", zone.getZoneId(), zone.getRegionName(),
                        zone.getZoneName());
            }
            System.out.println("You have access to " + availabilityZonesResult.getAvailabilityZones().size() +
                    " Availability Zones.");

        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage());
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
        }

    }

    public static void startInstance(String instance_id) {

        System.out.printf("Starting .... %s\n", instance_id);
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DryRunSupportedRequest<StartInstancesRequest> dry_request =
                () -> {
                    StartInstancesRequest request = new StartInstancesRequest()
                            .withInstanceIds(instance_id);

                    return request.getDryRunRequest();
                };

        StartInstancesRequest request = new StartInstancesRequest()
                .withInstanceIds(instance_id);

        ec2.startInstances(request);

        System.out.printf("Successfully started instance %s", instance_id);
    }

    public static void availableRegions() {

        System.out.println("Available regions ....");

        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DescribeRegionsResult regions_response = ec2.describeRegions();

        for (Region region : regions_response.getRegions()) {
            System.out.printf(
                    "[region] %15s, " +
                            "[endpoint] %s\n",
                    region.getRegionName(),
                    region.getEndpoint());
        }
    }

    public static void stopInstance(String instance_id) {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DryRunSupportedRequest<StopInstancesRequest> dry_request =
                () -> {
                    StopInstancesRequest request = new StopInstancesRequest()
                            .withInstanceIds(instance_id);

                    return request.getDryRunRequest();
                };

        try {
            StopInstancesRequest request = new StopInstancesRequest()
                    .withInstanceIds(instance_id);

            ec2.stopInstances(request);
            System.out.printf("Successfully stop instance %s\n", instance_id);

        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
        }

    }

    public static void createInstance(String ami_id) {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        RunInstancesRequest run_request = new RunInstancesRequest()
                .withImageId(ami_id)
                .withInstanceType(InstanceType.T2Micro)
                .withMaxCount(1)
                .withMinCount(1);

        RunInstancesResult run_response = ec2.runInstances(run_request);

        String reservation_id = run_response.getReservation().getInstances().get(0).getInstanceId();

        System.out.printf(
                "Successfully started EC2 instance %s based on AMI %s",
                reservation_id, ami_id);

    }

    public static void rebootInstance(String instance_id) {

        System.out.printf("Rebooting .... %s\n", instance_id);

        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        try {
            RebootInstancesRequest request = new RebootInstancesRequest()
                    .withInstanceIds(instance_id);

            ec2.rebootInstances(request);

            System.out.printf(
                    "Successfully rebooted instance %s", instance_id);

        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
        }


    }

    public static void listImages() {
        System.out.println("Listing images....");

        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DescribeImagesRequest request = new DescribeImagesRequest();
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();

        request.getFilters().add(new Filter().withName("name").withValues("aws-htcondor-worker"));
        request.setRequestCredentialsProvider(credentialsProvider);

        DescribeImagesResult results = ec2.describeImages(request);

        for (Image images : results.getImages()) {
            System.out.printf("[ImageID] %s, [Name] %s, [Owner] %s\n",
                    images.getImageId(), images.getName(), images.getOwnerId());
        }
    }

    public static void monitorInstance(String instance_id) {
        System.out.printf("Monitoring .... %s\n", instance_id);

        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DryRunSupportedRequest<MonitorInstancesRequest> dry_request = () -> {
            MonitorInstancesRequest request = new MonitorInstancesRequest()
                    .withInstanceIds(instance_id);

            return request.getDryRunRequest();
        };

        try {
            MonitorInstancesRequest request = new MonitorInstancesRequest()
                    .withInstanceIds(instance_id);

            ec2.monitorInstances(request);

            System.out.printf(
                    "Successfully enabled monitoring for instance %s", instance_id);
        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
        }
    }

    public static void unmonitorInstance(String instance_id) {
        System.out.printf("Unmonitoring .... %s\n", instance_id);

        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DryRunSupportedRequest<UnmonitorInstancesRequest> dry_request = () -> {
            UnmonitorInstancesRequest request = new UnmonitorInstancesRequest()
                    .withInstanceIds(instance_id);

            return request.getDryRunRequest();
        };

        try {
            UnmonitorInstancesRequest request = new UnmonitorInstancesRequest()
                    .withInstanceIds(instance_id);

            ec2.unmonitorInstances(request);

            System.out.printf(
                    "Successfully enabled unmonitoring for instance %s", instance_id);
        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
        }
    }

    public static void executeCondorStatus(String instance_id, String private_key) {
        System.out.println("Executing condor_status on the EC2 instance...");

        String host = getPublicIpAddress(instance_id);
        String user = "ec2-user";
        String command = "condor_status";

        try {
            JSch jsch = new JSch();
            jsch.addIdentity(private_key);

            Session session = jsch.getSession(user, host, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            System.out.println("Connecting to " + host + "...");
            session.connect();

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setErrStream(System.err);

            InputStream in = channel.getInputStream();
            channel.connect();

            System.out.println("\nCommand output:");
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                System.out.print(new String(buffer, 0, read));
            }

            channel.disconnect();
            session.disconnect();
            System.out.println("\n\nCommand executed successfully");
        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
        }
    }
}