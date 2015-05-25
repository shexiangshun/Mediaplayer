
package com.jamdeo.tv.vod.player.thirdparty;

import java.util.Map;
import android.content.Context;
import android.util.Log;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.content.ContentResolver;
import android.os.SystemProperties;



/**
 *  The Class VodSourcePlayerHelper is used to provide APIs for
 *  external player to interwork with VOD application.
 */
public class VodSourcePlayerHelper {
    private final static boolean DEBUG = true;
    private static final String TAG = "VodSourcePlayerHelper";
    private static final String VOD_SOURCE = "com.jamdeo.vod_source";
    private static String vod_source  = SystemProperties.get(VOD_SOURCE, "");
    private static Context mContext;
    private static SOURCE mSource;

    /**
     *  It is used to define the extra parameters name transferred between
     *  VOD application and 3rd party player.
     */
    public static final class JamdeoUri {
        /** content uri */
        public static final Uri CONTENT_URI = (vod_source=="" || vod_source==null) ?
            Uri.parse("content://com.hisense.data.vod/external_player_action") :
            Uri.parse("content://com.jamdeo.data.vod/external_player_action");

        /** action */
        public static final String ACTION = "action";

        /** played video source */
        public static final String SOURCE = "source";

        /** played video program id */
        public static final String PROGRAM_ID = "programSeriesId";

        /** played video episode id */
        public static final String EPISODE_ID = "episodeId";

        /** played video resolution */
        public static final String RESOLUTION = "resolution";

        /** played video playback position */
        public static final String POSITION = "position";

        /** played video playback percentage */
        public static final String PERCENTAGE = "percentage";

        /** logging event */
        public static final String EVENT = "event";

        /** logging event timeslot */
        public static final String TIMESLOT = "ts";

        /** logging event start timeslot */
        public static final String START_TIMESLOT = "s_ts";

        /** logging event end timeslot */
        public static final String END_TIMESLOT = "e_ts";

        /** logging event old resolution*/
        public static final String OLD_RESOLUTION = "old_resolution";

        /** logging event new resolution*/
        public static final String NEW_RESOLUTION = "new_resolution";
        
        /**logging event price*/ 

        public static final String PRICE = "price";
        
        public static final String PAY_TYPE = "payType";
        
        public static final String CONCERT_ID = "concertId";
        
        public static final String PAYMENT_ID = "paymentId";
        
        public static final String PLAY_TYPE = "playType";
        
        public static final String ERROR_CODE = "error_code";
        
        public static final String ERROR_EXTRA = "error_extra";
        
        public static final String ERROR_MESSAGE = "message";     
        
        public static final String LICENCE = "licence"; 
        
        public static final String RESOURCE_TYPE = "resourceType";  
        
        public static final String PAY_RESULT = "payResult";

    }


    /**
     *  EVENT is used to define the event values for specific actions.
     */
    public enum EVENT{
        /** Video Start Event. */
        VIDEO_START("video_start"),

        /** Video Seek Event. */
        VIDEO_SEEK("video_seek"),

        /** Video Buffering Event. */
        VIDEO_BUFFERING("video_buff"),

        /** Video Exit Event. */
        VIDEO_EXIT("video_exit"),

        /** Video End Event. */
        VIDEO_END("video_end"),

        /** Video Error. */
        VIDEO_ERROR("video_error"),

        /** Video Resolution Change Event. */
        VIDEO_RESOLUTION_CHANGE("video_resolution_change"),
        
        /**Video payed*/
        VIDEO_PAYED("ui_third_buy");

        private final String event;

        private EVENT(String event) {
            this.event = event;
        }

        public String getValue() {
            return event;
        }
    }

    /**
     *  SOURCE is used to define the source(content provider) names.
     */
    public enum SOURCE {
        /** Sohu */
        SOHU("SOHU"),

        /** Lekan */
        LEKAN("LEKAN"),

        /** IQIYI */
        IQIYI("IQIYI"),

        /** PPTV */
        PPTV("PPTV"),

        /** KU6 */
        KU6("KU6"),

        /** CNTV */
        CNTV("CNTV"),

        /** VOOLE */
        VOOLE("VOOLE"),

        /** TENCENT */
        TENCENT("TENCENT"),

        /**YOUKU*/
        YOUKU("YOUKU"),

        /**IFENG*/
        IFENG("IFENG"),
        
        /**WASU*/
        WASU("WASU"), 
        
        AD("AD"), 
        
        LETV("LETV");

        private final String source;

        private SOURCE(String source) {
            this.source = source;
        }

        public String getValue() {
            return source;
        }
    }

    /**
     *  MapKey is used to define the key names of the map,
     *  which is transferred from external player to VOD
     *  application.
     */
    public enum MapKey {
        /** program id */
        PROGRAM_ID("programId"),

        /** episode id */
        EPISODE_ID("episodeId"),

        /** resolution */
        RESOLUTION("resolution"),

        /** timeslot */
        TIMESLOT("timeslot"),

        /** start timeslot */
        START_TIMESLOT("s_timeslot"),

        /** end timeslot */
        END_TIMESLOT("e_timeslot"),

        /** old resolution */
        OLD_RESOLUTION("old_resolution"),

        /** new resolution */
        NEW_RESOLUTION("new_resolution"),

        /** position*/
        POSITION("position"),
        
        /**price*/
        PRICE("price"),
        
        /**pay type*/
        PAY_TYPE("payType"),
        
        CONCERT_ID("concertId"),
        
        PAYMENT_ID("paymentId"),
        
        PLAY_TYPE("playType"),
        
        ERROR_CODE("error_code"),
        
        ERROR_EXTRA("error_extra"),
        
        LICENCE("licence"),
        
        RSOURCE_TYPE("resourceType"),
        
        PAY_RESULT("payResult"),
        
        ERROR_MESSAGE("message");

        private final String key;

        private MapKey(String key) {
            this.key = key;
        }
    }

    /**
     *  Extras is used to define the extra parameters which are
     *  transferred from VOD application to 3rd party player when
     *  starting external player.
     */
    public static final class Extras {

        /** Program Id. */
        public static final String PROGRAM_ID = "program_id";

        /** Episode Id. */
        public static final String EPISODE_ID = "episode_id";

        /** Episode Resolution. */
        public static final String EPISODE_RESOLUTION = "episode_resolution";

        /** Last Video Position Information. */
        public static final String LAST_POSITION_INFO = "last_position_info";

        /** Episode Action URL. */
        public static final String EPISODE_ACTION_URL = "episode_action_url";

        /** Video Name. */
        public static final String VIDEO_NAME = "video_name";

        /** Favorite. */
        public static final String FAVORITE = "isCollect";
    }

    /**
     *   Update specific video playback history.
     *
     *   @param context the 3rd party application/activity context
     *   @param programId the program id value which is from VOD application.
     *   @param episodeId the episode id of content video which is from VOD application.
     *   @param lastPosition the position of last play.
     *   @param percentage the percentage of last play.
     **/
    public static void tagPlaybackHistory(Context context, String programId, String episodeId,
        String lastPosition, String percentage)
    {
        mContext = context;

        // The Uri is as following:
        //   content://com.jamdeo.data.vod/external_player_action?
        //   action=history&source=JamdeoCloud&programSeriesId=760400&episodeId=1166774
        //   &position=123456&percentage=1
        Uri EXTERNAL_PLAYER_ACTION_HISTORY_URI = JamdeoUri.CONTENT_URI
            .buildUpon()
            .appendQueryParameter(JamdeoUri.ACTION, "history")
            .appendQueryParameter(JamdeoUri.SOURCE, "JamdeoCloud")
            .appendQueryParameter(JamdeoUri.PROGRAM_ID, programId)
            .appendQueryParameter(JamdeoUri.EPISODE_ID, episodeId)
            .appendQueryParameter(JamdeoUri.POSITION, lastPosition)
            .appendQueryParameter(JamdeoUri.PERCENTAGE, percentage)
            .build();

        if (DEBUG) {
            Log.d(TAG, "tagPlaybackHistory(): " + EXTERNAL_PLAYER_ACTION_HISTORY_URI);
        }
        new UpdateCursorTask(EXTERNAL_PLAYER_ACTION_HISTORY_URI).execute();
    }


    /**
	 * Execute journal report of the player actions from external player to VOD
	 * application.
	 *
	 * The content of values for associated events are as following:
	 * <br>
	 *
	 * <br>
	 * event: VIDEO_START <br>
	 * map: <br>
	 * PROGRAM_ID, xxxx <br>
	 * EPISODE_ID, xxxx <br>
	 * RESOLUTION, xxxx <br>
	 * TIMESLOT, xxxx
	 *
	 * <br>
	 * <br>
	 * event: VIDEO_SEEK <br>
	 * map: <br>
	 * PROGRAM_ID, xxxx <br>
	 * EPISODE_ID, xxxx <br>
	 * RESOLUTION, xxxx <br>
	 * STAET_TIMESLOT, xxxx <br>
	 * END_TIMESLOT, xxxx
	 *
	 * <br>
	 * <br>
	 * event: VIDEO_BUFFERING <br>
	 * map: <br>
	 * PROGRAM_ID, xxxx <br>
	 * EPISODE_ID, xxxx <br>
	 * RESOLUTION, xxxx <br>
	 * STAET_TIMESLOT, xxxx <br>
	 * END_TIMESLOT, xxxx
	 *
	 * <br>
	 * <br>
	 * event: VIDEO_EXIT <br>
	 * map: <br>
	 * PROGRAM_ID, xxxx <br>
	 * EPISODE_ID, xxxx <br>
	 * RESOLUTION, xxxx <br>
	 * POSITION, xxxx <br>
	 * TIMESLOT, xxxx
	 *
	 * <br>
	 * <br>
	 * event: VIDEO_ERROR <br>
	 * map: <br>
	 * PROGRAM_ID, xxxx <br>
	 * EPISODE_ID, xxxx <br>
	 * RESOLUTION, xxxx <br>
	 * POSITION, xxxx <br>
	 * TIMESLOT, xxxx
	 *
	 * <br>
	 * <br>
	 * event: VIDEO_END <br>
	 * map: <br>
	 * PROGRAM_ID, xxxx <br>
	 * EPISODE_ID, xxxx <br>
	 * RESOLUTION, xxxx <br>
	 * TIMESLOT, xxxx
	 *
	 * <br>
	 * <br>
	 * event: VIDEO_RESOLUTION_CHANGE <br>
	 * map: <br>
	 * PROGRAM_ID, xxxx <br>
	 * EPISODE_ID, xxxx <br>
	 * RESOLUTION, xxxx <br>
	 * OLD_RESOLUTION, xxxx <br>
	 * NEW_RESOLUTION, xxxx
	 *
	 * @param context
	 *            the 3rd party application/activity context
	 * @param source
	 *            content provider value.
	 * @param event
	 *            journal action.
	 * @param values
	 *            content of the journal.
	 *
	 **/
    public static void journalReport(Context context, SOURCE source, EVENT event, Map<MapKey, String> values)
    {
        mContext = context;
        mSource = source;

        if (event.equals(EVENT.VIDEO_START)) {
            journalVideo(EVENT.VIDEO_START, values);
        } else if (event.equals(EVENT.VIDEO_SEEK)) {
            journalDoubleTimeStamp(EVENT.VIDEO_SEEK, values);
        } else if (event.equals(EVENT.VIDEO_BUFFERING)) {
            journalDoubleTimeStamp(EVENT.VIDEO_BUFFERING, values);
        } else if (event.equals(EVENT.VIDEO_EXIT)) {
            journalVideoExit(EVENT.VIDEO_EXIT, values);
        } else if (event.equals(EVENT.VIDEO_END)) {
            journalVideo(EVENT.VIDEO_END, values);
        } else if (event.equals(EVENT.VIDEO_RESOLUTION_CHANGE)) {
            journalResolutionChange(EVENT.VIDEO_RESOLUTION_CHANGE, values);
        } else if (event.equals(EVENT.VIDEO_ERROR)) {
            journalVideoError(EVENT.VIDEO_ERROR, values);
        } else if(event.equals(EVENT.VIDEO_PAYED)){
        	 journalVideoPayed(EVENT.VIDEO_PAYED, values);
        }
    }

    // logging journal with one time stamp
    private static void journalSingleTimeStamp(EVENT event, Map<MapKey, String> values)
    {
        // The Uri is as following:
        //   content://com.jamdeo.data.vod/external_player_action?
        //   action=logging&source=CNTV&event=xxxx&programSeriesId=760400&episodeId=1166774
        //   &resolution=SD&ts=xxxx&concertId|payType|paymentId|playType
        Uri JOURNAL_URI = buildJournalUriPrefix(event, values);
        JOURNAL_URI = JOURNAL_URI
            .buildUpon()
            .appendQueryParameter(JamdeoUri.TIMESLOT, values.get(MapKey.TIMESLOT))            
            .build();
        if (DEBUG) {
            Log.d(TAG, "journalSingleTimeStamp(): " + JOURNAL_URI);
        }
        new UpdateCursorTask(JOURNAL_URI).execute();
    }
    
    private static void journalVideo(EVENT event, Map<MapKey, String> values)
    {
        // The Uri is as following:
        //   content://com.jamdeo.data.vod/external_player_action?
        //   action=logging&source=CNTV&event=xxxx&programSeriesId=760400&episodeId=1166774
        //   &resolution=SD&ts=xxxx&concertId|payType|paymentId|playType
        Uri JOURNAL_URI = buildJournalUriPrefix(event, values);
        JOURNAL_URI = JOURNAL_URI
            .buildUpon()
            .appendQueryParameter(JamdeoUri.TIMESLOT, values.get(MapKey.TIMESLOT))
            .appendQueryParameter(JamdeoUri.CONCERT_ID, values.get(MapKey.CONCERT_ID))
            .appendQueryParameter(JamdeoUri.PAY_TYPE, values.get(MapKey.PAY_TYPE))
            .appendQueryParameter(JamdeoUri.PAYMENT_ID, values.get(MapKey.PAYMENT_ID))
            .appendQueryParameter(JamdeoUri.PLAY_TYPE, values.get(MapKey.PLAY_TYPE))
            .appendQueryParameter(JamdeoUri.RESOURCE_TYPE, values.get(MapKey.RSOURCE_TYPE))
            .appendQueryParameter(JamdeoUri.LICENCE, values.get(MapKey.LICENCE))
            .build();
        if (DEBUG) {
            Log.d(TAG, "journalSingleTimeStamp(): " + JOURNAL_URI);
        }
        new UpdateCursorTask(JOURNAL_URI).execute();
    }   

    // logging journal video exit
    private static void journalVideoExit(EVENT event, Map<MapKey, String> values)
    {
        // The Uri is as following:
        //   content://com.jamdeo.data.vod/external_player_action?
        //   action=logging&source=CNTV&event=xxxx&programSeriesId=760400&episodeId=1166774
        //   &resolution=SD&ts=xxxx&position=yyyy
        Uri JOURNAL_URI = buildJournalUriPrefix(event, values);
        JOURNAL_URI = JOURNAL_URI
            .buildUpon()
            .appendQueryParameter(JamdeoUri.TIMESLOT, values.get(MapKey.TIMESLOT))
            .appendQueryParameter(JamdeoUri.POSITION, values.get(MapKey.POSITION))
            .appendQueryParameter(JamdeoUri.CONCERT_ID, values.get(MapKey.CONCERT_ID))
            .appendQueryParameter(JamdeoUri.PAY_TYPE, values.get(MapKey.PAY_TYPE))
            .appendQueryParameter(JamdeoUri.PAYMENT_ID, values.get(MapKey.PAYMENT_ID))
            .appendQueryParameter(JamdeoUri.PLAY_TYPE, values.get(MapKey.PLAY_TYPE))
            .appendQueryParameter(JamdeoUri.RESOURCE_TYPE, values.get(MapKey.RSOURCE_TYPE))
            .appendQueryParameter(JamdeoUri.LICENCE, values.get(MapKey.LICENCE))
            .build();

        if (DEBUG) {
            Log.d(TAG, "journalVideoExit(): " + JOURNAL_URI);
        }
        new UpdateCursorTask(JOURNAL_URI).execute();
    }
    
    private static void journalVideoError(EVENT event, Map<MapKey, String> values)
    {
        // The Uri is as following:
        //   content://com.jamdeo.data.vod/external_player_action?
        //   action=logging&source=CNTV&event=xxxx&programSeriesId=760400&episodeId=1166774
        //   &resolution=SD&ts=xxxx&position=yyyy
        Uri JOURNAL_URI = buildJournalUriPrefix(event, values);
        JOURNAL_URI = JOURNAL_URI
            .buildUpon()
            .appendQueryParameter(JamdeoUri.TIMESLOT, values.get(MapKey.TIMESLOT))
            .appendQueryParameter(JamdeoUri.POSITION, values.get(MapKey.POSITION))
            .appendQueryParameter(JamdeoUri.ERROR_CODE, values.get(MapKey.ERROR_CODE))
            .appendQueryParameter(JamdeoUri.ERROR_EXTRA, values.get(MapKey.ERROR_EXTRA))
            .appendQueryParameter(JamdeoUri.ERROR_MESSAGE, values.get(MapKey.ERROR_MESSAGE))
            .build();
        if (DEBUG) {
            Log.d(TAG, "journalVideoExit(): " + JOURNAL_URI);
        }
        new UpdateCursorTask(JOURNAL_URI).execute();
    }

    // logging journal with two time stamps
    private static void journalDoubleTimeStamp(EVENT event, Map<MapKey, String> values)
    {
        // The Uri is as following:
        //   content://com.jamdeo.data.vod/external_player_action?
        //   action=logging&source=CNTV&event=xxxx&programSeriesId=760400&episodeId=1166774
        //   &resolution=SD&s_ts=xxxx&e_ts=xxxx
        Uri JOURNAL_URI = buildJournalUriPrefix(event, values);
        JOURNAL_URI = JOURNAL_URI
            .buildUpon()
            .appendQueryParameter(JamdeoUri.START_TIMESLOT, values.get(MapKey.START_TIMESLOT))
            .appendQueryParameter(JamdeoUri.END_TIMESLOT, values.get(MapKey.END_TIMESLOT))
            .build();

        if (DEBUG) {
            Log.d(TAG, "journalDoubleTimeStamp(): " + JOURNAL_URI);
        }
        new UpdateCursorTask(JOURNAL_URI).execute();
    }


    // logging resolution change journal
    private static void journalResolutionChange(EVENT event, Map<MapKey, String> values)
    {
        // The Uri is as following:
        //   content://com.jamdeo.data.vod/external_player_action?
        //   action=logging&source=CNTV&event=xxxx&programSeriesId=760400&episodeId=1166774
        //   &resolution=SD&old_resolution=SD&new_resolution=HD
        Uri JOURNAL_URI = buildJournalUriPrefix(event, values);
        JOURNAL_URI = JOURNAL_URI
            .buildUpon()
            .appendQueryParameter(JamdeoUri.OLD_RESOLUTION, values.get(MapKey.OLD_RESOLUTION))
            .appendQueryParameter(JamdeoUri.NEW_RESOLUTION, values.get(MapKey.NEW_RESOLUTION))
            .build();

        if (DEBUG) {
            Log.d(TAG, "journalResolutionChange(): " + JOURNAL_URI);
        }
        new UpdateCursorTask(JOURNAL_URI).execute();
    }
    
    // logging video payed journal
    private static void journalVideoPayed(EVENT event, Map<MapKey, String> values)
    {
        // The Uri is as following:
        //   content://com.jamdeo.data.vod/external_player_action?
        //   action=logging&source=CNTV&event=xxxx&programSeriesId=760400&episodeId=1166774
        //   &resolution=SD&price=5&ts=xxxx&position=xxxxx
        Uri JOURNAL_URI = buildJournalUriPrefix(event, values);
        JOURNAL_URI = JOURNAL_URI
            .buildUpon()
            .appendQueryParameter(JamdeoUri.PRICE, values.get(MapKey.PRICE))
            .appendQueryParameter(JamdeoUri.TIMESLOT, values.get(MapKey.TIMESLOT))
            .appendQueryParameter(JamdeoUri.PAY_RESULT, values.get(MapKey.PAY_RESULT))
            .build();

        if (DEBUG) {
            Log.d(TAG, "journalResolutionChange(): " + JOURNAL_URI);
        }
        new UpdateCursorTask(JOURNAL_URI).execute();
    }
    

    // journal Uri prefix
    private static Uri buildJournalUriPrefix(EVENT event, Map<MapKey, String> values)
    {
        return JamdeoUri.CONTENT_URI
               .buildUpon()
               .appendQueryParameter(JamdeoUri.ACTION, "logging")
               .appendQueryParameter(JamdeoUri.SOURCE, mSource.getValue())
               .appendQueryParameter(JamdeoUri.EVENT, event.getValue())
               .appendQueryParameter(JamdeoUri.PROGRAM_ID, values.get(MapKey.PROGRAM_ID))
               .appendQueryParameter(JamdeoUri.EPISODE_ID, values.get(MapKey.EPISODE_ID))
               .appendQueryParameter(JamdeoUri.RESOLUTION, values.get(MapKey.RESOLUTION))
               .build();

    }

    private static class UpdateCursorTask extends AsyncTask<Void, Void, Cursor> {
        private Uri mUri;

        public UpdateCursorTask(Uri uri) {
            super();
            mUri = uri;
        }

        @Override
        protected Cursor doInBackground(Void... params) {
            if (DEBUG) {
                Log.d(TAG, "opening cursor " + mUri.toString());
            }

            if (mContext == null) {
                Log.e(TAG, "activity is null, return.");
                return null;
            }

            ContentResolver resolver = mContext.getContentResolver();
            if (resolver == null) {
                Log.e(TAG, "resolver is null, return.");
                return null;
            }
            try{
            	Cursor cursor = resolver.query(mUri, null, null, null, null);
            	return cursor;
            }catch(Exception e){
            	Log.e(TAG," fatal exception in query:"+e.getMessage());
            }
            
            return null;           
        }

        protected void onPostExecute(Cursor cursor) {
            if (cursor == null) {
                Log.e(TAG, "The operation is failed, mUri: " + mUri);
            }
            else {
                cursor.close();
            }
        }
    }

}
