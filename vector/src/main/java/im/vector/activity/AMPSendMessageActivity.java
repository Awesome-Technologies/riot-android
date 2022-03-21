package im.vector.activity;

import android.content.Intent;
import android.text.TextUtils;

import org.matrix.androidsdk.core.Log;

import im.vector.R;
import im.vector.receiver.AMPSendMessageReceiver;

public class AMPSendMessageActivity extends VectorAppCompatActivity {
    private static final String LOG_TAG = AMPSendMessageActivity.class.getSimpleName();

    @Override
    public int getLayoutRes() {
        // display a spinner while binding the email
        return R.layout.activity_amp_send_message_activity;
    }

    @Override
    public void initUiAndData() {
        configureToolbar();

        Log.d(LOG_TAG, "## initUiAndData(): AMP Send Message Activity created");

        if (TextUtils.equals(getIntent().getAction(), AMPSendMessageReceiver.SEND_MESSAGE_ACTION)) {
            Log.d(LOG_TAG, "## initUiAndData(): Appropriate action found. Broadcasting data…");
            Intent intent = new Intent(this, AMPSendMessageReceiver.class);
            intent.setAction(AMPSendMessageReceiver.BROADCAST_ACTION_AMP_SEND_MESSAGE);
            intent.putExtras(getIntent().getExtras());

            sendBroadcast(intent);
            finish();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }
}