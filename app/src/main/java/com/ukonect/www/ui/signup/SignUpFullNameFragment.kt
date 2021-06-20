package com.ukonect.www.ui.signup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ukonect.www.R
import com.ukonect.www.Ukonect
import com.ukonect.www.Ukonect.Companion.appUser

import com.ukonect.www.databinding.SignUpFullNameFragmentBinding
import com.ukonect.www.data.*;
import com.ukonect.www.ui.main.MainViewModel
import com.ukonect.www.util.Constants

class SignUpFullNameFragment : Fragment() {
    private val authModel: MainViewModel by activityViewModels()
    private val navController by lazy { findNavController() }
    private var _binding: SignUpFullNameFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.

    private val binding get() = _binding

    companion object {
        fun newInstance() = SignUpFullNameFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SignUpFullNameFragmentBinding.inflate(inflater, container, false)
        val view = binding?.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        binding?.signUpFullNameBtnNext?.setOnClickListener {

            var first_name = binding?.signUpFullNameTxtFirstName?.text.toString()
            var last_name = binding?.signUpFullNameTxtLasttName?.text.toString()

            var req = mapOf<String, String>("user_id" to (appUser?.user_id?: ""), "first_name" to first_name, "last_name" to last_name)

            authModel.singupStage(req, AuthState.AUTH_STAGE_FULL_NANE)
        }

        binding?.signUpFullNameBtnBack?.setOnClickListener {
            //TODO go back
        }
		
		val observer = Observer<AppUser> { app_user ->
		
			if(app_user.auth_state == AuthState.AUTH_STAGE_PROFILE_PHOTO){

                navController.navigate(R.id.move_to_signUpProfilePhotoFragment)
            }
		
		}
		
		// Observe the LiveData, passing in this fragment LifecycleOwner and the observer.
        authModel.auth.observe(viewLifecycleOwner, observer)

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
