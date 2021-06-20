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
import com.ukonect.www.Ukonect.Companion.appUser
import com.ukonect.www.databinding.SignUpConfirmVerificationCodeFragmentBinding
import com.ukonect.www.data.*;
import com.ukonect.www.ui.main.MainViewModel
import com.ukonect.www.util.Constants

class SignUpConfirmVerificationCodeFragment : Fragment() {
    private val authModel: MainViewModel by activityViewModels()
    private val navController by lazy { findNavController() }
    private var _binding: SignUpConfirmVerificationCodeFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.

    private val binding get() = _binding

    companion object {
        fun newInstance() = SignUpConfirmVerificationCodeFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SignUpConfirmVerificationCodeFragmentBinding.inflate(inflater, container, false)
        val view = binding?.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        binding?.signUpConfirmVerificationCodeBtnNext?.setOnClickListener {

            var confirm_verification_code = binding?.signUpConfirmVerificationCodeTxtCode?.text.toString()

            var req = mapOf<String, String>("user_id" to (appUser?.user_id?: ""), "confirm_verification_code" to confirm_verification_code)

            authModel.singupStage(req, AuthState.AUTH_STAGE_CONFIRM_VERIFICATION_CODE)
        }

        binding?.signUpConfirmVerificationCodeBtnBack?.setOnClickListener {
            //TODO go back
        }
		
		val observer = Observer<AppUser> { app_user ->
		
			if(app_user.auth_state == AuthState.AUTH_STAGE_FULL_NANE){

                navController.navigate(R.id.move_to_signUpFullNameFragment)
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
