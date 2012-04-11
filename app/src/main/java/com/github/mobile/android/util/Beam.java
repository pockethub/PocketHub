package com.github.mobile.android.util;


import static android.nfc.NdefRecord.TNF_MIME_MEDIA;
import static android.nfc.NdefRecord.createApplicationRecord;
import static android.nfc.NfcAdapter.ACTION_NDEF_DISCOVERED;
import static android.nfc.NfcAdapter.EXTRA_NDEF_MESSAGES;
import static android.nfc.NfcAdapter.getDefaultAdapter;
import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
import static org.eclipse.egit.github.core.client.IGitHubConstants.CHARSET_UTF8;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Build;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.Gson;

import java.nio.charset.Charset;

import org.eclipse.egit.github.core.client.GsonUtils;

/**
 * Wraps the Android Beam API introduced with ICS - it should be safe to invoke all the public methods on this class,
 * even if your code is running a device that is NFC-less or pre-ICS (as long as it's Eclair or later).
 *
 * @see <a href="http://developer.android.com/guide/topics/nfc/nfc.html">Android NFC</a> docs.
 */
public class Beam {

    private static final Gson GSON = GsonUtils.getGson();
    private static final Charset PAYLOAD_CHARSET = Charset.forName(CHARSET_UTF8);
    private static final String TAG = "BEAM";

    public static boolean isBeamApiAvailable() {
        boolean beamApiAvailable = Build.VERSION.SDK_INT >= ICE_CREAM_SANDWICH;
        Log.d(TAG, "beamApiAvailable="+beamApiAvailable);
        return beamApiAvailable;
    }

    public static boolean isBeamHardwareAvailable(Context context) {
        return isBeamApiAvailable() && getDefaultAdapter(context) != null;
    }

    /**
     * Associates an Activity with a particular beam-able object. When the activity is active, this is the object
     * that will get beamed.
     *
     * @param activity      the Activity to associate the payload with
     * @param typeStem      the suffix to be used for the mime type - e.g. 'repo.issue'
     *                      for 'application/vnd.github.repo.issue+json'. Note that this mime-type is used in the
     *                      AndroidManifest.xml
     * @param payloadObject JSON-serializable object
     */
    public static void setBeamMessage(Activity activity, String typeStem, Object payloadObject) {
        if (!isBeamHardwareAvailable(activity))
            return;

        getDefaultAdapter(activity).setNdefPushMessage(createNdefMessage(typeStem, payloadObject), activity);
    }

    /**
     * Extract an object received by beam from the resulting intent.
     *
     * @param intent an intent which may possibly be a Beam intent and have a beamed object
     * @param clazz  the expected type of the beamed payload
     * @return the beamed object, or null if this intent wasn't anything to do with Beam
     */
    public static <T> T objectFromBeamIntent(Intent intent, Class<T> clazz) {
        if (!isBeamApiAvailable() || !ACTION_NDEF_DISCOVERED.equals(intent.getAction()))
            return null;

        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(EXTRA_NDEF_MESSAGES);
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        String json = new String(msg.getRecords()[0].getPayload(), PAYLOAD_CHARSET);
        return GSON.fromJson(json, clazz);
    }

    private static NdefMessage createNdefMessage(String typeStem, Object payloadObject) {
        return (NdefMessage) createJsonNdefMessage(typeStem, GSON.toJson(payloadObject));
    }

    public static Parcelable createJsonNdefMessage(String typeStem, String jsonString) {
        return new NdefMessage(
                new NdefRecord[] {
                        createMimeRecord(mimeTypeFor(typeStem), jsonString.getBytes(PAYLOAD_CHARSET)),
                        createApplicationRecord("com.github.mobile.android")
                });
    }

    public static String mimeTypeFor(String typeStem) {
        return "application/vnd.github." + typeStem + "+json";
    }

    private static NdefRecord createMimeRecord(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        return new NdefRecord(TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
    }
}
