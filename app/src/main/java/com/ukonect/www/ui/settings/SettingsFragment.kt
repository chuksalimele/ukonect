package com.ukonect.www.ui.settings

import android.os.Bundle
import android.view.*
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ukonect.www.MainActivity
import com.ukonect.www.R
import com.ukonect.www.databinding.SettingsFragmentBinding
import com.ukonect.www.ui.binding.SettingsListAdapter

class SettingsFragment : Fragment() {
    private val model: SettingsViewModel by viewModels()
    private val navController by lazy { findNavController() }
    private var _binding: SettingsFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.

    private val binding get() = _binding

    companion object {
        fun newInstance() = SettingsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SettingsFragmentBinding.inflate(inflater, container, false)
        val view = binding?.root


        // bind ListView
        var listView: ListView? = binding?.settingsListView
        listView?.adapter = SettingsListAdapter(this.requireContext())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as MainActivity).supportActionBar?.setTitle(R.string.settings)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        menu.clear() // clear the initial ones, otherwise they are included

        // Inflate the menu; this adds items to the action bar if it is present.
        //inflater.inflate(0, menu)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

}
