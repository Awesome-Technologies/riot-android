package im.vector.fragments

import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import butterknife.BindView
import im.vector.R
import im.vector.activity.MXCActionBarActivity
import im.vector.adapters.SendMessagePermissionsListAdapter

class SettingsManageSendMessagePermissionsFragment: VectorBaseFragment() {
    @BindView(R.id.send_message_permission_list_view)
    lateinit var mlistView: ListView

    override fun getLayoutResId() = R.layout.fragment_settings_manage_send_message_permissions

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pref = requireContext().getSharedPreferences("SendMessagePreferences", MODE_PRIVATE)
        val apps = arrayListOf<String>()
        apps.addAll(pref.all.keys)
        val adapter = SendMessagePermissionsListAdapter(requireContext(), R.layout.adapter_item_send_message_permission_apps, apps)
        mlistView.adapter = adapter

        mlistView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val pm: PackageManager = requireContext().packageManager
            val appInfo = pm.getApplicationInfo(apps[position], PackageManager.GET_META_DATA)
            val packageName = mlistView.getItemAtPosition(position) as String
            AlertDialog.Builder(requireContext())
                    .setTitle(R.string.settings_send_message_manage_permissions_revoke_title)
                    .setIcon(pm.getApplicationIcon(appInfo))
                    .setMessage(R.string.settings_send_message_manage_permissions_revoke_desc)
                    .setNegativeButton(R.string.no) { _, _ -> }
                    .setPositiveButton(R.string.yes) { _, _ ->
                        // Revoke permission
                        pref.edit().remove(packageName).apply()
                        adapter.updateApps(pref.all.keys)
                    }
                    .show()
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? MXCActionBarActivity)?.supportActionBar?.setTitle(R.string.settings_send_message_manage_permissions)
    }
}
