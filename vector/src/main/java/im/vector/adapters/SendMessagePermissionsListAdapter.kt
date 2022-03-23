package im.vector.adapters

import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import im.vector.R
import im.vector.adapters.VectorMessagesAdapter.ROW_TYPE_TEXT

class SendMessagePermissionsListAdapter(context: Context, layoutResourceId: Int, apps: List<String>): ArrayAdapter<String>(context, layoutResourceId) {
    private var mApps: List<String>
    private var mContext: Context? = null
    private var mLayoutInflater: LayoutInflater? = null
    private var mLayoutResourceId = 0

    init {
        mContext = context
        mLayoutResourceId = layoutResourceId
        mLayoutInflater = LayoutInflater.from(mContext)
        mApps = apps
    }

    override fun getCount(): Int {
        return mApps.size
    }

    override fun getItem(position: Int): String {
        return mApps[position]
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).hashCode().toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: mLayoutInflater!!.inflate(mLayoutResourceId, parent, false)

        val packageName: String = getItem(position)

        val pm: PackageManager = mContext!!.packageManager
        val appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        val appName = pm.getApplicationLabel(appInfo).toString()
        val appIcon = pm.getApplicationIcon(appInfo)

        // retrieve the UI items
        val iconImageView = view.findViewById<ImageView>(R.id.app_icon)
        val nameTextView = view.findViewById<TextView>(R.id.app_name)
        val packageTextView = view.findViewById<TextView>(R.id.app_package_name)

        iconImageView.setImageDrawable(appIcon)
        nameTextView.text = appName
        packageTextView.text = packageName

        return view
    }

    override fun getItemViewType(position: Int): Int {
        return ROW_TYPE_TEXT
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun isEnabled(position: Int): Boolean {
        return true
    }

    fun updateApps(newApps: Collection<String>) {
        mApps = newApps.toList()
        notifyDataSetChanged()
    }
}
