package io.github.a13e300.ksuwebui

import android.content.res.ColorStateList
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

        val magiskFound = RootCheckUtil.isMagiskSuFound()
        val systemModified = RootCheckUtil.isSystemPartitionModified()
        val usbDebugging = RootCheckUtil.isUsbDebuggingEnabled(requireContext())
        val bootloaderLocked = RootCheckUtil.isBootloaderLocked()

        setupIntegrityCheck(
            binding.checkMagisk.root,
            getString(R.string.magisk_su_found),
            getString(R.string.detected),
            getString(R.string.magisk_su_desc),
            !magiskFound
        )

        setupIntegrityCheck(
            binding.checkSystem.root,
            getString(R.string.system_partition_modified),
            getString(R.string.detected),
            getString(R.string.system_partition_desc),
            !systemModified
        )

        setupIntegrityCheck(
            binding.checkUsb.root,
            getString(R.string.usb_debugging_enabled),
            getString(R.string.active),
            getString(R.string.usb_debugging_desc),
            !usbDebugging
        )

        setupIntegrityCheck(
            binding.checkBootloader.root,
            getString(R.string.bootloader_status),
            getString(R.string.detected),
            getString(R.string.bootloader_desc),
            bootloaderLocked
        )

        updateStatusCard(magiskFound || systemModified || usbDebugging || !bootloaderLocked)

        binding.deviceModel.text = RootCheckUtil.getModel()
        binding.kernelVersion.text = RootCheckUtil.getKernelVersion()
        binding.androidVersion.text = getString(R.string.android_version, RootCheckUtil.getAndroidVersion())
        binding.securityPatch.text = RootCheckUtil.getSecurityPatch()
    }

    private fun setupIntegrityCheck(
        root: View,
        title: String,
        badStatusLabel: String,
        description: String,
        isGood: Boolean
    ) {
        val titleView = root.findViewById<TextView>(R.id.title)
        val statusView = root.findViewById<TextView>(R.id.status)
        val descView = root.findViewById<TextView>(R.id.description)
        val iconView = root.findViewById<ImageView>(R.id.icon)
        val summary = root.findViewById<LinearLayout>(R.id.summary)
        val details = root.findViewById<LinearLayout>(R.id.details)
        val expandIcon = root.findViewById<ImageView>(R.id.expand_icon)

        titleView.text = title
        descView.text = description

        statusView.text = if (isGood) getString(R.string.secure) else badStatusLabel

        val statusColorRes = if (isGood) R.color.status_secure else R.color.status_detected
        val iconRes = if (isGood) R.drawable.ic_check_circle else R.drawable.ic_cancel

        val color = resources.getColor(statusColorRes, null)
        statusView.setTextColor(color)
        iconView.setImageResource(iconRes)
        iconView.setColorFilter(color)

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

    private fun updateStatusCard(isCompromised: Boolean) {
        if (isCompromised) {
            binding.statusCard.setCardBackgroundColor(resources.getColor(R.color.status_compromised_bg, null))
            binding.statusIconContainer.setCardBackgroundColor(resources.getColor(R.color.status_compromised_icon_bg, null))
            binding.statusIcon.setImageResource(R.drawable.ic_warning)
            binding.statusIcon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.status_compromised_subtext, null))
            binding.statusTitle.text = getString(R.string.system_compromised)
            binding.statusTitle.setTextColor(resources.getColor(R.color.status_compromised_text, null))
            binding.statusSubtitle.text = getString(R.string.abnormal_environment_detected)
            binding.statusSubtitle.setTextColor(resources.getColor(R.color.status_compromised_subtext, null))
        } else {
            binding.statusCard.setCardBackgroundColor(resources.getColor(R.color.status_secure_bg, null))
            binding.statusIconContainer.setCardBackgroundColor(resources.getColor(R.color.status_secure_icon_bg, null))
            binding.statusIcon.setImageResource(R.drawable.ic_check_circle)
            binding.statusIcon.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.status_secure_subtext, null))
            binding.statusTitle.text = getString(R.string.system_secure)
            binding.statusTitle.setTextColor(resources.getColor(R.color.status_secure_text, null))
            binding.statusSubtitle.text = getString(R.string.all_checks_passed)
            binding.statusSubtitle.setTextColor(resources.getColor(R.color.status_secure_subtext, null))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
