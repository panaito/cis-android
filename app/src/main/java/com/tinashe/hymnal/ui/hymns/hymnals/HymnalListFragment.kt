package com.tinashe.hymnal.ui.hymns.hymnals

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tinashe.hymnal.R
import com.tinashe.hymnal.databinding.HymnalListFragmentBinding
import com.tinashe.hymnal.extensions.arch.observeNonNull
import com.tinashe.hymnal.extensions.prefs.HymnalPrefs
import com.tinashe.hymnal.ui.hymns.hymnals.adapter.HymnalsListAdapter
import com.tinashe.hymnal.ui.hymns.hymnals.adapter.SortOptionsAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HymnalListFragment : Fragment() {

    @Inject
    lateinit var hymnalPrefs: HymnalPrefs

    private val viewModel: HymnalListViewModel by activityViewModels()

    private var binding: HymnalListFragmentBinding? = null

    private val sortOptionsAdapter: SortOptionsAdapter by lazy {
        SortOptionsAdapter(hymnalPrefs) {
            viewModel.sortOrderChanged()
        }
    }
    private val listAdapter: HymnalsListAdapter = HymnalsListAdapter {
        with(findNavController()) {
            previousBackStackEntry
                ?.savedStateHandle
                ?.set(SELECTED_HYMNAL_KEY, it)
            popBackStack()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return HymnalListFragmentBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.hymnalsListView?.adapter = ConcatAdapter(sortOptionsAdapter, listAdapter)

        viewModel.hymnalListLiveData.observeNonNull(viewLifecycleOwner) { hymnals ->
            binding?.progressBar?.isVisible = false
            listAdapter.submitList(ArrayList(hymnals))
            if (hymnals.size > 1) {
                sortOptionsAdapter.init()
            }
        }

        viewModel.loadData()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.hymnals_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_info -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setMessage(R.string.switch_between_help)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val SELECTED_HYMNAL_KEY = "arg:hymnal"
    }
}
