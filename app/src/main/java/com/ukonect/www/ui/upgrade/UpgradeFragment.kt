package com.ukonect.www.ui.upgrade

import android.os.Bundle
import android.view.*
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ukonect.www.MainActivity

import com.ukonect.www.R
import com.ukonect.www.databinding.UpgradeFragmentBinding
import com.ukonect.www.data.*;
import com.ukonect.www.ui.binding.UpgradeListAdapter
import com.ukonect.www.ui.main.MainViewModel

class UpgradeFragment : Fragment() {
    private val authModel: MainViewModel by activityViewModels()
    private val model: UpgradeViewModel by viewModels()
    private val navController by lazy { findNavController() }
    private var _binding: UpgradeFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.

    private val binding get() = _binding

    companion object {
        fun newInstance() = UpgradeFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = UpgradeFragmentBinding.inflate(inflater, container, false)
        val view = binding?.root

        var header_item = ListItemData()
        header_item.text = "Choose a upgrade plan"
        header_item.sub_text = "Get exciting features with no ads"

        binding?.upgradeHeader?.item  = header_item;

        // bind ListView
        var listView: ListView? = binding?.upgradeListView
        listView?.adapter = UpgradeListAdapter(this.requireContext(), navController, authModel.app_user)

        //listView.addHeaderView()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as MainActivity).supportActionBar?.setTitle(R.string.upgrade)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        menu.clear() // clear the initial ones, otherwise they are included

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.upgrade_app_bar, menu)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

}
