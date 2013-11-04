package com.kerio.dashboard.api;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class NotificationGetter {

	private ApiClient client;
	private JSONArray fakeNotification;
	private JSONArray lastNotifications;
	private HashMap<String, Notification> knownNotifications;

	public enum NotificationType {
		Info,
		Error,
		Warning
	};

	public class Notification
	{
		public NotificationType type;
		public String title;
		public String description;

		public Notification(NotificationType type, String title, String description)
		{
			this.type = type;
			this.title = title;
			this.description = description;
		}
	}

	public NotificationGetter(ApiClient client) {
		this.client = client;
		this.fakeNotification = new JSONArray();
		JSONObject not = new JSONObject();
		try {
			not.put("type", "NotificationUpdate");
		} catch (JSONException e) {}
		this.fakeNotification.put(not);

		this.knownNotifications = new HashMap<String, Notification>();
		this.knownNotifications.put("NotificationUpdate", new Notification(NotificationType.Info, "New update is available", "A new version of Kerio Control is ready for update."));
		this.knownNotifications.put("NotificationDump", new Notification(NotificationType.Error, "System fault", "Kerio Control encountered a problem and was restarted. We apologize for the inconvenience."));
		this.knownNotifications.put("NotificationLowMemory", new Notification(NotificationType.Warning, "Low memory warning", "Available memory is lower than Kerio Control requires. We cannot guarantee smooth run of your system and some features may not work."));
		this.knownNotifications.put("NotificationDomains", new Notification(NotificationType.Error, "Problem with connection to domain controler", ""));
		this.knownNotifications.put("NotificationSubWillExpire", new Notification(NotificationType.Info, "The Software Maintenance for this product will expire soon", "Even though the product will continue to function properly after expiration, the Software Maintenance is required for continuous access to important product enhancements and updates, such as antivirus database updates or the latest security patches."));
		this.knownNotifications.put("NotificationSubExpired", new Notification(NotificationType.Info, "The Software Maintenance for this product has expired", "Even though the product will continue to function properly, you will have no access to important product enhancements and updates, such as antivirus database updates or the latest security patches."));
		this.knownNotifications.put("NotificationLicWillExpire", new Notification(NotificationType.Warning, "The license for Kerio Control will expire soon", "When your product license expires, Kerio Control will stop functioning or its features will be limited."));
		this.knownNotifications.put("NotificationLicExpired", new Notification(NotificationType.Warning, "The license for Kerio Control has expired.", "Your product license has expired. Kerio Control functionality is limited."));
		this.knownNotifications.put("NotificationBackupLine", new Notification(NotificationType.Warning, "Backup line active", "Internet connectivity through the primary line has failed. The backup line is currently used for Internet connection."));
		this.knownNotifications.put("NotificationInterfaceSpeed", new Notification(NotificationType.Warning, "Bandwidth management must know link speed", "There is a bandwidth management rule with bandwidth reservation or percentual speed limit. In such case, it is necessary to set the link speed of the Internet connection to make the bandwidth management work properly."));
		this.knownNotifications.put("NotificationSmtp", new Notification(NotificationType.Warning, "No SMTP server is defined", "One or more features of Kerio Control is configured to send email messages but no SMTP server is defined."));
		this.knownNotifications.put("NotificationLlbLine", new Notification(NotificationType.Warning, "Internet line failure", "One of the lines used for load balancing has failed."));
		this.knownNotifications.put("NotificationLlb", new Notification(NotificationType.Warning, "Dial-up line compatibility problem", "Due to a problem in the TCP/IP implementation in Windows 7 and Windows Server 2008 R2, Kerio Control is unable to perform link load balancing and connection failover using dial-up lines properly."));
		this.knownNotifications.put("NotificationConnectionOnDemand", new Notification(NotificationType.Warning, "Duplicate default gateway was detected", "Kerio Control is configured for on-demand dialing. However it has been detected that there are gateways configured on other interfaces than the one designed for dialing. Dialing on demand cannot be performed."));
		this.knownNotifications.put("NotificationConnectionFailover", new Notification(NotificationType.Warning, "A problem with default gateway was detected", "Kerio Control has detected that there are default gateways configured on other interfaces than those configured for Internet connection failover. This is often wrong as the default gateway should be typically left blank on any other interfaces."));
		this.knownNotifications.put("NotificationConnectionBalancing", new Notification(NotificationType.Warning, "A problem with default gateway was detected", "Kerio Control has detected that there are default gateways configured on other interfaces than those configured for load balancing. This is often wrong as the default gateway should be typically left blank on any other interfaces."));
		this.knownNotifications.put("NotificationConnectionPersistent", new Notification(NotificationType.Warning, "Duplicate default gateway was detected", "Kerio Control has detected that multiple default gateways are configured on the machine. This is often wrong as the default gateway should be typically configured only on the interface that is connected to the Internet. On any other interfaces it should be left blank."));
		this.knownNotifications.put("NotificationCertificateError", new Notification(NotificationType.Error, "Non-existing certificate is used", ""));
		this.knownNotifications.put("NotificationCertificateWillExpire", new Notification(NotificationType.Error, "Certificate is expiring", ""));
		this.knownNotifications.put("NotificationCertificateExpired", new Notification(NotificationType.Error, "Certificate has expired", ""));
		this.knownNotifications.put("NotificationCaWillExpire", new Notification(NotificationType.Error, "Certification Authority is expiring", ""));
		this.knownNotifications.put("NotificationCaExpired", new Notification(NotificationType.Error, "Certification Authority has expired", ""));
		this.knownNotifications.put("NotificationBackupFailed", new Notification(NotificationType.Warning, "Backup failed", "Automatic configuration backup failed with error message \"%1\"."));
		this.knownNotifications.put("NotificationPacketDump", new Notification(NotificationType.Warning, "Packet dump in progress", ""));
	}

	public HashMap<String, Notification> getAllNotifications() {

		HashMap<String, Notification> result = null;

		JSONObject arguments = new JSONObject();
		try {
			arguments.put("lastNotifications", this.fakeNotification);
			arguments.put("timeout", 1);
		} catch (JSONException e) {
			return result;
		}

		JSONObject ret = client.exec("Notifications.get", arguments);
		if (ret == null) {
			return result;
		}

		try
		{
			JSONArray nn = ret.getJSONArray("notifications");
			if (nn.length() == 0) {
				return result;
			}

			lastNotifications = nn;

			result = new HashMap<String, Notification>();

			for (int i = 0; i < lastNotifications.length(); ++i) {
				JSONObject notificationInfo = lastNotifications.getJSONObject(i);
				String key = lastNotifications.getString(i);

				String type = notificationInfo.getString("type");
				String value = notificationInfo.getString("value");

				Notification notification = this.createNotification(type, value);

				result.put(key, notification);
			}
		}
		catch (JSONException e) {
		}

		return result;
	}

	private Notification createNotification(String type, String value) {
		if ( ! this.knownNotifications.containsKey(type)) {
			return null;
		}
		Notification notification = this.knownNotifications.get(type);

		if (!value.isEmpty()) {
			notification.description = notification.description.replace("%1", value);
		}
		return notification;
	}
}
