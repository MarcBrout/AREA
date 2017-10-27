
package eu.epitech;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class ActionGCalendar extends AAction {
    private String lastSyncToken = null;
    private DateTime lastSyncDate = null;
    private ArrayList<JSONObject> eventsStore = new ArrayList<>();
    private static List<String> fields = ImmutableList.of(
            "start", "timezone", "end", "creator", "description", "location", "summary");
    static private Map<String, FieldType> requiredConfigFields = ImmutableMap.of("email", FieldType.EMAIL);

    // Application Name
    private static final String APPLICATION_NAME = "THE AREA";

    // Directory to store user credentials for this application.
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.dir"), ".credentials/calendar-java-quickstart");

    // Global instance of the {@link FileDataStoreFactory}.
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    // Global instance of the JSON factory.
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    // Global instance of the HTTP transport.
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/calendar-java-quickstart
     */
    private static final List<String> SCOPES = Arrays.asList(CalendarScopes.CALENDAR_READONLY);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    private static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in = ActionGCalendar.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =  new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
        Credential credential =
                new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        System.out.println("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Calendar client service.
     * @return an authorized Calendar client service
     * @throws IOException
     */
    private static com.google.api.services.calendar.Calendar getCalendarService() throws IOException {
        Credential credential = authorize();
        return new com.google.api.services.calendar.Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Events fullSync(Calendar calendar) throws Exception {
        DateTime now = new DateTime(System.currentTimeMillis());
        return calendar.events().list("primary")
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
    }

    private Events partialSync(Calendar calendar) throws Exception {
        return calendar.events().list("primary")
                .setSyncToken(lastSyncToken)
                .setSingleEvents(true)
                .execute();
    }

    private Events syncCalendar(Calendar calendar) throws Exception {
        try {
            return lastSyncToken == null ? fullSync(calendar) : partialSync(calendar);
        } catch (GoogleJsonResponseException e) {
            lastSyncToken = null;
            fullSync(calendar);
            return null;
        } catch (Exception e) {
            Logger.error(e.getMessage());
            return null;
        }
    }

    private JSONObject translate(Event e) {
        JSONObject json = new JSONObject();

        DateTime start = e.getStart().getDateTime();
        DateTime end = e.getEnd().getDateTime();

        json.put("start", start == null ? e.getStart().getDate() : start);
        json.put("timezone", e.getStart().getTimeZone());
        json.put("end", end == null ? e.getEnd().getDate() : end);
        json.put("creator", e.getCreator().getDisplayName());
        json.put("description", e.getDescription());
        json.put("location", e.getLocation());
        json.put("summary", e.getSummary());
        return json;
    }

    private boolean process() {
        Calendar calendar;
        try {
            calendar = getCalendarService();
        } catch (Exception e) {
            Logger.error(e.getMessage());
            return false;
        }

        String pageToken;
        Events events;
        boolean actionFound = false;

        do {
            try {
                events = syncCalendar(calendar);
            } catch (Exception e) {
                Logger.error(e.getMessage());
                return false;
            }

            if (events == null) {
                Logger.info("An Error has occurred during the Google Calendar syncing");
                return false;
            }

            List<Event> items = events.getItems();
            if (items.size() == 0)
                return false;

            for (Event event : items) {
                if (isNewEvent(event))
                {
                    eventsStore.add(translate(event));
                    actionFound = true;
                }
            }
            pageToken = events.getNextPageToken();
        } while (pageToken != null);

        lastSyncToken = events.getNextSyncToken();
        return actionFound;
    }

    private boolean isNewEvent(Event event) {
        return event.getCreated().getValue() > lastSyncDate.getValue();
    }

    @Override
    public boolean hasHappened() {
        lastSyncDate = new DateTime(System.currentTimeMillis());
        return process();
    }

    @Override
    public List<JSONObject> whatHappened() {
        return eventsStore;
    }

    @Override
    public Map<String, FieldType> configFields() {
        return requiredConfigFields;
    }

    @Override
    public List<String> returnedFields() {
        return fields;
    }
}