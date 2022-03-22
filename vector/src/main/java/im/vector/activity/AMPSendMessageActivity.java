package im.vector.activity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import androidx.appcompat.app.AlertDialog;

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
            try {
                PackageManager pm = getPackageManager();
                String packageName = getCallingPackage();
                ApplicationInfo appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                String appName = pm.getApplicationLabel(appInfo).toString();
                new AlertDialog.Builder(this)
                        .setTitle(R.string.send_message_permission_dialog_title)
                        .setIcon(pm.getApplicationIcon(appInfo))
                        .setMessage(getString(R.string.send_message_permission_dialog_desc, appName, packageName))
                        // Decline the request
                        .setNegativeButton(R.string.no, (dialogInterface, i) -> setResult(-2))
                        .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                            Intent intent = new Intent(this, AMPSendMessageReceiver.class);
                            intent.setAction(AMPSendMessageReceiver.BROADCAST_ACTION_AMP_SEND_MESSAGE);
                            intent.putExtras(getIntent().getExtras());
                            sendBroadcast(intent);
                            setResult(RESULT_OK);
                        })
                        .setOnDismissListener(dialogInterface -> {
                            finish();
                        })
                        .show();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                setResult(RESULT_CANCELED);
                finish();
            }
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }
}