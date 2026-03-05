package io.github.a13e300.ksuwebui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import io.github.a13e300.ksuwebui.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupIntegrityCheck(
            binding.checkMagisk.root,
            getString(R.string.magisk_su_found),
            getString(R.string.detected),
            getString(R.string.magisk_su_desc),
            RootCheckUtil.isMagiskSuFound()
        )

        setupIntegrityCheck(
            binding.checkSystem.root,
            getString(R.string.system_partition_modified),
            getString(R.string.detected),
            getString(R.string.system_partition_desc),
            RootCheckUtil.isSystemPartitionModified()
        )

        setupIntegrityCheck(
            binding.checkUsb.root,
            getString(R.string.usb_debugging_enabled),
            getString(R.string.active),
            getString(R.string.usb_debugging_desc),
            RootCheckUtil.isUsbDebuggingEnabled(requireContext())
        )

        val bootloaderLocked = RootCheckUtil.isBootloaderLocked()
        setupIntegrityCheck(
            binding.checkBootloader.root,
            getString(R.string.bootloader_status),
            if (bootloaderLocked) getString(R.string.secure) else getString(R.string.detected),
            getString(R.string.bootloader_desc),
            !bootloaderLocked,
            statusColor = if (bootloaderLocked) R.color.status_secure else R.color.status_detected,
            iconRes = if (bootloaderLocked) R.drawable.ic_check_circle else R.drawable.ic_cancel
        )

        binding.deviceModel.text = RootCheckUtil.getModel()
        binding.kernelVersion.text = RootCheckUtil.getKernelVersion()
        binding.androidVersion.text = getString(R.string.android_version, RootCheckUtil.getAndroidVersion())
        binding.securityPatch.text = RootCheckUtil.getSecurityPatch()
    }

    private fun setupIntegrityCheck(
        root: View,
        title: String,
        status: String,
        description: String,
        isDetected: Boolean,
        statusColor: Int? = null,
        iconRes: Int? = null
    ) {
        val titleView = root.findViewById<TextView>(R.id.title)
        val statusView = root.findViewById<TextView>(R.id.status)
        val descView = root.findViewById<TextView>(R.id.description)
        val iconView = root.findViewById<ImageView>(R.id.icon)
        val summary = root.findViewById<LinearLayout>(R.id.summary)
        val details = root.findViewById<LinearLayout>(R.id.details)
        val expandIcon = root.findViewById<ImageView>(R.id.expand_icon)

        titleView.text = title
        statusView.text = status
        descView.text = description

        val finalStatusColor = statusColor ?: if (isDetected) R.color.status_detected else R.color.status_secure
        val finalIconRes = iconRes ?: if (isDetected) R.drawable.ic_cancel else R.drawable.ic_check_circle

        statusView.setTextColor(resources.getColor(finalStatusColor, null))
        iconView.setImageResource(finalIconRes)
        iconView.setColorFilter(resources.getColor(finalStatusColor, null))

        summary.setOnClickListener {
            if (details.visibility == View.VISIBLE) {
                details.visibility = View.GONE
                expandIcon.animate().rotation(0f).start()
            } else {
                details.visibility = View.VISIBLE
                expandIcon.animate().rotation(180f).start()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
