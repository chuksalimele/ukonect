package com.ukonect.www.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ukonect.www.MainActivity
import com.ukonect.www.R
import com.ukonect.www.data.ListItemData
import com.ukonect.www.databinding.PersonalProfileSettingsFragmentBinding
import com.ukonect.www.ui.binding.PersonalProfileSettingsListAdapter
import com.ukonect.www.ui.main.MainViewModel
import com.ukonect.www.ui.settings.SettingsViewModel


class PersonalProfileSettingsFragment : Fragment(){

    private val model: SettingsViewModel by viewModels()
    private val authModel: MainViewModel by activityViewModels()
    private val navController by lazy { findNavController() }
    private var _binding: PersonalProfileSettingsFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.

    private val binding get() = _binding

    companion object {
        fun newInstance() = PersonalProfileSettingsFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = PersonalProfileSettingsFragmentBinding.inflate(inflater, container, false)
        val view = binding?.root

        var header_item = ListItemData()
        header_item.text = authModel.app_user?.display_name ?: ""
        header_item.sub_text = authModel.app_user?.status_message ?: ""
        header_item.left_image_url = authModel.app_user?.photo_url?: ""
        header_item.left_image_placeholder = this.context?.getDrawable(R.drawable.ic_person_blue_70dp)


        binding?.personalProfileSettingsHeader?.item = header_item;

        // bind ListView
        var listView: ListView? = binding?.personalProfileSettingsListView
        listView?.adapter = PersonalProfileSettingsListAdapter(this.requireContext())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as MainActivity).supportActionBar?.setTitle(R.string.profile_settings)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

}
