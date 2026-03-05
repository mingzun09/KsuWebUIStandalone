package io.github.a13e300.ksuwebui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.topjohnwu.superuser.nio.FileSystemManager
import io.github.a13e300.ksuwebui.databinding.FragmentModulesBinding
import io.github.a13e300.ksuwebui.databinding.ItemModuleBinding

@SuppressLint("NotifyDataSetChanged")
class ModulesFragment : Fragment(), FileSystemService.Listener {

    private var _binding: FragmentModulesBinding? = null
    private val binding get() = _binding!!
    private var moduleList = emptyList<Module>()
    private lateinit var adapter: Adapter
    private val prefs by lazy { requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentModulesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = Adapter()
        binding.list.adapter = adapter
        binding.swipeRefresh.setOnRefreshListener {
            refresh()
        }
        binding.swipeRefresh.isRefreshing = true
        refresh()
    }

    private fun refresh() {
        moduleList = emptyList()
        adapter.notifyDataSetChanged()
        binding.info.setText(R.string.loading)
        binding.info.isVisible = true
        FileSystemService.start(this)
    }

    override fun onServiceAvailable(fs: FileSystemManager) {
        App.executor.submit {
            val mods = mutableListOf<Module>()
            val showDisabled = prefs.getBoolean("show_disabled", false)
            fs.getFile("/data/adb/modules").listFiles()?.forEach { f ->
                if (!f.isDirectory) return@forEach
                if (!fs.getFile(f, "webroot").isDirectory) return@forEach
                if (fs.getFile(f, "disable").exists() && !showDisabled) return@forEach
                var name = f.name
                val id = f.name
                var author = "?"
                var version = "?"
                var desc = ""
                fs.getFile(f, "module.prop").newInputStream().bufferedReader().use {
                    it.lines().forEach { l ->
                        val ls = l.split("=", limit = 2)
                        if (ls.size == 2) {
                            if (ls[0] == "name") name = ls[1]
                            else if (ls[0] == "description") desc = ls[1]
                            else if (ls[0] == "author") author = ls[1]
                            else if (ls[0] == "version") version = ls[1]
                        }

                    }
                }
                mods.add(Module(name, id, desc, author, version))
            }
            activity?.runOnUiThread {
                moduleList = mods
                adapter.notifyDataSetChanged()
                binding.swipeRefresh.isRefreshing = false
                if (mods.isEmpty()) {
                    binding.info.setText(R.string.no_modules)
                    binding.info.isVisible = true
                } else {
                    binding.info.isVisible = false
                }
            }
        }
    }

    override fun onLaunchFailed() {
        moduleList = emptyList()
        adapter.notifyDataSetChanged()
        binding.info.setText(R.string.please_grant_root)
        binding.info.isVisible = true
        binding.swipeRefresh.isRefreshing = false
    }

    data class Module(val name: String, val id: String, val desc: String, val author: String, val version: String)

    class ViewHolder(val binding: ItemModuleBinding) : RecyclerView.ViewHolder(binding.root)

    inner class Adapter : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemModuleBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        override fun getItemCount(): Int = moduleList.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = moduleList[position]
            val id = item.id
            val name = item.name
            holder.binding.name.text = name
            holder.binding.author.text = resources.getString(R.string.author, item.author)
            holder.binding.version.text = resources.getString(R.string.version, item.version)
            holder.binding.desc.text = item.desc
            holder.binding.root.setOnClickListener {
                startActivity(
                    Intent(requireContext(), WebUIActivity::class.java)
                        .setData(Uri.parse("ksuwebui://webui/$id"))
                        .putExtra("id", id)
                        .putExtra("name", name)
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        FileSystemService.removeListener(this)
        _binding = null
    }
}
